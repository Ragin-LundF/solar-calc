import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideTrash2 } from '@ng-icons/lucide';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardInputDirective } from '@/shared/components/input';
import { ZardCardComponent } from '@/shared/components/card';

interface ProfileDto {
  id?: string;
  name: string;
}

@Component({
  selector: 'app-profile-settings',
  imports: [ReactiveFormsModule, TranslatePipe, ZardButtonComponent, ZardInputDirective, ZardCardComponent, NgIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './profile-settings.component.html',
  viewProviders: [provideIcons({ lucideTrash2 })],
})
export class ProfileSettingsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly fb = inject(FormBuilder);

  readonly profileId = this.state.profileId;
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly profiles = signal<ProfileDto[]>([]);

  readonly heatingTypes = ['NONE', 'OIL', 'GAS'] as const;

  readonly form = this.fb.group({
    name: ['', Validators.required],
    hasWallbox: [false],
    hasHeatPump: [false],
    heatingReferenceType: ['NONE'],
    defaultElectricityPrice: [null as number | null],
    defaultFeedInTariff: [null as number | null],
    defaultPetrolPrice: [null as number | null],
  });

  ngOnInit(): void {
    this.loadProfiles();
    const pid = this.profileId();
    if (pid) this.loadProfile(pid);
  }

  private loadProfiles(): void {
    this.api.get<ProfileDto[]>('/profiles').subscribe({
      next: p => this.profiles.set(p),
    });
  }

  private loadProfile(profileUuid: string): void {
    this.api.get<ProfileDto>(`/profiles/${profileUuid}`).subscribe({
      next: p => this.form.patchValue(p),
    });
  }

  selectProfile(id: string): void {
    this.state.setProfile(id);
    this.loadProfile(id);
    this.saved.set(false);
    this.error.set(null);
  }

  newProfile(): void {
    this.state.setProfile(null);
    this.form.reset();
    this.saved.set(false);
    this.error.set(null);
  }

  deleteProfile(id: string, name: string): void {
    const confirmed = window.confirm(`Delete profile "${name}"? This will permanently remove all associated data.`);
    if (!confirmed) return;

    this.api.delete(`/profiles/${id}`).subscribe({
      next: () => {
        this.profiles.update(list => list.filter(p => p.id !== id));
        if (this.profileId() === id) {
          this.state.setProfile(null);
          this.form.reset();
        }
      },
      error: () => this.error.set('common.error'),
    });
  }

  save(): void {
    if (this.form.invalid) return;

    this.saving.set(true);
    this.error.set(null);
    const body = this.form.getRawValue() as ProfileDto;
    const pid = this.profileId();
    const call = pid
      ? this.api.put<ProfileDto>(`/profiles/${pid}`, body)
      : this.api.post<ProfileDto>('/profiles', body);

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
