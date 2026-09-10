import { Injectable, effect, inject, signal, untracked } from '@angular/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { EnergyProfile } from '@/core/api/models';
import { toProfileRequest } from '@/core/api/profile-request';

/** Holds the active profile; shared by Settings, Data and the Overview layout toggle. */
@Injectable({ providedIn: 'root' })
export class ProfileStore {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);

  readonly profile = signal<EnergyProfile | null>(null);
  private readonly version = signal(0);
  private reqId = 0;

  constructor() {
    effect(() => {
      const pid = this.state.profileId();
      this.version();
      untracked(() => this.fetch(pid));
    });
  }

  reload(): void {
    this.version.update(v => v + 1);
  }

  /** Optimistically merges a patch into the active profile and persists it. */
  save(patch: Partial<EnergyProfile>): void {
    const current = this.profile();
    const pid = this.state.profileId();
    if (!current || !pid) return;
    const next: EnergyProfile = { ...current, ...patch };
    this.profile.set(next);
    this.api.put<EnergyProfile>(`/profiles/${pid}`, toProfileRequest(next)).subscribe({
      next: r => this.profile.set(r),
      error: () => this.reload(),
    });
  }

  private fetch(pid: string | null): void {
    if (!pid) {
      this.resolveFromList();
      return;
    }
    const id = ++this.reqId;
    this.api.get<EnergyProfile>(`/profiles/${pid}`).subscribe({
      next: r => {
        if (id === this.reqId) this.profile.set(r);
      },
      error: e => {
        if (id !== this.reqId) return;
        // Stored id is stale (e.g. after switching accounts) — recover a valid profile.
        if (e.status === 404) this.resolveFromList();
        else this.profile.set(null);
      },
    });
  }

  /** Picks the user's first profile and adopts it as the active one. */
  private resolveFromList(): void {
    const id = ++this.reqId;
    this.api.get<EnergyProfile[]>('/profiles').subscribe({
      next: list => {
        if (id !== this.reqId) return;
        const chosen = list[0] ?? null;
        this.profile.set(chosen);
        if (chosen && chosen.id !== this.state.profileId()) {
          this.state.setProfile(chosen.id);
        }
      },
      error: () => {
        if (id === this.reqId) this.profile.set(null);
      },
    });
  }
}
