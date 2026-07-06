import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardCardComponent } from '@/shared/components/card';
import { ZardBadgeComponent } from '@/shared/components/badge';

interface CalculationResultDto {
  period: string;
  selfConsumptionSavingsEur: number;
  feedInRevenueEur: number;
  heatPumpSavingsEur: number;
  wallboxSavingsEur: number;
  totalElectricitySavingsEur: number;
  allocatedHouseholdKwh: number;
  allocatedHeatPumpKwh: number;
  allocatedWallboxKwh: number;
  unallocatedKwh: number;
  completenessFlags: string[];
}

@Component({
  selector: 'app-dashboard',
  imports: [DecimalPipe, FormsModule, TranslatePipe, ZardButtonComponent, ZardCardComponent, ZardBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent {
  private readonly api = inject(ApiService);
  readonly state = inject(AppStateService);

  readonly loading = signal(false);
  readonly result = signal<CalculationResultDto | null>(null);
  readonly error = signal<string | null>(null);
  period = '';

  calculate(): void {
    const tid = this.state.tenantId();
    const pid = this.state.profileId();
    if (!tid || !pid || !this.period) return;

    this.loading.set(true);
    this.result.set(null);
    this.error.set(null);

    this.api.get<CalculationResultDto>(`/tenants/${tid}/profiles/${pid}/calculations/${this.period}`).subscribe({
      next: r => { this.result.set(r); this.loading.set(false); },
      error: () => { this.error.set('common.error'); this.loading.set(false); },
    });
  }
}
