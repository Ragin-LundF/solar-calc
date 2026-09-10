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
import {PriceSnapshot} from '@/core/api/models';
import {fmtEUR, fmtEURperKwh, monthLongLabel} from '@/shared/utils/format';

interface EntryForm {
  validFrom: string;
  electricityPrice: string;
  feedInTariff: string;
  petrolPrice: string;
  oilReferenceCost: string;
  gasReferenceCost: string;
}

const EMPTY_FORM: EntryForm = {
  validFrom: '', electricityPrice: '', feedInTariff: '', petrolPrice: '', oilReferenceCost: '', gasReferenceCost: '',
};

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
  }

  patch(field: keyof EntryForm, value: string): void {
    this.form.update(f => ({ ...f, [field]: value }));
  }

  startEdit(entry: PriceSnapshot): void {
    const str = (n: number | null): string => (n === null || n === undefined ? '' : String(n));
    this.form.set({
      validFrom: entry.validFrom,
      electricityPrice: str(entry.electricityPrice),
      feedInTariff: str(entry.feedInTariff),
      petrolPrice: str(entry.petrolPrice),
      oilReferenceCost: str(entry.oilReferenceCost),
      gasReferenceCost: str(entry.gasReferenceCost),
    });
    this.editingId.set(entry.id);
    this.error.set(null);
  }

  cancelEdit(): void {
    this.form.set({ ...EMPTY_FORM });
    this.editingId.set(null);
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
    return [f.electricityPrice, f.feedInTariff, f.petrolPrice, f.oilReferenceCost, f.gasReferenceCost]
      .some(v => v.trim() !== '');
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
