package syntax.backend.runways.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.entity.CommentStatus
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.repository.CommentApiRepository
import syntax.backend.runways.repository.CourseApiRepository

@Service
class CourseQueryService(
    private val courseApiRepository: CourseApiRepository,
    private val locationApiService: LocationApiService,
    private val commentApiRepository: CommentApiRepository
) {
    private val geoJsonWriter = GeoJsonWriter()

    fun getCourseList(userId: String, pageable: Pageable, status: Boolean): Page<ResponseCourseDTO> {
        // 상태에 따라 CourseStatus 목록 설정
        val statuses = if (status) {
            listOf(CourseStatus.PUBLIC)
        } else {
            listOf(CourseStatus.PUBLIC, CourseStatus.FILTERED, CourseStatus.PRIVATE)
        }

        // 코스 ID 조회 (페이징 적용)
        val courseIdsPage = courseApiRepository.findCourseIdsByMakerAndStatuses(userId, statuses, pageable)
        val courseIds = courseIdsPage.content

        // 코스 데이터 조회
        val courses = courseApiRepository.findCoursesWithTagsByIds(courseIds)


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

            val commentCount = commentApiRepository.countByPostId_IdAndStatus(course.id, CommentStatus.PUBLIC)

            val tags = course.courseTags.map { it.tag }

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
                author = course.maker.id == userId,
                status = course.status,
                tag = tags,
                sido = sido,
                sigungu = sigungu,
                commentCount = commentCount
            )
        }

        // 페이징 결과 반환
        return PageImpl(responseCourses, pageable, courseIdsPage.totalElements)
    }

    private fun removeCrsFieldAsJsonNode(geoJson: String): ObjectNode {
        val objectMapper = ObjectMapper()
        val node = objectMapper.readTree(geoJson) as ObjectNode
        node.remove("crs")
        return node
    }

    private fun extractCoordinates(position: String): Pair<Double, Double> {
        val objectMapper = ObjectMapper()
        val node = objectMapper.readTree(position)
        val coordinates = node.get("coordinates")
        val x = coordinates.get(0).asDouble()
        val y = coordinates.get(1).asDouble()
        return Pair(x, y)
    }
}