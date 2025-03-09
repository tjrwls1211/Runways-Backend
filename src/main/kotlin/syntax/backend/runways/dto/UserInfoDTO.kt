package syntax.backend.runways.dto

import com.nimbusds.openid.connect.sdk.claims.Gender
import java.time.LocalDate

data class UserInfoDTO(
    val name: String,
    val email: String,
    val platform: String,
    val profileImage: String?,
    val birthDate: LocalDate,
    val gender: String?,
    val nickname: String?,
)