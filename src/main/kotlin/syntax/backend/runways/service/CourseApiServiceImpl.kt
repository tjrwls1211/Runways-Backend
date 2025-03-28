package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CourseApiRepository
import java.util.*

@Service
class CourseApiServiceImpl(
    private val courseApiRepository: CourseApiRepository,
    private val userApiService: UserApiService
) : CourseApiService {

    // 만든 코스 다 불러오기
    override fun getCourseList(maker: User): List<Course> {
        val statuses = listOf(CourseStatus.PUBLIC, CourseStatus.FILTERED, CourseStatus.PRIVATE)
        val courseData = courseApiRepository.findByMaker_IdAndStatusIn(maker.id, statuses)
        return courseData
    }

    // 코스 업데이트
    override fun updateCourse(courseId: UUID, title: String, token: String): String {
        val courseData = courseApiRepository.findById(courseId).orElse(null) ?: return "Course not found"
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
    override fun getCourseById(courseId: UUID, token: String): ResponseCourseDTO {
        val optCourseData = courseApiRepository.findById(courseId)
        if (optCourseData.isPresent) {
            val course = optCourseData.get()
            return ResponseCourseDTO(
                id = course.id,
                title = course.title,
                maker = course.maker,
                bookmark = course.bookmark,
                hits = course.hits,
                distance = course.distance,
                coordinate = course.coordinate,
                mapUrl = course.mapUrl,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                author = course.maker.id == userApiService.getUserDataFromToken(token).id,
                status = course.status,
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
}