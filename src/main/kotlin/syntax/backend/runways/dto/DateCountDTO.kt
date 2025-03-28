package syntax.backend.runways.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.*

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
data class DateCountDTO @JsonCreator constructor(
    @JsonProperty("date") val date: String,
    @JsonProperty("count") val count: Int
)