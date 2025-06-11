package syntax.backend.runways.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.EntityNotFoundException
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import syntax.backend.runways.dto.*
import syntax.backend.runways.entity.*
import syntax.backend.runways.event.CourseCreatedEvent
import syntax.backend.runways.event.CourseUpdatedEvent
import syntax.backend.runways.exception.NotAuthorException
import syntax.backend.runways.repository.*
import syntax.backend.runways.util.DistanceUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@Service
class CourseApiServiceImpl(
    private val courseRepository: CourseRepository,
    private val userApiService: UserApiService,
    private val locationApiService: LocationApiService,
    private val commentRepository: CommentRepository,
    private val courseQueryService: CourseQueryService,
    private val weatherService: WeatherService,
    private val tendencyApiService: TendencyApiService,
    private val attendanceApiService: AttendanceApiService,
    private val runningLogRepository: RunningLogRepository,
    private val popularCourseRepository: PopularCourseRepository,
    private val courseTagRepository: CourseTagRepository,
    private val tagApiService: TagApiService,
    private val tagRepository: TagRepository,
    private val tagLogRepository: TagLogRepository,
    private val experienceService: ExperienceService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val bookmarkRepository: BookmarkRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val fineDustService: FineDustService
) : CourseApiService {

    @Value("\${llm-server-url}")
    private lateinit var llmServerUrl : String

    private val geoJsonWriter = GeoJsonWriter()
    private val wktReader = WKTReader()
    private val objectMapper = ObjectMapper()

    // GeoJSON 변환
    fun convertToGeoJsonPoint(lon: Double, lat: Double): ObjectNode {
        return objectMapper.createObjectNode().apply {
            put("type", "Point")
            putArray("coordinates").add(lon).add(lat)
        }
    }

    // GeoJSON LineString 변환
    private fun convertToGeoJsonLineString(coords: List<List<Double>>): ObjectNode {
        return objectMapper.createObjectNode().apply {
            put("type", "LineString")
            val array = putArray("coordinates")
            coords.forEach { coord ->
                array.addArray().add(coord[0]).add(coord[1])
            }
        }
    }

    // 좌표 추출
    private fun extractCoordinates(position: String): Pair<Double, Double> {
        val node = objectMapper.readTree(position)
        val coordinates = node.get("coordinates")
        val x = coordinates.get(0).asDouble()
        val y = coordinates.get(1).asDouble()
        return Pair(x, y)
    }

    // CRS 필드 제거
    private fun removeCrsFieldAsJsonNode(geoJson: String): ObjectNode {
        val objectMapper = ObjectMapper()
        val node = objectMapper.readTree(geoJson) as ObjectNode
        node.remove("crs")
        return node
    }

    // 코스 데이터 호출
    override fun getCourseData(courseId: UUID): Course {
        val courseData = courseRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        return courseData
    }

    // 댓글 개수 호출
    private fun getCommentCount(courseId: UUID): Int {
        val commentStatus = CommentStatus.PUBLIC
        return commentRepository.countByPost_IdAndStatus(courseId, commentStatus)
    }

    // 코스 생성
    @Transactional
    override fun createCourse(requestCourseDTO: RequestCourseDTO, userId: String) : UUID {
        val user = userApiService.getUserDataFromId(userId)

        // WKT 문자열을 Geometry 객체로 변환
        val position = wktReader.read(requestCourseDTO.position) // Point
        val coordinate = wktReader.read(requestCourseDTO.coordinate) // LineString

        // 공간 연산을 위해 SRID 설정
        position.srid = 4326
        coordinate.srid = 4326

        // 유효성 검사
        if (position.geometryType != "Point" || coordinate.geometryType != "LineString") {
            throw IllegalArgumentException("유효하지 않은 WKT 형식: position은 Point여야 하고 coordinate는 LineString이어야 합니다.")
        }

        if (requestCourseDTO.sido == requestCourseDTO.sigungu || requestCourseDTO.sido=="Unknown" || requestCourseDTO.sigungu=="Unknown") {
            val x = position.coordinate.x
            val y = position.coordinate.y
            val nearestLocation = locationApiService.getNearestLocation(x, y)
                ?: throw IllegalArgumentException("가장 가까운 Location을 찾을 수 없습니다.")
            requestCourseDTO.sido = nearestLocation.sido
            requestCourseDTO.sigungu = nearestLocation.sigungu
        }

        val newCourse = Course(
            title = requestCourseDTO.title,
            maker = user,
            distance = requestCourseDTO.distance,
            position = position as Point,
            coordinate = coordinate as LineString,
            mapUrl = requestCourseDTO.mapUrl,
            status = requestCourseDTO.status,
            usageCount = 0,
            sido = requestCourseDTO.sido,
            sigungu = requestCourseDTO.sigungu
        )

        courseRepository.save(newCourse)

        // 태그 ID를 기반으로 CourseTag 및 TagLog 생성
        val tags = tagRepository.findAllById(requestCourseDTO.tag).map { tag ->
            tag.apply {
                usageCount += 1 // 태그 사용 횟수 증가
            }
        }

        // 코스 태그 및 태그 로그 생성
        val courseTags = tags.map { tag -> CourseTag(course = newCourse, tag = tag) }
        val tagLogs = tags.map { tag -> TagLog(tag = tag, user = user, actionType = ActionType.USED) }

        // 태그, 태그 로그, 코스 태그를 한 번에 저장
        tagRepository.saveAll(tags)
        tagLogRepository.saveAll(tagLogs)
        courseTagRepository.saveAll(courseTags)

        // 경험치 증가
        experienceService.addExperience(user, 50)

        eventPublisher.publishEvent(CourseCreatedEvent(newCourse.id))

        return newCourse.id
    }

    // 마이페이지 코스 리스트
    override fun getMyCourseList(userId: String, pageable: Pageable): Page<ResponseMyCourseDTO> {
        return courseQueryService.getCourseList(userId, pageable, false)
    }

    // 공개 코스 조회
    override fun getCourseList(userId: String, pageable: Pageable): Page<ResponseMyCourseDTO> {
        return courseQueryService.getCourseList(userId, pageable, true)
    }

    // 코스 업데이트
    @Transactional
    override fun updateCourse(requestUpdateCourseDTO: RequestUpdateCourseDTO, userId : String): UUID {
        val courseData = courseRepository.findById(requestUpdateCourseDTO.courseId)
            .orElseThrow { EntityNotFoundException("코스를 찾을 수 없습니다.") }

        // 코스 제작자 확인
        if (courseData.maker.id != userId) {
            throw NotAuthorException("코스 제작자가 아닙니다.")
        }

        // WKT 문자열을 Geometry 객체로 변환
        val position = wktReader.read(requestUpdateCourseDTO.position) // Point
        val coordinate = wktReader.read(requestUpdateCourseDTO.coordinate) // LineString

        // 유효성 검사
        if (position.geometryType != "Point" || coordinate.geometryType != "LineString") {
            throw IllegalArgumentException("유효하지 않은 WKT 형식: position은 Point여야 하고 coordinate는 LineString이어야 합니다.")
        }

        if (requestUpdateCourseDTO.sido == requestUpdateCourseDTO.sigungu || requestUpdateCourseDTO.sido=="Unknown" || requestUpdateCourseDTO.sigungu=="Unknown") {
            val x = position.coordinate.x
            val y = position.coordinate.y
            val nearestLocation = locationApiService.getNearestLocation(x, y)
                ?: throw IllegalArgumentException("가장 가까운 Location을 찾을 수 없습니다.")
            requestUpdateCourseDTO.sido = nearestLocation.sido
            requestUpdateCourseDTO.sigungu = nearestLocation.sigungu
        }

        courseData.title = requestUpdateCourseDTO.title
        courseData.distance = requestUpdateCourseDTO.distance
        courseData.position = position as Point
        courseData.coordinate = coordinate as LineString
        courseData.mapUrl = requestUpdateCourseDTO.mapUrl
        courseData.status = requestUpdateCourseDTO.status
        courseData.updatedAt = LocalDateTime.now()
        courseData.sido = requestUpdateCourseDTO.sido
        courseData.sigungu = requestUpdateCourseDTO.sigungu

        courseRepository.save(courseData)

        // 기존 태그와 요청된 태그 비교
        val existingTags = courseData.courseTags.map { it.tag.id }
        val newTags = requestUpdateCourseDTO.tag

        // 추가해야 할 태그
        val tagsToAdd = newTags.filterNot { it in existingTags }.distinct()
        val tagsToAddEntities = tagRepository.findAllById(tagsToAdd).map { tag ->
            tag.apply { usageCount += 1 } // 태그 사용 횟수 증가
        }
        val courseTagsToAdd = tagsToAddEntities.map { tag -> CourseTag(course = courseData, tag = tag) }
        val tagLogsToAdd = tagsToAddEntities.map { tag ->
            TagLog(tag = tag, user = courseData.maker, actionType = ActionType.USED) // 태그 로그 생성
        }
        courseTagRepository.saveAll(courseTagsToAdd) // 코스 태그 저장
        tagLogRepository.saveAll(tagLogsToAdd) // 태그 로그 저장
        tagRepository.saveAll(tagsToAddEntities) // 태그 저장

        // 삭제해야 할 태그
        val tagsToRemove = existingTags.filterNot { it in newTags }.distinct()
        val tagsToRemoveEntities = tagRepository.findAllById(tagsToRemove).map { tag ->
            tag.apply { usageCount = (usageCount - 1).coerceAtLeast(0) }
        }
        courseTagRepository.deleteAllByCourseIdAndTagIdIn(courseData.id, tagsToRemove) // 코스 태그 삭제
        tagRepository.saveAll(tagsToRemoveEntities) // 태그 저장

        eventPublisher.publishEvent(CourseUpdatedEvent(courseData.id))
        return courseData.id
    }

    // 코스 상세정보
    override fun getCourseById(courseId: UUID, userId: String): ResponseCourseDetailDTO {
        val courseStatus = listOf(CourseStatus.PUBLIC, CourseStatus.PRIVATE, CourseStatus.FILTERED)
        val optCourseData = courseRepository.findCourseWithTagsByIdAndStatuses(courseId, courseStatus)
        val commentCount = getCommentCount(courseId)

        // 코스 데이터가 존재하는지 확인
        if (optCourseData.isPresent) {
            val course = optCourseData.get()

            // 코스 제작자 확인
            if (course.maker.id != userId && course.status == CourseStatus.PRIVATE) {
                throw NotAuthorException("비공개 코스는 제작자만 조회할 수 있습니다.")
            }

            // 북마크 여부 확인
            val isBookmarked = bookmarkRepository.existsByCourseIdAndUserId(courseId, userId)

            // 코스의 위치와 좌표를 GeoJSON 형식으로 변환
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            // CRS 필드를 제거
            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            // List<Tag> 형태로 변환
            val tags = course.courseTags.map { it.tag }

            return ResponseCourseDetailDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = isBookmarked,
                hits = course.hits,
                distance = course.distance,
                position = positionNode,
                coordinate = coordinateNode,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == userId,
                status = course.status,
                tag = tags,
                sido = course.sido,
                sigungu = course.sigungu,
                commentCount = commentCount,
                usageCount = course.usageCount
            )
        } else {
            throw EntityNotFoundException("존재하지 않거나 삭제된 코스입니다.")
        }
    }

    // 코스 삭제
    @Transactional
    override fun deleteCourse(courseId: UUID, userId: String): String {
        val optCourseData = courseRepository.findById(courseId)
        if (optCourseData.isPresent) {
            val course = optCourseData.get()
            if (course.maker.id != userId) {
                throw NotAuthorException("코스 제작자가 아닙니다.")
            }
            // 코스 상태 변경 -> 삭제 상태
            course.status = CourseStatus.DELETED
            courseRepository.save(course)

            // 특정 코스와 관련된 인기 코스 데이터 삭제
            popularCourseRepository.findByCourseId(courseId).forEach {
                popularCourseRepository.delete(it)
            }

            // 북마크 삭제
            bookmarkRepository.deleteByCourseId(courseId)

            return "코스 삭제 성공"
        } else {
            return "코스를 찾을 수 없습니다."
        }
    }

    // 북마크 추가
    @Transactional
    override fun addBookmark(courseId: UUID, userId:String): String {
        val course = courseRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다")
        val user = userApiService.getUserDataFromId(userId)

        // 코스 제작자 확인
        if (course.maker.id == userId) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        // 북마크 되어 있는지 확인
        val isBookmark = bookmarkRepository.existsByCourseIdAndUserId(courseId, userId)

        if (isBookmark) {
            return "이미 북마크된 코스입니다."
        }

        // 태그 로그 추가
        val tags = course.courseTags.map { it.tag }
        val tagLogs = tags.map { tag ->
            TagLog(tag = tag, user = user, actionType = ActionType.BOOKMARKED)
        }
        tagLogRepository.saveAll(tagLogs)

        // 북마크 추가
        bookmarkRepository.save(Bookmark(course = course, user = user))

        return "북마크 추가 성공"
    }

    // 북마크 삭제
    @Transactional
    override fun removeBookmark(courseId: UUID, userId:String): String {
        if (!bookmarkRepository.existsByCourseIdAndUserId(courseId, userId))
            throw EntityNotFoundException("북마크를 찾을 수 없습니다.")

        bookmarkRepository.deleteByCourseIdAndUserId(courseId, userId)

        return "북마크 삭제 성공"
    }

    // 북마크된 코스 조회
    @Transactional
    override fun getBookmarkedCourses(userId: String, pageable: Pageable): Page<ResponseMyCourseDTO> {
        // 북마크된 코스 ID 조회
        val bookmarkedCourseIdsPage = bookmarkRepository.findCourseIdsByUserId(userId, pageable)
        val bookmarkedCourseIds = bookmarkedCourseIdsPage.content

        if (bookmarkedCourseIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0)
        }

        // 코스 데이터 조회
        val courses = courseRepository.findCoursesWithTagsByIds(bookmarkedCourseIds)

        // 북마크 수 조회
        val bookmarkCounts = bookmarkRepository.countBookmarksByCourseIds(bookmarkedCourseIds)
        val bookmarkCountMap = bookmarkCounts.associateBy({ it.courseId }, { it.bookmarkCount })

        // ResponseCourseDTO로 매핑
        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            val commentCount = commentRepository.countByPost_IdAndStatus(course.id, CommentStatus.PUBLIC)

            val tags = course.courseTags.map { it.tag }

            // 북마크 수 조회 (Long -> Int 변환)
            val bookmarkCount = (bookmarkCountMap[course.id] ?: 0L).toInt()

            ResponseMyCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = true, // 북마크된 코스이므로 항상 true
                bookmarkCount = bookmarkCount,
                hits = course.hits,
                position = positionNode,
                coordinate = coordinateNode,
                distance = course.distance,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == userId,
                status = course.status,
                tag = tags,
                sido = course.sido,
                sigungu = course.sigungu,
                commentCount = commentCount,
                usageCount = course.usageCount,
            )
        }

        // 페이징 결과 반환
        return PageImpl(responseCourses, pageable, bookmarkedCourseIdsPage.totalElements)
    }

    // 전체 코스 리스트
    override fun getAllCourses(userId: String, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC

        // 코스 ID 조회
        val courseIdsPage = courseRepository.findCourseIdsByStatus(statuses, pageable)
        val courseIds = courseIdsPage.content

        // 북마크된 courseIds 조회
        val bookmarkedCourseIds = bookmarkRepository.findBookmarkedCourseIdsByUserIdAndCourseIds(userId, courseIds)

        // 코스 데이터 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        // ResponseCourseDTO로 매핑
        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            // CRS 필드를 제거
            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            // 댓글 개수 조회
            val commentCount = getCommentCount(course.id)

            // List<Tag> 형태로 변환
            val tags = course.courseTags.map { it.tag }

            // 북마크 여부 확인
            val isBookmakred = course.id in bookmarkedCourseIds

            ResponseCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = isBookmakred,
                hits = course.hits,
                distance = course.distance,
                position = positionNode,
                coordinate = coordinateNode,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == userId,
                status = course.status,
                tag = tags,
                sido = course.sido,
                sigungu = course.sigungu,
                commentCount = commentCount,
                usageCount = course.usageCount,
            )
        }

        // 페이징 결과 반환
        return PageImpl(responseCourses, pageable, courseIdsPage.totalElements)
    }

    // 코스 검색
    override fun searchCoursesByTitle(title: String, userId: String, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC

        // 코스 ID 조회
        val courseIdsPage = courseRepository.findCourseIdsByTitleContainingAndStatus(title, statuses, pageable)
        val courseIds = courseIdsPage.content

        // 북마크된 courseIds 조회
        val bookmarkedCourseIds = bookmarkRepository.findBookmarkedCourseIdsByUserIdAndCourseIds(userId, courseIds)

        // 코스 데이터 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        // ResponseCourseDTO로 매핑
        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            // CRS 필드를 제거
            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            // 댓글 개수 조회
            val commentCount = getCommentCount(course.id)

            // List<Tag> 형태로 변환
            val tags = course.courseTags.map { it.tag }

            // 북마크 여부 확인
            val isBookmakred = course.id in bookmarkedCourseIds

            ResponseCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = isBookmakred,
                hits = course.hits,
                distance = course.distance,
                position = positionNode,
                coordinate = coordinateNode,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == userId,
                status = course.status,
                tag = tags,
                sido = course.sido,
                sigungu = course.sigungu,
                commentCount = commentCount,
                usageCount = course.usageCount,
            )
        }

        // 페이징 결과 반환
        return PageImpl(responseCourses, pageable, courseIdsPage.totalElements)
    }

    // 코스 조회수 증가
    @Transactional
    override fun increaseHits(courseId: UUID): String {
        val course = courseRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        course.hits.increaseHits()
        println(course.hits)
        courseRepository.save(course)
        return "조회수 증가 성공"
    }

    // 최근 사용 코스 조회
    override fun getRecentCourses(userId: String): ResponseRecommendCourseDTO? {
        // RunningLog에서 유효한 코스 ID만 Top 5 조회
        val courseIds = runningLogRepository.findTop5CourseIdsByUserIdAndCourseStatusNotOrderByEndTimeDesc(userId, CourseStatus.DELETED)

        if (courseIds.isEmpty()) {
            return null
        }

        // 코스 정보를 한 번에 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        // 코스 정보를 CourseSummary로 매핑
        val courseSummaries = courses.map { course ->
            CourseSummary(
                id = course.id,
                title = course.title,
                distance = course.distance,
                mapUrl = course.mapUrl,
                sido = course.sido,
                sigungu = course.sigungu,
                tags = course.courseTags.map { it.tag.name },
                usageCount = course.usageCount
            )
        }

        return ResponseRecommendCourseDTO(
            title = "🕓 최근에 이용하셨어요!",
            item = courseSummaries
        )
    }

    // 인기 코스 조회
    override fun getPopularCourses(): ResponseRecommendCourseDTO? {
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 04시 초기화기 때문에 어제 날짜로 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(2) else LocalDate.now().minusDays(1)

        // 스케줄러에서 저장된 인기 코스 조회
        val popularCourses = popularCourseRepository.findByDate(targetDate)

        if (popularCourses.isEmpty()) {
            return null
        }

        val courseIds = popularCourses.map { it.courseId }
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        // 순서 유지를 위한 맵 생성
        val courseMap = courses.associateBy { it.id }

        val courseSummaries = popularCourses
            .sortedByDescending { it.usageCount }
            .map { popularCourse ->
                val course = courseMap[popularCourse.courseId]
                    ?: throw EntityNotFoundException("코스 ID ${popularCourse.courseId}를 찾을 수 없습니다.")

                CourseSummary(
                    id = course.id,
                    title = course.title,
                    distance = course.distance,
                    mapUrl = course.mapUrl,
                    sido = course.sido,
                    sigungu = course.sigungu,
                    tags = course.courseTags.map { it.tag.name },
                    usageCount = course.usageCount
                )
            }

        return ResponseRecommendCourseDTO(
            title = "🌟 어제 많이 이용한 코스에요!",
            item = courseSummaries
        )
    }

    // 급상승 코스 조회
    override fun getRisingCourse() : ResponseRecommendCourseDTO? {
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 조회할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(1) else LocalDate.now()

        // 스케줄러에서 저장된 인기 코스 조회
        val risingCourses = popularCourseRepository.findByDate(targetDate)

        if (risingCourses.isEmpty()) {
            return null
        }

        // 코스 ID 리스트 추출
        val courseIds = risingCourses.map { it.courseId }

        // 코스 데이터 한 번에 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)
        val courseMap = courses.associateBy { it.id }

        // 코스 정보를 CourseSummary로 매핑
        val courseSummaries = risingCourses
            .sortedByDescending { it.usageCount } // usageCount 기준 내림차순 정렬
            .map { risingCourse ->
                val course = courseMap[risingCourse.courseId]
                    ?: throw EntityNotFoundException("코스 ID ${risingCourse.courseId}를 찾을 수 없습니다.")

                CourseSummary(
                    id = course.id,
                    title = course.title,
                    distance = course.distance,
                    mapUrl = course.mapUrl,
                    sido = course.sido,
                    sigungu = course.sigungu,
                    tags = course.courseTags.map { it.tag.name },
                    usageCount = course.usageCount
                )
            }

        return ResponseRecommendCourseDTO(
            title = "📈 실시간으로 급상승중이에요!",
            item = courseSummaries
        )
    }

    // LLM 서버에 요청하여 코스 생성, 세션 ID를 통해 상태 메시지 전송
    override fun createCourseByLLM(llmRequestDTO: LlmRequestDTO, userId: String): List<AutoGeneratedCourseDTO> {
        val distanceUtil = DistanceUtil()
        val session = "/topic/status/${llmRequestDTO.statusSessionId}"

        val weather = weatherService.getWeatherByCity(llmRequestDTO.city, llmRequestDTO.nx, llmRequestDTO.ny)

        val condition = attendanceApiService.getAttendance(userId)?.bodyState
            ?: tendencyApiService.getTendency(userId)?.exerciseFrequency
            ?: "사용자 컨디션을 찾을 수 없습니다."

        val requestData = mapOf(
            "question" to llmRequestDTO.request,
            "lon" to llmRequestDTO.nx,
            "lat" to llmRequestDTO.ny,
            "weather" to weather.sky,
            "temperature" to weather.temperature,
            "condition" to condition
        )

        val restTemplate = RestTemplate()

        repeat(5) { attempt -> // 최대 5번 시도
            try {
                val response = restTemplate.postForEntity(llmServerUrl, requestData, Map::class.java)

                // 응답 상태 코드 확인
                if (response.statusCode.is2xxSuccessful) {
                    val responseBody = response.body as Map<*, *>
                    val courses = responseBody["data"] as? List<Map<String, Any>> ?: emptyList()

                    return courses.map { data ->
                        val lon = (data["position"] as List<Double>)[0]
                        val lat = (data["position"] as List<Double>)[1]
                        val positionNode = convertToGeoJsonPoint(lon, lat)
                        val coordinateNode = convertToGeoJsonLineString(data["coordinate"] as List<List<Double>>)

                        // 좌표 리스트로 거리 계산, km 단위로 변환을 위해 1000으로 나눔
                        val coordinates = data["coordinate"] as List<List<Double>>
                        val totalDistance = coordinates.zipWithNext { start, end ->
                            distanceUtil.haversine(start[1], start[0], end[1], end[0])
                        }.sum() / 1000.0

                        val location = locationApiService.getNearestLocation(lon, lat)
                        val sido = location?.sido ?: "Unknown"
                        val sigungu = location?.sigungu ?: "Unknown"

                        val tags = (data["tags"] as List<String>).map { tagName ->
                            tagRepository.findByName(tagName) ?: tagRepository.save(Tag(name = tagName))
                        }

                        AutoGeneratedCourseDTO(
                            id = UUID.randomUUID(),
                            title = data["title"] as String,
                            distance = totalDistance.toFloat(),
                            position = positionNode,
                            coordinate = coordinateNode,
                            tag = tags,
                            // 여긴 뺄 수도 있음
                            sido = sido,
                            sigungu = sigungu
                        )
                    }
                }
            } catch (e: HttpServerErrorException) {
                // 서버 오류 처리
                if (e.statusCode.is5xxServerError) {
                    messagingTemplate.convertAndSend(
                        session,
                        StatusMessageDTO("RETRY", "서버 오류 발생, 재시도 중...", null)
                    )
                } else {
                    throw RuntimeException("LLM 요청 실패", e)
                }
            } catch (e: Exception) {
                if (attempt == 4) { // 마지막 시도에서 예외 발생 시
                    throw RuntimeException("LLM 요청 중 오류 발생", e)
                }
            }
        }
        // 모든 시도에서 실패한 경우
       throw IllegalStateException("LLM 요청이 실패하여 코스를 생성할 수 없습니다.")
    }

    // 사용자 관심 태그 기반 코스 추천
    fun getUserInterestedTags(userId: String): ResponseRecommendCourseDTO? {
        val interestTags = tagApiService.getPersonalizedTags(userId)
            .sortedByDescending { it.score } // score 기준으로 정렬

        if (interestTags.isEmpty()) return null

        val coursesByTags = mutableListOf<ResponseCourseDTO>()
        var startIndex = 0

        // 최소 3개의 코스를 찾을 때까지 반복
        while (coursesByTags.size < 3 && startIndex < interestTags.size) {
            val tag = interestTags[startIndex]
            val tagEntity = tagRepository.findByName(tag.name)
                ?: throw EntityNotFoundException("태그를 찾을 수 없습니다: ${tag.name}")

            val courseIds = courseRepository.findCourseIdsByTagIdExcludingUser(
                tagEntity.id, CourseStatus.PUBLIC, userId, PageRequest.of(0, 3)
            ).content

            if (courseIds.isNotEmpty()) {
                val courses = courseRepository.findCoursesWithTagsByIds(courseIds)
                val bookmarkedCourseIds = bookmarkRepository.findBookmarkedCourseIdsByUserIdAndCourseIds(userId, courseIds)

                coursesByTags.addAll(
                    courses.map { course ->
                        val geoJsonPosition = geoJsonWriter.write(course.position)
                        val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

                        ResponseCourseDTO(
                            id = course.id,
                            title = course.title,
                            maker = course.maker,
                            bookmark = course.id in bookmarkedCourseIds,
                            hits = course.hits,
                            distance = course.distance,
                            position = removeCrsFieldAsJsonNode(geoJsonPosition),
                            coordinate = removeCrsFieldAsJsonNode(geoJsonCoordinate),
                            mapUrl = course.mapUrl,
                            createdAt = course.createdAt,
                            updatedAt = course.updatedAt,
                            author = course.maker.id == userId,
                            status = course.status,
                            tag = course.courseTags.map { it.tag },
                            sido = course.sido,
                            sigungu = course.sigungu,
                            commentCount = getCommentCount(course.id),
                            usageCount = course.usageCount
                        )
                    }
                )
            }
            startIndex++
        }

        if (coursesByTags.size < 3) return null

        val uniqueCourse = coursesByTags.shuffled().distinctBy { it.id }

        val courseSummaries = uniqueCourse.map { course ->
            CourseSummary(
                id = course.id,
                title = course.title,
                distance = course.distance,
                mapUrl = course.mapUrl,
                sido = course.sido,
                sigungu = course.sigungu,
                tags = course.tag.map { it.name },
                usageCount = course.usageCount
            )
        }

        return ResponseRecommendCourseDTO(
            title = "🎯 이런 코스들은 어때요?",
            item = courseSummaries
        )
    }


    // 홈에 아무 것도 안 뜰때를 대비한 전체 코스 반환
    fun getAllCoursesForHome(userId: String): ResponseRecommendCourseDTO {
        val allCourse = getAllCourses(userId, PageRequest.of(0, 10))
        val courseSummaries = allCourse.content.map { course ->
            CourseSummary(
                id = course.id,
                title = course.title,
                distance = course.distance,
                mapUrl = course.mapUrl,
                sido = course.sido,
                sigungu = course.sigungu,
                tags = course.tag.map { it.name },
                usageCount = course.usageCount
            )
        }

        return ResponseRecommendCourseDTO(
            title = "🗺️ 추천 코스에요!",
            item = courseSummaries
        )
    }

    // 최근 생성된 코스 조회
    private fun getRecentCreatedCourses(): ResponseRecommendCourseDTO {
        // 최근 생성된 PUBLIC 코스 조회
        val recentCreatedCourseIds = courseRepository.findTop10ByStatusOrderByCreatedAtDesc(CourseStatus.PUBLIC)

        val recentCreatedCourse = courseRepository.findCoursesWithTagsByIds(recentCreatedCourseIds)

        // ID 순서를 유지하며 정렬
        val sortedCourses = recentCreatedCourseIds.mapNotNull { id ->
            recentCreatedCourse.find { it.id == id }
        }

        // 코스 정보를 CourseSummary로 매핑
        val courseSummaries = sortedCourses.map { course ->
            CourseSummary(
                id = course.id,
                title = course.title,
                distance = course.distance,
                mapUrl = course.mapUrl,
                sido = course.sido,
                sigungu = course.sigungu,
                tags = course.courseTags.map { it.tag.name },
                usageCount = course.usageCount
            )
        }

        return ResponseRecommendCourseDTO(
            title = "🍞 따끈따끈 갓 나온 코스에요!",
            item = courseSummaries
        )

    }

    // 추천 코스 리스트
    override fun getCombinedRecommendCourses(nx: Double, ny:Double, city: String, userId: String): List<ResponseRecommendCourseDTO> {
        val nearCourseByDifficulty = getNearbyCoursesByDifficulty(nx, ny, city, userId)
        val recentCourse = getRecentCourses(userId)
        val popularCourse = getPopularCourses()
        val risingCourse = getRisingCourse()
        val userInterestedTags = getUserInterestedTags(userId)

        // 최근 코스, 인기 코스, 급상승 코스, 관심 태그 코스가 모두 null인 경우
        if (recentCourse == null && popularCourse == null && risingCourse == null && userInterestedTags == null) {
            return listOf(getAllCoursesForHome(userId), getRecentCreatedCourses())
        }

        // 필요한 코스 데이터를 리스트로 추가
        return listOfNotNull( nearCourseByDifficulty, userInterestedTags, recentCourse, popularCourse, risingCourse)
            .distinctBy { it.title } // 제목 기준으로 중복 제거
    }

    // 태그로 코스 검색
    override fun searchCoursesByTag(tagName: String, userId: String, pageable: Pageable): Page<ResponseCourseDTO> {
        // 태그 이름으로 태그 ID 조회
        val tag = tagRepository.findByName(tagName)
            ?: throw EntityNotFoundException("태그를 찾을 수 없습니다: $tagName")

        // 코스 ID만 조회 (PUBLIC 상태 필터링)
        val courseIdsPage = courseRepository.findCourseIdsByTagIdExcludingUser(tag.id, CourseStatus.PUBLIC, userId, pageable)
        val courseIds = courseIdsPage.content

        // 북마크된 courseIds 조회
        val bookmarkedCourseIds = bookmarkRepository.findBookmarkedCourseIdsByUserIdAndCourseIds(userId, courseIds)

        // Fetch Join으로 코스와 관련 데이터를 한 번에 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        // `user` 객체를 한 번만 조회
        val user = userApiService.getUserDataFromId(userId)

        // ID 순서를 유지하도록 수동 정렬
        val sortedCourses = courseIds.mapNotNull { id -> courses.find { it.id == id } }

        val responseCourses = sortedCourses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            // CRS 필드를 제거
            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            // 댓글 개수 조회
            val commentCount = getCommentCount(course.id)

            // List<Tag> 형태로 변환
            val tags = course.courseTags.map { it.tag }

            // 북마크 여부 확인
            val isBookmakred = course.id in bookmarkedCourseIds

            ResponseCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = isBookmakred,
                hits = course.hits,
                distance = course.distance,
                position = positionNode,
                coordinate = coordinateNode,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == user.id,
                status = course.status,
                tag = tags,
                sido = course.sido,
                sigungu = course.sigungu,
                commentCount = commentCount,
                usageCount = course.usageCount
            )
        }

        // 태그 로그 생성
        val tagLog = TagLog(
            tag = tag,
            user = user,
            actionType = ActionType.SEARCHED
        )
        tagLogRepository.save(tagLog)

        return PageImpl(responseCourses, pageable, courseIdsPage.totalElements)
    }

    // 난이도로 코스 검색
    private fun getNearbyCoursesByDifficulty(
        nx: Double,
        ny: Double,
        city: String,
        userId: String,
    ): ResponseRecommendCourseDTO? {

        val weather = weatherService.getWeatherByCity(city, nx, ny)
        val radius = 2000.0

        println("날씨 정보: ${weather.temperature}, ${weather.humidity}, ${weather.sky}")
        val attendance = attendanceApiService.getAttendance(userId)
            ?: return null

        // 미세먼지 데이터 가져오기
        val fineDustData = fineDustService.getFineDustData(nx, ny)
        println("미세먼지 정보: PM10=${fineDustData.pm10value}, PM2.5=${fineDustData.pm25value}")

        // 미세먼지 수치
        val pm10 = fineDustData.pm10value.toIntOrNull() ?: 0
        val pm25 = fineDustData.pm25value.toIntOrNull() ?: 0

        // 날씨 수치
        val temperature = weather.temperature.toDoubleOrNull() ?: 20.0
        val humidity = weather.humidity.replace("%", "").toIntOrNull() ?: 50
        val sky = weather.sky

        // 온도 점수 계산
        val tempScore = when {
            temperature >= 30.0 -> -2
            temperature in 25.0..29.9 -> -1
            temperature in 10.0..24.9 -> 0
            temperature < 10.0 -> -1
            else -> 0
        }

        // 습도 점수 계산
        val humidityScore = when {
            humidity >= 80 -> -1
            humidity in 60..79 -> 0
            else -> 1
        }

        // 하늘 상태 점수 계산
        val skyScore = when (sky) {
            "맑음" -> 1
            "구름 많음", "흐림" -> 0
            "비", "소나기", "눈" -> -1
            else -> 0
        }

        // 미세먼지 점수 계산
        val fineDustScore = when {
            pm10 > 150 || pm25 > 75 -> -2 // 매우 나쁨
            pm10 in 81..150 || pm25 in 36..75 -> -1 // 나쁨
            pm10 in 31..80 || pm25 in 16..35 -> 0 // 보통
            else -> 1 // 좋음
        }

        val weatherScore = tempScore + humidityScore + skyScore + fineDustScore

        // 난이도 결정
        val preference = attendance.courseDifficultyPreference?.toIntOrNull()

        val difficulties = when (preference) {
            1 -> listOf(CourseDifficulty.EASY)
            2 -> listOf(CourseDifficulty.NORMAL)
            3 -> listOf(CourseDifficulty.HARD)
            0, null -> {
                val conditionScore = (attendance.bodyState?.toIntOrNull() ?: 0) +
                        (attendance.feeling?.toIntOrNull() ?: 0)
                val totalScore = conditionScore + weatherScore

                when {
                    totalScore <= 1 -> listOf(CourseDifficulty.EASY)
                    totalScore in 2..4 -> listOf(CourseDifficulty.EASY, CourseDifficulty.NORMAL)
                    totalScore in 5..6 -> listOf(CourseDifficulty.NORMAL, CourseDifficulty.HARD)
                    else -> listOf(CourseDifficulty.HARD)
                }
            }
            else -> listOf(CourseDifficulty.EASY, CourseDifficulty.NORMAL)
        }

        println("난이도 : ${difficulties.joinToString { it.name }}")

        val courseIds = courseRepository.findNearbyCourseIdsByDifficulty(
            lon = nx,
            lat = ny,
            difficulties = difficulties.map { it.name },
            radius = radius
        )

        if (courseIds.isEmpty()) return null

        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        val courseSummaries = courses.map { course ->
            val tags = course.courseTags.map { it.tag }
            CourseSummary(
                id = course.id,
                title = course.title,
                distance = course.distance,
                mapUrl = course.mapUrl,
                sido = course.sido,
                sigungu = course.sigungu,
                tags = tags.map { it.name },
                usageCount = course.usageCount
            )
        }.shuffled()

        // 날씨 + 난이도 기반 추천 제목 설정
        val title = when {
            sky.contains("비", ignoreCase = true) || sky.contains("소나기", ignoreCase = true) ->
                "☔ 비 오는 날엔 가볍게 걷는 코스 어때요?"

            temperature >= 30.0 ->
                "🥵 무더운 날엔 짧고 쉬운 코스로 안전하게!"

            temperature < 10.0 ->
                "❄️ 추운 날씨엔 몸이 덜 무리가는 코스를 추천해요"

            humidity >= 85 ->
                "💧 습한 날씨엔 숨쉬기 편한 코스가 좋아요"

            pm10 in 81..150 || pm25 in 36..75 ->
                "🌫️ 미세먼지가 나쁜 날엔 쉬운 코스를 추천해요."

            difficulties.containsAll(listOf(CourseDifficulty.EASY, CourseDifficulty.NORMAL)) ->
                "🌤️ 오늘은 조금 가볍게 뛰어볼까요?"

            difficulties.containsAll(listOf(CourseDifficulty.NORMAL, CourseDifficulty.HARD)) ->
                "🔥 오늘은 조금 열심히 달려볼까요!!"

            difficulties.contains(CourseDifficulty.EASY) ->
                "😊 오늘 지친 당신을 위한 힐링 코스"

            difficulties.contains(CourseDifficulty.NORMAL) ->
                "🏃‍♂️ 오늘은 기분 좋게 달려볼까요?"

            difficulties.contains(CourseDifficulty.HARD) ->
                "💪 기운 넘치는 당신! 한계에 도전해볼까요?"

            else -> "📍 지금 날씨에 어울리는 추천 코스를 골라봤어요!"
        }

        return ResponseRecommendCourseDTO(
            title = title,
            item = courseSummaries
        )
    }

}
