package io.github.raginlundf.solarcalc.restapi.security

import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import kotlinx.serialization.Serializable

@RestController
@RequestMapping("/api/dev")
@Profile("local")
class DevTokenController(private val jwtEncoder: JwtEncoder) {

    @PostMapping("/token")
    fun issueToken(@RequestParam tenantId: Long): DevTokenResponse {
        val claims = JwtClaimsSet.builder()
            .subject("dev-user@solarcalc.local")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(86_400))
            .claim("tenantId", tenantId)
            .claim(
                "scope",
                listOf(
                    SolarcalcScopes.TENANTS_READ,
                    SolarcalcScopes.TENANTS_WRITE,
                    SolarcalcScopes.PROFILES_READ,
                    SolarcalcScopes.PROFILES_WRITE,
                    SolarcalcScopes.INPUTS_READ,
                    SolarcalcScopes.INPUTS_WRITE,
                    SolarcalcScopes.PRICES_READ,
                    SolarcalcScopes.PRICES_WRITE,
                    SolarcalcScopes.POLICIES_READ,
                    SolarcalcScopes.POLICIES_WRITE,
                    SolarcalcScopes.CALCULATIONS_READ,
                ),
            )
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        return DevTokenResponse(token = token, tenantId = tenantId, expiresInSeconds = 86_400)
    }
}

@Serializable
data class DevTokenResponse(val token: String, val tenantId: Long, val expiresInSeconds: Long)
