package syntax.backend.runways.dto

import org.springframework.data.domain.Page
import syntax.backend.runways.entity.Follow
import java.time.LocalDate

data class ResponseMyInfoDTO(
    val id : String,
    val name: String?,
    val email: String?,
    val platform: String,
    val profileImage: String?,
    val birthDate: LocalDate,
    val gender: String?,
    val nickname: String?,
    val follow:Follow?,
    val marketing: Boolean,
    val accountPrivate: Boolean,
    val courses: Page<ResponseMyCourseDTO>,
    val experience: Int
)