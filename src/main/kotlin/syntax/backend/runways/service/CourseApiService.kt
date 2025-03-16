package syntax.backend.runways.service

import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User

interface CourseApiService {
    fun getCourseList(id: User): List<Course>
}