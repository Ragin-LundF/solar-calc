package io.github.raginlundf.solarcalc.domain.services.summary

import io.github.raginlundf.solarcalc.domain.models.input.MonthlyEnergyInputEntity
import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshotEntity
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyEfficiencyClassEnum
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfileEntity
import io.github.raginlundf.solarcalc.domain.models.profile.HeatingReferenceTypeEnum
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.MonthlyEnergyInputRepository
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.domain.services.price.PriceResolver
import io.github.raginlundf.solarcalc.dtos.summary.EnergyEfficiencyRating
import io.github.raginlundf.solarcalc.dtos.summary.MonthlySummary
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Covers how the price timeline, the per-month overrides and the profile defaults combine. */
class SummaryDomainControllerImplTest {

    private companion object {
        val FIXED_CLOCK: Clock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC)
    }

    private val profileRepository = mockk<EnergyProfileRepository>(relaxed = true)
    private val inputRepository = mockk<MonthlyEnergyInputRepository>(relaxed = true)
    private val policyRepository = mockk<AllocationPolicyRepository>(relaxed = true)
    private val priceRepository = mockk<PriceSnapshotRepository>(relaxed = true)

    private val controller = SummaryDomainControllerImpl(
        profileRepository = profileRepository,
        inputRepository = inputRepository,
        policyRepository = policyRepository,
        // A real resolver over a mocked repository, so the actual resolution chain is exercised.
        priceResolver = PriceResolver(priceSnapshotRepository = priceRepository),
        summaryService = SummaryServiceImpl(),
        // Fixed just after the readings below, so the rated window is deterministic.
        efficiencyCalculator = EnergyEfficiencyCalculator(clock = FIXED_CLOCK),
    )

    private val profile = EnergyProfileEntity().apply {
        id = 1L
        uuid = "p1"
        heatingReferenceType = HeatingReferenceTypeEnum.NONE
        defaultElectricityPrice = BigDecimal("0.30")
        defaultFeedInTariff = BigDecimal("0.08")
    }

    /** No solar at all, so the whole household demand is bought from the grid. */
    private fun month(period: String, electricityPriceOverride: String? = null): MonthlyEnergyInputEntity {
        return MonthlyEnergyInputEntity().apply {
            this.period = period
            consumptionKwh = BigDecimal("100")
            householdConsumptionKwh = BigDecimal("100")
            generationKwh = BigDecimal.ZERO
            feedInKwh = BigDecimal.ZERO
            this.electricityPriceOverride = electricityPriceOverride?.let { BigDecimal(it) }
        }
    }

    private fun entry(validFrom: String, electricity: String): PriceSnapshotEntity {
        return PriceSnapshotEntity().apply {
            this.validFrom = validFrom
            electricityPrice = BigDecimal(electricity)
        }
    }

    private fun summarize(
        months: List<MonthlyEnergyInputEntity>,
        prices: List<PriceSnapshotEntity>,
    ): Map<String, MonthlySummary> {
        every { profileRepository.findByUuidAndUserUsername(uuid = "p1", userUsername = "alice") } returns profile
        every { inputRepository.findAllByEnergyProfileId(energyProfileId = 1L) } returns months
        every { policyRepository.findAllByEnergyProfileId(energyProfileId = 1L) } returns emptyList()
        every { priceRepository.findAllByEnergyProfileIdOrderByValidFromDesc(energyProfileId = 1L) } returns prices

        return controller.summarize(profileUuid = "p1", username = "alice", startDate = null, endDate = null)
            .months
            .associateBy { it.period }
    }

    @Test
    fun `costs each month at the contract price in effect back then`() {
        val result = summarize(
            months = listOf(month("2024-06"), month("2025-06")),
            prices = listOf(
                entry(validFrom = "2024-01", electricity = "0.32"),
                entry(validFrom = "2025-01", electricity = "0.28"),
            ),
        )

        // 100 kWh from the grid, priced by the entry in force that month.
        assertEquals(expected = BigDecimal("32.00"), actual = result.getValue("2024-06").gridCost)
        assertEquals(expected = BigDecimal("28.00"), actual = result.getValue("2025-06").gridCost)
    }

    @Test
    fun `keeps the tariff difference at zero across a contract change`() {
        val result = summarize(
            months = listOf(month("2024-06"), month("2025-06")),
            prices = listOf(
                entry(validFrom = "2024-01", electricity = "0.32"),
                entry(validFrom = "2025-01", electricity = "0.28"),
            ),
        )

        // Changing contract moves the reference with the actual price, so a contract change must not
        // masquerade as a dynamic-tariff saving.
        assertEquals(expected = BigDecimal("0.00"), actual = result.getValue("2024-06").dynamicTariffDelta)
        assertEquals(expected = BigDecimal("0.00"), actual = result.getValue("2025-06").dynamicTariffDelta)
    }

    @Test
    fun `lets a per-month price beat the timeline without moving the reference`() {
        val result = summarize(
            months = listOf(month("2025-06", electricityPriceOverride = "0.20")),
            prices = listOf(entry(validFrom = "2025-01", electricity = "0.28")),
        )

        val june = result.getValue("2025-06")
        assertEquals(expected = BigDecimal("0.200"), actual = june.purchasePricePerKwh)
        assertEquals(expected = BigDecimal("20.00"), actual = june.gridCost)
        assertEquals(expected = BigDecimal("28.00"), actual = june.gridCostAtReferencePrice)
        assertEquals(expected = BigDecimal("8.00"), actual = june.dynamicTariffDelta)
    }

    @Test
    fun `falls back to the settings price for months before the timeline starts`() {
        val result = summarize(
            months = listOf(month("2023-06")),
            prices = listOf(entry(validFrom = "2024-01", electricity = "0.32")),
        )

        assertEquals(expected = BigDecimal("30.00"), actual = result.getValue("2023-06").gridCost)
    }

    @Test
    fun `loads the price timeline once for the whole history`() {
        val twoYears = (1..24).map { index ->
            val year = 2024 + (index - 1) / 12
            val monthOfYear = (index - 1) % 12 + 1
            month("%04d-%02d".format(year, monthOfYear))
        }

        summarize(months = twoYears, prices = listOf(entry(validFrom = "2024-01", electricity = "0.32")))

        // One query for the timeline, not one (or two) per month.
        verify(exactly = 1) { priceRepository.findAllByEnergyProfileIdOrderByValidFromDesc(energyProfileId = 1L) }
    }

    /** A profile the efficiency rating can actually be computed for. */
    private fun ratedProfile(): EnergyProfileEntity {
        return EnergyProfileEntity().apply {
            id = 1L
            uuid = "p1"
            heatingReferenceType = HeatingReferenceTypeEnum.NONE
            hasHeatPump = true
            usableAreaSqm = BigDecimal("100")
            heatPumpScop = BigDecimal("1")
        }
    }

    /** The 12 months of 2025, each carrying [kwh] of heat-pump electricity unless listed in [without]. */
    private fun heatPumpYear(kwh: String, without: Set<String> = emptySet()): List<MonthlyEnergyInputEntity> {
        return (1..12).map { monthOfYear ->
            val period = "2025-%02d".format(monthOfYear)
            month(period).apply {
                heatPumpConsumptionKwh = if (period in without) null else BigDecimal(kwh)
            }
        }
    }

    private fun rate(months: List<MonthlyEnergyInputEntity>, profile: EnergyProfileEntity): EnergyEfficiencyRating {
        every { profileRepository.findByUuidAndUserUsername(uuid = "p1", userUsername = "alice") } returns profile
        every { inputRepository.findAllByEnergyProfileId(energyProfileId = 1L) } returns months
        every { policyRepository.findAllByEnergyProfileId(energyProfileId = 1L) } returns emptyList()
        every { priceRepository.findAllByEnergyProfileIdOrderByValidFromDesc(energyProfileId = 1L) } returns emptyList()

        return controller.summarize(profileUuid = "p1", username = "alice", startDate = null, endDate = null)
            .efficiency
    }

    @Test
    fun `rates a full year of heat-pump readings`() {
        val rating = rate(months = heatPumpYear(kwh = "500"), profile = ratedProfile())

        assertEquals(expected = 12, actual = rating.monthsConsidered)
        assertEquals(expected = "2025-01", actual = rating.windowStart)
        assertEquals(expected = "2025-12", actual = rating.windowEnd)
        assertEquals(expected = BigDecimal("6000.00"), actual = rating.heatPumpElectricityKwh)
        assertEquals(expected = EnergyEfficiencyClassEnum.B, actual = rating.energyClass)
    }

    @Test
    fun `treats a month without a heat-pump reading as a gap, not as zero`() {
        // The month row exists (it carries household consumption), it just has no heat-pump value.
        val rating = rate(months = heatPumpYear(kwh = "500", without = setOf("2025-06")), profile = ratedProfile())

        assertEquals(expected = 11, actual = rating.monthsConsidered)
        assertNull(actual = rating.energyClass)
    }
}
