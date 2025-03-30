package syntax.backend.runways.service

import org.apache.coyote.Response
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.dto.ResponseCourseDetailDTO
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User
import java.util.*

interface CourseApiService {
    fun getCourseList(maker: User): List<ResponseCourseDTO>
    fun updateCourse(courseId: UUID, title:String, token: String) : String
    fun getCourseById(courseId: UUID, token: String): ResponseCourseDetailDTO
    fun deleteCourse(courseId: UUID, token: String): String
    fun addBookmark(courseId: UUID, token: String): String
    fun getAllCourses(token: String): List<ResponseCourseDTO>
}