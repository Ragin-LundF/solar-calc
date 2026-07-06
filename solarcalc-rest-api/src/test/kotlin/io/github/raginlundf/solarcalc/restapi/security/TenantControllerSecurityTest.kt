package io.github.raginlundf.solarcalc.restapi.security

import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.restapi.tenant.TenantController
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@ExtendWith(SpringExtension::class)
@WebAppConfiguration
@ContextConfiguration(classes = [TenantControllerSecurityTest.TestConfig::class])
@TestPropertySource(properties = ["solarcalc.security.jwt.secret-key=dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW9ubHk="])
class TenantControllerSecurityTest {

    @Configuration
    @EnableWebMvc
    @EnableConfigurationProperties(JwtProperties::class)
    @Import(SecurityConfig::class, TenantController::class)
    open class TestConfig {
        @Bean
        open fun tenantRepository(): TenantRepository = mockk()
    }

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var tenantRepository: TenantRepository

    private lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `list tenants returns 401 without authentication`() {
        mvc.get(urlTemplate = "/api/tenants")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `list tenants returns 403 with wrong scope`() {
        mvc.get(urlTemplate = "/api/tenants") {
            with(processor = jwt().authorities(SimpleGrantedAuthority("SCOPE_${SolarcalcScopes.PROFILES_READ}")))
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `list tenants returns 200 with correct read scope`() {
        every { tenantRepository.findAll() } returns emptyList()

        mvc.get("/api/tenants") {
            with(processor = jwt().authorities(SimpleGrantedAuthority("SCOPE_${SolarcalcScopes.TENANTS_READ}")))
        }.andExpect {
            status { isOk() }
            content { json(jsonContent = "[]") }
        }
    }

    @Test
    fun `create tenant returns 403 with read scope only`() {
        mvc.post(urlTemplate = "/api/tenants") {
            with(processor = jwt().authorities(SimpleGrantedAuthority("SCOPE_${SolarcalcScopes.TENANTS_READ}")))
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"test"}"""
        }.andExpect { status { isForbidden() } }
    }
}
