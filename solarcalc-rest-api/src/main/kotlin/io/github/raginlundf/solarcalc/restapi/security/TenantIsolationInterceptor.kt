package io.github.raginlundf.solarcalc.restapi.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping

@Component
class TenantIsolationInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        @Suppress("UNCHECKED_CAST")
        val vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>
            ?: return true

        val pathTenantId = vars["tenantId"]?.toLongOrNull() ?: return true

        val jwt = SecurityContextHolder.getContext().authentication?.credentials as? Jwt ?: return true

        val jwtTenantId = jwt.claims["tenantId"]?.toString()?.toLongOrNull()
        if (jwtTenantId == null || jwtTenantId != pathTenantId) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant access denied")
            return false
        }
        return true
    }
}
