package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CourseApiRepository

@Service
class CourseApiServiceImpl(private val courseApiRepository : CourseApiRepository) : CourseApiService {
    override fun getCourseList(maker: User): List<Course> {
        val courseData = courseApiRepository.findByMaker_Id(maker.id)
        return courseData
    }
}