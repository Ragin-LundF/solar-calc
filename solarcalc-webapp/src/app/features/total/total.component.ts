import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { SummaryStore } from '@/core/api/summary.store';
import { FilterService } from '@/core/state/filter.service';
import { MonthlySummary } from '@/core/api/models';
import { TranslatePipe } from '@ngx-translate/core';
import { AnalysisKpi, AnalysisLayoutComponent } from '@/shared/components/analysis-layout/analysis-layout.component';
import { BarSeries } from '@/shared/components/charts/bar-chart.component';
import { fmtEUR, monthLongLabel } from '@/shared/utils/format';

@Component({
  selector: 'app-total',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AnalysisLayoutComponent, TranslatePipe],
  templateUrl: './total.component.html',
})
export class TotalComponent {
  private readonly store = inject(SummaryStore);
  private readonly filterState = inject(FilterService);

  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly fmtEUR = fmtEUR;
  readonly monthLongLabel = monthLongLabel;
  readonly gridFmt = fmtEUR;

  readonly months = computed<MonthlySummary[]>(() => {
    const s = this.store.summary();
    return s ? this.filterState.select(s.months) : [];
  });
  readonly rows = computed(() => [...this.months()].reverse());

  private sum(fn: (m: MonthlySummary) => number): number {
    return this.months().reduce((a, m) => a + fn(m), 0);
  }

  readonly kpis = computed<AnalysisKpi[]>(() => [
    { label: 'solar.k.feedInRevenue', value: fmtEUR(this.sum(m => m.feedInRevenue)), sub: 'solar.ks.total.feedIn', color: '#eab308' },
    { label: 'solar.k.heating', value: fmtEUR(this.sum(m => m.heatingSavings)), sub: 'solar.ks.total.heating', color: '#f97066' },
    { label: 'solar.k.householdWallbox', value: fmtEUR(this.sum(m => m.householdSavings + m.wallboxSavingsVsGasoline)), sub: 'solar.ks.total.householdWallbox', color: '#3b82f6' },
    { label: 'solar.k.totalSavings', value: fmtEUR(this.sum(m => m.totalSavings)), sub: 'solar.ks.total.total', color: '#22c55e' },
  ]);

  readonly series: BarSeries[] = [
    { label: 'solar.k.feedInRevenue', color: '#eab308', value: m => m.feedInRevenue },
    { label: 'solar.k.heating', color: '#f04438', value: m => m.heatingSavings },
    { label: 'solar.k.household', color: '#3b82f6', value: m => m.householdSavings },
    { label: 'solar.k.wallbox', color: '#22c55e', value: m => m.wallboxSavingsVsGasoline },
  ];
}
