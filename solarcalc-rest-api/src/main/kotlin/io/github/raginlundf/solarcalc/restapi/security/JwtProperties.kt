package io.github.raginlundf.solarcalc.restapi.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Base64

@ConfigurationProperties(prefix = "solarcalc.security.jwt")
data class JwtProperties(
    val secretKey: String,
    val issuer: String = "solarcalc",
    val audience: String = "solarcalc-api",
) {
    fun decodedSecret(): ByteArray = Base64.getDecoder().decode(secretKey)
}
