/**
 * Wire types for the REST API.
 *
 * The server serialises with Jackson's NON_EMPTY inclusion, so a field whose value is null is
 * omitted from the JSON entirely and arrives here as `undefined`, not `null`. A `| null` below
 * therefore means "may be null or absent": test these with `== null`, never `=== null`.
 */
export type AllocationCategory = 'HOUSEHOLD' | 'HEAT_PUMP' | 'WALLBOX';
export type OverviewLayout = 'KPI' | 'STORY';
export type HeatingReferenceType = 'NONE' | 'OIL' | 'GAS';
export type EnergyEfficiencyClass = 'A_PLUS' | 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'G' | 'H';

export interface MonthlySummary {
  period: string;
  generationKwh: number;
  feedInKwh: number;
  selfConsumedKwh: number;
  selfConsumptionQuotePct: number;
  feedInRevenue: number;
  householdConsumptionKwh: number;
  householdSolarKwh: number;
  householdGridKwh: number;
  householdCostWithoutSolar: number;
  householdCostWithSolar: number;
  householdSavings: number;
  heatPumpConsumptionKwh: number;
  heatPumpSolarKwh: number;
  heatPumpGridKwh: number;
  heatPumpCostWithSolar: number;
  heizPct: number;
  oilCost: number;
  heatingSavings: number;
  wallboxConsumptionKwh: number;
  wallboxSolarKwh: number;
  wallboxGridKwh: number;
  wallboxCostWithSolar: number;
  estimatedKm: number;
  gasolineEquivalentCost: number;
  wallboxSavingsVsGasoline: number;
  totalSavings: number;
  /** Total energy bought from the grid: household + heat pump + wallbox. */
  gridKwh: number;
  /** Price actually paid per kWh this month. */
  purchasePricePerKwh: number;
  gridCost: number;
  /** What the same gridKwh would have cost at the standing contract price. */
  gridCostAtReferencePrice: number;
  /** gridCostAtReferencePrice - gridCost; positive means the dynamic tariff was cheaper. */
  dynamicTariffDelta: number;
  cumulativeSavings: number;
}

/**
 * Rough German energy efficiency estimate, computed over the last 12 calendar months of all
 * history. `energyClass` is null when it cannot be estimated; `monthsConsidered` says how many
 * of those 12 months carry data.
 */
export interface EnergyEfficiencyRating {
  usableAreaSqm: number | null;
  heatPumpScop: number | null;
  heatingEnergyKwh: number;
  kwhPerSqmPerYear: number;
  energyClass: EnergyEfficiencyClass | null;
  monthsConsidered: number;
}

export interface PaybackProjection {
  cumulativeSavings: number;
  investKosten: number;
  paybackPct: number;
  amortised: boolean;
  projectedPaybackPeriod: string | null;
}

export interface SummaryResponse {
  allocationPriority: AllocationCategory[];
  months: MonthlySummary[];
  aggregates: unknown;
  payback: PaybackProjection;
  efficiency: EnergyEfficiencyRating;
}

/** One entry of the price timeline: these prices apply from `validFrom` until a later entry does. */
export interface PriceSnapshot {
  id: number;
  validFrom: string;
  electricityPrice: number | null;
  feedInTariff: number | null;
  petrolPrice: number | null;
  /** The fuel the reference cost stands for from this month on; null leaves it unchanged. */
  heatingReferenceType: HeatingReferenceType | null;
  oilReferenceCost: number | null;
  gasReferenceCost: number | null;
}

/** Prices the server would actually use for one month, after the timeline and the profile defaults. */
export interface EffectivePrices {
  period: string;
  electricityPrice: number | null;
  feedInTariff: number | null;
  petrolPrice: number | null;
  /** The fuel heated with that month, which may differ from the profile's current setting. */
  heatingReferenceType: HeatingReferenceType;
  heatingReferenceCost: number | null;
}

export interface AllocationPolicy {
  id: number;
  name: string;
  priorityOrder: AllocationCategory[];
}

export interface MonthlyInput {
  id: string;
  period: string;
  generationKwh: number;
  feedInKwh: number | null;
  householdConsumptionKwh: number | null;
  heatPumpConsumptionKwh: number | null;
  wallboxConsumptionKwh: number | null;
  /** Average price actually paid per kWh this month; null falls back to the contract price. */
  electricityPriceOverride: number | null;
  feedInTariffOverride: number | null;
  petrolPriceOverride: number | null;
  heatingReferenceCostOverride: number | null;
}

export interface EnergyProfile {
  id: string;
  name: string;
  hasWallbox: boolean;
  hasHeatPump: boolean;
  heatingReferenceType: HeatingReferenceType;
  defaultElectricityPrice: number | null;
  defaultFeedInTariff: number | null;
  defaultPetrolPrice: number | null;
  defaultOilReferenceCost: number | null;
  defaultGasReferenceCost: number | null;
  kmPerKwh: number | null;
  litersPer100km: number | null;
  investKosten: number | null;
  usableAreaSqm: number | null;
  heatPumpScop: number | null;
  heatingMonthlyDistribution: number[];
  overviewLayout: OverviewLayout;
}
