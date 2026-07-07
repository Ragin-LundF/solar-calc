package io.github.raginlundf.solarcalc.domain.models.profile

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/** Persists a fixed-length list of integers (e.g. the 12-month heating distribution) as a CSV string. */
@Converter
class IntListConverter : AttributeConverter<List<Int>, String> {

    override fun convertToDatabaseColumn(attribute: List<Int>?): String {
        return attribute?.joinToString(separator = ",") ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<Int> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }
        return dbData.split(",").map { it.trim().toInt() }
    }
}
