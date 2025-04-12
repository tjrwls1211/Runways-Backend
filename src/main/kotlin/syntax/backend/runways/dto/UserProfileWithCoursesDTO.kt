package syntax.backend.runways.dto

import org.springframework.data.domain.Page

data class UserProfileWithCoursesDTO(
    val profileImage: String?,
    val nickname: String?,
    val accountPrivate: Boolean?,
    val followers: List<String>,
    val following: List<String>,
    val courses: Page<ResponseCourseDTO>
)