package syntax.backend.runways.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class DateCountDTO @JsonCreator constructor(
    @JsonProperty("date") var date: LocalDate,
    @JsonProperty("count") var count: Int
)