package io.github.raginlundf.solarcalc.restapi.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
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
@RequestMapping("/api/tenants/{tenantId}/profiles/{profileId}/allocation-policies")
class AllocationPolicyController(
    private val tenantRepository: TenantRepository,
    private val profileRepository: EnergyProfileRepository,
    private val policyRepository: AllocationPolicyRepository,
) {

    @GetMapping
    fun list(@PathVariable tenantId: Long, @PathVariable profileId: Long): List<AllocationPolicyResponse> {
        requireProfile(tenantId, profileId)
        return policyRepository.findAllByTenantIdAndEnergyProfileId(tenantId, profileId).map { it.toResponse() }
    }

    @GetMapping("/{policyId}")
    fun get(@PathVariable tenantId: Long, @PathVariable profileId: Long, @PathVariable policyId: Long): AllocationPolicyResponse {
        return policyRepository.findByIdAndTenantId(policyId, tenantId)?.toResponse()
            ?: throw ResourceNotFoundException("AllocationPolicy $policyId not found for tenant $tenantId")
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @Valid @RequestBody request: CreateAllocationPolicyRequest,
    ): AllocationPolicyResponse {
        val tenant = tenantRepository.findById(tenantId).orElseThrow {
            ResourceNotFoundException("Tenant $tenantId not found")
        }
        val profile = profileRepository.findByIdAndTenantId(profileId, tenantId)
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")

        val policy = AllocationPolicy().apply {
            this.tenant = tenant
            this.energyProfile = profile
            name = request.name
            priorityOrder = request.priorityOrder
            isDefault = request.isDefault
        }
        return policyRepository.save(policy).toResponse()
    }

    @PutMapping("/{policyId}")
    fun update(
        @PathVariable tenantId: Long,
        @PathVariable profileId: Long,
        @PathVariable policyId: Long,
        @Valid @RequestBody request: UpdateAllocationPolicyRequest,
    ): AllocationPolicyResponse {
        val policy = policyRepository.findByIdAndTenantId(policyId, tenantId)
            ?: throw ResourceNotFoundException("AllocationPolicy $policyId not found for tenant $tenantId")
        policy.name = request.name
        policy.priorityOrder = request.priorityOrder
        policy.isDefault = request.isDefault
        policy.updatedAt = LocalDateTime.now()
        return policyRepository.save(policy).toResponse()
    }

    @DeleteMapping("/{policyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable tenantId: Long, @PathVariable profileId: Long, @PathVariable policyId: Long) {
        val policy = policyRepository.findByIdAndTenantId(policyId, tenantId)
            ?: throw ResourceNotFoundException("AllocationPolicy $policyId not found for tenant $tenantId")
        policyRepository.delete(policy)
    }

    private fun requireProfile(tenantId: Long, profileId: Long) {
        profileRepository.findByIdAndTenantId(profileId, tenantId)
            ?: throw ResourceNotFoundException("Profile $profileId not found for tenant $tenantId")
    }
}

private fun AllocationPolicy.toResponse(): AllocationPolicyResponse {
    return AllocationPolicyResponse(
        id = id!!,
        tenantId = tenantId!!,
        energyProfileId = energyProfileId!!,
        name = name,
        priorityOrder = priorityOrder,
        isDefault = isDefault,
    )
}
