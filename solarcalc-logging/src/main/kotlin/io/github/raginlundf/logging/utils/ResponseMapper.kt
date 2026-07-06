package io.github.raginlundf.logging.utils

import com.fasterxml.jackson.annotation.JsonInclude
import io.github.oshai.kotlinlogging.KotlinLogging
import tools.jackson.core.JacksonException
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.blackbird.BlackbirdModule
import tools.jackson.module.kotlin.KotlinModule

/**
 * This class is responsible for mapping and converting response objects.
 *
 * @property objectMapper The ObjectMapper instance used for object serialization and deserialization.
 */
class ResponseMapper {
    private val objectMapper: ObjectMapper = JsonMapper.builder()
        .disable(
            tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        )
        .disable(
            DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS,
            DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
        )
        .enable(SerializationFeature.INDENT_OUTPUT)
        .addModule(BlackbirdModule())
        .addModule(KotlinModule.Builder().build())
        .changeDefaultPropertyInclusion { incl ->
            incl.withValueInclusion(JsonInclude.Include.ALWAYS)
        }.build()

    /**
     * Converts an object to another type based on its structure and contents.
     *
     * @param obj The object to be converted. Can be of any type.
     * @return The converted object or null if the input object is null or cannot be converted.
     */
    @Suppress("SwallowedException")
    fun convertObject(obj: Any?): Any? {
        return if (obj == null) null else convertObjectToType(
            obj = obj
        )
    }

    /**
     * Converts an object to a JSON string representation using ObjectMapper.
     *
     * @param obj The object to be converted. Can be of any type.
     * @return The JSON string representation of the object, or WRITING_FAIL_MESSAGE if conversion fails.
     */
    fun writeObject(obj: Any?): String {
        return try {
            objectMapper.writer().writeValueAsString(obj)
        } catch (e: JacksonException) {
            log.error(e) { WRITING_FAIL_MESSAGE }
            WRITING_FAIL_MESSAGE
        }
    }

    private fun convertObjectToType(obj: Any): Any? {
        val types = listOf(
            MapTypeReference(),
            ArrayTypeReference(),
            PrimitiveTypeReference()
        )

        for (type in types) {
            val result = tryConvertValue(obj = obj, typeRef = type)
            if (result != null) {
                return result
            }
        }

        return null
    }

    private fun tryConvertValue(obj: Any, typeRef: TypeReference<*>): Any? {
        return try {
            objectMapper.convertValue(obj, typeRef)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private class MapTypeReference : TypeReference<Map<String?, Any?>?>()
    private class ArrayTypeReference : TypeReference<List<Any?>?>()
    private class PrimitiveTypeReference : TypeReference<String?>()

    companion object {
        private val log = KotlinLogging.logger {  }
        private const val WRITING_FAIL_MESSAGE = "Failed to write parameters"
    }
}
