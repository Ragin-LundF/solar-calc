import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardInputDirective } from '@/shared/components/input';
import { ZardCardComponent } from '@/shared/components/card';

interface ProfileDto {
  id?: number;
  name: string;
  hasWallbox: boolean;
  hasHeatPump: boolean;
  heatingReferenceType: 'NONE' | 'OIL' | 'GAS';
  defaultElectricityPrice?: number;
  defaultFeedInTariff?: number;
  defaultPetrolPrice?: number;
}

@Component({
  selector: 'app-profile-settings',
  imports: [ReactiveFormsModule, TranslatePipe, ZardButtonComponent, ZardInputDirective, ZardCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profile-settings.component.html',
})
export class ProfileSettingsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly fb = inject(FormBuilder);

  readonly tenantId = this.state.tenantId;
  readonly profileId = this.state.profileId;
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly profiles = signal<ProfileDto[]>([]);

  readonly form = this.fb.group({
    name: ['', Validators.required],
    hasWallbox: [false],
    hasHeatPump: [false],
    heatingReferenceType: ['NONE' as ProfileDto['heatingReferenceType']],
    defaultElectricityPrice: [null as number | null],
    defaultFeedInTariff: [null as number | null],
    defaultPetrolPrice: [null as number | null],
  });

  readonly heatingTypes: ProfileDto['heatingReferenceType'][] = ['NONE', 'OIL', 'GAS'];

  ngOnInit(): void {
    const tid = this.tenantId();
    if (tid) this.loadProfiles(tid);
    const pid = this.profileId();
    if (pid && tid) this.loadProfile(tid, pid);
  }

  private loadProfiles(tenantId: number): void {
    this.api.get<ProfileDto[]>(`/tenants/${tenantId}/profiles`).subscribe({
      next: p => this.profiles.set(p),
    });
  }

  private loadProfile(tenantId: number, profileId: number): void {
    this.api.get<ProfileDto>(`/tenants/${tenantId}/profiles/${profileId}`).subscribe({
      next: p => this.form.patchValue(p),
    });
  }

  selectProfile(id: number): void {
    this.state.setProfile(id);
    const tid = this.tenantId()!;
    this.loadProfile(tid, id);
    this.saved.set(false);
    this.error.set(null);
  }

  newProfile(): void {
    this.state.setProfile(null);
    this.form.reset({ heatingReferenceType: 'NONE', hasWallbox: false, hasHeatPump: false });
    this.saved.set(false);
    this.error.set(null);
  }

  save(): void {
    if (this.form.invalid) return;
    const tid = this.tenantId();
    if (!tid) return;

    this.saving.set(true);
    this.error.set(null);
    const body = this.form.getRawValue() as ProfileDto;
    const pid = this.profileId();
    const call = pid
      ? this.api.put<ProfileDto>(`/tenants/${tid}/profiles/${pid}`, body)
      : this.api.post<ProfileDto>(`/tenants/${tid}/profiles`, body);

    call.subscribe({
      next: p => {
        this.state.setProfile(p.id ?? null);
        this.profiles.update(list => {
          const idx = list.findIndex(x => x.id === p.id);
          return idx >= 0 ? list.map(x => x.id === p.id ? p : x) : [...list, p];
        });
        this.saving.set(false);
        this.saved.set(true);
      },
      error: () => {
        this.saving.set(false);
        this.error.set('common.error');
      },
    });
  }
}
