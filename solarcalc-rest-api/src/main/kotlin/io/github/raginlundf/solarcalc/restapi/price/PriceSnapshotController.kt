package io.github.raginlundf.solarcalc.restapi.price

import io.github.raginlundf.logging.annotations.LogDuration
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
@RequestMapping("/api/v1/profiles/{profileUuid}/prices")
class PriceSnapshotController(
    private val priceSnapshotDomainController: PriceSnapshotDomainController,
) {

    @LogDuration
    @GetMapping
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_READ}')")
    fun list(@PathVariable profileUuid: String): List<PriceSnapshotResponse> {
        return priceSnapshotDomainController.list(profileUuid = profileUuid)
    }

    @LogDuration
    @GetMapping("/{priceId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_READ}')")
    fun get(
        @PathVariable profileUuid: String,
        @PathVariable priceId: Long,
    ): PriceSnapshotResponse {
        return priceSnapshotDomainController.get(profileUuid = profileUuid, priceId = priceId)
    }

    @LogDuration
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_WRITE}')")
    fun create(
        @PathVariable profileUuid: String,
        @Valid @RequestBody request: UpsertPriceSnapshotRequest,
    ): PriceSnapshotResponse {
        return priceSnapshotDomainController.create(profileUuid = profileUuid, request = request)
    }

    @LogDuration
    @PutMapping("/{priceId}")
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_WRITE}')")
    fun update(
        @PathVariable profileUuid: String,
        @PathVariable priceId: Long,
        @Valid @RequestBody request: UpsertPriceSnapshotRequest,
    ): PriceSnapshotResponse {
        return priceSnapshotDomainController.update(profileUuid = profileUuid, priceId = priceId, request = request)
    }

    @LogDuration
    @DeleteMapping("/{priceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('${SolarcalcScopes.SCOPE_PRICES_WRITE}')")
    fun delete(
        @PathVariable profileUuid: String,
        @PathVariable priceId: Long,
    ) {
        priceSnapshotDomainController.delete(profileUuid = profileUuid, priceId = priceId)
    }
}
