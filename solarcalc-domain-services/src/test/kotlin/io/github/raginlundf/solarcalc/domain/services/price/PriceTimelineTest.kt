package io.github.raginlundf.solarcalc.domain.services.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshotEntity
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PriceTimelineTest {

    private fun profile(
        heatingReferenceType: HeatingReferenceTypeEnum = HeatingReferenceTypeEnum.OIL,
        electricity: String? = "0.30",
        feedIn: String? = "0.08",
        petrol: String? = "1.80",
        oil: String? = "2400",
        gas: String? = "2000",
    ): EnergyProfileEntity {
        return EnergyProfileEntity().apply {
            this.heatingReferenceType = heatingReferenceType
            defaultElectricityPrice = electricity?.let { BigDecimal(it) }
            defaultFeedInTariff = feedIn?.let { BigDecimal(it) }
            defaultPetrolPrice = petrol?.let { BigDecimal(it) }
            defaultOilReferenceCost = oil?.let { BigDecimal(it) }
            defaultGasReferenceCost = gas?.let { BigDecimal(it) }
        }
    }

    private fun entry(
        validFrom: String,
        electricity: String? = null,
        feedIn: String? = null,
        petrol: String? = null,
        oil: String? = null,
        gas: String? = null,
    ): PriceSnapshotEntity {
        return PriceSnapshotEntity().apply {
            this.validFrom = validFrom
            electricityPrice = electricity?.let { BigDecimal(it) }
            feedInTariff = feedIn?.let { BigDecimal(it) }
            petrolPrice = petrol?.let { BigDecimal(it) }
            oilReferenceCost = oil?.let { BigDecimal(it) }
            gasReferenceCost = gas?.let { BigDecimal(it) }
        }
    }

    @Test
    fun `falls back to the profile defaults when the timeline is empty`() {
        val prices = PriceTimeline(profile = profile(), snapshots = emptyList()).at("2025-06")

        assertEquals(expected = BigDecimal("0.30"), actual = prices.electricityPrice)
        assertEquals(expected = BigDecimal("0.08"), actual = prices.feedInTariff)
        assertEquals(expected = BigDecimal("1.80"), actual = prices.petrolPrice)
        assertEquals(expected = BigDecimal("2400"), actual = prices.heatingReferenceCost)
    }

    @Test
    fun `applies an entry from its start month onwards`() {
        val timeline = PriceTimeline(
            profile = profile(),
            snapshots = listOf(entry(validFrom = "2025-01", electricity = "0.28")),
        )

        assertEquals(expected = BigDecimal("0.28"), actual = timeline.at("2025-01").electricityPrice)
        assertEquals(expected = BigDecimal("0.28"), actual = timeline.at("2025-12").electricityPrice)
    }

    @Test
    fun `leaves months before the first entry on the profile defaults`() {
        val timeline = PriceTimeline(
            profile = profile(),
            snapshots = listOf(entry(validFrom = "2025-01", electricity = "0.28")),
        )

        assertEquals(expected = BigDecimal("0.30"), actual = timeline.at("2024-12").electricityPrice)
    }

    @Test
    fun `uses the newest entry that is already in effect`() {
        val timeline = PriceTimeline(
            profile = profile(),
            snapshots = listOf(
                entry(validFrom = "2024-01", electricity = "0.32"),
                entry(validFrom = "2025-01", electricity = "0.28"),
            ),
        )

        assertEquals(expected = BigDecimal("0.32"), actual = timeline.at("2024-07").electricityPrice)
        assertEquals(expected = BigDecimal("0.28"), actual = timeline.at("2025-07").electricityPrice)
    }

    @Test
    fun `resolves each price independently of the others`() {
        // The 2025 entry records only a new petrol price, so electricity must keep the 2024 value
        // rather than falling back to the profile default.
        val timeline = PriceTimeline(
            profile = profile(),
            snapshots = listOf(
                entry(validFrom = "2024-01", electricity = "0.32", petrol = "1.72"),
                entry(validFrom = "2025-01", petrol = "1.90"),
            ),
        )

        val prices = timeline.at("2025-07")
        assertEquals(expected = BigDecimal("1.90"), actual = prices.petrolPrice)
        assertEquals(expected = BigDecimal("0.32"), actual = prices.electricityPrice)
    }

    @Test
    fun `ignores an entry that carries no value for the price being resolved`() {
        val timeline = PriceTimeline(
            profile = profile(),
            snapshots = listOf(
                entry(validFrom = "2024-01", electricity = "0.32"),
                entry(validFrom = "2025-01"),
            ),
        )

        assertEquals(expected = BigDecimal("0.32"), actual = timeline.at("2025-07").electricityPrice)
    }

    @Test
    fun `does not depend on the order the snapshots arrive in`() {
        val timeline = PriceTimeline(
            profile = profile(),
            snapshots = listOf(
                entry(validFrom = "2025-01", electricity = "0.28"),
                entry(validFrom = "2023-01", electricity = "0.35"),
                entry(validFrom = "2024-01", electricity = "0.32"),
            ),
        )

        assertEquals(expected = BigDecimal("0.35"), actual = timeline.at("2023-06").electricityPrice)
        assertEquals(expected = BigDecimal("0.32"), actual = timeline.at("2024-06").electricityPrice)
        assertEquals(expected = BigDecimal("0.28"), actual = timeline.at("2025-06").electricityPrice)
    }

    @Test
    fun `reads the heating reference cost matching the profile heating type`() {
        val entries = listOf(entry(validFrom = "2024-01", oil = "2600", gas = "1800"))

        val asOil = PriceTimeline(
            profile = profile(heatingReferenceType = HeatingReferenceTypeEnum.OIL),
            snapshots = entries,
        )
        val asGas = PriceTimeline(
            profile = profile(heatingReferenceType = HeatingReferenceTypeEnum.GAS),
            snapshots = entries,
        )

        assertEquals(expected = BigDecimal("2600"), actual = asOil.at("2024-06").heatingReferenceCost)
        assertEquals(expected = BigDecimal("1800"), actual = asGas.at("2024-06").heatingReferenceCost)
    }

    @Test
    fun `reports no heating reference cost when the profile has no heating reference`() {
        val timeline = PriceTimeline(
            profile = profile(heatingReferenceType = HeatingReferenceTypeEnum.NONE),
            snapshots = listOf(entry(validFrom = "2024-01", oil = "2600")),
        )

        assertNull(actual = timeline.at("2024-06").heatingReferenceCost)
    }

    @Test
    fun `returns null when neither the timeline nor the profile has a price`() {
        val timeline = PriceTimeline(
            profile = profile(electricity = null),
            snapshots = emptyList(),
        )

        assertNull(actual = timeline.at("2025-06").electricityPrice)
    }
}
