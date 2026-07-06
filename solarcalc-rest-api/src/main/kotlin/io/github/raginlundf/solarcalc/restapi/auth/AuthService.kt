package io.github.raginlundf.solarcalc.restapi.auth

import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.domain.models.repository.UserRepository
import io.github.raginlundf.solarcalc.domain.models.tenant.Tenant
import io.github.raginlundf.solarcalc.domain.models.user.User
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val jwtEncoder: JwtEncoder,
    private val passwordEncoder: PasswordEncoder,
) {
    companion object {
        const val TOKEN_EXPIRY_SECONDS = 86_400L // 24 hours
    }

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw UsernameAlreadyExistsException("Username '${request.username}' is already taken")
        }

        val tenant = Tenant().apply {
            name = request.username
        }
        val savedTenant = tenantRepository.save(tenant)

        val user = User().apply {
            username = request.username
            passwordHash = passwordEncoder.encode(request.password)!!
            tenantId = savedTenant.id!!
        }
        userRepository.save(user)

        return AuthResponse(
            token = generateToken(user.username, savedTenant.id!!),
            username = user.username,
            tenantId = savedTenant.id!!,
            expiresInSeconds = TOKEN_EXPIRY_SECONDS,
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { InvalidCredentialsException("Invalid username or password") }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException("Invalid username or password")
        }

        return AuthResponse(
            token = generateToken(user.username, user.tenantId),
            username = user.username,
            tenantId = user.tenantId,
            expiresInSeconds = TOKEN_EXPIRY_SECONDS,
        )
    }

    private fun generateToken(username: String, tenantId: Long): String {
        val claims = JwtClaimsSet.builder()
            .subject(username)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(TOKEN_EXPIRY_SECONDS))
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
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}

class UsernameAlreadyExistsException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
