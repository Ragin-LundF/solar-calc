import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { SummaryStore } from '@/core/api/summary.store';
import { ProfileStore } from '@/core/api/profile.store';
import { FilterService } from '@/core/state/filter.service';
import { MonthlySummary, OverviewLayout } from '@/core/api/models';
import { KpiCardComponent } from '@/shared/components/kpi-card/kpi-card.component';
import { fmtEUR, fmtKWh, fmtPct, monthLongLabel, monthShortLabel } from '@/shared/utils/format';

const C = {
  green: '#22c55e',
  blue: '#3b82f6',
  amber: '#eab308',
  red: '#f04438',
};

interface Kpi {
  label: string;
  value: string;
  sub: string;
  color: string;
}

@Component({
  selector: 'app-overview',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe, KpiCardComponent],
  templateUrl: './overview.component.html',
})
export class OverviewComponent {
  private readonly store = inject(SummaryStore);
  private readonly profileStore = inject(ProfileStore);
  private readonly filterState = inject(FilterService);

  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly monthLongLabel = monthLongLabel;

  readonly months = computed<MonthlySummary[]>(() => {
    const s = this.store.summary();
    return s ? this.filterState.select(s.months) : [];
  });

  readonly layout = computed<OverviewLayout>(() => this.profileStore.profile()?.overviewLayout ?? 'KPI');

  private sum(fn: (m: MonthlySummary) => number): number {
    return this.months().reduce((a, m) => a + fn(m), 0);
  }

  readonly kEigenverbrauch = computed(() => this.sum(m => m.selfConsumedKwh));
  readonly kEinspeise = computed(() => this.sum(m => m.feedInRevenue));
  readonly kHeiz = computed(() => this.sum(m => m.heatingSavings));
  readonly kHaushalt = computed(() => this.sum(m => m.householdSavings));
  readonly kWallbox = computed(() => this.sum(m => m.wallboxSavingsVsGasoline));
  readonly kGesamt = computed(() => this.kEinspeise() + this.kHeiz() + this.kHaushalt() + this.kWallbox());

  readonly kpis = computed<Kpi[]>(() => [
    { label: 'solar.k.selfConsumption', value: fmtKWh(this.kEigenverbrauch()), sub: 'solar.ks.overview.selfConsumption', color: C.green },
    { label: 'solar.k.feedInRevenue', value: fmtEUR(this.kEinspeise()), sub: 'solar.ks.overview.feedIn', color: C.amber },
    { label: 'solar.k.heating', value: fmtEUR(this.kHeiz()), sub: 'solar.ks.overview.heating', color: C.red },
    { label: 'solar.k.household', value: fmtEUR(this.kHaushalt()), sub: 'solar.ks.overview.household', color: C.blue },
    { label: 'solar.k.wallbox', value: fmtEUR(this.kWallbox()), sub: 'solar.ks.overview.wallbox', color: C.green },
  ]);

  readonly payback = computed(() => this.store.summary()?.payback ?? null);

  readonly ring = computed(() => {
    const p = this.payback();
    const pct = p ? Math.min(100, Math.max(0, p.paybackPct)) : 0;
    const r = 58;
    const circ = 2 * Math.PI * r;
    return {
      r,
      circ,
      offset: circ * (1 - pct / 100),
      pctLabel: fmtPct(Math.round(pct)),
      recovered: fmtEUR(p?.cumulativeSavings ?? 0),
      invest: fmtEUR(p?.investKosten ?? 0),
      statusKey: p?.amortised
        ? 'solar.ov.statusAmortised'
        : p?.projectedPaybackPeriod
          ? 'solar.ov.statusExpected'
          : 'solar.ov.statusUnknown',
      statusParam: p?.projectedPaybackPeriod ? { month: monthLongLabel(p.projectedPaybackPeriod) } : {},
    };
  });

