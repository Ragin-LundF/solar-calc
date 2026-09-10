import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { SummaryStore } from '@/core/api/summary.store';
import { FilterService } from '@/core/state/filter.service';
import { MonthlySummary } from '@/core/api/models';
import { AnalysisKpi, AnalysisLayoutComponent } from '@/shared/components/analysis-layout/analysis-layout.component';
import { BarSeries } from '@/shared/components/charts/bar-chart.component';
import { fmtEUR, fmtEURperKwh, fmtKWh, monthLongLabel } from '@/shared/utils/format';

@Component({
  selector: 'app-grid',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AnalysisLayoutComponent, TranslatePipe],
  templateUrl: './grid.component.html',
})
export class GridComponent {
  private readonly store = inject(SummaryStore);
  private readonly filterState = inject(FilterService);

  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly fmtEUR = fmtEUR;
  readonly fmtKWh = fmtKWh;
  readonly fmtEURperKwh = fmtEURperKwh;
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

  /** Weighted by kWh — averaging the monthly prices would over-weight low-consumption months. */
  private readonly averagePrice = computed(() => {
    const kwh = this.sum(m => m.gridKwh);
    return kwh === 0 ? 0 : this.sum(m => m.gridCost) / kwh;
  });

  readonly delta = computed(() => this.sum(m => m.dynamicTariffDelta));

  readonly kpis = computed<AnalysisKpi[]>(() => [
    {
      label: 'solar.k.purchased',
      value: fmtKWh(this.sum(m => m.gridKwh)),
      sub: 'solar.ks.grid.purchased',
      color: '#9aa4b2',
    },
    {
      label: 'solar.k.avgPurchasePrice',
      value: fmtEURperKwh(this.averagePrice()),
      sub: 'solar.ks.grid.avgPurchasePrice',
      color: '#3b82f6',
    },
    {
      label: 'solar.k.gridCost',
      value: fmtEUR(this.sum(m => m.gridCost)),
      sub: 'solar.ks.grid.gridCost',
      color: '#f97066',
    },
    {
      label: 'solar.k.tariffDelta',
      value: fmtEUR(this.delta()),
      sub: 'solar.ks.grid.tariffDelta',
      color: this.delta() >= 0 ? '#22c55e' : '#f04438',
    },
  ]);

  // The shared bar chart clamps negative values to zero, so the (signed) delta is reported in the
  // KPI card and the table instead of as a third series.
  readonly series: BarSeries[] = [
    { label: 'solar.k.gridCost', color: '#3b82f6', value: m => m.gridCost },
    { label: 'solar.k.gridCostAtReference', color: '#f04438', value: m => m.gridCostAtReferencePrice },
  ];
}
