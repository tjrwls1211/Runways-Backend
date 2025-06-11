package syntax.backend.runways.dto

data class LlmCourseResponseDTO(
    val title: String,
    val position: List<Double>, // [lon, lat]
    val coordinate: List<List<Double>>, // [[lon, lat], ...]
    val tags: List<String>
)