  /** Bars = monthly total savings (green), line = cumulative savings (blue). */
  readonly trend = computed(() => {
    const rows = this.months();
    const height = 260;
    const leftPad = 50, rightPad = 14, topPad = 14, bottomPad = 56;
    const plotH = height - topPad - bottomPad;
    const groupW = 28, gap = 14;
    const width = leftPad + rightPad + rows.length * (groupW + gap);
    let maxBar = 0.0001, maxLine = 0.0001;
    rows.forEach(m => { maxBar = Math.max(maxBar, m.totalSavings); maxLine = Math.max(maxLine, m.cumulativeSavings); });
    maxBar *= 1.2;
    maxLine = Math.max(maxLine * 1.1, 1);

    const rects: { x: number; y: number; w: number; h: number }[] = [];
    const xLabels: { x: number; text: string }[] = [];
    const dots: { x: number; y: number }[] = [];
    const pts: string[] = [];
    rows.forEach((m, i) => {
      const gx = leftPad + i * (groupW + gap);
      const h = Math.max(0, (m.totalSavings / maxBar)) * plotH;
      rects.push({ x: gx, y: topPad + plotH - h, w: groupW, h: Math.max(h, 0.5) });
      xLabels.push({ x: gx + groupW / 2, text: monthShortLabel(m.period) });
      const ly = topPad + plotH - (m.cumulativeSavings / maxLine) * plotH;
      const lx = gx + groupW / 2;
      pts.push(`${lx},${ly}`);
      dots.push({ x: lx, y: ly });
    });
    const gridLines = [0, 0.25, 0.5, 0.75, 1].map(f => ({ y: topPad + plotH * (1 - f), label: fmtEUR(maxBar * f) }));
    return { width, height, rects, xLabels, dots, gridLines, linePoints: pts.join(' ') };
  });

  /** Cumulative-only line chart for the Story layout. */
  readonly cumLine = computed(() => {
    const rows = this.months();
    const height = 260;
    const leftPad = 50, rightPad = 14, topPad = 14, bottomPad = 40;
    const plotH = height - topPad - bottomPad;
    const step = rows.length > 1 ? (700 - leftPad - rightPad) / (rows.length - 1) : 0;
    let maxLine = 0.0001;
    rows.forEach(m => { maxLine = Math.max(maxLine, m.cumulativeSavings); });
    maxLine = Math.max(maxLine * 1.1, 1);
    const pts: string[] = [];
    const dots: { x: number; y: number }[] = [];
    rows.forEach((m, i) => {
      const x = leftPad + i * step;
      const y = topPad + plotH - (m.cumulativeSavings / maxLine) * plotH;
      pts.push(`${x},${y}`);
      dots.push({ x, y });
    });
    const gridLines = [0, 0.5, 1].map(f => ({ y: topPad + plotH * (1 - f), label: fmtEUR(maxLine * f) }));
    return { width: 700, height, gridLines, linePoints: pts.join(' '), dots };
  });

  /** Horizontal stacked contribution bar across the four savings streams. */
  readonly contribution = computed(() => {
    const streams = [
      { label: 'solar.k.feedIn', value: Math.max(0, this.kEinspeise()), color: C.amber },
      { label: 'solar.k.heating', value: Math.max(0, this.kHeiz()), color: C.red },
      { label: 'solar.k.household', value: Math.max(0, this.kHaushalt()), color: C.blue },
      { label: 'solar.k.wallbox', value: Math.max(0, this.kWallbox()), color: C.green },
    ];
    const total = streams.reduce((a, s) => a + s.value, 0) || 1;
    return streams.map(s => {
      const pct = (s.value / total) * 100;
      return { ...s, pct, pctLabel: pct >= 8 ? `${Math.round(pct)} %` : '', valueLabel: fmtEUR(s.value) };
    });
  });

  readonly gesamtLabel = computed(() => fmtEUR(this.kGesamt()));

  setLayout(layout: OverviewLayout): void {
    if (layout !== this.layout()) this.profileStore.save({ overviewLayout: layout });
  }
}
