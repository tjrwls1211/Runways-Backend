package syntax.backend.runways.converter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class JsonNodeConverter : AttributeConverter<JsonNode, String> {
    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: JsonNode?): String? {
        return attribute?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): JsonNode? {
        return dbData?.let { objectMapper.readTree(it) }
    }
}