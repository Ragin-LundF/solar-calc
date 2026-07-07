import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { SummaryStore } from '@/core/api/summary.store';
import { FilterService } from '@/core/state/filter.service';
import { MonthlySummary } from '@/core/api/models';
import { TranslatePipe } from '@ngx-translate/core';
import { AnalysisKpi, AnalysisLayoutComponent } from '@/shared/components/analysis-layout/analysis-layout.component';
import { BarSeries } from '@/shared/components/charts/bar-chart.component';
import { fmtEUR, fmtKWh, monthLongLabel } from '@/shared/utils/format';

@Component({
  selector: 'app-household',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AnalysisLayoutComponent, TranslatePipe],
  templateUrl: './household.component.html',
})
export class HouseholdComponent {
  private readonly store = inject(SummaryStore);
  private readonly filterState = inject(FilterService);

  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly fmtEUR = fmtEUR;
  readonly fmtKWh = fmtKWh;
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
    { label: 'solar.k.consumption', value: fmtKWh(this.sum(m => m.householdConsumptionKwh)), sub: 'solar.ks.household.consumption', color: '#9aa4b2' },
    { label: 'solar.k.withoutSolar', value: fmtEUR(this.sum(m => m.householdCostWithoutSolar)), sub: 'solar.ks.household.withoutSolar', color: '#f97066' },
    { label: 'solar.k.withSolar', value: fmtEUR(this.sum(m => m.householdCostWithSolar)), sub: 'solar.ks.household.withSolar', color: '#3b82f6' },
    { label: 'solar.k.savings', value: fmtEUR(this.sum(m => m.householdSavings)), sub: 'solar.ks.household.savings', color: '#22c55e' },
  ]);

  readonly series: BarSeries[] = [
    { label: 'solar.k.withoutSolar', color: '#f04438', value: m => m.householdCostWithoutSolar },
    { label: 'solar.k.withSolar', color: '#3b82f6', value: m => m.householdCostWithSolar },
    { label: 'solar.k.savings', color: '#22c55e', value: m => m.householdSavings },
  ];
}
