package syntax.backend.runways.dto

data class LlmRequestDTO (
    val request : String,
    val city : String,
    val nx : Double,
    val ny : Double,
)