import {ChangeDetectionStrategy, Component, computed, effect, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgIcon, provideIcons} from '@ng-icons/core';
import {lucidePencil, lucideTrash2} from '@ng-icons/lucide';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {firstValueFrom} from 'rxjs';
import {ApiService} from '@/core/api/api.service';
import {AppStateService} from '@/core/state/app-state.service';
import {SummaryStore} from '@/core/api/summary.store';
import {ProfileStore} from '@/core/api/profile.store';
import {EffectivePrices, MonthlyInput} from '@/core/api/models';
import {fmtEURperKwh, fmtKWh, monthLongLabel, monthNames} from '@/shared/utils/format';

interface EntryForm {
  period: string;
  generation: string;
  feedIn: string;
  household: string;
  heatPump: string;
  wallbox: string;
  electricityPrice: string;
  feedInTariff: string;
}

const EMPTY_FORM: EntryForm = {
  period: '', generation: '', feedIn: '', household: '', heatPump: '', wallbox: '',
  electricityPrice: '', feedInTariff: '',
};

@Component({
  selector: 'app-data',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, TranslatePipe, NgIcon],
  viewProviders: [provideIcons({ lucidePencil, lucideTrash2 })],
  templateUrl: './data.component.html',
})
export class DataComponent {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly summaryStore = inject(SummaryStore);
  private readonly profileStore = inject(ProfileStore);
  private readonly translate = inject(TranslateService);

  readonly fmtKWh = fmtKWh;
  readonly fmtEURperKwh = fmtEURperKwh;
  readonly monthLongLabel = monthLongLabel;
  readonly monthNames = monthNames;

  readonly form = signal<EntryForm>({ ...EMPTY_FORM });
  readonly editingId = signal<string | null>(null);
  readonly months = signal<MonthlyInput[]>([]);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  /** Guards against an out-of-order effective-price response overwriting a newer one. */
  private pricesReqId = 0;

  readonly dist = signal<number[]>(Array(12).fill(0));
  readonly distSum = computed(() => this.dist().reduce((a, b) => a + b, 0));
  readonly distValid = computed(() => this.distSum() === 100);

  readonly rows = computed(() => [...this.months()].sort((a, b) => b.period.localeCompare(a.period)));

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

  /**
   * `ngModelChange` on a `type="number"` input emits a number (or null when cleared), never a
   * string, so every value is normalised here before it reaches the string-typed form.
   */
  patch(field: keyof EntryForm, value: string | number | null): void {
    const next = value === null ? '' : String(value);
    this.form.update(f => ({ ...f, [field]: next }));
    // Picking a month decides which contract prices apply, so re-seed the inherited ones.
    if (field === 'period') this.prefillFromPrices(next);
  }

  /** Clears a field so the month inherits the price from the timeline again. */
  clear(field: keyof EntryForm): void {
    this.form.update(f => ({ ...f, [field]: '' }));
  }

  /**
   * Seeds the feed-in tariff with the price the server would use for this month. Only the feed-in
   * tariff: the electricity price is the dynamic price actually paid, and prefilling it with the
   * contract price would make every month's tariff comparison come out as exactly zero.
   */
  private async prefillFromPrices(period: string, force = false): Promise<void> {
    const pid = this.state.profileId();
    if (!pid || !period) return;
    const id = ++this.pricesReqId;
    try {
      const effective = await firstValueFrom(
        this.api.get<EffectivePrices>(`/profiles/${pid}/prices/effective?period=${period}`),
      );
      if (id !== this.pricesReqId) return;
      if (!force && this.form().feedInTariff.trim() !== '') return;
      const tariff = effective.feedInTariff;
      this.form.update(f => ({ ...f, feedInTariff: tariff == null ? '' : String(tariff) }));
    } catch {
      // A missing price is not an error worth interrupting data entry for.
    }
  }

  startEdit(m: MonthlyInput): void {
    const str = (n: number | null): string => (n === null || n === undefined ? '' : String(n));
    this.form.set({
      period: m.period,
      generation: str(m.generationKwh),
      feedIn: str(m.feedInKwh),
      household: str(m.householdConsumptionKwh),
      heatPump: str(m.heatPumpConsumptionKwh),
      wallbox: str(m.wallboxConsumptionKwh),
      electricityPrice: str(m.electricityPriceOverride),
      feedInTariff: str(m.feedInTariffOverride),
    });
    this.editingId.set(m.id);
    this.error.set(null);
    // A month that never stored a tariff inherits one; show which.
    if (m.feedInTariffOverride === null || m.feedInTariffOverride === undefined) {
      this.prefillFromPrices(m.period);
    }
  }

  cancelEdit(): void {
    this.form.set({ ...EMPTY_FORM });
    this.editingId.set(null);
  }

  async remove(m: MonthlyInput): Promise<void> {
    const pid = this.state.profileId();
    if (!pid || !window.confirm(this.translate.instant('solar.data.confirmDelete'))) return;
    try {
      await firstValueFrom(this.api.delete(`/profiles/${pid}/monthly-inputs/${m.id}`));
      if (this.editingId() === m.id) this.cancelEdit();
      await this.loadMonths();
      this.summaryStore.reload();
    } catch {
      this.error.set('common.error');
    }
  }

  setDist(index: number, value: number): void {
    this.dist.update(d => d.map((v, i) => (i === index ? (Number.isFinite(value) ? value : 0) : v)));
  }

  private num(value: string | number | null | undefined): number | null {
    if (value == null || value === '') return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }

  async submit(): Promise<void> {
    const pid = this.state.profileId();
    const f = this.form();
    if (!pid || !f.period) return;
    this.saving.set(true);
    this.error.set(null);

    const targetId = this.editingId() ?? this.months().find(m => m.period === f.period)?.id;
    // The server replaces every override on write, so the ones this form does not edit
    // have to be sent back unchanged or they would be wiped.
    const existing = this.months().find(m => m.id === targetId);

    const body = {
      period: f.period,
      generationKwh: this.num(f.generation) ?? 0,
      feedInKwh: this.num(f.feedIn),
      householdConsumptionKwh: this.num(f.household),
      heatPumpConsumptionKwh: this.num(f.heatPump),
      wallboxConsumptionKwh: this.num(f.wallbox),
      electricityPriceOverride: this.num(f.electricityPrice),
      feedInTariffOverride: this.num(f.feedInTariff),
      petrolPriceOverride: existing?.petrolPriceOverride ?? null,
      heatingReferenceCostOverride: existing?.heatingReferenceCostOverride ?? null,
    };

    const call = targetId
      ? this.api.put(`/profiles/${pid}/monthly-inputs/${targetId}`, body)
      : this.api.post(`/profiles/${pid}/monthly-inputs`, body);

    try {
      await firstValueFrom(call);
      this.form.set({ ...EMPTY_FORM });
      this.editingId.set(null);
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
