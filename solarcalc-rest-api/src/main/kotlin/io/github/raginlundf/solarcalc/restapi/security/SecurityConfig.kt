package io.github.raginlundf.solarcalc.restapi.security

import com.nimbusds.jose.jwk.OctetSequenceKey
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtProperties: JwtProperties,
    private val environment: Environment,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Only the anonymous entry points are open; everything else under /api requires a token.
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                if (environment.acceptsProfiles(org.springframework.core.env.Profiles.of("local"))) {
                    // Dev token endpoint exists only in the local profile; open it only there.
                    auth.requestMatchers("/api/v1/dev/**").permitAll()
                }
                auth.requestMatchers("/api/**").authenticated()
                auth.anyRequest().permitAll()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt -> jwt.decoder(jwtDecoder()) }
            }
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val secretKey = SecretKeySpec(jwtProperties.decodedSecret(), "HmacSHA256")
        val decoder = NimbusJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
        decoder.setJwtValidator(tokenValidator())
        return decoder
    }

    private fun tokenValidator(): OAuth2TokenValidator<Jwt> {
        val audienceValidator = JwtClaimValidator<List<String>>(JwtClaimNames.AUD) { audience ->
            audience != null && audience.contains(jwtProperties.audience)
        }
        return DelegatingOAuth2TokenValidator(
            JwtValidators.createDefaultWithIssuer(jwtProperties.issuer),
            audienceValidator,
        )
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val key = OctetSequenceKey.Builder(jwtProperties.decodedSecret()).build()
        return NimbusJwtEncoder(com.nimbusds.jose.jwk.source.ImmutableJWKSet(com.nimbusds.jose.jwk.JWKSet(key)))
    }
}
