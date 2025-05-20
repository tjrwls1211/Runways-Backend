package syntax.backend.runways.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.dto.ResponseMyCourseDTO
import syntax.backend.runways.entity.CommentStatus
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.repository.BookmarkRepository
import syntax.backend.runways.repository.CommentRepository
import syntax.backend.runways.repository.CourseRepository

@Service
class CourseQueryService(
    private val courseRepository: CourseRepository,
    private val locationApiService: LocationApiService,
    private val commentRepository: CommentRepository,
    private val bookmarkRepository: BookmarkRepository
) {
    private val geoJsonWriter = GeoJsonWriter()

    // 코스 목록 조회
    fun getCourseList(userId: String, pageable: Pageable, status: Boolean): Page<ResponseMyCourseDTO> {
        val statuses = if (status) {
            listOf(CourseStatus.PUBLIC)
        } else {
            listOf(CourseStatus.PUBLIC, CourseStatus.FILTERED, CourseStatus.PRIVATE)
        }

        val courseIdsPage = courseRepository.findCourseIdsByMakerAndStatuses(userId, statuses, pageable)
        val courseIds = courseIdsPage.content

        val bookmarkedCourseIds = bookmarkRepository.findBookmarkedCourseIdsByUserIdAndCourseIds(userId, courseIds)

        // 북마크 수를 한 번에 조회
        val bookmarkCounts = bookmarkRepository.countBookmarksByCourseIds(courseIds)
        val bookmarkCountMap = bookmarkCounts.associateBy({ it.courseId }, { it.bookmarkCount })

        // 댓글 수를 한 번에 조회
        val commentCounts = commentRepository.countCommentsByCourseIdsAndStatus(courseIds, CommentStatus.PUBLIC)
        val commentCountMap = commentCounts.associateBy({ it.courseId }, { it.commentCount })

        val courses = courseRepository.findCoursesWithTagsByIds(courseIds)

        val responseCourses = courses.map { course ->
            val geoJsonPosition = geoJsonWriter.write(course.position)
            val geoJsonCoordinate = geoJsonWriter.write(course.coordinate)

            val positionNode = removeCrsFieldAsJsonNode(geoJsonPosition)
            val coordinateNode = removeCrsFieldAsJsonNode(geoJsonCoordinate)

            val (x, y) = extractCoordinates(geoJsonPosition)

            val location = locationApiService.getNearestLocation(x, y)
            val sido = location?.sido ?: "Unknown"
            val sigungu = location?.sigungu ?: "Unknown"

            // 댓글 수를 맵에서 조회 (Long -> Int 변환)
            val commentCount = (commentCountMap[course.id] ?: 0L).toInt()

            val tags = course.courseTags.map { it.tag }

            val isBookmarked = course.id in bookmarkedCourseIds

            // 북마크 수를 맵에서 조회 (Long -> Int 변환)
            val bookmarkCount = (bookmarkCountMap[course.id] ?: 0L).toInt()

            ResponseMyCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = isBookmarked,
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
                sido = sido,
                sigungu = sigungu,
                commentCount = commentCount,
                usageCount = course.usageCount,
            )
        }

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