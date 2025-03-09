package syntax.backend.runways.dto

import com.nimbusds.openid.connect.sdk.claims.Gender
import java.time.LocalDate

data class SignupDTO (
    val birthDate: LocalDate,
    val gender: Gender,
    val nickname: String,
)