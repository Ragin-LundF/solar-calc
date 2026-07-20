package io.github.raginlundf.solarcalc.jackson

import tools.jackson.core.JsonGenerator
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.ValueSerializer
import tools.jackson.databind.module.SimpleModule
import java.math.BigDecimal

/**
 * Wire contract for BigDecimal, ported from the former kotlinx BigDecimalSerializer:
 * serialize as an unquoted JSON number in plain notation (no exponent), and on the way in accept
 * both JSON numbers (400.6) and quoted strings with a German comma decimal ("400,6").
 * Registered globally on the mapper, so every BigDecimal field is covered without per-field annotations.
 */
class BigDecimalModule : SimpleModule() {
    init {
        addSerializer(BigDecimal::class.java, Serializer())
        addDeserializer(BigDecimal::class.java, Deserializer())
    }

    private class Serializer : ValueSerializer<BigDecimal>() {
        override fun serialize(value: BigDecimal, gen: JsonGenerator, ctxt: SerializationContext) {
            // writeNumber(String) emits the value as an unquoted numeric literal.
            gen.writeNumber(value.toPlainString())
        }
    }

    private class Deserializer : ValueDeserializer<BigDecimal>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
            // valueAsString yields the textual form for both number and string tokens.
            return BigDecimal(p.valueAsString.replace(',', '.'))
        }
    }
}
