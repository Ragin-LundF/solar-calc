package io.github.raginlundf.solarcalc.domain.services.input

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInput
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

    private fun ownedProfile(): EnergyProfile {
        return EnergyProfile().apply {
            id = 7L
            uuid = "p1"
        }
    }

    @Test
    fun `get rejects access when the profile is not owned by the caller`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = "p1", userUsername = "mallory")
        } returns null

        assertFailsWith<ResourceNotFoundException> {
            controller.get(profileUuid = "p1", inputId = 5L, username = "mallory")
        }
        verify(exactly = 0) { inputRepository.findByIdAndEnergyProfileId(any(), any()) }
    }

    @Test
    fun `get rejects an input that belongs to a different profile`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = "p1", userUsername = "alice")
        } returns ownedProfile()
        every { inputRepository.findByIdAndEnergyProfileId(id = 5L, energyProfileId = 7L) } returns null

        assertFailsWith<ResourceNotFoundException> {
            controller.get(profileUuid = "p1", inputId = 5L, username = "alice")
        }
    }

    @Test
    fun `get returns an input scoped to the caller's own profile`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = "p1", userUsername = "alice")
        } returns ownedProfile()
        every { inputRepository.findByIdAndEnergyProfileId(id = 5L, energyProfileId = 7L) } returns
            MonthlyEnergyInput().apply {
                id = 5L
                period = "2024-03"
            }

        val result = controller.get(profileUuid = "p1", inputId = 5L, username = "alice")

        assertEquals(expected = 5L, actual = result.id)
        assertEquals(expected = "2024-03", actual = result.period)
    }
}
