package io.github.raginlundf.solarcalc.restapi.security

object SolarcalcScopes {
    const val PROFILES_READ = "io.github.raginlundf.solarcalc.profiles.read"
    const val PROFILES_WRITE = "io.github.raginlundf.solarcalc.profiles.write"
    const val INPUTS_READ = "io.github.raginlundf.solarcalc.inputs.read"
    const val INPUTS_WRITE = "io.github.raginlundf.solarcalc.inputs.write"
    const val PRICES_READ = "io.github.raginlundf.solarcalc.prices.read"
    const val PRICES_WRITE = "io.github.raginlundf.solarcalc.prices.write"
    const val POLICIES_READ = "io.github.raginlundf.solarcalc.policies.read"
    const val POLICIES_WRITE = "io.github.raginlundf.solarcalc.policies.write"
    const val CALCULATIONS_READ = "io.github.raginlundf.solarcalc.calculations.read"

    // Spring Security prefixes JWT scope claims with "SCOPE_" when converting to GrantedAuthority
    const val SCOPE_PROFILES_READ = "SCOPE_$PROFILES_READ"
    const val SCOPE_PROFILES_WRITE = "SCOPE_$PROFILES_WRITE"
    const val SCOPE_INPUTS_READ = "SCOPE_$INPUTS_READ"
    const val SCOPE_INPUTS_WRITE = "SCOPE_$INPUTS_WRITE"
    const val SCOPE_PRICES_READ = "SCOPE_$PRICES_READ"
    const val SCOPE_PRICES_WRITE = "SCOPE_$PRICES_WRITE"
    const val SCOPE_POLICIES_READ = "SCOPE_$POLICIES_READ"
    const val SCOPE_POLICIES_WRITE = "SCOPE_$POLICIES_WRITE"
    const val SCOPE_CALCULATIONS_READ = "SCOPE_$CALCULATIONS_READ"
}
