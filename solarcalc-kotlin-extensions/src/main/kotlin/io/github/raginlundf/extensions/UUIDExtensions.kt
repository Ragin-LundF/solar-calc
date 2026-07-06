package io.github.raginlundf.extensions

import java.util.UUID

fun String.asUUID(): UUID {
    return UUID.fromString(this)
}
