package io.github.raginlundf.solarcalc.domain.models

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationCategoryEnum
import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicyEntity
import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInputEntity
import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshotEntity
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.domain.models.repository.UserRepository
import io.github.raginlundf.solarcalc.domain.models.user.UserEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mariadb.MariaDBContainer
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RepositoryTestConfig::class])
@Testcontainers
@Transactional
class RepositoryIntegrationTest {
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var profileRepository: EnergyProfileRepository
    @Autowired lateinit var inputRepository: MonthlyEnergyInputRepository
    @Autowired lateinit var policyRepository: AllocationPolicyRepository
    @Autowired lateinit var priceRepository: PriceSnapshotRepository

    @PersistenceContext lateinit var entityManager: EntityManager

    private fun profileFor(username: String): EnergyProfileEntity {
        val user = userRepository.save(UserEntity().apply { this.username = username })
        return profileRepository.save(EnergyProfileEntity().apply {
            name = "Profile"
            this.user = user
        })
    }

    @Test
    fun `stores a price snapshot against the migrated valid_from column`() {
        // ddl-auto is none, so nothing validates the entity against the schema. Actually writing
        // and reading the row is what proves migration 0015 renamed the column as the entity expects.
        val profile = profileFor(username = "price-owner")

        priceRepository.save(PriceSnapshotEntity().apply {
            energyProfile = profile
            validFrom = "2025-01"
            electricityPrice = BigDecimal("0.280000")
        })
        entityManager.flush()
        entityManager.clear()

        val stored = priceRepository.findAllByEnergyProfileIdOrderByValidFromDesc(profile.id!!)
        assertEquals(expected = 1, actual = stored.size)
        assertEquals(expected = "2025-01", actual = stored.first().validFrom)
    }

    @Test
    fun `returns the price timeline newest first`() {
        val profile = profileFor(username = "timeline-owner")
        listOf("2024-01", "2026-01", "2025-01").forEach { start ->
            priceRepository.save(PriceSnapshotEntity().apply {
                energyProfile = profile
                validFrom = start
                electricityPrice = BigDecimal("0.300000")
            })
        }
        entityManager.flush()
        entityManager.clear()

        val stored = priceRepository.findAllByEnergyProfileIdOrderByValidFromDesc(profile.id!!)

        assertEquals(expected = listOf("2026-01", "2025-01", "2024-01"), actual = stored.map { it.validFrom })
    }

    @Test
    fun `rejects two price snapshots starting in the same month`() {
        val profile = profileFor(username = "duplicate-owner")
        priceRepository.save(PriceSnapshotEntity().apply {
            energyProfile = profile
            validFrom = "2025-01"
            electricityPrice = BigDecimal("0.300000")
        })
        entityManager.flush()

        // The unique constraint added by migration 0015; without it the resolver's single-result
        // lookup would blow up at read time instead.
        assertFailsWith<DataIntegrityViolationException> {
            priceRepository.save(PriceSnapshotEntity().apply {
                energyProfile = profile
                validFrom = "2025-01"
                electricityPrice = BigDecimal("0.310000")
            })
            entityManager.flush()
        }
    }

    @Test
    fun `lazy many-to-one loads without a runtime proxy`() {
        val user = userRepository.save(UserEntity().apply { username = "owner" })
        val profile = profileRepository.save(EnergyProfileEntity().apply {
            name = "Profile"
            this.user = user
        })

        // Detach everything so the reloaded profile's `user` is a fresh lazy association. With
        // hibernate.bytecode.provider=none (see RepositoryTestConfig) this dereference throws the
        // "Generation of HibernateProxy instances at runtime is not allowed" error unless the
        // entities are build-time enhanced.
        entityManager.flush()
        entityManager.clear()

        val reloaded = profileRepository.findById(profile.id!!).orElseThrow()
        assertEquals(expected = "owner", actual = reloaded.user?.username)
    }

    @Test
    fun `user can be persisted and retrieved`() {
        val user = UserEntity().apply {
            username = "test-user"
        }

        val saved = userRepository.save(user)
        val found = userRepository.findById(saved.id!!).orElseThrow()

        assertEquals(expected = "test-user", actual = found.username)
    }

    @Test
    fun `monthly input is unique by profile and period`() {
        val user = userRepository.save(UserEntity().apply { username = "user" })
        val profile = profileRepository.save(EnergyProfileEntity().apply {
            name = "Profile"
            this.user = user
        })

        inputRepository.save(MonthlyEnergyInputEntity().apply {
            energyProfile = profile
            period = "2024-06"
            consumptionKwh = BigDecimal("500")
            generationKwh = BigDecimal("300")
        })

        val found = inputRepository.findByEnergyProfileIdAndPeriod(
            energyProfileId = profile.id!!,
            period = "2024-06",
        )

        assertNotNull(actual = found)
        assertEquals(expected = "2024-06", actual = found.period)
    }

    @Test
    fun `cross-profile input lookup returns null`() {
        val user = userRepository.save(UserEntity().apply { username = "u" })
        val profileA = profileRepository.save(EnergyProfileEntity().apply { name = "A"; this.user = user })
        val profileB = profileRepository.save(EnergyProfileEntity().apply { name = "B"; this.user = user })
        inputRepository.save(MonthlyEnergyInputEntity().apply {
            energyProfile = profileA
            period = "2024-07"
            consumptionKwh = BigDecimal("400")
            generationKwh = BigDecimal("200")
        })

        val result = inputRepository.findByEnergyProfileIdAndPeriod(
            energyProfileId = profileB.id!!,
            period = "2024-07"
        )
        assertNull(actual = result)
    }

    @Test
    fun `allocation policy priority order round-trips through converter`() {
        val user = userRepository.save(UserEntity().apply { username = "u" })
        val profile = profileRepository.save(EnergyProfileEntity().apply {
            name = "P"
            this.user = user
            hasWallbox = true
            hasHeatPump = true
        })

        policyRepository.save(AllocationPolicyEntity().apply {
            energyProfile = profile
            name = "Wallbox first"
            priorityOrder = listOf(
                AllocationCategoryEnum.WALLBOX,
                AllocationCategoryEnum.HEAT_PUMP,
                AllocationCategoryEnum.HOUSEHOLD,
            )
        })

        val found = policyRepository.findAllByEnergyProfileId(profile.id!!).single()
        assertEquals(
            expected = listOf(
                AllocationCategoryEnum.WALLBOX,
                AllocationCategoryEnum.HEAT_PUMP,
                AllocationCategoryEnum.HOUSEHOLD,
            ),
            actual = found.priorityOrder,
        )
    }

    companion object {
        @Container
        @JvmStatic
        val mariadb = MariaDBContainer("mariadb:11").apply {
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
}
