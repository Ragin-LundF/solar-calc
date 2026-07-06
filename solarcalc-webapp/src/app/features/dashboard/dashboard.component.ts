import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardCardComponent } from '@/shared/components/card';
import { ZardBadgeComponent } from '@/shared/components/badge';

interface CalculationResultDto {
  period: string;
  calculationRunId?: number;
  feedInKwh: number;
  feedInRevenue: number | null;
  selfConsumptionPoolKwh: number;
  unallocatedKwh: number;
  householdAllocatedKwh: number | null;
  householdGridKwh: number | null;
  householdSavings: number | null;
  heatPumpAllocatedKwh: number | null;
  heatPumpGridKwh: number | null;
  heatPumpElectricitySavings: number | null;
  heatPumpHeatingReferenceSavings: number | null;
  wallboxAllocatedKwh: number | null;
  wallboxGridKwh: number | null;
  wallboxElectricitySavings: number | null;
  totalElectricitySavings: number | null;
  completenessFlags: string[];
}

@Component({
  selector: 'app-dashboard',
  imports: [DecimalPipe, FormsModule, RouterLink, TranslatePipe, ZardButtonComponent, ZardCardComponent, ZardBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly state = inject(AppStateService);

  readonly loading = signal(false);
  readonly result = signal<CalculationResultDto | null>(null);
  readonly error = signal<string | null>(null);
  startDate = '';
  endDate = '';

  ngOnInit(): void {
    if (this.state.profileId()) this.calculate();
  }

  calculate(): void {
    const pid = this.state.profileId();
    if (!pid) return;

    this.loading.set(true);
    this.result.set(null);
    this.error.set(null);

    const params = new URLSearchParams();
    if (this.startDate) params.set('startDate', this.startDate);
    if (this.endDate) params.set('endDate', this.endDate);
    const qs = params.toString();

    const path = qs ? `/profiles/${pid}/calculations?${qs}` : `/profiles/${pid}/calculations`;

    this.api.get<CalculationResultDto>(path).subscribe({
      next: r => { this.result.set(r); this.loading.set(false); },
      error: err => {
        this.loading.set(false);
        if (err.status === 404) {
          this.error.set('dashboard.noData');
        } else {
          this.error.set('common.error');
        }
      },
    });
  }
}
