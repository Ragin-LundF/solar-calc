import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AuthService } from '@/core/auth/auth.service';
import { SetupStep } from '@/core/auth/setup-step.enum';
import { AppStateService } from '@/core/state/app-state.service';
import { EnergyProfile, HeatingReferenceType } from '@/core/api/models';
import { toProfileRequest } from '@/core/api/profile-request';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardInputDirective } from '@/shared/components/input';
import { ZardCardComponent } from '@/shared/components/card';
import { ZardBadgeComponent } from '@/shared/components/badge';

type AllocationCategory = 'HOUSEHOLD' | 'HEAT_PUMP' | 'WALLBOX';

interface AllocationPolicyDto {
  id?: number;
  priorityOrder: AllocationCategory[];
}

@Component({
  selector: 'app-setup-wizard',
  imports: [ReactiveFormsModule, TranslatePipe, ZardButtonComponent, ZardInputDirective, ZardCardComponent, ZardBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './setup-wizard.component.html',
})
export class SetupWizardComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly state = inject(AppStateService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly currentStep = signal(1);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly heatingTypes = ['NONE', 'OIL', 'GAS'] as const;
  readonly allCategories: AllocationCategory[] = ['HOUSEHOLD', 'HEAT_PUMP', 'WALLBOX'];
  readonly priorityOrder = signal<AllocationCategory[]>([...this.allCategories]);
  readonly policyId = signal<number | null>(null);

  /**
   * The profile as the server last returned it. Both save steps edit a subset of the fields,
   * so they merge onto this before writing — otherwise the untouched fields would be cleared.
   */
  private readonly loadedProfile = signal<EnergyProfile | null>(null);

  readonly profileForm = this.fb.group({
    name: ['', Validators.required],
    hasWallbox: [false],
    hasHeatPump: [false],
    heatingReferenceType: ['NONE'],
    defaultElectricityPrice: [null as number | null],
    defaultFeedInTariff: [null as number | null],
    defaultPetrolPrice: [null as number | null],
  });

  readonly pricesForm = this.fb.group({
    defaultElectricityPrice: [null as number | null, [Validators.required, Validators.min(0)]],
    defaultFeedInTariff: [null as number | null, [Validators.required, Validators.min(0)]],
    defaultPetrolPrice: [null as number | null, Validators.min(0)],
    defaultOilReferenceCost: [null as number | null, Validators.min(0)],
    defaultGasReferenceCost: [null as number | null, Validators.min(0)],
  });

  readonly totalSteps = 4;

  ngOnInit(): void {
    const step = this.auth.setupStep();
    if (step === SetupStep.COMPLETE) {
      this.router.navigate(['/dashboard']);
      return;
    }

    if (step >= SetupStep.ALLOCATION_SET) {
      this.currentStep.set(3);
      this.loadExistingProfile();
    } else if (step >= SetupStep.PROFILE_CREATED) {
      this.currentStep.set(2);
      this.loadExistingProfile();
    }
  }

  private loadExistingProfile(): void {
    const pid = this.state.profileId();
    if (!pid) return;
    this.api.get<EnergyProfile>(`/profiles/${pid}`).subscribe({
      next: p => {
        this.adoptProfile(p);
        if (this.currentStep() >= 2) {
          this.loadAllocationPolicy(pid);
        }
      },
    });
  }

  private loadAllocationPolicy(pid: string): void {
    this.api.get<AllocationPolicyDto[]>(`/profiles/${pid}/allocation-policies`).subscribe({
      next: policies => {
        const p = policies[0];
        if (p) {
          this.policyId.set(p.id ?? null);
          this.priorityOrder.set(p.priorityOrder);
        }
      },
    });
  }

  /** Copies a server profile into both forms and remembers it for the next merge. */
  private adoptProfile(profile: EnergyProfile): void {
    this.loadedProfile.set(profile);
    this.profileForm.patchValue(profile);
    this.pricesForm.patchValue(profile);
  }

  /** The loaded profile with the given edits applied, ready to send back in full. */
  private mergedProfile(edits: Partial<EnergyProfile>): EnergyProfile {
    return { ...this.loadedProfile(), ...edits } as EnergyProfile;
  }

  saveProfile(): void {
    if (this.profileForm.invalid) return;
    this.saving.set(true);
    this.error.set(null);

    const form = this.profileForm.getRawValue();
    const body = toProfileRequest(this.mergedProfile({
      ...form,
      heatingReferenceType: form.heatingReferenceType as HeatingReferenceType,
    } as Partial<EnergyProfile>));

    const pid = this.state.profileId();
    const call = pid
      ? this.api.put<EnergyProfile>(`/profiles/${pid}`, body)
      : this.api.post<EnergyProfile>('/profiles', body);

    call.subscribe({
      next: p => {
        this.adoptProfile(p);
        this.state.setProfile(p.id ?? null);
        this.saving.set(false);
        this.currentStep.set(2);
        this.auth.updateSetupStep(SetupStep.PROFILE_CREATED);
      },
      error: () => { this.saving.set(false); this.error.set('common.error'); },
    });
  }

  saveAllocation(): void {
    const pid = this.state.profileId();
    if (!pid) return;

    this.saving.set(true);
    this.error.set(null);
    const body: AllocationPolicyDto = { priorityOrder: this.priorityOrder() };
    const id = this.policyId();
    const call = id
      ? this.api.put<AllocationPolicyDto>(`/profiles/${pid}/allocation-policies/${id}`, body)
      : this.api.post<AllocationPolicyDto>(`/profiles/${pid}/allocation-policies`, body);

    call.subscribe({
      next: p => {
        this.policyId.set(p.id ?? null);
        this.saving.set(false);
        this.currentStep.set(3);
        this.auth.updateSetupStep(SetupStep.ALLOCATION_SET);
      },
      error: () => { this.saving.set(false); this.error.set('common.error'); },
    });
  }

  /**
   * Writes the prices onto the profile itself. They used to go to a price_snapshot, which the
   * price resolver prefers over the profile defaults — so a snapshot here would have shadowed
   * the very fields the Settings page edits.
   */
  savePrices(): void {
    if (this.pricesForm.invalid) return;
    const pid = this.state.profileId();
    if (!pid) return;

    this.saving.set(true);
    this.error.set(null);
    const body = toProfileRequest(this.mergedProfile(this.pricesForm.getRawValue()));

    this.api.put<EnergyProfile>(`/profiles/${pid}`, body).subscribe({
      next: p => {
        this.adoptProfile(p);
        this.saving.set(false);
        this.finishSetup();
      },
      error: () => { this.saving.set(false); this.error.set('common.error'); },
    });
  }

  skipPrices(): void {
    this.finishSetup();
  }

  private finishSetup(): void {
    this.auth.updateSetupStep(SetupStep.COMPLETE);
    this.currentStep.set(4);
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  goBack(): void {
    this.currentStep.update(s => Math.max(1, s - 1));
  }

  moveUp(index: number): void {
    if (index === 0) return;
    this.priorityOrder.update(list => {
      const arr = [...list];
      [arr[index - 1], arr[index]] = [arr[index], arr[index - 1]];
      return arr;
    });
  }

  moveDown(index: number): void {
    const list = this.priorityOrder();
    if (index === list.length - 1) return;
    this.priorityOrder.update(arr => {
      const copy = [...arr];
      [copy[index + 1], copy[index]] = [copy[index], copy[index + 1]];
      return copy;
    });
  }
}
