package io.github.raginlundf.solarcalc.restapi.price

import io.github.raginlundf.solarcalc.domain.services.price.PriceSnapshotDomainController
import io.github.raginlundf.solarcalc.dtos.price.PriceSnapshotResponse
import io.github.raginlundf.solarcalc.dtos.price.UpsertPriceSnapshotRequest
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

@RestController
@RequestMapping("/api/v1/profiles/{profileId}/prices")
class PriceSnapshotController(
    private val priceSnapshotDomainController: PriceSnapshotDomainController,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_READ}')")
    fun list(@PathVariable profileId: Long): List<PriceSnapshotResponse> {
        return priceSnapshotDomainController.list(profileId = profileId)
    }

    @GetMapping("/{priceId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_READ}')")
    fun get(
        @PathVariable profileId: Long,
        @PathVariable priceId: Long,
    ): PriceSnapshotResponse {
        return priceSnapshotDomainController.get(profileId = profileId, priceId = priceId)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_WRITE}')")
    fun create(
        @PathVariable profileId: Long,
        @Valid @RequestBody request: UpsertPriceSnapshotRequest,
    ): PriceSnapshotResponse {
        return priceSnapshotDomainController.create(profileId = profileId, request = request)
    }

    @PutMapping("/{priceId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_WRITE}')")
    fun update(
        @PathVariable profileId: Long,
        @PathVariable priceId: Long,
        @Valid @RequestBody request: UpsertPriceSnapshotRequest,
    ): PriceSnapshotResponse {
        return priceSnapshotDomainController.update(profileId = profileId, priceId = priceId, request = request)
    }

    @DeleteMapping("/{priceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_WRITE}')")
    fun delete(
        @PathVariable profileId: Long,
        @PathVariable priceId: Long,
    ) {
        priceSnapshotDomainController.delete(profileId = profileId, priceId = priceId)
    }
}
