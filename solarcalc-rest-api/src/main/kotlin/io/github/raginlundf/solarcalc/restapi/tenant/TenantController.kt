package io.github.raginlundf.solarcalc.restapi.tenant

import io.github.raginlundf.solarcalc.domain.models.repository.TenantRepository
import io.github.raginlundf.solarcalc.domain.models.tenant.Tenant
import io.github.raginlundf.solarcalc.restapi.error.ResourceNotFoundException
import io.github.raginlundf.solarcalc.restapi.security.SolarcalcScopes
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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
@RequestMapping("/api/tenants")
class TenantController(
    private val tenantRepository: TenantRepository,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_TENANTS_READ}')")
    fun list(): List<TenantResponse> {
        return tenantRepository.findAll().map { it.toResponse() }
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_TENANTS_READ}')")
    fun get(@PathVariable tenantId: Long): TenantResponse {
        return tenantRepository.findById(tenantId).orElseThrow {
            ResourceNotFoundException("Tenant $tenantId not found")
        }.toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_TENANTS_WRITE}')")
    fun create(@Valid @RequestBody request: CreateTenantRequest): TenantResponse {
        val tenant = Tenant().apply {
            name = request.name
            locale = request.locale
            providerLabel = request.providerLabel
            hasWallbox = request.hasWallbox
            hasHeatPump = request.hasHeatPump
            heatingReferenceType = request.heatingReferenceType
            defaultElectricityPrice = request.defaultElectricityPrice
            defaultFeedInTariff = request.defaultFeedInTariff
            defaultPetrolPrice = request.defaultPetrolPrice
            defaultOilReferenceCost = request.defaultOilReferenceCost
            defaultGasReferenceCost = request.defaultGasReferenceCost
            defaultEvEfficiencyKwh100km = request.defaultEvEfficiencyKwh100km
            defaultIceEfficiencyL100km = request.defaultIceEfficiencyL100km
        }
        return tenantRepository.save(tenant).toResponse()
    }

    @PutMapping("/{tenantId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_TENANTS_WRITE}')")
    fun update(@PathVariable tenantId: Long, @Valid @RequestBody request: UpdateTenantRequest): TenantResponse {
        val tenant = tenantRepository.findById(tenantId).orElseThrow {
            ResourceNotFoundException("Tenant $tenantId not found")
        }
        tenant.apply {
            name = request.name
            locale = request.locale
            providerLabel = request.providerLabel
            hasWallbox = request.hasWallbox
            hasHeatPump = request.hasHeatPump
            heatingReferenceType = request.heatingReferenceType
            defaultElectricityPrice = request.defaultElectricityPrice
            defaultFeedInTariff = request.defaultFeedInTariff
            defaultPetrolPrice = request.defaultPetrolPrice
            defaultOilReferenceCost = request.defaultOilReferenceCost
            defaultGasReferenceCost = request.defaultGasReferenceCost
            defaultEvEfficiencyKwh100km = request.defaultEvEfficiencyKwh100km
            defaultIceEfficiencyL100km = request.defaultIceEfficiencyL100km
            updatedAt = LocalDateTime.now()
        }
        return tenantRepository.save(tenant).toResponse()
    }

    @DeleteMapping("/{tenantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_TENANTS_WRITE}')")
    fun delete(@PathVariable tenantId: Long) {
        if (!tenantRepository.existsById(tenantId)) {
            throw ResourceNotFoundException("Tenant $tenantId not found")
        }
        tenantRepository.deleteById(tenantId)
    }
}

private fun Tenant.toResponse(): TenantResponse {
    return TenantResponse(
        id = id!!,
        name = name,
        locale = locale,
        providerLabel = providerLabel,
        hasWallbox = hasWallbox,
        hasHeatPump = hasHeatPump,
        heatingReferenceType = heatingReferenceType,
        defaultElectricityPrice = defaultElectricityPrice,
        defaultFeedInTariff = defaultFeedInTariff,
        defaultPetrolPrice = defaultPetrolPrice,
        defaultOilReferenceCost = defaultOilReferenceCost,
        defaultGasReferenceCost = defaultGasReferenceCost,
        defaultEvEfficiencyKwh100km = defaultEvEfficiencyKwh100km,
        defaultIceEfficiencyL100km = defaultIceEfficiencyL100km,
    )
}
