package io.github.raginlundf.solarcalc.domain.services.auth

/**
 * OAuth2 scopes for the solarcalc API. Two coarse permissions:
 * read for all GET/calculation access, write for create/update/delete.
 *
 * Every user currently receives both scopes ([FULL_ACCESS]). Read-only users are
 * introduced later by issuing only [READ] — no API or annotation change is then required.
 */
object SolarcalcScopes {
    const val READ = "solarcalc:read"
    const val WRITE = "solarcalc:write"

    // Spring Security prefixes JWT scope claims with "SCOPE_" when converting to GrantedAuthority.
    const val SCOPE_READ = "SCOPE_$READ"
    const val SCOPE_WRITE = "SCOPE_$WRITE"

    /** Scopes granted to a full-access user. */
    val FULL_ACCESS: List<String> = listOf(READ, WRITE)
}
