import { Injectable, inject, signal, effect } from '@angular/core';
import { AuthService } from '@/core/auth/auth.service';

@Injectable({ providedIn: 'root' })
export class AppStateService {
  private readonly auth = inject(AuthService);

  readonly profileId = signal<string | null>(this.loadStr('profileId'));

  constructor() {
    effect(() => {
      if (!this.auth.token()) {
        this.setProfile(null);
      }
    });
  }

  setProfile(id: string | null): void {
    this.profileId.set(id);
    this.persist('profileId', id);
  }

  private loadStr(key: string): string | null {
    return localStorage.getItem(key);
  }

  private persist(key: string, value: string | null): void {
    if (value == null) localStorage.removeItem(key);
    else localStorage.setItem(key, value);
  }
}
