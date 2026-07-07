import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ZardButtonComponent } from '@/shared/components/button';
import { ZardInputDirective } from '@/shared/components/input';
import { ZardCardComponent } from '@/shared/components/card';

interface MonthlyInputDto {
  id: string;
  period: string;
  generationKwh: number;
  feedInKwh: number;
  householdConsumptionKwh?: number;
  heatPumpConsumptionKwh?: number;
  wallboxConsumptionKwh?: number;
}

@Component({
  selector: 'app-monthly-input',
  imports: [ReactiveFormsModule, RouterLink, TranslatePipe, ZardButtonComponent, ZardInputDirective, ZardCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './monthly-input.component.html',
})
export class MonthlyInputComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly fb = inject(FormBuilder);

  readonly profileId = this.state.profileId;
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly error = signal<string | null>(null);
  readonly inputs = signal<MonthlyInputDto[]>([]);
  readonly selectedInputId = signal<string | null>(null);

  readonly periodPattern = /^\d{4}-(0[1-9]|1[0-2])$/;

  readonly form = this.fb.group({
    period: ['', [Validators.required, Validators.pattern(this.periodPattern)]],
    generationKwh: [null as number | null, [Validators.required, Validators.min(0)]],
    feedInKwh: [null as number | null, [Validators.required, Validators.min(0)]],
    householdConsumptionKwh: [null as number | null, Validators.min(0)],
    heatPumpConsumptionKwh: [null as number | null, Validators.min(0)],
    wallboxConsumptionKwh: [null as number | null, Validators.min(0)],
  });

  ngOnInit(): void {
    const pid = this.profileId();
    if (pid) this.loadInputs(pid);
  }

  private loadInputs(profileUuid: string): void {
    this.api.get<MonthlyInputDto[]>(`/profiles/${profileUuid}/monthly-inputs`).subscribe({
      next: data => this.inputs.set(data),
    });
  }

  selectInput(input: MonthlyInputDto): void {
    this.selectedInputId.set(input.id);
    this.form.patchValue(input);
    this.saved.set(false);
    this.error.set(null);
  }

  newInput(): void {
    this.selectedInputId.set(null);
    this.form.reset();
    this.saved.set(false);
    this.error.set(null);
  }

  save(): void {
    if (this.form.invalid) return;
    const pid = this.profileId();
    if (!pid) return;

    this.saving.set(true);
    this.error.set(null);
    const body = this.form.getRawValue() as MonthlyInputDto;
    const editId = this.selectedInputId();

    const request = editId
      ? this.api.put<MonthlyInputDto>(`/profiles/${pid}/monthly-inputs/${editId}`, body)
      : this.api.post<MonthlyInputDto>(`/profiles/${pid}/monthly-inputs`, body);

    request.subscribe({
      next: saved => {
        this.inputs.update(list => {
          const idx = list.findIndex(x => x.id === saved.id);
          return idx >= 0 ? list.map(x => x.id === saved.id ? saved : x) : [...list, saved];
        });
        this.selectedInputId.set(saved.id);
        this.saving.set(false);
        this.saved.set(true);
      },
      error: err => {
        this.saving.set(false);
        const code = err?.error?.code;
        this.error.set(code === 'MONTHLY_INPUT_FEED_IN_EXCEEDS_GENERATION'
          ? 'validation.feedInExceedsGeneration'
          : 'common.error');
      },
    });
  }
}
