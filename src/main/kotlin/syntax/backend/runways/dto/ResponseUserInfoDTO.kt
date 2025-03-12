package syntax.backend.runways.dto

import java.time.LocalDate

data class ResponseUserInfoDTO(
    val name: String?,
    val email: String?,
    val platform: String,
    val profileImage: String?,
    val birthDate: LocalDate,
    val gender: String?,
    val nickname: String?,
)