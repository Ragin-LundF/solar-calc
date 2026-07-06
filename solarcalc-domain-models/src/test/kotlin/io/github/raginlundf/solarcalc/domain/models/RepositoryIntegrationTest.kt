package io.github.raginlundf.solarcalc.domain.models

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategory
import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy
import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.domain.models.tenant.Tenant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
class RepositoryIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val mariadb = MariaDBContainer<Nothing>("mariadb:11").apply {
            withDatabaseName("solarcalc_test")
            withUsername("test")
            withPassword("test")
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mariadb::getJdbcUrl)
            registry.add("spring.datasource.username", mariadb::getUsername)
            registry.add("spring.datasource.password", mariadb::getPassword)
            registry.add("spring.datasource.driver-class-name") { "org.mariadb.jdbc.Driver" }
            registry.add("spring.liquibase.change-log") { "classpath:db/db.changelog-master.xml" }
        }
    }

    @Autowired lateinit var tenantRepository: TenantRepository
    @Autowired lateinit var profileRepository: EnergyProfileRepository
    @Autowired lateinit var inputRepository: MonthlyEnergyInputRepository
    @Autowired lateinit var policyRepository: AllocationPolicyRepository

    @Test
    fun `tenant can be persisted and retrieved`() {
        val tenant = Tenant().apply {
            name = "Test Tenant"
            hasWallbox = true
            hasHeatPump = false
        }

        val saved = tenantRepository.save(tenant)
        val found = tenantRepository.findById(saved.id!!).orElseThrow()

        assertEquals("Test Tenant", found.name)
        assertEquals(true, found.hasWallbox)
    }

    @Test
    fun `energy profile is scoped to tenant`() {
        val tenantA = tenantRepository.save(Tenant().apply { name = "Tenant A" })
        val tenantB = tenantRepository.save(Tenant().apply { name = "Tenant B" })

        val profileA = profileRepository.save(EnergyProfile().apply {
            tenant = tenantA
            name = "Profile A"
        })
        profileRepository.save(EnergyProfile().apply {
            tenant = tenantB
            name = "Profile B"
        })

        val results = profileRepository.findAllByTenantId(tenantA.id!!)
        assertEquals(1, results.size)
        assertEquals(profileA.id, results.first().id)
    }

    @Test
    fun `monthly input is unique by tenant, profile, period`() {
        val tenant = tenantRepository.save(Tenant().apply { name = "Tenant" })
        val profile = profileRepository.save(EnergyProfile().apply {
            this.tenant = tenant
            name = "Profile"
        })

        inputRepository.save(MonthlyEnergyInput().apply {
            this.tenant = tenant
            energyProfile = profile
            period = "2024-06"
            consumptionKwh = BigDecimal("500")
            generationKwh = BigDecimal("300")
        })

        val found = inputRepository.findByTenantIdAndEnergyProfileIdAndPeriod(
            tenantId = tenant.id!!,
            energyProfileId = profile.id!!,
            period = "2024-06",
        )

        assertNotNull(found)
        assertEquals("2024-06", found.period)
    }

    @Test
    fun `cross-tenant input lookup returns null`() {
        val tenantA = tenantRepository.save(Tenant().apply { name = "A" })
        val tenantB = tenantRepository.save(Tenant().apply { name = "B" })
        val profile = profileRepository.save(EnergyProfile().apply {
            tenant = tenantA
            name = "Profile"
        })
        val input = inputRepository.save(MonthlyEnergyInput().apply {
            this.tenant = tenantA
            energyProfile = profile
            period = "2024-07"
            consumptionKwh = BigDecimal("400")
            generationKwh = BigDecimal("200")
        })

        val result = inputRepository.findByIdAndTenantId(id = input.id!!, tenantId = tenantB.id!!)
        assertNull(result)
    }

    @Test
    fun `allocation policy priority order round-trips through converter`() {
        val tenant = tenantRepository.save(Tenant().apply { name = "T"; hasWallbox = true; hasHeatPump = true })
        val profile = profileRepository.save(EnergyProfile().apply { this.tenant = tenant; name = "P" })

        policyRepository.save(AllocationPolicy().apply {
            this.tenant = tenant
            energyProfile = profile
            name = "Wallbox first"
            priorityOrder = listOf(AllocationCategory.WALLBOX, AllocationCategory.HEAT_PUMP, AllocationCategory.HOUSEHOLD)
            isDefault = true
        })

        val found = policyRepository.findByTenantIdAndEnergyProfileIdAndIsDefaultTrue(tenant.id!!, profile.id!!)
        assertNotNull(found)
        assertEquals(
            listOf(AllocationCategory.WALLBOX, AllocationCategory.HEAT_PUMP, AllocationCategory.HOUSEHOLD),
            found.priorityOrder,
        )
    }
}
