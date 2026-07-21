package io.github.raginlundf.solarcalc.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

object JacksonUtil {
    @JvmStatic
    val mapper: ObjectMapper = createObjectMapper()

    @JvmStatic
    fun createObjectMapper(): ObjectMapper {
        val mapper: ObjectMapper = jsonBuilder().build()

        return mapper
    }

    @JvmStatic
    fun jsonBuilder(): JsonMapper.Builder {
        return jsonBuilder(builder = JsonMapper.builder())
    }

    @JvmStatic
    fun jsonBuilder(builder: JsonMapper.Builder): JsonMapper.Builder {
        // BlackbirdModule is intentionally omitted: it generates accessors at runtime via
        // LambdaMetafactory, which GraalVM native-image cannot do — and native is why we're on Jackson.
        return builder.addModule(KotlinModule.Builder().build())
            .addModule(BigDecimalModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            // The builder form returns a NEW inclusion value that must be returned from the lambda;
            // the previous code discarded it, so inclusion stayed at the default (a no-op).
            .changeDefaultPropertyInclusion { incl ->
                incl.withValueInclusion(JsonInclude.Include.NON_EMPTY)
            }
    }
}
