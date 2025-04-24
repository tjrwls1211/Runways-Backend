package syntax.backend.runways.entity

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import syntax.backend.runways.deserializer.HitsDeserializer
import syntax.backend.runways.dto.DateCountDTO
import java.time.LocalDate

@JsonDeserialize(using = HitsDeserializer::class)
data class Hits(
    var dateCounts: MutableList<DateCountDTO> = mutableListOf()
){
    fun increaseHits() {
        val today = LocalDate.now()
        var found = false

        dateCounts.forEach { dateCount ->
            if (dateCount.date.isEqual(today)) {
                dateCount.count += 1
                found = true
            }
        }

        if (!found) {
            dateCounts.add(DateCountDTO(today, 1))
            println("dateCounts : $dateCounts")
        }
    }
}