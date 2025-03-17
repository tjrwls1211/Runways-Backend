package syntax.backend.runways.service

import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User

interface CourseApiService {
    fun getCourseList(maker: User): List<Course>
}