import {ChangeDetectionStrategy, Component, computed, input, signal} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {MonthlySummary} from '@/core/api/models';
import {buildCompareRows, CompareKind, CompareRow, CompareUnit, findYearComparison} from '@/core/state/year-comparison';
import {fmtDeltaPct, fmtDeltaPts, fmtEUR, fmtKWh, fmtPct, monthLongLabel, monthShortLabel} from '@/shared/utils/format';

interface RowView {
  label: string;
  prev: string;
  curr: string;
  delta: string;
  arrow: string;
  tone: string;
  total: boolean;
}

function fmtValue(unit: CompareUnit, n: number): string {
  if (unit === 'eur') return fmtEUR(n);
  if (unit === 'pct') return fmtPct(n);
  return fmtKWh(n);
}

function toView(r: CompareRow): RowView {
  // Below half a unit the rounded label reads "0", so it is shown neutral as well.
  const moved = r.delta != null && Math.abs(r.delta) >= 0.5;
  const better = moved && (r.delta! > 0) === r.higherIsBetter;
  return {
    label: r.label,
    prev: r.prev == null ? '–' : fmtValue(r.unit, r.prev),
    curr: fmtValue(r.unit, r.curr),
    delta: r.unit === 'pct' ? fmtDeltaPts(r.delta) : fmtDeltaPct(r.delta),
    arrow: moved ? (r.delta! > 0 ? '▲' : '▼') : '',
    tone: !moved ? 'text-solar-muted' : better ? 'text-solar-green' : 'text-solar-red',
    total: r.total,
  };
}

/** Last entered month next to the same month one year earlier, as energy or savings view. */
@Component({
  selector: 'app-year-compare',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
  templateUrl: './year-compare.component.html',
})
export class YearCompareComponent {
  /** All-history months, sorted ascending; must not be cut by the global time filter. */
  readonly months = input.required<MonthlySummary[]>();

  readonly tab = signal<CompareKind>('energy');

  readonly comparison = computed(() => findYearComparison(this.months()));

  readonly labels = computed(() => {
    const c = this.comparison();
    return c
      ? {
          current: monthLongLabel(c.current.period),
          previous: monthLongLabel(c.previousPeriod),
          currentShort: monthShortLabel(c.current.period),
          previousShort: monthShortLabel(c.previousPeriod),
        }
      : null;
  });

  readonly rows = computed<RowView[]>(() => {
    const c = this.comparison();
    return c ? buildCompareRows(c.current, c.previous, this.tab()).map(toView) : [];
  });
}
