package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshotEntity
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.dtos.error.DuplicateInputException
import io.github.raginlundf.solarcalc.dtos.error.ValidationException
import io.github.raginlundf.solarcalc.dtos.price.UpsertPriceSnapshotRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PriceSnapshotDomainControllerImplTest {

    private val profileRepository = mockk<EnergyProfileRepository>(relaxed = true)
    private val priceRepository = mockk<PriceSnapshotRepository>(relaxed = true)

    private val controller = PriceSnapshotDomainControllerImpl(
        profileRepository = profileRepository,
        priceRepository = priceRepository,
        priceResolver = PriceResolver(priceSnapshotRepository = priceRepository),
    )

    private val profile = EnergyProfileEntity().apply {
        id = 1L
        uuid = "p1"
        heatingReferenceType = HeatingReferenceTypeEnum.OIL
        defaultElectricityPrice = BigDecimal("0.30")
        defaultOilReferenceCost = BigDecimal("2400")
    }

    private fun request(validFrom: String = "2025-01", electricity: String? = "0.28") =
        UpsertPriceSnapshotRequest(
            validFrom = validFrom,
            electricityPrice = electricity?.let { BigDecimal(it) },
        )

    private fun existingEntry(id: Long, validFrom: String) = PriceSnapshotEntity().apply {
        this.id = id
        this.validFrom = validFrom
        electricityPrice = BigDecimal("0.32")
    }

    private fun profileFound() {
        every { profileRepository.findByUuidAndUserUsername(uuid = "p1", userUsername = "alice") } returns profile
    }

    @Test
    fun `rejects a second entry starting in the same month`() {
        profileFound()
        every {
            priceRepository.findByEnergyProfileIdAndValidFrom(energyProfileId = 1L, validFrom = "2025-01")
        } returns existingEntry(id = 7L, validFrom = "2025-01")

        assertFailsWith<DuplicateInputException> {
            controller.create(profileUuid = "p1", request = request(), username = "alice")
        }
        verify(exactly = 0) { priceRepository.save(any()) }
    }

    @Test
    fun `rejects an entry that sets no price at all`() {
        profileFound()

        // Such a row resolves to nothing yet still occupies its start month.
        assertFailsWith<ValidationException> {
            controller.create(
                profileUuid = "p1",
                request = UpsertPriceSnapshotRequest(validFrom = "2025-01"),
                username = "alice",
            )
        }
        verify(exactly = 0) { priceRepository.save(any()) }
    }

    @Test
    fun `rejects moving an entry onto a month another entry already starts`() {
        profileFound()
        every {
            priceRepository.findByIdAndEnergyProfileId(id = 5L, energyProfileId = 1L)
        } returns existingEntry(id = 5L, validFrom = "2024-01")
        every {
            priceRepository.findByEnergyProfileIdAndValidFrom(energyProfileId = 1L, validFrom = "2025-01")
        } returns existingEntry(id = 9L, validFrom = "2025-01")

        assertFailsWith<DuplicateInputException> {
            controller.update(profileUuid = "p1", priceId = 5L, request = request(), username = "alice")
        }
    }

    @Test
    fun `allows an entry to keep its own start month while being edited`() {
        profileFound()
        val entry = existingEntry(id = 5L, validFrom = "2025-01")
        every { priceRepository.findByIdAndEnergyProfileId(id = 5L, energyProfileId = 1L) } returns entry
        every {
            priceRepository.findByEnergyProfileIdAndValidFrom(energyProfileId = 1L, validFrom = "2025-01")
        } returns entry
        val saved = slot<PriceSnapshotEntity>()
        every { priceRepository.save(capture(saved)) } answers { saved.captured }

        val response = controller.update(
            profileUuid = "p1",
            priceId = 5L,
            request = request(validFrom = "2025-01", electricity = "0.26"),
            username = "alice",
        )

        assertEquals(expected = "2025-01", actual = response.validFrom)
        assertEquals(expected = BigDecimal("0.26"), actual = response.electricityPrice)
    }

    @Test
    fun `reports the prices in effect for a month`() {
        profileFound()
        every { priceRepository.findAllByEnergyProfileIdOrderByValidFromDesc(energyProfileId = 1L) } returns listOf(
            existingEntry(id = 1L, validFrom = "2024-01"),
        )

        val effective = controller.effectivePrices(profileUuid = "p1", period = "2024-06", username = "alice")

        assertEquals(expected = "2024-06", actual = effective.period)
        assertEquals(expected = BigDecimal("0.32"), actual = effective.electricityPrice)
        // Nothing in the timeline carries it, so the profile default stands.
        assertEquals(expected = BigDecimal("2400"), actual = effective.heatingReferenceCost)
    }
}
