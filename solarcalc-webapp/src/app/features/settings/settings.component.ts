import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { ProfileStore } from '@/core/api/profile.store';
import { SummaryStore } from '@/core/api/summary.store';
import { AllocationCategory, AllocationPolicy, EnergyEfficiencyRating, EnergyProfile, HeatingReferenceType } from '@/core/api/models';

@Component({
  selector: 'app-settings',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe, RouterLink],
  templateUrl: './settings.component.html',
})
export class SettingsComponent {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);
  private readonly profileStore = inject(ProfileStore);
  private readonly summaryStore = inject(SummaryStore);

  readonly profile = this.profileStore.profile;
  readonly policy = signal<AllocationPolicy | null>(null);
  readonly heatingTypes: HeatingReferenceType[] = ['NONE', 'OIL', 'GAS'];

  readonly order = computed<AllocationCategory[]>(() => this.policy()?.priorityOrder ?? []);

  /** Server-computed over the last 12 months of all history, so it ignores the range filter. */
  readonly efficiency = computed<EnergyEfficiencyRating | null>(() => this.summaryStore.summary()?.efficiency ?? null);

  readonly heatingRefCost = computed<number | null>(() => {
    const p = this.profile();
    if (!p) return null;
    return p.heatingReferenceType === 'GAS' ? p.defaultGasReferenceCost : p.defaultOilReferenceCost;
  });

  constructor() {
    effect(() => {
      if (this.state.profileId()) this.loadPolicy();
    });
  }

  private async loadPolicy(): Promise<void> {
    const pid = this.state.profileId();
    if (!pid) return;
    try {
      const list = await firstValueFrom(this.api.get<AllocationPolicy[]>(`/profiles/${pid}/allocation-policies`));
      this.policy.set(list[0] ?? null);
    } catch {
      this.policy.set(null);
    }
  }

  move(index: number, dir: -1 | 1): void {
    const pid = this.state.profileId();
    const current = this.policy();
    if (!pid || !current) return;
    const target = index + dir;
    if (target < 0 || target >= current.priorityOrder.length) return;

    const next = [...current.priorityOrder];
    [next[index], next[target]] = [next[target], next[index]];
    const updated = { ...current, priorityOrder: next };
    this.policy.set(updated);

    this.api.put(`/profiles/${pid}/allocation-policies/${current.id}`, {
      name: current.name,
      priorityOrder: next,
    }).subscribe({
      next: () => this.summaryStore.reload(),
      error: () => this.loadPolicy(),
    });
  }

  private patch(patch: Partial<EnergyProfile>): void {
    this.profileStore.save(patch);
    this.summaryStore.reload();
  }

  private num(value: string): number | null {
    if (value.trim() === '') return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }

  setName(v: string): void { this.patch({ name: v }); }
  setWallbox(v: boolean): void { this.patch({ hasWallbox: v }); }
  setHeatPump(v: boolean): void { this.patch({ hasHeatPump: v }); }
  setHeatingType(v: HeatingReferenceType): void { this.patch({ heatingReferenceType: v }); }
  setElectricity(v: string): void { this.patch({ defaultElectricityPrice: this.num(v) }); }
  setFeedIn(v: string): void { this.patch({ defaultFeedInTariff: this.num(v) }); }
  setPetrol(v: string): void { this.patch({ defaultPetrolPrice: this.num(v) }); }
  setLiters(v: string): void { this.patch({ litersPer100km: this.num(v) }); }
  setKmPerKwh(v: string): void { this.patch({ kmPerKwh: this.num(v) }); }
  setInvest(v: string): void { this.patch({ investKosten: this.num(v) }); }
  setUsableArea(v: string): void { this.patch({ usableAreaSqm: this.num(v) }); }
  setHeatPumpScop(v: string): void { this.patch({ heatPumpScop: this.num(v) }); }

  setHeatingRefCost(v: string): void {
    const value = this.num(v);
    this.patch(this.profile()?.heatingReferenceType === 'GAS'
      ? { defaultGasReferenceCost: value }
      : { defaultOilReferenceCost: value });
  }
}
