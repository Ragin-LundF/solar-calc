import { Injectable, effect, inject, signal, untracked } from '@angular/core';
import { ApiService } from '@/core/api/api.service';
import { AppStateService } from '@/core/state/app-state.service';
import { SummaryResponse } from '@/core/api/models';

/**
 * Holds the server-computed all-history summary for the active profile. Fetched
 * once per profile (and on {@link reload}); every analysis view reads the same
 * signal and slices it with {@link FilterService}. Payback is always all-history.
 */
@Injectable({ providedIn: 'root' })
export class SummaryStore {
  private readonly api = inject(ApiService);
  private readonly state = inject(AppStateService);

  readonly summary = signal<SummaryResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  private readonly version = signal(0);
  private reqId = 0;

  constructor() {
    effect(() => {
      const pid = this.state.profileId();
      this.version();
      untracked(() => this.fetch(pid));
    });
  }

  /** Re-fetch after allocation order / profile / month data changes. */
  reload(): void {
    this.version.update(v => v + 1);
  }

  private fetch(pid: string | null): void {
    if (!pid) {
      this.summary.set(null);
      return;
    }
    const id = ++this.reqId;
    this.loading.set(true);
    this.error.set(null);
    this.api.get<SummaryResponse>(`/profiles/${pid}/summary`).subscribe({
      next: r => {
        if (id === this.reqId) {
          this.summary.set(r);
          this.loading.set(false);
        }
      },
      error: e => {
        if (id === this.reqId) {
          this.error.set(e.status === 404 ? 'solar.noData' : 'common.error');
          this.loading.set(false);
        }
      },
    });
  }
}
