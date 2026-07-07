package io.github.raginlundf.solarcalc.domain.services.profile

import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.UserRepository
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.dtos.profile.UpdateEnergyProfileRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProfileDomainControllerImplTest {

    private val profileRepository = mockk<EnergyProfileRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val controller = ProfileDomainControllerImpl(
        profileRepository = profileRepository,
        userRepository = userRepository,
    )

    private fun profile(uuid: String): EnergyProfile {
        return EnergyProfile().apply {
            id = 1L
            this.uuid = uuid
            name = "profile"
        }
    }

    @Test
    fun `list returns only profiles owned by the caller`() {
        every { profileRepository.findAllByUserUsername(userUsername = "alice") } returns listOf(profile("u1"))

        val result = controller.list(username = "alice")

        assertEquals(expected = 1, actual = result.size)
        assertEquals(expected = "u1", actual = result.first().id)
        verify(exactly = 0) { profileRepository.findAll() }
    }

    @Test
    fun `get returns the profile when owned by the caller`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = "u1", userUsername = "alice")
        } returns profile("u1")

        val result = controller.get(profileUuid = "u1", username = "alice")

        assertEquals(expected = "u1", actual = result.id)
    }

    @Test
    fun `get rejects a profile not owned by the caller`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = "u1", userUsername = "mallory")
        } returns null

        assertFailsWith<ResourceNotFoundException> {
            controller.get(profileUuid = "u1", username = "mallory")
        }
    }

    @Test
    fun `update rejects a profile not owned by the caller`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = "u1", userUsername = "mallory")
        } returns null

        assertFailsWith<ResourceNotFoundException> {
            controller.update(profileUuid = "u1", request = mockk<UpdateEnergyProfileRequest>(), username = "mallory")
        }
        verify(exactly = 0) { profileRepository.save(any()) }
    }

    @Test
    fun `delete rejects a profile not owned by the caller`() {
        every {
            profileRepository.findByUuidAndUserUsername(uuid = "u1", userUsername = "mallory")
        } returns null

        assertFailsWith<ResourceNotFoundException> {
            controller.delete(profileUuid = "u1", username = "mallory")
        }
        verify(exactly = 0) { profileRepository.delete(any()) }
    }
}
