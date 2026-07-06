package io.github.raginlundf.solarcalc.restapi.serialization

import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class KotlinSerializationConfig : WebMvcConfigurer {

    @Bean
    fun json(): Json = Json { ignoreUnknownKeys = true }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val custom = KotlinSerializationJsonHttpMessageConverter(json())
        val idx = converters.indexOfFirst { it is KotlinSerializationJsonHttpMessageConverter }
        if (idx >= 0) converters[idx] = custom else converters.add(custom)
    }
}
