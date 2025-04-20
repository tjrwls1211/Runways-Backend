package syntax.backend.runways.dto

import org.springframework.data.domain.Page
import syntax.backend.runways.entity.Follow

data class UserProfileWithCoursesDTO(
    val profileImage: String?,
    val nickname: String?,
    val accountPrivate: Boolean?,
    val follow: Follow?,
    val courses: Page<ResponseCourseDTO>,
    val isFollow: Boolean,
    val experience : Float
)