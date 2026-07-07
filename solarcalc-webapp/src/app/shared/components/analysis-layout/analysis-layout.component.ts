import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { MonthlySummary } from '@/core/api/models';
import { KpiCardComponent } from '@/shared/components/kpi-card/kpi-card.component';
import { BarChartComponent, BarSeries } from '@/shared/components/charts/bar-chart.component';

export interface AnalysisKpi {
  label: string;
  value: string;
  sub: string;
  color: string;
}

/**
 * Shared scaffold for the analysis tabs: loading/error/empty states, the KPI-card
 * grid and the bar chart. The per-view table is projected as content.
 */
@Component({
  selector: 'solar-analysis',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe, KpiCardComponent, BarChartComponent],
  template: `
    @if (loading()) {
      <div class="text-solar-text2 text-sm py-10 text-center">{{ 'common.loading' | translate }}</div>
    } @else if (error()) {
      <div class="text-solar-red text-sm py-10 text-center">{{ error()! | translate }}</div>
    } @else if (rows().length === 0) {
      <div class="text-solar-text2 text-sm py-10 text-center">{{ 'solar.noData' | translate }}</div>
    } @else {
      <div class="grid grid-cols-[repeat(auto-fit,minmax(190px,1fr))] gap-4">
        @for (k of kpis(); track k.label) {
          <solar-kpi-card [label]="k.label" [value]="k.value" [sub]="k.sub" [color]="k.color" />
        }
      </div>

      <div class="bg-solar-panel border border-solar-border rounded-[14px] p-5 mt-4">
        @if (chartTitle()) { <div class="text-[14px] font-semibold mb-3">{{ chartTitle() | translate }}</div> }
        <solar-bar-chart [rows]="rows()" [series]="series()" [stacked]="stacked()" [gridFormat]="gridFormat()" />
      </div>

      <div class="mt-4">
        <ng-content />
      </div>
    }
  `,
})
export class AnalysisLayoutComponent {
  readonly loading = input.required<boolean>();
  readonly error = input.required<string | null>();
  readonly rows = input.required<MonthlySummary[]>();
  readonly kpis = input.required<AnalysisKpi[]>();
  readonly series = input.required<BarSeries[]>();
  readonly stacked = input<boolean>(false);
  readonly chartTitle = input<string>('');
  readonly gridFormat = input<(n: number) => string>((n: number) => String(Math.round(n)));

  readonly ready = computed(() => !this.loading() && !this.error() && this.rows().length > 0);
}
