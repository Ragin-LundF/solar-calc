import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { SummaryStore } from '@/core/api/summary.store';
import { ProfileStore } from '@/core/api/profile.store';
import { MonthlyInput } from '@/core/api/models';
import { MONTH_NAMES_DE, fmtKWh, monthLongLabel } from '@/shared/utils/format';

interface EntryForm {
  period: string;
  generation: string;
  feedIn: string;
  household: string;
  heatPump: string;
  wallbox: string;
  refPrice: string;
}

const EMPTY_FORM: EntryForm = { period: '', generation: '', feedIn: '', household: '', heatPump: '', wallbox: '', refPrice: '' };

@Component({
  selector: 'app-data',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, TranslatePipe],
  templateUrl: './data.component.html',
})
export class DataComponent {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly summaryStore = inject(SummaryStore);
  private readonly profileStore = inject(ProfileStore);

  readonly fmtKWh = fmtKWh;
  readonly monthLongLabel = monthLongLabel;
  readonly monthNames = MONTH_NAMES_DE;

  readonly form = signal<EntryForm>({ ...EMPTY_FORM });
  readonly months = signal<MonthlyInput[]>([]);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly dist = signal<number[]>(Array(12).fill(0));
  readonly distSum = computed(() => this.dist().reduce((a, b) => a + b, 0));
  readonly distValid = computed(() => this.distSum() === 100);

  readonly rows = computed(() => [...this.months()].sort((a, b) => b.period.localeCompare(a.period)));
  readonly refPlaceholder = computed(() => String(this.profileStore.profile()?.defaultElectricityPrice ?? ''));

  constructor() {
    effect(() => {
      const d = this.profileStore.profile()?.heatingMonthlyDistribution;
      if (d && d.length === 12) this.dist.set([...d]);
    });
    effect(() => {
      if (this.state.profileId()) this.loadMonths();
    });
  }

  private async loadMonths(): Promise<void> {
    const pid = this.state.profileId();
    if (!pid) return;
    try {
      this.months.set(await firstValueFrom(this.api.get<MonthlyInput[]>(`/profiles/${pid}/monthly-inputs`)));
    } catch {
      this.months.set([]);
    }
  }

  patch(field: keyof EntryForm, value: string): void {
    this.form.update(f => ({ ...f, [field]: value }));
  }

  setDist(index: number, value: number): void {
    this.dist.update(d => d.map((v, i) => (i === index ? (Number.isFinite(value) ? value : 0) : v)));
  }

  private num(value: string | number | null): number | null {
    if (value === null || value === '') return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }

  async submit(): Promise<void> {
    const pid = this.state.profileId();
    const f = this.form();
    if (!pid || !f.period) return;
    this.saving.set(true);
    this.error.set(null);

    const body = {
      period: f.period,
      generationKwh: this.num(f.generation) ?? 0,
      feedInKwh: this.num(f.feedIn),
      householdConsumptionKwh: this.num(f.household),
      heatPumpConsumptionKwh: this.num(f.heatPump),
      wallboxConsumptionKwh: this.num(f.wallbox),
      referencePrice: this.num(f.refPrice),
    };

    const existing = this.months().find(m => m.period === f.period);
    const call = existing
      ? this.api.put(`/profiles/${pid}/monthly-inputs/${existing.id}`, body)
      : this.api.post(`/profiles/${pid}/monthly-inputs`, body);

    try {
      await firstValueFrom(call);
      this.form.set({ ...EMPTY_FORM });
      await this.loadMonths();
      this.summaryStore.reload();
    } catch {
      this.error.set('common.error');
    } finally {
      this.saving.set(false);
    }
  }

  saveDistribution(): void {
    if (!this.distValid()) return;
    this.profileStore.save({ heatingMonthlyDistribution: this.dist() });
    this.summaryStore.reload();
  }
}
