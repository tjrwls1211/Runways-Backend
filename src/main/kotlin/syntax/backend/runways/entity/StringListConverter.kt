package syntax.backend.runways.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        return dbData?.let { objectMapper.readValue(it) } ?: emptyList()
    }
}