package io.github.raginlundf.solarcalc.restapi.security

import io.github.raginlundf.logging.annotations.LogDuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import kotlinx.serialization.Serializable

@RestController
@RequestMapping("/api/v1/dev")
@Profile("local")
class DevTokenController(private val jwtEncoder: JwtEncoder) {

    @LogDuration
    @PostMapping("/token")
    fun issueToken(): DevTokenResponse {
        val claims = JwtClaimsSet.builder()
            .subject("dev-user@solarcalc.local")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(86_400))
            .claim(
                "scope",
                listOf(
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
        return DevTokenResponse(token = token, expiresInSeconds = 86_400)
    }
}

@Serializable
data class DevTokenResponse(val token: String, val expiresInSeconds: Long)
