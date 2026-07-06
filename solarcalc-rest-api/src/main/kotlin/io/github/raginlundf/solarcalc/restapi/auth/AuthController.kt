package io.github.raginlundf.solarcalc.restapi.auth

import io.github.raginlundf.solarcalc.domain.services.auth.AuthDomainController
import io.github.raginlundf.solarcalc.dtos.auth.ErrorResponse
import io.github.raginlundf.solarcalc.dtos.auth.LoginRequest
import io.github.raginlundf.solarcalc.dtos.auth.RegisterRequest
import io.github.raginlundf.solarcalc.dtos.error.InvalidCredentialsException
import io.github.raginlundf.solarcalc.dtos.error.UsernameAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authDomainController: AuthDomainController,
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val response = authDomainController.register(request = request)
            ResponseEntity.status(HttpStatus.CREATED).body(response as Any)
        } catch (e: UsernameAlreadyExistsException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(error = e.message!!) as Any)
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            val response = authDomainController.login(request = request)
            ResponseEntity.ok(response as Any)
        } catch (e: InvalidCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(error = e.message!!) as Any)
        }
    }
}
