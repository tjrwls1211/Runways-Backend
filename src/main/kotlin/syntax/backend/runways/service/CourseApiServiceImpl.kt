package syntax.backend.runways.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.persistence.Entity
import jakarta.persistence.EntityNotFoundException
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import syntax.backend.runways.dto.RequestCourseDTO
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.dto.ResponseCourseDetailDTO
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CommentApiRepository
import syntax.backend.runways.repository.CourseApiRepository
import java.util.*

@Service
class CourseApiServiceImpl(
    private val courseApiRepository: CourseApiRepository,
    private val userApiService: UserApiService,
    private val locationApiService: LocationApiService,
    private val commentApiRepository: CommentApiRepository
) : CourseApiService {

    private val geoJsonWriter = GeoJsonWriter()

    private fun extractCoordinates(position: String): Pair<Double, Double> {
        val objectMapper = ObjectMapper()
        val node = objectMapper.readTree(position)
        val coordinates = node.get("coordinates")
        val y = coordinates.get(0).asDouble()
        val x = coordinates.get(1).asDouble()
        return Pair(x, y)
    }

    private fun removeCrsField(geoJson: String): String {
        val objectMapper = ObjectMapper()
        val node = objectMapper.readTree(geoJson) as ObjectNode
        node.remove("crs")
        return node.toString()
    }

    // 코스 데이터 호출
    override fun getCourseData(courseId: UUID): Course {
        val courseData = courseApiRepository.findById(courseId).orElse(null) ?: throw Exception("코스를 찾을 수 없습니다.")
        return courseData
    }

    // 댓글 개수 호출
    private fun getCommentCount(courseId: UUID): Long {
        return commentApiRepository.countByPostId_Id(courseId)
    }

    // 코스 생성
    override fun createCourse(requestCourseDTO: RequestCourseDTO, token: String) {
        val user = userApiService.getUserDataFromToken(token)
        val geometryFactory = GeometryFactory()

        // Point 변환
        val positionCoordinates = requestCourseDTO.position.split(",").map { it.toDouble() }
        val position = geometryFactory.createPoint(Coordinate(positionCoordinates[0], positionCoordinates[1]))

        // LineString 변환
        val coordinatePairs = requestCourseDTO.coordinate.split(";").map {
            val coords = it.split(",").map { coord -> coord.toDouble() }
            Coordinate(coords[0], coords[1])
        }.toTypedArray()
        val coordinate = geometryFactory.createLineString(coordinatePairs)

        val newCourse = Course(
            title = requestCourseDTO.title,
            maker = user,
            distance = requestCourseDTO.distance,
            position = position,
            coordinate = coordinate,
            mapUrl = requestCourseDTO.mapUrl
        )
        courseApiRepository.save(newCourse)
    }

    // 마이페이지 코스 리스트
    override fun getCourseList(maker: User, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = listOf(CourseStatus.PUBLIC, CourseStatus.FILTERED, CourseStatus.PRIVATE)
        val courseData = courseApiRepository.findByMaker_IdAndStatusInWithTags(maker.id, statuses, pageable)

        return courseData.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsField(geoJsonPosition)
            val coordinateNode = removeCrsField(geoJsonCoordinate)

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
                position = positionNode,
                coordinate = coordinateNode,
                distance = course.distance,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == maker.id,
                status = course.status,
                tag = course.courseTags.map { it.tag.name },
                sido = sido,
                sigungu = sigungu,
                commentCount = commentCount
            )
        }
    }

    // 코스 업데이트
    override fun updateCourse(courseId: UUID, title: String, token: String): String {
        val courseData = courseApiRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        val user = userApiService.getUserDataFromToken(token)

        if (courseData.maker.id != user.id) {
            return "코스 제작자가 아닙니다."
        }

        // 새로운 객체 생성 후 저장
        val updatedCourse = courseData.copy(title = title)
        courseApiRepository.save(updatedCourse)

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

            val positionNode = removeCrsField(geoJsonPosition)
            val coordinateNode = removeCrsField(geoJsonCoordinate)

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
        val course = courseApiRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.addBookMark(user.id)
        courseApiRepository.save(course)

        return "북마크 추가 성공"
    }

    // 전체 코스 리스트
    override fun getAllCourses(token: String, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC
        val allCourseData = courseApiRepository.findByStatus(statuses, pageable)
        val maker = userApiService.getUserDataFromToken(token)

        return allCourseData.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsField(geoJsonPosition)
            val coordinateNode = removeCrsField(geoJsonCoordinate)

            val (x, y) = if (geoJsonPosition != "{}") extractCoordinates(geoJsonPosition) else Pair(0.0, 0.0)

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
    }

    // 북마크 삭제
    override fun removeBookmark(courseId: UUID, token: String): String {
        val course = courseApiRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다")
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.removeBookMark(user.id)
        courseApiRepository.save(course)

        return "북마크 삭제 성공"
    }

    // 코스 검색
    override fun searchCoursesByTitle(title: String, token: String, pageable: Pageable): Page<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC
        val courseData = courseApiRepository.findByTitleContainingAndStatus(title, statuses, pageable)
        val maker = userApiService.getUserDataFromToken(token)
        return courseData.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsField(geoJsonPosition)
            val coordinateNode = removeCrsField(geoJsonCoordinate)

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
    }

    // 코스 조회수 증가
    @Transactional
    override fun increaseHits(courseId: UUID): String {
        val course = courseApiRepository.findById(courseId).orElse(null) ?: throw EntityNotFoundException("코스를 찾을 수 없습니다.")
        course.hits.increaseHits()
        println(course.hits)
        courseApiRepository.save(course)
        return "조회수 증가 성공"
    }
}