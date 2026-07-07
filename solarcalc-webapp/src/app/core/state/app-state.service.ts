import { Injectable, inject, signal, computed, effect } from '@angular/core';
import { AuthService } from '@/core/auth/auth.service';
import { SetupStep } from '@/core/auth/setup-step.enum';

@Injectable({ providedIn: 'root' })
export class AppStateService {
  private readonly auth = inject(AuthService);

  readonly profileId = signal<string | null>(this.loadStr('profileId'));
  readonly isSetupComplete = computed(() => this.auth.setupStep() === SetupStep.COMPLETE);

  constructor() {
    effect(() => {
      if (!this.auth.token()) {
        this.setProfile(null);
      }
    });
    effect(() => {
      if (this.auth.token() && this.profileId() === null) {
        const lastUuid = this.auth.lastProfileUuid();
        if (lastUuid) {
          this.profileId.set(lastUuid);
          this.persist('profileId', lastUuid);
        }
      }
    });
  }

  setProfile(id: string | null): void {
    this.profileId.set(id);
    this.persist('profileId', id);
    if (this.auth.token()) {
      this.auth.updateLastProfile(id);
    }
  }

  private loadStr(key: string): string | null {
    return localStorage.getItem(key);
  }

  private persist(key: string, value: string | null): void {
    if (value == null) localStorage.removeItem(key);
    else localStorage.setItem(key, value);
  }
}
