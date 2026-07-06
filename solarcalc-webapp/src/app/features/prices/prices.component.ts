import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardInputDirective } from '@/shared/components/input';
import { ZardCardComponent } from '@/shared/components/card';

interface PriceSnapshotDto {
  id?: number;
  period?: string;
  electricityPricePerKwh: number;
  feedInTariffPerKwh: number;
  petrolPricePerLiter?: number;
  oilReferenceCostPerMonth?: number;
  gasReferenceCostPerMonth?: number;
  evEfficiencyKwhPer100Km?: number;
  iceEfficiencyLiterPer100Km?: number;
}

@Component({
  selector: 'app-prices',
  imports: [ReactiveFormsModule, TranslatePipe, ZardButtonComponent, ZardInputDirective, ZardCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './prices.component.html',
})
export class PricesComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly fb = inject(FormBuilder);

  readonly tenantId = this.state.tenantId;
  readonly profileId = this.state.profileId;
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly snapshots = signal<PriceSnapshotDto[]>([]);
  readonly selectedId = signal<number | null>(null);

  readonly form = this.fb.group({
    period: [null as string | null],
    electricityPricePerKwh: [null as number | null, [Validators.required, Validators.min(0)]],
    feedInTariffPerKwh: [null as number | null, [Validators.required, Validators.min(0)]],
    petrolPricePerLiter: [null as number | null, Validators.min(0)],
    oilReferenceCostPerMonth: [null as number | null, Validators.min(0)],
    gasReferenceCostPerMonth: [null as number | null, Validators.min(0)],
    evEfficiencyKwhPer100Km: [null as number | null, Validators.min(0)],
    iceEfficiencyLiterPer100Km: [null as number | null, Validators.min(0)],
  });

  ngOnInit(): void {
    const tid = this.tenantId();
    const pid = this.profileId();
    if (tid && pid) {
      this.api.get<PriceSnapshotDto[]>(`/tenants/${tid}/profiles/${pid}/prices`).subscribe({
        next: data => this.snapshots.set(data),
      });
    }
  }

  select(s: PriceSnapshotDto): void {
    this.selectedId.set(s.id ?? null);
    this.form.patchValue(s);
    this.saved.set(false);
    this.error.set(null);
  }

  newSnapshot(): void {
    this.selectedId.set(null);
    this.form.reset();
    this.saved.set(false);
    this.error.set(null);
  }

  save(): void {
    if (this.form.invalid) return;
    const tid = this.tenantId();
    const pid = this.profileId();
    if (!tid || !pid) return;

    this.saving.set(true);
    this.error.set(null);
    const body = this.form.getRawValue() as PriceSnapshotDto;
    const sid = this.selectedId();
    const call = sid
      ? this.api.put<PriceSnapshotDto>(`/tenants/${tid}/profiles/${pid}/prices/${sid}`, body)
      : this.api.post<PriceSnapshotDto>(`/tenants/${tid}/profiles/${pid}/prices`, body);

    call.subscribe({
      next: p => {
        this.snapshots.update(list => {
          const idx = list.findIndex(x => x.id === p.id);
          return idx >= 0 ? list.map(x => x.id === p.id ? p : x) : [...list, p];
        });
        this.selectedId.set(p.id ?? null);
        this.saving.set(false);
        this.saved.set(true);
      },
      error: () => { this.saving.set(false); this.error.set('common.error'); },
    });
  }
}
