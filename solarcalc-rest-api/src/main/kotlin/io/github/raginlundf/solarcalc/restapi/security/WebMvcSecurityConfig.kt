package io.github.raginlundf.solarcalc.restapi.security

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcSecurityConfig(
    private val tenantIsolationInterceptor: TenantIsolationInterceptor,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(tenantIsolationInterceptor)
            .addPathPatterns("/api/tenants/**")
    }
}
