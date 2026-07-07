package io.github.raginlundf.solarcalc.domain.services.allocation

import io.github.raginlundf.solarcalc.domain.models.allocation.AllocationPolicy
import io.github.raginlundf.solarcalc.domain.models.profile.EnergyProfile
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

    override fun list(profileUuid: String): List<AllocationPolicyResponse> {
        val profile = requireProfile(profileUuid = profileUuid)
        return policyRepository.findAllByEnergyProfileId(energyProfileId = profile.id!!)
            .map { it.toResponse(energyProfileUuid = profileUuid) }
    }

    override fun get(profileUuid: String, policyId: Long): AllocationPolicyResponse {
        requireProfile(profileUuid = profileUuid)
        return policyRepository.findById(policyId).orElseThrow {
            ResourceNotFoundException(message = "AllocationPolicy $policyId not found")
        }.toResponse(energyProfileUuid = profileUuid)
    }

    override fun create(profileUuid: String, request: CreateAllocationPolicyRequest): AllocationPolicyResponse {
        val profile = profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException(message = "Profile $profileUuid not found")

        val policy = AllocationPolicy().apply {
            this.energyProfile = profile
            name = request.name
            priorityOrder = request.priorityOrder
        }
        return policyRepository.save(policy).toResponse(energyProfileUuid = profileUuid)
    }

    override fun update(
        profileUuid: String,
        policyId: Long,
        request: UpdateAllocationPolicyRequest
    ): AllocationPolicyResponse {
        requireProfile(profileUuid = profileUuid)
        val policy = policyRepository.findById(policyId).orElseThrow {
            ResourceNotFoundException(message = "AllocationPolicy $policyId not found")
        }
        policy.name = request.name
        policy.priorityOrder = request.priorityOrder
        policy.updatedAt = LocalDateTime.now()
        return policyRepository.save(policy).toResponse(energyProfileUuid = profileUuid)
    }

    override fun delete(profileUuid: String, policyId: Long) {
        requireProfile(profileUuid = profileUuid)
        val policy = policyRepository.findById(policyId).orElseThrow {
            ResourceNotFoundException(message = "AllocationPolicy $policyId not found")
        }
        policyRepository.delete(policy)
    }

    private fun requireProfile(profileUuid: String): EnergyProfile {
        return profileRepository.findByUuid(profileUuid)
            ?: throw ResourceNotFoundException(message = "Profile $profileUuid not found")
    }
}

private fun AllocationPolicy.toResponse(energyProfileUuid: String): AllocationPolicyResponse {
    return AllocationPolicyResponse(
        id = id!!,
        energyProfileUuid = energyProfileUuid,
        name = name,
        priorityOrder = priorityOrder,
    )
}
