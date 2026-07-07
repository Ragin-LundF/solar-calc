import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { SummaryStore } from '@/core/api/summary.store';
import { FilterService } from '@/core/state/filter.service';
import { MonthlySummary } from '@/core/api/models';
import { TranslatePipe } from '@ngx-translate/core';
import { AnalysisKpi, AnalysisLayoutComponent } from '@/shared/components/analysis-layout/analysis-layout.component';
import { BarSeries } from '@/shared/components/charts/bar-chart.component';
import { fmtEUR, fmtKm, fmtKWh, monthLongLabel } from '@/shared/utils/format';

@Component({
  selector: 'app-wallbox',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AnalysisLayoutComponent, TranslatePipe],
  templateUrl: './wallbox.component.html',
})
export class WallboxComponent {
  private readonly store = inject(SummaryStore);
  private readonly filterState = inject(FilterService);

  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly fmtEUR = fmtEUR;
  readonly fmtKWh = fmtKWh;
  readonly fmtKm = fmtKm;
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
    { label: 'solar.k.consumption', value: fmtKWh(this.sum(m => m.wallboxConsumptionKwh)), sub: 'solar.ks.wallbox.consumption', color: '#9aa4b2' },
    { label: 'solar.k.estimatedKm', value: fmtKm(this.sum(m => m.estimatedKm)), sub: 'solar.ks.wallbox.km', color: '#9aa4b2' },
    { label: 'solar.k.gasolineEquivalent', value: fmtEUR(this.sum(m => m.gasolineEquivalentCost)), sub: 'solar.ks.wallbox.gasoline', color: '#f97066' },
    { label: 'solar.k.chargingCost', value: fmtEUR(this.sum(m => m.wallboxCostWithSolar)), sub: 'solar.ks.wallbox.charging', color: '#3b82f6' },
    { label: 'solar.k.savingsVsGasoline', value: fmtEUR(this.sum(m => m.wallboxSavingsVsGasoline)), sub: 'solar.ks.wallbox.savings', color: '#22c55e' },
  ]);

  readonly series: BarSeries[] = [
    { label: 'solar.k.gasolineEquivalent', color: '#f97066', value: m => m.gasolineEquivalentCost },
    { label: 'solar.k.chargingCost', color: '#3b82f6', value: m => m.wallboxCostWithSolar },
    { label: 'solar.k.savings', color: '#22c55e', value: m => m.wallboxSavingsVsGasoline },
  ];
}
