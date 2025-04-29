package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.*
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User
import java.util.*

interface CourseApiService {
    fun getMyCourseList(maker: User, pageable: Pageable): Page<ResponseCourseDTO>
    fun updateCourse(requestUpdateCourseDTO: RequestUpdateCourseDTO, token : String) : String
    fun getCourseById(courseId: UUID, token: String): ResponseCourseDetailDTO
    fun deleteCourse(courseId: UUID, token: String): String
    fun addBookmark(courseId: UUID, token: String): String
    fun getAllCourses(token: String, pageable: Pageable): Page<ResponseCourseDTO>
    fun removeBookmark(courseId: UUID, token: String): String
    fun searchCoursesByTitle(title: String, token: String, pageable: Pageable): Page<ResponseCourseDTO>
    fun getCourseData(courseId: UUID): Course
    fun createCourse(requestCourseDTO: RequestCourseDTO,token: String)
    fun increaseHits(courseId: UUID): String
    fun getRecentCourses(token: String): ResponseRecommendCourseDTO?
    fun getCourseList(userId:String, pageable: Pageable): Page<ResponseCourseDTO>
    fun getPopularCourses(): ResponseRecommendCourseDTO?
    fun getRisingCourse() : ResponseRecommendCourseDTO?
}