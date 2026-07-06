package io.github.raginlundf.extensions

import kotlin.reflect.KProperty1

inline fun <reified T : Any> T.kotlinEquals(
    other: Any?,
    properties: Array<out KProperty1<T, Any?>>,
    noinline superEquals: (() -> Boolean)? = null
): Boolean {
    return when {
        other === this -> true
        other !is T -> false
        superEquals != null && !superEquals() -> false
        else -> properties.all { property ->
            val value = property.get(this)
            val otherValue = property.get(other)
            if (value is Array<*>) value.contentDeepEquals(otherValue as Array<*>)
            else value === otherValue || (value != null && value == otherValue)
        }
    }
}

@Suppress("MagicNumber")
inline fun <reified T : Any> T.kotlinHashCode(
    properties: Array<out KProperty1<T, Any?>>,
    noinline superHashCode: (() -> Int)? = null
): Int {
    var hashResult = 1
    properties.forEach { property ->
        val hash = when (val value = property.get(this)) {
            null -> 0
            is Array<*> -> value.contentDeepHashCode()
            else -> value.hashCode()
        }
        hashResult = 31 * hashResult + hash
    }
    if (superHashCode != null) {
        hashResult = 31 * hashResult + superHashCode()
    }
    return hashResult
}

@Suppress("MagicNumber")
inline fun <reified T : Any> T.kotlinToString(
    properties: Array<out KProperty1<T, Any?>>,
    omitNulls: Boolean = false,
    noinline superToString: (() -> String)? = null
): String {
    val className = T::class.java.simpleName
    val builder = StringBuilder(128).append(className).append("(")

    properties.forEach { property ->
        val value = property.get(this)
        if (omitNulls && value == null) return@forEach

        builder.append(property.name).append("=")
        if (value is Array<*>) {
            builder.append(value.contentDeepToString())
        } else {
            builder.append(value)
        }
        builder.append(", ")
    }

    if (superToString == null) {
        builder.setLength(builder.length - 2) // Remove the last ", "
    } else {
        builder.append("super=").append(superToString())
    }

    return builder.append(")").toString()
}
