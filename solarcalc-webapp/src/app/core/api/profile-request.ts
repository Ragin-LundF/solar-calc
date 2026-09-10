import { EnergyProfile } from '@/core/api/models';

/**
 * Serialises a profile for POST/PUT `/profiles`.
 *
 * The server replaces every field on write and defaults anything absent to null, so a caller
 * that sends a partial body silently clears the rest of the profile. Every writer therefore
 * goes through here with a complete profile — and a new profile field only has to be added
 * in this one place.
 */
export function toProfileRequest(p: EnergyProfile): Record<string, unknown> {
  return {
    name: p.name,
    hasWallbox: p.hasWallbox,
    hasHeatPump: p.hasHeatPump,
    heatingReferenceType: p.heatingReferenceType,
    defaultElectricityPrice: p.defaultElectricityPrice,
    defaultFeedInTariff: p.defaultFeedInTariff,
    defaultPetrolPrice: p.defaultPetrolPrice,
    defaultOilReferenceCost: p.defaultOilReferenceCost,
    defaultGasReferenceCost: p.defaultGasReferenceCost,
    kmPerKwh: p.kmPerKwh,
    litersPer100km: p.litersPer100km,
    investKosten: p.investKosten,
    usableAreaSqm: p.usableAreaSqm,
    heatPumpScop: p.heatPumpScop,
    heatingMonthlyDistribution: p.heatingMonthlyDistribution,
    overviewLayout: p.overviewLayout,
  };
}
