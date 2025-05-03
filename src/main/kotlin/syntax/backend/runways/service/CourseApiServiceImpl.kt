package syntax.backend.runways.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.EntityNotFoundException
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import syntax.backend.runways.dto.*
import syntax.backend.runways.entity.CommentStatus
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.CourseTag
import syntax.backend.runways.entity.User
import syntax.backend.runways.exception.NotAuthorException
import syntax.backend.runways.repository.CommentRepository
import syntax.backend.runways.repository.CourseRepository
import syntax.backend.runways.repository.CourseTagRepository
import syntax.backend.runways.repository.PopularCourseRepository
import syntax.backend.runways.repository.RunningLogRepository
import syntax.backend.runways.repository.TagRepository
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
    private val runningLogRepository: RunningLogRepository,
    private val popularCourseRepository: PopularCourseRepository,
    private val courseTagRepository : CourseTagRepository,
    private val tagRepository: TagRepository,
) : CourseApiService {

    private val geoJsonWriter = GeoJsonWriter()
    private val wktReader = WKTReader()
    private val objectMapper = ObjectMapper()

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
    private fun getCommentCount(courseId: UUID): Long {
        val commentStatus = CommentStatus.PUBLIC
        return commentRepository.countByPostId_IdAndStatus(courseId, commentStatus)
    }

    // 코스 생성
    override fun createCourse(requestCourseDTO: RequestCourseDTO, token: String) : UUID {
        val user = userApiService.getUserDataFromToken(token)

        // WKT 문자열을 Geometry 객체로 변환
        val position = wktReader.read(requestCourseDTO.position) // Point
        val coordinate = wktReader.read(requestCourseDTO.coordinate) // LineString

        if (position.geometryType != "Point" || coordinate.geometryType != "LineString") {
            throw IllegalArgumentException("유효하지 않은 WKT 형식: position은 Point여야 하고 coordinate는 LineString이어야 합니다.")
        }

        val newCourse = Course(
            title = requestCourseDTO.title,
            maker = user,
            distance = requestCourseDTO.distance,
            position = position as Point,
            coordinate = coordinate as LineString,
            mapUrl = requestCourseDTO.mapUrl,
            status = requestCourseDTO.status,
        )
        courseRepository.save(newCourse)

        // 태그 ID를 기반으로 CourseTag 생성 및 저장
        val courseTags = requestCourseDTO.tag.map { tagId ->
            val tag = tagRepository.findById(tagId).orElseThrow {
                IllegalArgumentException("태그 ID를 찾을 수 없습니다. : $tagId")
            }
            tag.usageCount += 1 // 태그 사용 횟수 증가
            CourseTag(course = newCourse, tag = tag)
        }
        courseTagRepository.saveAll(courseTags)

        return newCourse.id
    }

    // 마이페이지 코스 리스트
    override fun getMyCourseList(maker: User, pageable: Pageable): Page<ResponseCourseDTO> {
        return courseQueryService.getCourseList(maker.id, pageable, false)
    }

    // 공개 코스 조회
    override fun getCourseList(userId: String, pageable: Pageable): Page<ResponseCourseDTO> {
        return courseQueryService.getCourseList(userId, pageable, true)
    }

    // 코스 업데이트
    @Transactional
    override fun updateCourse(requestUpdateCourseDTO: RequestUpdateCourseDTO, token: String): UUID {
        val courseData =
            courseRepository.findById(requestUpdateCourseDTO.courseId).orElse(null) ?: throw EntityNotFoundException(
                "코스를 찾을 수 없습니다."
            )
        val user = userApiService.getUserDataFromToken(token)

        // 코스 제작자 확인
        if (courseData.maker.id != user.id) {
            throw NotAuthorException("코스 제작자가 아닙니다.")
        }

        // WKT 문자열을 Geometry 객체로 변환
        val position = wktReader.read(requestUpdateCourseDTO.position) // Point
        val coordinate = wktReader.read(requestUpdateCourseDTO.coordinate) // LineString

        if (position.geometryType != "Point" || coordinate.geometryType != "LineString") {
            throw IllegalArgumentException("유효하지 않은 WKT 형식: position은 Point여야 하고 coordinate는 LineString이어야 합니다.")
        }

        courseData.title = requestUpdateCourseDTO.title
        courseData.distance = requestUpdateCourseDTO.distance
        courseData.position = position as Point
        courseData.coordinate = coordinate as LineString
        courseData.mapUrl = requestUpdateCourseDTO.mapUrl
        courseData.status = requestUpdateCourseDTO.status
        courseData.updatedAt = LocalDateTime.now()

        courseRepository.save(courseData)

        // 기존 태그와 요청된 태그 비교
        val existingTags = courseData.courseTags.map { it.tag.id }
        val newTags = requestUpdateCourseDTO.tag

        // 추가해야 할 태그
        val tagsToAdd = newTags.filterNot { it in existingTags }.distinct()
        val courseTagsToAdd = tagsToAdd.map { tagId ->
            val tag = tagRepository.findById(tagId).orElseThrow {
                EntityNotFoundException("태그 ID를 찾을 수 없습니다. : $tagId")
            }
            tag.usageCount += 1 // 태그 사용 횟수 증가
            CourseTag(course = courseData, tag = tag)
        }
        courseTagRepository.saveAll(courseTagsToAdd)

        // 삭제해야 할 태그
        val tagsToRemove = existingTags.filterNot { it in newTags }.distinct()
        tagsToRemove.forEach { tagId ->
            val tag = tagRepository.findById(tagId).orElseThrow {
                EntityNotFoundException("태그 ID를 찾을 수 없습니다. : $tagId")
            }
            tag.usageCount -= 1 // 태그 사용 횟수 감소
            tagRepository.save(tag) // 태그 업데이트
        }
        courseTagRepository.deleteAllByCourseIdAndTagIdIn(courseData.id, tagsToRemove)

        return courseData.id
    }

    // 코스 상세정보
    override fun getCourseById(courseId: UUID, token: String): ResponseCourseDetailDTO {
        val optCourseData = courseRepository.findByIdWithTags(courseId)
        val commentCount = getCommentCount(courseId)

        // 코스 데이터가 존재하는지 확인
        if (optCourseData.isPresent) {
            val course = optCourseData.get()
            val user = userApiService.getUserDataFromToken(token)

            // 북마크 여부 확인
            val isBookmarked = course.bookmark.isBookmarked(user.id)

            // 코스의 위치와 좌표를 GeoJSON 형식으로 변환
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            // CRS 필드를 제거
            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            // 좌표 추출
            val (x, y) = extractCoordinates(geoJsonPosition)

            // 위치 정보 조회
            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

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
                author = course.maker.id == user.id,
                status = course.status,
                tag = tags,
                sido = sido,
                sigungu = sigungu,
                commentCount = commentCount,
            )
        } else {
            throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        }
    }

    // 코스 삭제
    override fun deleteCourse(courseId: UUID, token: String): String {
        val optCourseData = courseRepository.findById(courseId)
        if (optCourseData.isPresent) {
            val course = optCourseData.get()
            val user = userApiService.getUserDataFromToken(token)
            if (course.maker.id != user.id) {
                throw NotAuthorException("코스 제작자가 아닙니다.")
            }
            // 코스 상태 변경 -> 삭제 상태
            course.status = CourseStatus.DELETED
            courseRepository.save(course)
            return "코스 삭제 성공"
        } else {
            return "코스를 찾을 수 없습니다."
        }
    }

    // 북마크 추가
    override fun addBookmark(courseId: UUID, token: String): String {
        val course = courseRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.addBookMark(user.id)
        courseRepository.save(course)

        return "북마크 추가 성공"
    }

    // 북마크 삭제
    override fun removeBookmark(courseId: UUID, token: String): String {
        val course = courseRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다")
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.removeBookMark(user.id)
        courseRepository.save(course)

        return "북마크 삭제 성공"
    }

    // 전체 코스 리스트
    override fun getAllCourses(token: String, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC

        // 코스 ID 조회
        val courseIdsPage = courseRepository.findCourseIdsByStatus(statuses, pageable)
        val courseIds = courseIdsPage.content

        // 코스 데이터 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)
        val maker = userApiService.getUserDataFromToken(token)

        // ResponseCourseDTO로 매핑
        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            // CRS 필드를 제거
            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            // 좌표 추출
            val (x, y) = extractCoordinates(geoJsonPosition)

            // 위치 정보 조회
            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

            // 댓글 개수 조회
            val commentCount = getCommentCount(course.id)

            // List<Tag> 형태로 변환
            val tags = course.courseTags.map { it.tag }

            ResponseCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = course.bookmark,
                hits = course.hits,
                distance = course.distance,
                position = positionNode,
                coordinate = coordinateNode,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == maker.id,
                status = course.status,
                tag = tags,
                sido = sido,
                sigungu = sigungu,
                commentCount = commentCount,
            )
        }

        // 페이징 결과 반환
        return PageImpl(responseCourses, pageable, courseIdsPage.totalElements)
    }

    // 코스 검색
    override fun searchCoursesByTitle(title: String, token: String, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC

        // 코스 ID 조회
        val courseIdsPage = courseRepository.findCourseIdsByTitleContainingAndStatus(title, statuses, pageable)
        val courseIds = courseIdsPage.content

        // 코스 데이터 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)
        val maker = userApiService.getUserDataFromToken(token)

        // ResponseCourseDTO로 매핑
        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            // CRS 필드를 제거
            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            // 좌표 추출
            val (x, y) = extractCoordinates(geoJsonPosition)

            // 위치 정보 조회
            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

            // 댓글 개수 조회
            val commentCount = getCommentCount(course.id)

            // List<Tag> 형태로 변환
            val tags = course.courseTags.map { it.tag }

            ResponseCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = course.bookmark,
                hits = course.hits,
                distance = course.distance,
                position = positionNode,
                coordinate = coordinateNode,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == maker.id,
                status = course.status,
                tag = tags,
                sido = sido,
                sigungu = sigungu,
                commentCount = commentCount,
            )
        }

        // 페이징 결과 반환
        return PageImpl(responseCourses, pageable, courseIdsPage.totalElements)
    }

    // 코스 조회수 증가
    @Transactional
    override fun increaseHits(courseId: UUID): String {
        val course =
            courseRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        course.hits.increaseHits()
        println(course.hits)
        courseRepository.save(course)
        return "조회수 증가 성공"
    }

    // 최근 사용 코스 조회
    override fun getRecentCourses(token: String): ResponseRecommendCourseDTO? {
        val userId = userApiService.getUserDataFromToken(token).id
        val pageable = PageRequest.of(0, 10)

        // RunningLog에서 코스 ID만 조회
        val runningLogPage = runningLogRepository.findByUserIdOrderByEndTimeDesc(userId, pageable)
        val courseIdCountMap = runningLogPage.groupingBy { it.course.id }.eachCount() // 코스별 이용 횟수 집계

        // courseIds를 courseIdCountMap 키로 생성
        val courseIds = courseIdCountMap.keys.toList()

        // 코스 정보를 한 번에 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        // 코스 정보를 CourseSummary로 매핑
        val courseSummaries = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)

            val (x, y) = extractCoordinates(geoJsonPosition)

            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

            CourseSummary(
                id = course.id,
                title = course.title,
                distance = course.distance,
                mapUrl = course.mapUrl,
                sido = sido,
                sigungu = sigungu,
                tags = course.courseTags.map { it.tag.name },
                usageCount = courseIdCountMap[course.id] ?: 0
            )
        }

        return ResponseRecommendCourseDTO(
            title = "최근 사용한 코스에요!",
            item = courseSummaries
        )
    }

    // 인기 코스 조회
    override fun getPopularCourses(): ResponseRecommendCourseDTO? {
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 조회할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(2) else LocalDate.now().minusDays(1)

        // 스케줄러에서 저장된 인기 코스 조회
        val popularCourses = popularCourseRepository.findByDate(targetDate)

        if (popularCourses.isEmpty()) {
            return null
        }

        // 코스 ID 리스트 추출
        val courseIds = popularCourses.map { it.courseId }

        // 코스 데이터 한 번에 조회
        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)
        val courseMap = courses.associateBy { it.id }

        // 코스 정보를 CourseSummary로 매핑
        val courseSummaries = popularCourses
            .sortedByDescending { it.usageCount } // usageCount 기준 내림차순 정렬
            .map { popularCourse ->
                val course = courseMap[popularCourse.courseId]
                    ?: throw EntityNotFoundException("코스 ID ${popularCourse.courseId}를 찾을 수 없습니다.")

                val geoJsonPosition = geoJsonWriter.write(course.position)
                val (x, y) = extractCoordinates(geoJsonPosition)

                val location = locationApiService.getNearestLocation(x, y)
                val sido = location?.sido ?: "Unknown"
                val sigungu = location?.sigungu ?: "Unknown"

                CourseSummary(
                    id = course.id,
                    title = course.title,
                    distance = course.distance,
                    mapUrl = course.mapUrl,
                    sido = sido,
                    sigungu = sigungu,
                    tags = course.courseTags.map { it.tag.name },
                    usageCount = popularCourse.usageCount
                )
            }

        return ResponseRecommendCourseDTO(
            title = "어제 많이 이용한 코스에요!",
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

                val geoJsonPosition = geoJsonWriter.write(course.position)
                val (x, y) = extractCoordinates(geoJsonPosition)

                val location = locationApiService.getNearestLocation(x, y)
                val sido = location?.sido ?: "Unknown"
                val sigungu = location?.sigungu ?: "Unknown"

                CourseSummary(
                    id = course.id,
                    title = course.title,
                    distance = course.distance,
                    mapUrl = course.mapUrl,
                    sido = sido,
                    sigungu = sigungu,
                    tags = course.courseTags.map { it.tag.name },
                    usageCount = risingCourse.usageCount
                )
            }

        return ResponseRecommendCourseDTO(
            title = "오늘 급상승 코스에요!",
            item = courseSummaries
        )
    }

    override fun createCourseByLLM(question: String, token: String): Map<String, Any> {
        val user = userApiService.getUserDataFromToken(token)

        // LLM 서버 URL
        val url = "http://127.0.0.1:8000/api/recommend"

        // 요청 데이터 생성
        val requestData = mapOf(
            "question" to question,
            "lon" to 126.9348964, // 사용자 위치 경도 (예시 값)
            "lat" to 37.5157975,  // 사용자 위치 위도 (예시 값)
            "weather" to "맑음",   // 날씨 정보 (예시 값)
            "temperature" to 25,  // 온도 (예시 값)
            "condition" to "좋음"  // 사용자 컨디션 (예시 값)
        )

        // RestTemplate 초기화
        val restTemplate = RestTemplate()

        return try {
            // LLM 서버로 POST 요청 보내기
            val response = restTemplate.postForEntity(url, requestData, Map::class.java)

            if (response.statusCode.is2xxSuccessful) {
                // 응답 데이터 반환
                response.body as? Map<String, Any>
                    ?: throw RuntimeException("LLM 서버 응답이 비어 있습니다.")
            } else {
                throw RuntimeException("LLM 서버 요청 실패: ${response.statusCode}")
            }
        } catch (e: Exception) {
            throw RuntimeException("LLM 요청 중 오류 발생: ${e.message}", e)
        }
    }

    // 추천 코스 리스트
    override fun getCombinedRecommendCourses(token: String): List<ResponseRecommendCourseDTO> {
        val recentCourse = getRecentCourses(token)
        val popularCourse = getPopularCourses()
        val risingCourse = getRisingCourse()

        // 필요한 코스 데이터를 리스트로 추가
        return listOfNotNull(recentCourse, popularCourse, risingCourse)
    }
}