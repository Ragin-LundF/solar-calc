import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SummaryStore } from '@/core/api/summary.store';
import { ProfileStore } from '@/core/api/profile.store';
import { FilterService } from '@/core/state/filter.service';
import { MonthlySummary } from '@/core/api/models';
import { TranslatePipe } from '@ngx-translate/core';
import { AnalysisKpi, AnalysisLayoutComponent } from '@/shared/components/analysis-layout/analysis-layout.component';
import { BarSeries } from '@/shared/components/charts/bar-chart.component';
import { fmtEUR, fmtKWh, fmtPct, monthLongLabel } from '@/shared/utils/format';

@Component({
  selector: 'app-heating',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AnalysisLayoutComponent, RouterLink, TranslatePipe],
  templateUrl: './heating.component.html',
})
export class HeatingComponent {
  private readonly store = inject(SummaryStore);
  private readonly profileStore = inject(ProfileStore);
  private readonly filterState = inject(FilterService);

  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly fmtEUR = fmtEUR;
  readonly fmtKWh = fmtKWh;
  readonly fmtPct = fmtPct;
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

  readonly distributionSum = computed(() =>
    (this.profileStore.profile()?.heatingMonthlyDistribution ?? []).reduce((a, b) => a + b, 0),
  );

  readonly kpis = computed<AnalysisKpi[]>(() => [
    { label: 'solar.k.oilReference', value: fmtEUR(this.sum(m => m.oilCost)), sub: 'solar.ks.heating.oil', color: '#f97066' },
    { label: 'solar.k.heatPumpCost', value: fmtEUR(this.sum(m => m.heatPumpCostWithSolar)), sub: 'solar.ks.heating.hp', color: '#3b82f6' },
    { label: 'solar.k.savings', value: fmtEUR(this.sum(m => m.heatingSavings)), sub: 'solar.ks.heating.savings', color: '#22c55e' },
    {
      label: 'solar.k.distributionSum',
      value: fmtPct(this.distributionSum()),
      sub: 'solar.ks.heating.sum',
      color: this.distributionSum() === 100 ? '#22c55e' : '#f04438',
    },
  ]);

  readonly series: BarSeries[] = [
    { label: 'solar.k.oilReference', color: '#f04438', value: m => m.oilCost },
    { label: 'solar.k.heatPumpCost', color: '#3b82f6', value: m => m.heatPumpCostWithSolar },
    { label: 'solar.k.savings', color: '#eab308', value: m => m.heatingSavings },
  ];
}
