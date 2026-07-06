import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AppStateService {
  readonly tenantId = signal<number | null>(this.loadNum('tenantId'));
  readonly profileId = signal<number | null>(this.loadNum('profileId'));

  setTenant(id: number | null): void {
    this.tenantId.set(id);
    this.profileId.set(null);
    this.persist('tenantId', id);
    localStorage.removeItem('profileId');
  }

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
