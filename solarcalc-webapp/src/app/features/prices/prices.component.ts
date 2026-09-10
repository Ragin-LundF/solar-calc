import {ChangeDetectionStrategy, Component, computed, effect, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgIcon, provideIcons} from '@ng-icons/core';
import {lucidePencil, lucideTrash2} from '@ng-icons/lucide';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {firstValueFrom} from 'rxjs';
import {ApiService} from '@/core/api/api.service';
import {AppStateService} from '@/core/state/app-state.service';
import {ProfileStore} from '@/core/api/profile.store';
import {SummaryStore} from '@/core/api/summary.store';
import {HeatingReferenceType, PriceSnapshot} from '@/core/api/models';
import {fmtEUR, fmtEURperKwh, monthLongLabel} from '@/shared/utils/format';

interface EntryForm {
  validFrom: string;
  electricityPrice: string;
  feedInTariff: string;
  petrolPrice: string;
  heatingReferenceType: string;
  oilReferenceCost: string;
  gasReferenceCost: string;
}

const EMPTY_FORM: EntryForm = {
  validFrom: '', electricityPrice: '', feedInTariff: '', petrolPrice: '',
  heatingReferenceType: '', oilReferenceCost: '', gasReferenceCost: '',
};

/** Every field of an entry except the start month, which is never carried over. */
const PRICE_FIELDS = [
  'electricityPrice', 'feedInTariff', 'petrolPrice',
  'heatingReferenceType', 'oilReferenceCost', 'gasReferenceCost',
] as const;

type PriceField = (typeof PRICE_FIELDS)[number];

/** Null and absent both mean "not set"; the server omits null fields entirely. */
const str = (v: string | number | null | undefined): string => (v == null ? '' : String(v));

@Component({
  selector: 'app-prices',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, TranslatePipe, NgIcon],
  viewProviders: [provideIcons({ lucidePencil, lucideTrash2 })],
  templateUrl: './prices.component.html',
})
export class PricesComponent {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly profileStore = inject(ProfileStore);
  private readonly summaryStore = inject(SummaryStore);
  private readonly translate = inject(TranslateService);

  /** Empty means "unchanged": the fuel carries over from an earlier entry. */
  readonly heatingTypes: HeatingReferenceType[] = ['NONE', 'OIL', 'GAS'];
  readonly fmtEUR = fmtEUR;
  readonly fmtEURperKwh = fmtEURperKwh;
  readonly monthLongLabel = monthLongLabel;

  readonly form = signal<EntryForm>({ ...EMPTY_FORM });
  readonly editingId = signal<number | null>(null);
  readonly entries = signal<PriceSnapshot[]>([]);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly profile = this.profileStore.profile;

  /** Newest start month first, matching the order the server returns. */
  readonly rows = computed(() => [...this.entries()].sort((a, b) => b.validFrom.localeCompare(a.validFrom)));

  /** The entry in force today, so the table can point at the one that currently applies. */
  readonly currentId = computed(() => {
    const today = new Date().toISOString().slice(0, 7);
    return this.rows().find(e => e.validFrom <= today)?.id ?? null;
  });

  /**
   * The prices actually in force today. A null field in a snapshot means "unchanged", so the
   * timeline is replayed oldest first and each field keeps the last value that was actually set —
   * the newest entry alone would leave the inherited fields looking empty.
   */
  private readonly inForceToday = computed<Partial<Record<PriceField, string>>>(() => {
    const today = new Date().toISOString().slice(0, 7);
    const upToToday = this.entries()
      .filter(e => e.validFrom <= today)
      .sort((a, b) => a.validFrom.localeCompare(b.validFrom));

    const resolved: Partial<Record<PriceField, string>> = {};
    for (const entry of upToToday) {
      for (const field of PRICE_FIELDS) {
        if (entry[field] != null) resolved[field] = String(entry[field]);
      }
    }
    return resolved;
  });

