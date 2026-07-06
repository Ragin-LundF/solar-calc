package io.github.raginlundf.solarcalc.restapi.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Base64

@ConfigurationProperties(prefix = "solarcalc.security.jwt")
data class JwtProperties(
    val secretKey: String,
) {
    fun decodedSecret(): ByteArray = Base64.getDecoder().decode(secretKey)
}
