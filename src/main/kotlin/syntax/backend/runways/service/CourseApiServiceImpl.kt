package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CourseApiRepository
import java.util.*

@Service
class CourseApiServiceImpl(
    private val courseApiRepository : CourseApiRepository,
    private val userApiService: UserApiService
) : CourseApiService {

    // 만든 코스 다 불러오기
    override fun getCourseList(maker: User): List<Course> {
        val courseData = courseApiRepository.findByMaker_Id(maker.id)
        return courseData
    }

    // 코스 업데이트
    override fun updateCourse(courseId: UUID, title: String, token: String): String {
        val courseData = courseApiRepository.findById(courseId).orElse(null) ?: return "Course not found"
        val user = userApiService.getUserDataFromToken(token)

        if (courseData.maker.id != user.id) {
            return "Unauthorized: You are not the maker of this course"
        }

        // 새로운 객체 생성 후 저장
        val updatedCourse = courseData.copy(title = title)
        courseApiRepository.save(updatedCourse)

        return "Course updated successfully"
    }
}