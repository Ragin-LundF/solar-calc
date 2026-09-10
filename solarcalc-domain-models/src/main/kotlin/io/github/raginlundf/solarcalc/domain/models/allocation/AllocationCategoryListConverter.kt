package io.github.raginlundf.solarcalc.domain.models.allocation

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class AllocationCategoryListConverter : AttributeConverter<List<AllocationCategoryEnum>, String> {

    override fun convertToDatabaseColumn(attribute: List<AllocationCategoryEnum>?): String {
        return attribute?.joinToString(separator = ",") { it.name } ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<AllocationCategoryEnum> {
        if (dbData.isNullOrBlank()) {
            return emptyList()
        }
        return dbData.split(",").map { AllocationCategoryEnum.valueOf(it.trim()) }
    }
}
