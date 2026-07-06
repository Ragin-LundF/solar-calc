package io.github.raginlundf.logging.utils

import kotlin.collections.iterator

/**
 * Utility class for obfuscating sensitive data by replacing specified keys with asterisks.
 */
object ObfuscationUtil {
    const val OBFUSCATION = "*****"

    /**
     * Obfuscates the given data by replacing specified keys with asterisks.
     *
     * @param data The data to be obfuscated.
     * @param keysToObfuscate The set of keys to obfuscate in the data.
     * @return The obfuscated data with specified keys replaced by asterisks.
     */
    fun obfuscate(data: Any?, keysToObfuscate: Set<String>): Any? {
        return processElement(
            data = data, keys = keysToObfuscate,
            keyProcessor = { value, keyAsString, obfuscationKeys ->
                when {
                    obfuscationKeys.contains(keyAsString) -> OBFUSCATION
                    value is Map<*, *> || value is List<*> -> obfuscate(data = value, keysToObfuscate = obfuscationKeys)
                    else -> value
                }
            },
            listProcessor = { list, obfuscationKeys ->
                list.map { listItem -> obfuscate(data = listItem, keysToObfuscate = obfuscationKeys) }
            }
        )
    }

    /**
     * Skips specific keys in a given data structure.
     *
     * @param data The data structure to be processed. Can be any type.
     * @param keysToSkip The set of keys to be skipped.
     * @return The processed data structure with the specified keys skipped.
     */
    fun skip(data: Any?, keysToSkip: Set<String>): Any? {
        return processElement(
            data = data, keys = keysToSkip,
            keyProcessor = { value, keyAsString, skipKeys ->
                when {
                    skipKeys.contains(keyAsString) -> null
                    value is Map<*, *> || value is List<*> -> skip(data = value, keysToSkip = skipKeys)
                    else -> value
                }
            },
            listProcessor = { list, skipKeys ->
                list.filterNotNull().mapNotNull { listItem -> skip(data = listItem, keysToSkip = skipKeys) }
            }
        )
    }

    @Suppress("UncheckedCast")
    private fun processElement(
        data: Any?,
        keys: Set<String>,
        keyProcessor: (Any?, String, Set<String>) -> Any?,
        listProcessor: (List<Any?>, Set<String>) -> List<Any?>
    ): Any? {
        if (data == null) {
            return null
        }
        return when (data) {
            is List<*> -> listProcessor(data, keys)
            is Map<*, *> -> {
                val deepCopy = LinkedHashMap(data)
                for ((key, value) in data) {
                    deepCopy[key] = keyProcessor(value, key.toString(), keys)
                }
                deepCopy
            }

            else -> keyProcessor(data, data.toString(), keys)
        }
    }
}
