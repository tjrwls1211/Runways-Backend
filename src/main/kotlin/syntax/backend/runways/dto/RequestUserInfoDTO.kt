package syntax.backend.runways.dto

import java.time.LocalDate

data class RequestUserInfoDTO (
    val birthDate: LocalDate,
    val gender: String,
    val nickname: String,
    val marketing: Boolean,
    val accountPrivate: Boolean,
)