  /**
   * A new entry opens as a copy of the prices in force today, so only the values that actually
   * changed have to be touched; clearing a field still stores it as null. The start month is
   * deliberately left empty — it is the one value that must be chosen per entry.
   */
  private newEntryForm(): EntryForm {
    return { ...EMPTY_FORM, ...this.inForceToday() };
  }

  constructor() {
    effect(() => {
      if (this.state.profileId()) this.load();
    });
  }

  private async load(): Promise<void> {
    const pid = this.state.profileId();
    if (!pid) return;
    try {
      this.entries.set(await firstValueFrom(this.api.get<PriceSnapshot[]>(`/profiles/${pid}/prices`)));
    } catch {
      this.entries.set([]);
    }
    // Reseed the new-entry form from the timeline that just arrived, unless an edit is open.
    if (this.editingId() === null) this.form.set(this.newEntryForm());
  }

  /**
   * `ngModelChange` on a `type="number"` input emits a number (or null when cleared), never a
   * string, so every value is normalised here before it reaches the string-typed form.
   */
  patch(field: keyof EntryForm, value: string | number | null): void {
    this.form.update(f => ({ ...f, [field]: value === null ? '' : String(value) }));
  }

  startEdit(entry: PriceSnapshot): void {
    this.form.set({
      validFrom: entry.validFrom,
      electricityPrice: str(entry.electricityPrice),
      feedInTariff: str(entry.feedInTariff),
      petrolPrice: str(entry.petrolPrice),
      heatingReferenceType: str(entry.heatingReferenceType),
      oilReferenceCost: str(entry.oilReferenceCost),
      gasReferenceCost: str(entry.gasReferenceCost),
    });
    this.editingId.set(entry.id);
    this.error.set(null);
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.set(this.newEntryForm());
    this.error.set(null);
  }

  async remove(entry: PriceSnapshot): Promise<void> {
    const pid = this.state.profileId();
    if (!pid || !window.confirm(this.translate.instant('solar.prices.confirmDelete'))) return;
    try {
      await firstValueFrom(this.api.delete(`/profiles/${pid}/prices/${entry.id}`));
      if (this.editingId() === entry.id) this.cancelEdit();
      await this.load();
      this.summaryStore.reload();
    } catch {
      this.error.set('common.error');
    }
  }

  private num(value: string): number | null {
    if (value.trim() === '') return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }

  readonly formHasPrice = computed(() => {
    const f = this.form();
    return [
      f.electricityPrice, f.feedInTariff, f.petrolPrice,
      f.heatingReferenceType, f.oilReferenceCost, f.gasReferenceCost,
    ].some(v => v.trim() !== '');
  });

  async submit(): Promise<void> {
    const pid = this.state.profileId();
    const f = this.form();
    if (!pid || !f.validFrom || !this.formHasPrice()) return;
    this.saving.set(true);
    this.error.set(null);

    const body = {
      validFrom: f.validFrom,
      electricityPrice: this.num(f.electricityPrice),
      feedInTariff: this.num(f.feedInTariff),
      petrolPrice: this.num(f.petrolPrice),
      heatingReferenceType: f.heatingReferenceType === '' ? null : f.heatingReferenceType,
      oilReferenceCost: this.num(f.oilReferenceCost),
      gasReferenceCost: this.num(f.gasReferenceCost),
    };

    const id = this.editingId();
    const call = id
      ? this.api.put(`/profiles/${pid}/prices/${id}`, body)
      : this.api.post(`/profiles/${pid}/prices`, body);

    try {
      await firstValueFrom(call);
      this.cancelEdit();
      await this.load();
      this.summaryStore.reload();
    } catch (e: unknown) {
      // 409 is the duplicate start month, by far the most likely mistake here.
      const status = (e as { status?: number }).status;
      this.error.set(status === 409 ? 'solar.prices.duplicate' : 'common.error');
    } finally {
      this.saving.set(false);
    }
  }
}
