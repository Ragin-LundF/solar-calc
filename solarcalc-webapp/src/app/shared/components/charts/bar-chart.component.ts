import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { MonthlySummary } from '@/core/api/models';
import { monthShortLabel } from '@/shared/utils/format';

export interface BarSeries {
  /** i18n key for the legend label. */
  label: string;
  color: string;
  value: (m: MonthlySummary) => number;
}

interface Rect {
  x: number;
  y: number;
  w: number;
  h: number;
  fill: string;
}

@Component({
  selector: 'solar-bar-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
  template: `
    <div>
      <div class="flex flex-wrap items-center gap-4 mb-3">
        @for (s of series(); track s.label) {
          <div class="flex items-center gap-1.5 text-[12px] text-solar-text2">
            <span class="w-2.5 h-2.5 rounded-sm" [style.background]="s.color"></span>{{ s.label | translate }}
          </div>
        }
      </div>
      <div class="overflow-x-auto no-scrollbar">
        <svg [attr.width]="geo().width" [attr.height]="height()" class="block">
          @for (g of geo().gridLines; track g.y) {
            <line x1="0" [attr.x2]="geo().width" [attr.y1]="g.y" [attr.y2]="g.y" stroke="#1c222c" stroke-width="1" />
            <text x="0" [attr.y]="g.y - 3" fill="#5b6472" font-size="10">{{ g.label }}</text>
          }
          @for (r of geo().rects; track $index) {
            <rect [attr.x]="r.x" [attr.y]="r.y" [attr.width]="r.w" [attr.height]="r.h" [attr.fill]="r.fill" rx="1" />
          }
          @for (l of geo().xLabels; track $index) {
            <text [attr.x]="l.x" [attr.y]="height() - 34" fill="#8b93a1" font-size="10" text-anchor="middle">{{ l.text }}</text>
          }
        </svg>
      </div>
    </div>
  `,
})
export class BarChartComponent {
  readonly rows = input.required<MonthlySummary[]>();
  readonly series = input.required<BarSeries[]>();
  readonly stacked = input<boolean>(false);
  readonly height = input<number>(260);
  readonly gridFormat = input<(n: number) => string>((n: number) => String(Math.round(n)));

  private readonly leftPad = 46;
  private readonly rightPad = 14;
  private readonly topPad = 14;
  private readonly bottomPad = 56;

  readonly geo = computed(() => {
    const rows = this.rows();
    const series = this.series();
    const stacked = this.stacked();
    const plotH = this.height() - this.topPad - this.bottomPad;
    const groupW = 46;
    const gap = 20;
    const width = this.leftPad + this.rightPad + rows.length * (groupW + gap);

    let maxVal = 0.0001;
    for (const m of rows) {
      const v = stacked
        ? series.reduce((a, s) => a + Math.max(0, s.value(m)), 0)
        : Math.max(...series.map(s => Math.max(0, s.value(m))));
      maxVal = Math.max(maxVal, v);
    }
    maxVal *= 1.15;

    const barW = stacked ? groupW : (groupW - (series.length - 1) * 3) / series.length;
    const rects: Rect[] = [];
    const xLabels: { x: number; text: string }[] = [];

    rows.forEach((m, i) => {
      const groupX = this.leftPad + i * (groupW + gap);
      if (stacked) {
        let yCursor = this.topPad + plotH;
        series.forEach(s => {
          const h = (Math.max(0, s.value(m)) / maxVal) * plotH;
          yCursor -= h;
          rects.push({ x: groupX, y: yCursor, w: barW, h: Math.max(h, 0), fill: s.color });
        });
      } else {
        series.forEach((s, j) => {
          const h = (Math.max(0, s.value(m)) / maxVal) * plotH;
          rects.push({ x: groupX + j * (barW + 3), y: this.topPad + plotH - h, w: barW, h: Math.max(h, 0.5), fill: s.color });
        });
      }
      xLabels.push({ x: groupX + groupW / 2, text: monthShortLabel(m.period) });
    });

    const fmt = this.gridFormat();
    const gridLines = [0, 0.25, 0.5, 0.75, 1].map(f => ({
      y: this.topPad + plotH * (1 - f),
      label: fmt(maxVal * f),
    }));

    return { width, rects, xLabels, gridLines };
  });
}
