package io.github.raginlundf.solarcalc.domain.models.calculation

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class CompletenessFlagSetConverter : AttributeConverter<Set<CompletenessFlag>, String> {

    override fun convertToDatabaseColumn(attribute: Set<CompletenessFlag>?): String {
        return attribute?.joinToString(",") { it.name } ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): Set<CompletenessFlag> {
        if (dbData.isNullOrBlank()) {
            return emptySet()
        }
        return dbData.split(",").map { CompletenessFlag.valueOf(it.trim()) }.toSet()
    }
}
