package io.github.raginlundf.solarcalc.domain.services.auth

import io.github.raginlundf.solarcalc.domain.models.repository.UserRepository
import io.github.raginlundf.solarcalc.domain.models.user.UserEntity
import io.github.raginlundf.solarcalc.dtos.auth.AuthResponseDto
import io.github.raginlundf.solarcalc.dtos.auth.LoginRequestDto
import io.github.raginlundf.solarcalc.dtos.auth.RegisterRequestDto
import io.github.raginlundf.solarcalc.dtos.auth.UpdateLastProfileRequestDto
import io.github.raginlundf.solarcalc.dtos.auth.UpdateSetupStepRequestDto
import io.github.raginlundf.solarcalc.dtos.error.InvalidCredentialsException
import io.github.raginlundf.solarcalc.dtos.error.UsernameAlreadyExistsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthDomainControllerImpl(
    private val userRepository: UserRepository,
    private val tokenIssuer: JwtTokenIssuer,
    private val passwordEncoder: PasswordEncoder,
) : AuthDomainController {

    @Transactional
    override fun register(request: RegisterRequestDto): AuthResponseDto {
        if (userRepository.existsByUsername(request.username)) {
            throw UsernameAlreadyExistsException(message = "Username '${request.username}' is already taken")
        }

        val user = UserEntity().apply {
            username = request.username
            passwordHash = passwordEncoder.encode(request.password)!!
        }
        userRepository.save(user)

        return user.toAuthResponse()
    }

    override fun login(request: LoginRequestDto): AuthResponseDto {
        val user = userRepository.findByUsername(username = request.username)
            .orElseThrow { InvalidCredentialsException(message = "Invalid username or password") }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw InvalidCredentialsException(message = "Invalid username or password")
        }

        return user.toAuthResponse()
    }

    @Transactional
    override fun updateLastProfile(username: String, request: UpdateLastProfileRequestDto) {
        val user = userRepository.findByUsername(username)
            .orElseThrow { InvalidCredentialsException(message = "UserEntity not found") }
        user.lastProfileUuid = request.profileUuid
        userRepository.save(user)
    }

    @Transactional
    override fun updateSetupStep(username: String, request: UpdateSetupStepRequestDto): Int {
        val user = userRepository.findByUsername(username = username)
            .orElseThrow { InvalidCredentialsException(message = "UserEntity not found") }
        user.setupStep = request.setupStep
        userRepository.save(user)
        return user.setupStep
    }

    private fun UserEntity.toAuthResponse(): AuthResponseDto {
        // Every user currently receives full access; read-only users get only READ later.
        val issued = tokenIssuer.issue(subject = username, scopes = SolarcalcScopes.FULL_ACCESS)
        return AuthResponseDto(
            token = issued.token,
            username = username,
            expiresInSeconds = issued.expiresInSeconds,
            setupStep = setupStep,
            lastProfileUuid = lastProfileUuid,
        )
    }
}
