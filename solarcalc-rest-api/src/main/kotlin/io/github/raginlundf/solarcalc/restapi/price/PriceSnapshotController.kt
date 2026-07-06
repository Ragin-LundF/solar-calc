package io.github.raginlundf.solarcalc.restapi.price

import io.github.raginlundf.solarcalc.domain.models.price.PriceSnapshot
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.domain.models.repository.PriceSnapshotRepository
import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.restapi.error.ResourceNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/tenants/{tenantId}/profiles/{profileId}/prices")
class PriceSnapshotController(
    private val tenantRepository: TenantRepository,
    private val profileRepository: EnergyProfileRepository,
    private val priceRepository: PriceSnapshotRepository,
) {

    @GetMapping
    fun list(@PathVariable tenantId: Long, @PathVariable profileId: Long): List<PriceSnapshotResponse> {
        requireProfile(tenantId, profileId)
        return priceRepository.findAllByTenantIdAndEnergyProfileId(tenantId, profileId).map { it.toResponse() }
    }

    @GetMapping("/{priceId}")
    fun get(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable priceId: Long,
    ): PriceSnapshotResponse {
        requireProfile(tenantId = tenantId, profileId = profileId)
        return priceRepository.findByIdAndTenantId(id = priceId, tenantId = tenantId)?.toResponse()
            ?: throw ResourceNotFoundException("PriceSnapshot $priceId not found for tenant $tenantId")
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @Valid @RequestBody request: UpsertPriceSnapshotRequest,
    ): PriceSnapshotResponse {
        val tenant = tenantRepository.findById(tenantId).orElseThrow {
            ResourceNotFoundException("Tenant $tenantId not found")
        }
        val profile = profileRepository.findByIdAndTenantId(profileId, tenantId)
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")

        val snapshot = PriceSnapshot().apply {
            this.tenant = tenant
            this.energyProfile = profile
            applyRequest(request)
        }
        return priceRepository.save(snapshot).toResponse()
    }

    @PutMapping("/{priceId}")
    fun update(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable priceId: Long,
        @Valid @RequestBody request: UpsertPriceSnapshotRequest,
    ): PriceSnapshotResponse {
        requireProfile(tenantId = tenantId, profileId = profileId)
        val snapshot = priceRepository.findByIdAndTenantId(id = priceId, tenantId = tenantId)
            ?: throw ResourceNotFoundException("PriceSnapshot $priceId not found for tenant $tenantId")
        snapshot.applyRequest(request)
        snapshot.updatedAt = LocalDateTime.now()
        return priceRepository.save(snapshot).toResponse()
    }

    @DeleteMapping("/{priceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable priceId: Long,
    ) {
        requireProfile(tenantId = tenantId, profileId = profileId)
        val snapshot = priceRepository.findByIdAndTenantId(id = priceId, tenantId = tenantId)
            ?: throw ResourceNotFoundException("PriceSnapshot $priceId not found for tenant $tenantId")
        priceRepository.delete(snapshot)
    }

    private fun requireProfile(tenantId: Long, profileId: Long) {
        profileRepository.findByIdAndTenantId(profileId, tenantId)
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")
    }
}

private fun PriceSnapshot.applyRequest(request: UpsertPriceSnapshotRequest) {
    period = request.period
    electricityPrice = request.electricityPrice
    feedInTariff = request.feedInTariff
    petrolPrice = request.petrolPrice
    oilReferenceCost = request.oilReferenceCost
    gasReferenceCost = request.gasReferenceCost
    evEfficiencyKwh100km = request.evEfficiencyKwh100km
    iceEfficiencyL100km = request.iceEfficiencyL100km
}

private fun PriceSnapshot.toResponse(): PriceSnapshotResponse {
    return PriceSnapshotResponse(
        id = id!!,
        tenantId = tenantId!!,
        energyProfileId = energyProfileId!!,
        period = period,
        electricityPrice = electricityPrice,
        feedInTariff = feedInTariff,
        petrolPrice = petrolPrice,
        oilReferenceCost = oilReferenceCost,
        gasReferenceCost = gasReferenceCost,
        evEfficiencyKwh100km = evEfficiencyKwh100km,
        iceEfficiencyL100km = iceEfficiencyL100km,
    )
}
