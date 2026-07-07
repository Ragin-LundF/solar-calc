export type AllocationCategory = 'HOUSEHOLD' | 'HEAT_PUMP' | 'WALLBOX';
export type OverviewLayout = 'KPI' | 'STORY';
export type HeatingReferenceType = 'NONE' | 'OIL' | 'GAS';

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
  cumulativeSavings: number;
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
  heatingMonthlyDistribution: number[];
  overviewLayout: OverviewLayout;
}
