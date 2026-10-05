import {MonthlySummary} from '@/core/api/models';
import {buildCompareRows, findYearComparison, previousYearPeriod} from './year-comparison';

function month(period: string, patch: Partial<MonthlySummary> = {}): MonthlySummary {
  return { period, generationKwh: 0, feedInKwh: 0, totalSavings: 0, ...patch } as MonthlySummary;
}

describe('previousYearPeriod', () => {
  it('keeps the month and steps back one year, also across a decade', () => {
    expect(previousYearPeriod('2026-06')).toBe('2025-06');
    expect(previousYearPeriod('2030-01')).toBe('2029-01');
  });
});

describe('findYearComparison', () => {
  it('pairs the last entered month with the same month one year earlier', () => {
    const months = [month('2025-05'), month('2025-06'), month('2025-07'), month('2026-06')];
    const c = findYearComparison(months)!;
    expect(c.current.period).toBe('2026-06');
    expect(c.previous?.period).toBe('2025-06');
    expect(c.previousPeriod).toBe('2025-06');
  });

  it('reports a missing previous-year month as null but still names its period', () => {
    const c = findYearComparison([month('2025-05'), month('2026-06')])!;
    expect(c.previous).toBeNull();
    expect(c.previousPeriod).toBe('2025-06');
  });

  it('returns null when nothing was entered', () => {
    expect(findYearComparison([])).toBeNull();
  });
});

describe('buildCompareRows', () => {
  it('computes the relative change for energy rows', () => {
    const rows = buildCompareRows(month('2026-06', { generationKwh: 1150 }), month('2025-06', { generationKwh: 1000 }), 'energy');
    const gen = rows.find(r => r.label === 'solar.col.generation')!;
    expect(gen).toMatchObject({ prev: 1000, curr: 1150, unit: 'kwh', higherIsBetter: true });
    expect(gen.delta).toBeCloseTo(15);
  });

  it('gives the self-consumption rate change in percentage points', () => {
    const rows = buildCompareRows(
      month('2026-06', { selfConsumptionQuotePct: 45 }),
      month('2025-06', { selfConsumptionQuotePct: 40 }),
      'energy',
    );
    expect(rows.find(r => r.unit === 'pct')?.delta).toBe(5);
  });

  it('has no change when the previous value is zero', () => {
    const rows = buildCompareRows(month('2026-06', { feedInKwh: 300 }), month('2025-06', { feedInKwh: 0 }), 'energy');
    expect(rows.find(r => r.label === 'solar.col.feedIn')?.delta).toBeNull();
  });

  it('has no change and no previous value when the previous-year month is missing', () => {
    const rows = buildCompareRows(month('2026-06', { generationKwh: 900 }), null, 'energy');
    expect(rows.find(r => r.label === 'solar.col.generation')).toMatchObject({ prev: null, delta: null });
  });

  it('leaves out rows that are zero in both months, e.g. a profile without heat pump', () => {
    const rows = buildCompareRows(month('2026-06', { generationKwh: 1 }), month('2025-06', { generationKwh: 1 }), 'energy');
    expect(rows.map(r => r.label)).toEqual(['solar.col.generation']);
  });

  it('treats more grid purchase as worse', () => {
    const rows = buildCompareRows(month('2026-06', { gridKwh: 200 }), month('2025-06', { gridKwh: 100 }), 'energy');
    expect(rows.find(r => r.label === 'solar.col.purchased')?.higherIsBetter).toBe(false);
  });

  it('always ends the savings view with the total, even when it is zero', () => {
    const rows = buildCompareRows(month('2026-06', { householdSavings: 20 }), month('2025-06'), 'savings');
    expect(rows.map(r => r.label)).toEqual(['solar.col.household', 'solar.col.total']);
    expect(rows.at(-1)).toMatchObject({ total: true, unit: 'eur' });
  });
});
