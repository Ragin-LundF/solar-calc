import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-scenario-comparison',
  imports: [TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="text-2xl font-semibold mb-6">{{ 'navigation.scenarioComparison' | translate }}</h1>
    <p class="text-muted-foreground">Scenario comparison coming soon.</p>
  `,
})
export class ScenarioComparisonComponent {}
