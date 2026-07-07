package io.github.raginlundf.solarcalc.domain.services.auth

import io.github.raginlundf.solarcalc.domain.models.repository.UserRepository
import io.github.raginlundf.solarcalc.domain.models.user.User
import io.github.raginlundf.solarcalc.dtos.auth.AuthResponse
import io.github.raginlundf.solarcalc.dtos.auth.LoginRequest
import io.github.raginlundf.solarcalc.dtos.auth.RegisterRequest
import io.github.raginlundf.solarcalc.dtos.auth.UpdateSetupStepRequest
import io.github.raginlundf.solarcalc.dtos.error.InvalidCredentialsException
import io.github.raginlundf.solarcalc.dtos.error.UsernameAlreadyExistsException
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
class AuthDomainControllerImpl(
    private val userRepository: UserRepository,
    private val jwtEncoder: JwtEncoder,
    private val passwordEncoder: PasswordEncoder,
) : AuthDomainController {

    companion object {
        private const val TOKEN_EXPIRY_SECONDS = 86_400L
    }

    @Transactional
    override fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username)) {
            throw UsernameAlreadyExistsException(message = "Username '${request.username}' is already taken")
        }

        val user = User().apply {
            username = request.username
            passwordHash = passwordEncoder.encode(request.password)!!
        }
        userRepository.save(user)

        return AuthResponse(
            token = generateToken(username = user.username),
            username = user.username,
            expiresInSeconds = TOKEN_EXPIRY_SECONDS,
            setupStep = user.setupStep,
        )
    }

    override fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(username = request.username)
            .orElseThrow { InvalidCredentialsException(message = "Invalid username or password") }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException(message = "Invalid username or password")
        }

        return AuthResponse(
            token = generateToken(username = user.username),
            username = user.username,
            expiresInSeconds = TOKEN_EXPIRY_SECONDS,
            setupStep = user.setupStep,
        )
    }

    @Transactional
    override fun updateSetupStep(username: String, request: UpdateSetupStepRequest): Int {
        val user = userRepository.findByUsername(username = username)
            .orElseThrow { InvalidCredentialsException(message = "User not found") }
        user.setupStep = request.setupStep
        userRepository.save(user)
        return user.setupStep
    }

    private fun generateToken(username: String): String {
        val claims = JwtClaimsSet.builder()
            .subject(username)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(TOKEN_EXPIRY_SECONDS))
            .claim(
                "scope",
                listOf(
                    "io.github.raginlundf.solarcalc.profiles.read",
                    "io.github.raginlundf.solarcalc.profiles.write",
                    "io.github.raginlundf.solarcalc.inputs.read",
                    "io.github.raginlundf.solarcalc.inputs.write",
                    "io.github.raginlundf.solarcalc.prices.read",
                    "io.github.raginlundf.solarcalc.prices.write",
                    "io.github.raginlundf.solarcalc.policies.read",
                    "io.github.raginlundf.solarcalc.policies.write",
                    "io.github.raginlundf.solarcalc.calculations.read",
                ),
            )
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}
