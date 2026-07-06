package io.github.raginlundf.solarcalc.restapi.auth

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val response = authService.register(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: UsernameAlreadyExistsException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(e.message!!))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(response)
        } catch (e: InvalidCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(e.message!!))
        }
    }
}
