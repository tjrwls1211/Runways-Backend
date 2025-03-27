package syntax.backend.runways.entity

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import syntax.backend.runways.deserializer.HitsDeserializer
import syntax.backend.runways.dto.DateCountDTO

@JsonDeserialize(using = HitsDeserializer::class)
data class Hits(
    val dateCounts: List<DateCountDTO> = emptyList()
)