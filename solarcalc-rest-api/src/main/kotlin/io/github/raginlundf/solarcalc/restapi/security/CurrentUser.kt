package io.github.raginlundf.solarcalc.restapi.security

import org.springframework.security.core.context.SecurityContextHolder

/**
 * Resolves the authenticated username (JWT subject) from the security context.
 * Endpoints exposing this are secured as `authenticated()`, so a missing principal
 * indicates a misconfiguration rather than an anonymous request.
 */
object CurrentUser {

    fun requireUsername(): String {
        return SecurityContextHolder.getContext().authentication?.name
            ?: throw IllegalStateException("No authenticated user in security context")
    }
}
