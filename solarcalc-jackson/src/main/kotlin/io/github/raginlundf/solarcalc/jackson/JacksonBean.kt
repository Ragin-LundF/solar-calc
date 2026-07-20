package io.github.raginlundf.solarcalc.jackson

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints
import tools.jackson.databind.json.JsonMapper

@Configuration
@ImportRuntimeHints(JacksonBindingNativeHints::class)
class JacksonBean {
    @Bean
    fun jsonMapper(): JsonMapper {
        return JacksonUtil.createObjectMapper() as JsonMapper
    }
}
