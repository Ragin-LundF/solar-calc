package io.github.raginlundf.solarcalc.domain.models.setup

@Suppress("MagicNumber")
enum class SetupStepEnum(val bitmask: Int) {
    NOT_STARTED(0),
    PROFILE_CREATED(1),
    ALLOCATION_SET(3),
    COMPLETE(7),
}
