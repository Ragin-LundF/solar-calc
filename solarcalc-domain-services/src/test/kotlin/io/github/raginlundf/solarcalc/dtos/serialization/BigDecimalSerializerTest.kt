package io.github.raginlundf.solarcalc.dtos.serialization

import io.github.raginlundf.solarcalc.dtos.input.UpsertMonthlyEnergyInputRequest
import io.github.raginlundf.solarcalc.jackson.JacksonUtil
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BigDecimalSerializerTest {

    private val mapper = JacksonUtil.createObjectMapper()

    @Test
    fun `decodes unquoted JSON numbers`() {
        val request = mapper.readValue(
            """{"period":"2024-12","generationKwh":400.6,"feedInKwh":11.5}""",
            UpsertMonthlyEnergyInputRequest::class.java,
        )
        assertEquals(expected = BigDecimal("400.6"), actual = request.generationKwh)
        assertEquals(expected = BigDecimal("11.5"), actual = request.feedInKwh)
    }

    @Test
    fun `still accepts quoted strings with a comma decimal`() {
        val request = mapper.readValue(
            """{"period":"2024-12","generationKwh":"400,6"}""",
            UpsertMonthlyEnergyInputRequest::class.java,
        )
        assertEquals(expected = BigDecimal("400.6"), actual = request.generationKwh)
    }

    @Test
    fun `encodes BigDecimal as an unquoted number`() {
        val encoded = mapper.writeValueAsString(
            UpsertMonthlyEnergyInputRequest(period = "2024-12", generationKwh = BigDecimal("400.6")),
        )
        assertTrue(actual = encoded.contains("\"generationKwh\":400.6"), message = encoded)
    }
}
