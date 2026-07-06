package io.github.raginlundf.solarcalc.domain.services.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy
import io.github.raginlundf.solarcalc.domain.models.repository.AllocationPolicyRepository
import io.github.raginlundf.solarcalc.domain.models.repository.EnergyProfileRepository
import io.github.raginlundf.solarcalc.dtos.allocation.AllocationPolicyResponse
import io.github.raginlundf.solarcalc.dtos.allocation.CreateAllocationPolicyRequest
import io.github.raginlundf.solarcalc.dtos.allocation.UpdateAllocationPolicyRequest
import io.github.raginlundf.solarcalc.dtos.error.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AllocationPolicyDomainControllerImpl(
    private val profileRepository: EnergyProfileRepository,
    private val policyRepository: AllocationPolicyRepository,
) : AllocationPolicyDomainController {

    override fun list(profileId: Long): List<AllocationPolicyResponse> {
        requireProfile(profileId = profileId)
        return policyRepository.findAllByEnergyProfileId(energyProfileId = profileId).map { it.toResponse() }
    }

    override fun get(profileId: Long, policyId: Long): AllocationPolicyResponse {
        requireProfile(profileId = profileId)
        return policyRepository.findById(policyId).orElseThrow {
            ResourceNotFoundException("AllocationPolicy $policyId not found")
        }.toResponse()
    }

    override fun create(profileId: Long, request: CreateAllocationPolicyRequest): AllocationPolicyResponse {
        val profile = profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }

        val policy = AllocationPolicy().apply {
            this.energyProfile = profile
            name = request.name
            priorityOrder = request.priorityOrder
            isDefault = request.isDefault
        }
        return policyRepository.save(policy).toResponse()
    }

    override fun update(profileId: Long, policyId: Long, request: UpdateAllocationPolicyRequest): AllocationPolicyResponse {
        requireProfile(profileId = profileId)
        val policy = policyRepository.findById(policyId).orElseThrow {
            ResourceNotFoundException("AllocationPolicy $policyId not found")
        }
        policy.name = request.name
        policy.priorityOrder = request.priorityOrder
        policy.isDefault = request.isDefault
        policy.updatedAt = LocalDateTime.now()
        return policyRepository.save(policy).toResponse()
    }

    override fun delete(profileId: Long, policyId: Long) {
        requireProfile(profileId = profileId)
        val policy = policyRepository.findById(policyId).orElseThrow {
            ResourceNotFoundException("AllocationPolicy $policyId not found")
        }
        policyRepository.delete(policy)
    }

    private fun requireProfile(profileId: Long) {
        profileRepository.findById(profileId).orElseThrow {
            ResourceNotFoundException("Profile $profileId not found")
        }
    }
}

private fun AllocationPolicy.toResponse(): AllocationPolicyResponse {
    return AllocationPolicyResponse(
        id = id!!,
        energyProfileId = energyProfileId!!,
        name = name,
        priorityOrder = priorityOrder,
        isDefault = isDefault,
    )
}
