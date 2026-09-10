package io.github.raginlundf.solarcalc.domain.models.profile

/**
 * German energy efficiency classes as used on the Energieausweis (GEG scale),
 * ordered from best to worst. Each entry carries the inclusive upper bound of its
 * band in kWh per square metre and year; [H] is open-ended.
 */
enum class EnergyEfficiencyClass {
    A_PLUS,
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
}
