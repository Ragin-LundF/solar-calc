package io.github.raginlundf.solarcalc.restapi.security

import io.github.raginlundf.logging.annotations.LogDuration
import io.github.raginlundf.solarcalc.domain.services.auth.JwtTokenIssuer
import io.github.raginlundf.solarcalc.domain.services.auth.SolarcalcScopes
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlinx.serialization.Serializable

@RestController
@RequestMapping("/api/v1/dev")
@Profile("local")
class DevTokenController(private val tokenIssuer: JwtTokenIssuer) {

    @LogDuration
    @PostMapping("/token")
    fun issueToken(): DevTokenResponse {
        val issued = tokenIssuer.issue(
            subject = "dev-user@solarcalc.local",
            scopes = SolarcalcScopes.FULL_ACCESS,
        )
        return DevTokenResponse(token = issued.token, expiresInSeconds = issued.expiresInSeconds)
    }
}

@Serializable
data class DevTokenResponse(val token: String, val expiresInSeconds: Long)
