package io.github.raginlundf.logging.utils

import kotlin.collections.get
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ObfuscationUtilTests {
    @Test
    internal fun `ObfuscationUtil - obfuscate() - Obfuscation of a map`() {
        val obfuscationMap = mapOf(
            "secret" to "mysecret",
            "secretKey" to "key"
        )
        val result = ObfuscationUtil.obfuscate(obfuscationMap, setOf("secret"))
        assertEquals(
            actual = (result as Map<*, *>)["secret"],
            expected = "*****"
        )
        assertEquals(
            actual = result["secretKey"],
            expected = "key"
        )
    }

    @Test
    internal fun `ObfuscationUtil - obfuscate() - Obfuscation of a list`() {
        val obfuscationList = listOf(
            "secret",
            "mySecretKey"
        )
        val result = ObfuscationUtil.obfuscate(obfuscationList, setOf("secret"))
        assertEquals(
            expected = "*****",
            actual = (result as List<*>)[0]
        )
        assertEquals(
            actual = result[1],
            expected = "mySecretKey"
        )
    }

    @Test
    internal fun `ObfuscationUtil - obfuscate() - Obfuscation of a string`() {
        val obfuscationString = "secret"
        val noObfuscationString = "mySecretKey"
        val result = ObfuscationUtil.obfuscate(obfuscationString, setOf("secret"))
        val resultNoObfuscation = ObfuscationUtil.obfuscate(noObfuscationString, setOf("secret"))

        assertEquals(
            actual = "*****",
            expected = result
        )
        assertEquals(
            actual = "mySecretKey",
            expected = resultNoObfuscation
        )
    }

    @Test
    internal fun `ObfuscationUtil - skip() - Skip of a list entry`() {
        val obj = listOf(
            "secret",
            "key"
        )
        val result = ObfuscationUtil.skip(obj, setOf("secret"))

        assertEquals(
            actual = (result as List<*>).size,
            expected = 1
        )
        assertContains(
            iterable = (result as List<String>),
            element = "key",
        )
    }

    @Test
    internal fun `ObfuscationUtil - skip() - Skip of a map entry`() {
        val obj = mapOf(
            "secret" to "mySecret",
            "key" to "myKey"
        )
        val result = ObfuscationUtil.skip(obj, setOf("secret"))

        assertEquals(
            actual = (result as Map<*, *>).size,
            expected = 2
        )

        assertNull(
            actual = result["secret"]
        )

        assertEquals(
            actual = result["key"],
            expected = "myKey"
        )
    }
}
