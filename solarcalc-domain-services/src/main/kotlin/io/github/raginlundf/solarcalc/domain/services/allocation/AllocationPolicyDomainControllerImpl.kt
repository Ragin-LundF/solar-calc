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

    override fun list(profileUuid: String, username: String): List<AllocationPolicyResponse> {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        return policyRepository.findAllByEnergyProfileId(energyProfileId = profile.id!!)
            .map { it.toResponse(energyProfileUuid = profileUuid) }
    }

    override fun get(profileUuid: String, policyId: Long, username: String): AllocationPolicyResponse {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        return requirePolicy(policyId = policyId, profileId = profile.id!!)
            .toResponse(energyProfileUuid = profileUuid)
    }

    override fun create(
        profileUuid: String,
        request: CreateAllocationPolicyRequest,
        username: String,
    ): AllocationPolicyResponse {
        val profile = requireProfile(profileUuid = profileUuid, username = username)

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
        request: UpdateAllocationPolicyRequest,
        username: String,
    ): AllocationPolicyResponse {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        val policy = requirePolicy(policyId = policyId, profileId = profile.id!!)
        policy.name = request.name
        policy.priorityOrder = request.priorityOrder
        policy.updatedAt = LocalDateTime.now()
        return policyRepository.save(policy).toResponse(energyProfileUuid = profileUuid)
    }

    override fun delete(profileUuid: String, policyId: Long, username: String) {
        val profile = requireProfile(profileUuid = profileUuid, username = username)
        val policy = requirePolicy(policyId = policyId, profileId = profile.id!!)
        policyRepository.delete(policy)
    }

    private fun requireProfile(profileUuid: String, username: String): EnergyProfile {
        return profileRepository.findByUuidAndUserUsername(uuid = profileUuid, userUsername = username)
            ?: throw ResourceNotFoundException(message = "Profile $profileUuid not found")
    }

    private fun requirePolicy(policyId: Long, profileId: Long): AllocationPolicy {
        return policyRepository.findByIdAndEnergyProfileId(id = policyId, energyProfileId = profileId)
            ?: throw ResourceNotFoundException(message = "AllocationPolicy $policyId not found")
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
