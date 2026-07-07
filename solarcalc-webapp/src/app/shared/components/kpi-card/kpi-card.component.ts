import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'solar-kpi-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
  template: `
    <div class="bg-solar-panel border border-solar-border rounded-[14px] p-[18px]">
      <div class="text-[12px] text-solar-text2">{{ label() | translate }}</div>
      <div class="text-[24px] font-bold mt-1.5" [style.color]="color()">{{ value() }}</div>
      @if (sub()) {
        <div class="text-[12px] text-solar-muted mt-1">{{ sub() | translate }}</div>
      }
    </div>
  `,
})
export class KpiCardComponent {
  /** i18n key. */
  readonly label = input.required<string>();
  readonly value = input.required<string>();
  /** i18n key (optional). */
  readonly sub = input<string>('');
  readonly color = input<string>('#e8eaed');
}
