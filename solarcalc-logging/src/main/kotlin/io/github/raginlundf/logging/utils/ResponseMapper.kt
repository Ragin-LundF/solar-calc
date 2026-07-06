package io.github.raginlundf.logging.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

class ResponseMapper {

    fun convertObject(obj: Any?): Any? = obj?.toJsonElement()?.toKotlinValue()

    fun writeObject(obj: Any?): String {
        return try {
            JSON.encodeToString(JsonElement.serializer(), obj.toJsonElement())
        } catch (e: SerializationException) {
            log.error(e) { WRITING_FAIL_MESSAGE }
            WRITING_FAIL_MESSAGE
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private const val WRITING_FAIL_MESSAGE = "Failed to write parameters"
        private val JSON = Json { prettyPrint = true }
    }
}

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Map<*, *> -> buildJsonObject {
        forEach { (k, v) -> put(k?.toString() ?: "null", v.toJsonElement()) }
    }
    is Iterable<*> -> buildJsonArray { forEach { add(it.toJsonElement()) } }
    is Array<*> -> buildJsonArray { forEach { add(it.toJsonElement()) } }
    else -> JsonPrimitive(toString())
}

private fun JsonElement.toKotlinValue(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
    is JsonObject -> entries.associate { (k, v) -> k to v.toKotlinValue() }
    is JsonArray -> map { it.toKotlinValue() }
}
