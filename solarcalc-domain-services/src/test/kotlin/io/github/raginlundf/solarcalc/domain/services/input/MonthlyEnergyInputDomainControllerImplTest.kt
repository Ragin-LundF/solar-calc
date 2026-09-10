package io.github.raginlundf.solarcalc.domain.services.input

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInputEntity
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MonthlyEnergyInputDomainControllerImplTest {

    private val profileRepository = mockk<EnergyProfileRepository>()
    private val inputRepository = mockk<MonthlyEnergyInputRepository>(relaxed = true)
    private val controller = MonthlyEnergyInputDomainControllerImpl(
        profileRepository = profileRepository,
        inputRepository = inputRepository,
    )

    private val profileUuid = UUID.randomUUID().toString()
    private val inputUuid = UUID.randomUUID().toString()

    private fun ownedProfile(): EnergyProfileEntity {
        return EnergyProfileEntity().apply {
            id = 7L
            uuid = profileUuid
        }
    }

    @Test
    fun `get rejects access when the profile is not owned by the caller`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = "mallory")
        } returns null

        assertFailsWith<ResourceNotFoundException> {
            controller.get(profileUuid = profileUuid, inputUuid = inputUuid, username = "mallory")
        }
        verify(exactly = 0) { inputRepository.findByUuidAndEnergyProfileId(any(), any()) }
    }

    @Test
    fun `get rejects an input that belongs to a different profile`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = "alice")
        } returns ownedProfile()
        every { inputRepository.findByUuidAndEnergyProfileId(uuid = inputUuid, energyProfileId = 7L) } returns null

        assertFailsWith<ResourceNotFoundException> {
            controller.get(profileUuid = profileUuid, inputUuid = inputUuid, username = "alice")
        }
    }

    @Test
    fun `get returns an input scoped to the caller's own profile`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = "alice")
        } returns ownedProfile()
        every { inputRepository.findByUuidAndEnergyProfileId(uuid = inputUuid, energyProfileId = 7L) } returns
            MonthlyEnergyInputEntity().apply {
                id = 5L
                uuid = inputUuid
                period = "2024-03"
            }

        val result = controller.get(profileUuid = profileUuid, inputUuid = inputUuid, username = "alice")

        assertEquals(expected = inputUuid, actual = result.id)
        assertEquals(expected = "2024-03", actual = result.period)
    }
}
