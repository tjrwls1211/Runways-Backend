package syntax.backend.runways.service

import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User
import java.util.*

interface CourseApiService {
    fun getCourseList(maker: User): List<Course>
    fun updateCourse(courseId: UUID, title:String, token: String) : String
    fun getCourseById(courseId: UUID, token: String): ResponseCourseDTO
    fun deleteCourse(courseId: UUID, token: String): String
}