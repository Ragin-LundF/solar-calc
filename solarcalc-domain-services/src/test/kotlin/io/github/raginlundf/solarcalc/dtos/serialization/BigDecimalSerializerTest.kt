package io.github.raginlundf.solarcalc.dtos.serialization

import io.github.raginlundf.solarcalc.dtos.input.UpsertMonthlyEnergyInputRequest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BigDecimalSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes unquoted JSON numbers`() {
        val request = json.decodeFromString<UpsertMonthlyEnergyInputRequest>(
            """{"period":"2024-12","generationKwh":400.6,"feedInKwh":11.5,"referencePrice":null}""",
        )
        assertEquals(BigDecimal("400.6"), request.generationKwh)
        assertEquals(BigDecimal("11.5"), request.feedInKwh)
    }

    @Test
    fun `still accepts quoted strings`() {
        val request = json.decodeFromString<UpsertMonthlyEnergyInputRequest>(
            """{"period":"2024-12","generationKwh":"400,6"}""",
        )
        assertEquals(BigDecimal("400.6"), request.generationKwh)
    }

    @Test
    fun `encodes BigDecimal as an unquoted number`() {
        val encoded = json.encodeToString(
            UpsertMonthlyEnergyInputRequest(period = "2024-12", generationKwh = BigDecimal("400.6")),
        )
        assertTrue(encoded.contains("\"generationKwh\":400.6"), encoded)
    }
}
