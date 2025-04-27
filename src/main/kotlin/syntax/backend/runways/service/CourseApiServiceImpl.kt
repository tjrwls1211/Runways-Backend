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
import syntax.backend.runways.dto.*
import syntax.backend.runways.entity.CommentStatus
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CommentApiRepository
import syntax.backend.runways.repository.CourseApiRepository
import syntax.backend.runways.repository.PopularCourseRepository
import syntax.backend.runways.repository.RunningLogApiRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.text.get

@Service
class CourseApiServiceImpl(
    private val courseApiRepository: CourseApiRepository,
    private val userApiService: UserApiService,
    private val locationApiService: LocationApiService,
    private val commentApiRepository: CommentApiRepository,
    private val courseQueryService: CourseQueryService,
    private val runningLogApiRepository: RunningLogApiRepository,
    private val popularCourseRepository: PopularCourseRepository,
) : CourseApiService {

    private val geoJsonWriter = GeoJsonWriter()

    // 좌표 추출
    private fun extractCoordinates(position: String): Pair<Double, Double> {
        val objectMapper = ObjectMapper()
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
        val courseData = courseApiRepository.findById(courseId).orElse(null) ?: throw Exception("코스를 찾을 수 없습니다.")
        return courseData
    }

    // 댓글 개수 호출
    private fun getCommentCount(courseId: UUID): Long {
        val commentStatus = CommentStatus.PUBLIC
        return commentApiRepository.countByPostId_IdAndStatus(courseId, commentStatus)
    }

    // 코스 생성
    override fun createCourse(requestCourseDTO: RequestCourseDTO, token: String) {
        val user = userApiService.getUserDataFromToken(token)
        val wktReader = WKTReader()

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
        courseApiRepository.save(newCourse)
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
    override fun updateCourse(requestUpdateCourseDTO: RequestUpdateCourseDTO, token: String): String {
        val courseData =
            courseApiRepository.findById(requestUpdateCourseDTO.courseId).orElse(null) ?: throw EntityNotFoundException(
                "코스를 찾을 수 없습니다."
            )
        val user = userApiService.getUserDataFromToken(token)

        if (courseData.maker.id != user.id) {
            return "코스 제작자가 아닙니다."
        }

        val wktReader = WKTReader()
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

        courseApiRepository.save(courseData)

        return "코스 업데이트 성공"
    }

    // 코스 상세정보
    override fun getCourseById(courseId: UUID, token: String): ResponseCourseDetailDTO {
        val optCourseData = courseApiRepository.findByIdWithTags(courseId)
        val commentCount = getCommentCount(courseId)

        if (optCourseData.isPresent) {
            val course = optCourseData.get()
            val user = userApiService.getUserDataFromToken(token)
            val isBookmarked = course.bookmark.isBookmarked(user.id)

            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            val (x, y) = extractCoordinates(geoJsonPosition)

            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

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
                tag = course.courseTags.map { it.tag.name },
                sido = sido,
                sigungu = sigungu,
                commentCount = commentCount,
            )
        } else {
            throw Exception("코스를 찾을 수 없습니다.")
        }
    }

    // 코스 삭제
    override fun deleteCourse(courseId: UUID, token: String): String {
        val optCourseData = courseApiRepository.findById(courseId)
        if (optCourseData.isPresent) {
            val course = optCourseData.get()
            val user = userApiService.getUserDataFromToken(token)
            if (course.maker.id != user.id) {
                return "코스 제작자가 아닙니다."
            }
            course.status = CourseStatus.DELETED
            courseApiRepository.save(course)
            return "코스 삭제 성공"
        } else {
            return "코스를 찾을 수 없습니다."
        }
    }

    // 북마크 추가
    override fun addBookmark(courseId: UUID, token: String): String {
        val course =
            courseApiRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.addBookMark(user.id)
        courseApiRepository.save(course)

        return "북마크 추가 성공"
    }

    // 북마크 삭제
    override fun removeBookmark(courseId: UUID, token: String): String {
        val course =
            courseApiRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다")
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.removeBookMark(user.id)
        courseApiRepository.save(course)

        return "북마크 삭제 성공"
    }

    // 전체 코스 리스트
    override fun getAllCourses(token: String, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC

        // 코스 ID 조회
        val courseIdsPage = courseApiRepository.findCourseIdsByStatus(statuses, pageable)
        val courseIds = courseIdsPage.content

        // 코스 데이터 조회
        val courses = courseApiRepository.findCoursesWithTagsByIds(courseIds)
        val maker = userApiService.getUserDataFromToken(token)

        // ResponseCourseDTO로 매핑
        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            val (x, y) = extractCoordinates(geoJsonPosition)

            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

            val commentCount = getCommentCount(course.id)

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
                tag = course.courseTags.map { it.tag.name },
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
        val courseIdsPage = courseApiRepository.findCourseIdsByTitleContainingAndStatus(title, statuses, pageable)
        val courseIds = courseIdsPage.content

        // 코스 데이터 조회
        val courses = courseApiRepository.findCoursesWithTagsByIds(courseIds)
        val maker = userApiService.getUserDataFromToken(token)

        // ResponseCourseDTO로 매핑
        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            val (x, y) = extractCoordinates(geoJsonPosition)

            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

            val commentCount = getCommentCount(course.id)

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
                tag = course.courseTags.map { it.tag.name },
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
            courseApiRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        course.hits.increaseHits()
        println(course.hits)
        courseApiRepository.save(course)
        return "조회수 증가 성공"
    }

    // 최근 사용 코스 조회
    override fun getRecentCourses(token: String): ResponseRecommendCourseDTO {
        val userId = userApiService.getUserDataFromToken(token).id
        val pageable = PageRequest.of(0, 10)

        // RunningLog에서 코스 ID만 조회
        val runningLogPage = runningLogApiRepository.findByUserIdOrderByEndTimeDesc(userId, pageable)
        val courseIdCountMap = runningLogPage.groupingBy { it.course.id }.eachCount() // 코스별 이용 횟수 집계

        // courseIds를 courseIdCountMap 키로 생성
        val courseIds = courseIdCountMap.keys.toList()

        // 코스 정보를 한 번에 조회
        val courses = courseApiRepository.findCoursesWithTagsByIds(courseIds)

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
                useCount = courseIdCountMap[course.id] ?: 0
            )
        }

        return ResponseRecommendCourseDTO(
            title = "최근 사용한 코스에요!",
            item = courseSummaries
        )
    }

    override fun getPopularCourses(): ResponseRecommendCourseDTO {
        val now = LocalDateTime.now()

        // 00:00 ~ 04:29 사이인지 확인
        val isEarlyMorning = now.toLocalTime().isBefore(LocalTime.of(4, 30))

        // 조회할 날짜 설정
        val targetDate = if (isEarlyMorning) LocalDate.now().minusDays(2) else LocalDate.now().minusDays(1)

        // 스케줄러에서 저장된 인기 코스 조회
        val popularCourses = popularCourseRepository.findByDate(targetDate) ?: emptyList()

        if (popularCourses.isEmpty()) {
            throw EntityNotFoundException("${targetDate}의 인기 코스를 찾을 수 없습니다.")
        }

        // 코스 ID 리스트 추출
        val courseIds = popularCourses.map { it.courseId }

        // 코스 데이터 한 번에 조회
        val courses = courseApiRepository.findCoursesWithTagsByIds(courseIds)
        val courseMap = courses.associateBy { it.id }

        // 코스 정보를 CourseSummary로 매핑
        val courseSummaries = popularCourses.map { popularCourse ->
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
                useCount = popularCourse.useCount
            )
        }

        return ResponseRecommendCourseDTO(
            title = "어제 많이 이용한 코스입니다!",
            item = courseSummaries
        )
    }
}