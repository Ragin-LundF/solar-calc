package io.github.raginlundf.solarcalc.domain.services.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Single place that mints signed access tokens. Sets issuer/audience/expiry so the
 * resource server can validate them (see the decoder in the REST module), and stamps
 * the granted scopes. Centralising this keeps token claims consistent between the
 * real login flow and the local dev token endpoint.
 */
@Component
class JwtTokenIssuer(
    private val jwtEncoder: JwtEncoder,
    @Value("\${solarcalc.security.jwt.issuer:solarcalc}") private val issuer: String,
    @Value("\${solarcalc.security.jwt.audience:solarcalc-api}") private val audience: String,
    @Value("\${solarcalc.security.jwt.access-token-validity-seconds:28800}") private val validitySeconds: Long,
) {

    data class IssuedToken(val token: String, val expiresInSeconds: Long)

    fun issue(subject: String, scopes: List<String>): IssuedToken {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .audience(listOf(audience))
            .subject(subject)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(validitySeconds))
            .claim("scope", scopes)
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        return IssuedToken(token = token, expiresInSeconds = validitySeconds)
    }
}
