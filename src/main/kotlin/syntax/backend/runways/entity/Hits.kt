package syntax.backend.runways.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.*

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
data class Hits(
    val hits: List<DateCount> = emptyList()
)
