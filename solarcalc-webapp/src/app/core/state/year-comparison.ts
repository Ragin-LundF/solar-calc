import {MonthlySummary} from '@/core/api/models';

export type CompareKind = 'energy' | 'savings';
export type CompareUnit = 'kwh' | 'eur' | 'pct';

export interface YearComparison {
  current: MonthlySummary;
  /** Same calendar month one year before `current`; null when that month was not entered. */
  previous: MonthlySummary | null;
  /** "YYYY-MM" of the previous-year month, also when it is missing. */
  previousPeriod: string;
}

export interface CompareRow {
  /** i18n key */
  label: string;
  unit: CompareUnit;
  prev: number | null;
  curr: number;
  /**
   * Relative change in percent for kWh/€ rows, absolute change in percentage points for
   * pct rows. Null when there is no previous value or a relative change would divide by zero.
   */
  delta: number | null;
  higherIsBetter: boolean;
  /** Sum row, rendered emphasised. */
  total: boolean;
}

interface RowDef {
  label: string;
  unit: CompareUnit;
  value: (m: MonthlySummary) => number | null | undefined;
  higherIsBetter?: boolean;
  total?: boolean;
}

const ENERGY_ROWS: RowDef[] = [
  { label: 'solar.col.generation', unit: 'kwh', value: m => m.generationKwh },
  { label: 'solar.col.feedIn', unit: 'kwh', value: m => m.feedInKwh },
  { label: 'solar.col.selfConsumption', unit: 'kwh', value: m => m.selfConsumedKwh },
  { label: 'solar.ov.compare.selfConsumptionQuote', unit: 'pct', value: m => m.selfConsumptionQuotePct },
  { label: 'solar.col.household', unit: 'kwh', value: m => m.householdConsumptionKwh, higherIsBetter: false },
  { label: 'solar.ov.compare.heatPump', unit: 'kwh', value: m => m.heatPumpConsumptionKwh, higherIsBetter: false },
  { label: 'solar.col.wallbox', unit: 'kwh', value: m => m.wallboxConsumptionKwh, higherIsBetter: false },
  { label: 'solar.col.purchased', unit: 'kwh', value: m => m.gridKwh, higherIsBetter: false },
];

const SAVINGS_ROWS: RowDef[] = [
  { label: 'solar.k.feedInRevenue', unit: 'eur', value: m => m.feedInRevenue },
  { label: 'solar.col.household', unit: 'eur', value: m => m.householdSavings },
  { label: 'solar.col.heating', unit: 'eur', value: m => m.heatingSavings },
  { label: 'solar.col.wallbox', unit: 'eur', value: m => m.wallboxSavingsVsGasoline },
  { label: 'solar.col.total', unit: 'eur', value: m => m.totalSavings, total: true },
];

/** "2026-06" -> "2025-06" */
export function previousYearPeriod(period: string): string {
  const [y, m] = period.split('-');
  return `${Number(y) - 1}-${m}`;
}

/**
 * Pairs the last entered month with the same month one year earlier.
 * `months` must be sorted ascending by period, as the summary endpoint returns them.
 */
export function findYearComparison(months: MonthlySummary[]): YearComparison | null {
  const current = months.at(-1);
  if (!current) return null;
  const previousPeriod = previousYearPeriod(current.period);
  const previous = months.find(m => m.period === previousPeriod) ?? null;
  return { current, previous, previousPeriod };
}

function delta(unit: CompareUnit, prev: number | null, curr: number): number | null {
  if (prev == null) return null;
  if (unit === 'pct') return curr - prev;
  return prev === 0 ? null : ((curr - prev) / Math.abs(prev)) * 100;
}

/** Rows for one side of the comparison; rows that are 0 in both months are left out. */
export function buildCompareRows(
  current: MonthlySummary,
  previous: MonthlySummary | null,
  kind: CompareKind,
): CompareRow[] {
  const defs = kind === 'energy' ? ENERGY_ROWS : SAVINGS_ROWS;
  return defs.flatMap(d => {
    const curr = d.value(current) ?? 0;
    const prev = previous ? (d.value(previous) ?? 0) : null;
    if (!d.total && curr === 0 && (prev ?? 0) === 0) return [];
    return [{
      label: d.label,
      unit: d.unit,
      prev,
      curr,
      delta: delta(d.unit, prev, curr),
      higherIsBetter: d.higherIsBetter ?? true,
      total: d.total ?? false,
    }];
  });
}
