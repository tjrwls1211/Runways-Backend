package syntax.backend.runways.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.dto.ResponseCourseDetailDTO
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CourseApiRepository
import java.util.*

@Service
class CourseApiServiceImpl(
    private val courseApiRepository: CourseApiRepository,
    private val userApiService: UserApiService,
    private val locationApiService: LocationApiService
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

    override fun getCourseList(maker: User): List<ResponseCourseDTO> {
        val statuses = listOf(CourseStatus.PUBLIC, CourseStatus.FILTERED, CourseStatus.PRIVATE)
        val courseData = courseApiRepository.findByMaker_IdAndStatusInWithTags(maker.id, statuses)

        return courseData.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsField(geoJsonPosition)
            val coordinateNode = removeCrsField(geoJsonCoordinate)

            val (x, y) = extractCoordinates(geoJsonPosition)

            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

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
            )
        }
    }

    // TODO 코스 업데이트 - 태그 추가, (공개, 비공개) 상태 전환,
    override fun updateCourse(courseId: UUID, title: String, token: String): String {
        val courseData = courseApiRepository.findById(courseId).orElse(null) ?: return "코스를 찾을 수 없습니다."
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
        } else{
            return "코스를 찾을 수 없습니다."
        }
    }

    // 북마크 추가
    override fun addBookmark(courseId: UUID, token: String): String {
        val course = courseApiRepository.findById(courseId).orElse(null) ?: return "코스를 찾을 수 없습니다."
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.addBookMark(user.id)
        courseApiRepository.save(course)

        return "북마크 추가 성공"
    }

    // 전체 코스 조회
    override fun getAllCourses(token: String): List<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC
        val allCourseData = courseApiRepository.findByStatus(statuses)
        val maker = userApiService.getUserDataFromToken(token)

        return allCourseData.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsField(geoJsonPosition)
            val coordinateNode = removeCrsField(geoJsonCoordinate)

            val (x, y) = extractCoordinates(geoJsonPosition)

            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

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
            )
        }
    }

    // 북마크 삭제
    override fun removeBookmark(courseId: UUID, token: String): String {
        val course = courseApiRepository.findById(courseId).orElse(null) ?: return "코스를 찾을 수 없습니다"
        val user = userApiService.getUserDataFromToken(token)

        if (course.maker.id == user.id) {
            return "자신의 코스는 북마크할 수 없습니다."
        }

        course.bookmark.removeBookMark(user.id)
        courseApiRepository.save(course)

        return "북마크 삭제 성공"
    }

    // 코스 검색
    override fun searchCoursesByTitle(title: String, token: String): List<ResponseCourseDTO> {
        val statuses = CourseStatus.PUBLIC
        val courseData = courseApiRepository.findByTitleContainingAndStatus(title, statuses)
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
            )
        }
    }
}