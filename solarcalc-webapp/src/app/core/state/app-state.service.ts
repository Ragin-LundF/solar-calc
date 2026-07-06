import { Injectable, inject, signal } from '@angular/core';
import { AuthService } from '@/core/auth/auth.service';

@Injectable({ providedIn: 'root' })
export class AppStateService {
  private readonly auth = inject(AuthService);

  readonly profileId = signal<number | null>(this.loadNum('profileId'));

  setProfile(id: number | null): void {
    this.profileId.set(id);
    this.persist('profileId', id);
  }

  private loadNum(key: string): number | null {
    const v = localStorage.getItem(key);
    return v ? Number(v) : null;
  }

  private persist(key: string, value: number | null): void {
    if (value == null) localStorage.removeItem(key);
    else localStorage.setItem(key, String(value));
  }
}
