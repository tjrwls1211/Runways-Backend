package syntax.backend.runways.dto

import java.util.UUID

data class LlmRequestDTO (
    val statusSessionId : UUID,
    val request : String,
    val city : String,
    val nx : Double,
    val ny : Double,
)