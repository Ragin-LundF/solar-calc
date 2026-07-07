package io.github.raginlundf.solarcalc.domain.services.auth

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JwtTokenIssuerTest {

    // 32-byte key: HS256 requires at least a 256-bit secret.
    private val secret = "0123456789abcdef0123456789abcdef".toByteArray()
    private val encoder = NimbusJwtEncoder(ImmutableJWKSet(JWKSet(OctetSequenceKey.Builder(secret).build())))
    private val decoder = NimbusJwtDecoder.withSecretKey(SecretKeySpec(secret, "HmacSHA256"))
        .macAlgorithm(MacAlgorithm.HS256)
        .build()

    private val issuer = JwtTokenIssuer(
        jwtEncoder = encoder,
        issuer = "solarcalc",
        audience = "solarcalc-api",
        validitySeconds = 3600,
    )

    @Test
    fun `issued token carries issuer audience subject scopes and reported expiry`() {
        val issued = issuer.issue(subject = "alice", scopes = SolarcalcScopes.FULL_ACCESS)

        val jwt = decoder.decode(issued.token)

        assertEquals(expected = "alice", actual = jwt.subject)
        assertEquals(expected = "solarcalc", actual = jwt.getClaimAsString("iss"))
        assertTrue(
            jwt.audience?.contains("solarcalc-api") == true,
            message = "audience must contain the API audience",
        )
        assertEquals(
            expected = listOf(SolarcalcScopes.READ, SolarcalcScopes.WRITE),
            actual = jwt.getClaim("scope"),
        )
        assertEquals(expected = 3600L, actual = issued.expiresInSeconds)
    }
}
