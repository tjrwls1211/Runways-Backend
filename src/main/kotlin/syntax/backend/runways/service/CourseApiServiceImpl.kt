package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CourseApiRepository

@Service
class CourseApiServiceImpl(private val courseApiRepository : CourseApiRepository) : CourseApiService {
    override fun getCourseList(id:User): List<Course> {
        return courseApiRepository.findByMaker(id)
    }
}