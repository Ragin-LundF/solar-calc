import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { SummaryStore } from '@/core/api/summary.store';
import { FilterService } from '@/core/state/filter.service';
import { MonthlySummary } from '@/core/api/models';
import { TranslatePipe } from '@ngx-translate/core';
import { AnalysisKpi, AnalysisLayoutComponent } from '@/shared/components/analysis-layout/analysis-layout.component';
import { BarSeries } from '@/shared/components/charts/bar-chart.component';
import { fmtEUR, fmtKWh, fmtPct, monthLongLabel } from '@/shared/utils/format';

@Component({
  selector: 'app-production',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AnalysisLayoutComponent, TranslatePipe],
  templateUrl: './production.component.html',
})
export class ProductionComponent {
  private readonly store = inject(SummaryStore);
  private readonly filterState = inject(FilterService);

  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly fmtEUR = fmtEUR;
  readonly fmtKWh = fmtKWh;
  readonly fmtPct = fmtPct;
  readonly monthLongLabel = monthLongLabel;
  readonly gridFmt = fmtKWh;

  readonly months = computed<MonthlySummary[]>(() => {
    const s = this.store.summary();
    return s ? this.filterState.select(s.months) : [];
  });
  readonly rows = computed(() => [...this.months()].reverse());

  private sum(fn: (m: MonthlySummary) => number): number {
    return this.months().reduce((a, m) => a + fn(m), 0);
  }

  readonly kpis = computed<AnalysisKpi[]>(() => [
    { label: 'solar.k.totalGeneration', value: fmtKWh(this.sum(m => m.generationKwh)), sub: 'solar.ks.production.totalGeneration', color: '#9aa4b2' },
    { label: 'solar.k.selfConsumption', value: fmtKWh(this.sum(m => m.selfConsumedKwh)), sub: 'solar.ks.production.selfConsumption', color: '#22c55e' },
    { label: 'solar.k.feedIn', value: fmtKWh(this.sum(m => m.feedInKwh)), sub: 'solar.ks.production.feedIn', color: '#eab308' },
    { label: 'solar.k.feedInRevenue', value: fmtEUR(this.sum(m => m.feedInRevenue)), sub: 'solar.ks.production.revenue', color: '#eab308' },
  ]);

  readonly series: BarSeries[] = [
    { label: 'solar.k.selfConsumption', color: '#22c55e', value: m => m.selfConsumedKwh },
    { label: 'solar.k.feedIn', color: '#eab308', value: m => m.feedInKwh },
  ];
}
