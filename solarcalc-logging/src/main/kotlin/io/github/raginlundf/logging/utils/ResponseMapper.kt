package io.github.raginlundf.logging.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.raginlundf.solarcalc.jackson.JacksonUtil
import tools.jackson.core.JacksonException

class ResponseMapper {

    fun convertObject(obj: Any?): Any? = obj?.normalize()

    fun writeObject(obj: Any?): String {
        return try {
            WRITER.writeValueAsString(obj.normalize())
        } catch (e: JacksonException) {
            log.error(e) { WRITING_FAIL_MESSAGE }
            WRITING_FAIL_MESSAGE
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private const val WRITING_FAIL_MESSAGE = "Failed to write parameters"
        private val WRITER = JacksonUtil.mapper.writerWithDefaultPrettyPrinter()
    }
}

// Normalize any value to a plain Map/List/primitive tree so obfuscation can walk it by key and
// Jackson can render it. Unknown objects are rendered via toString() (prior behavior) rather than
// reflected into — we deliberately do not expand arbitrary object graphs into logs.
private fun Any?.normalize(): Any? = when (this) {
    null -> null
    is Boolean, is Number, is String -> this
    is Map<*, *> -> entries.associate { (k, v) -> (k?.toString() ?: "null") to v.normalize() }
    is Iterable<*> -> map { it.normalize() }
    is Array<*> -> map { it.normalize() }
    else -> toString()
}
