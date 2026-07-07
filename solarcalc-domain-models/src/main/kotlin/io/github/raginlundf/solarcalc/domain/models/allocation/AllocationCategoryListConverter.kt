package io.github.raginlundf.solarcalc.domain.models.allocation

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class AllocationCategoryListConverter : AttributeConverter<List<AllocationCategory>, String> {

    override fun convertToDatabaseColumn(attribute: List<AllocationCategory>?): String {
        return attribute?.joinToString(separator = ",") { it.name } ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<AllocationCategory> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }
        return dbData.split(",").map { AllocationCategory.valueOf(it.trim()) }
    }
}
