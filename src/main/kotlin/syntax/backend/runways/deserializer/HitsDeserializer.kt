package syntax.backend.runways.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import syntax.backend.runways.dto.DateCountDTO
import syntax.backend.runways.entity.Hits

class HitsDeserializer : JsonDeserializer<Hits>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Hits {
        val node: JsonNode = p.codec.readTree(p)
        val dateCounts = node.map {
            val dateNode = it.get("date")
            val countNode = it.get("count")
            DateCountDTO(
                dateNode?.asText() ?: "",
                countNode?.asInt() ?: 0
            )
        }

        return Hits(dateCounts)
    }
}