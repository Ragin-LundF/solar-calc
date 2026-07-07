import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Router } from '@angular/router';
import { SetupStep } from './setup-step.enum';

export interface AuthResponse {
  token: string;
  username: string;
  expiresInSeconds: number;
  setupStep: number;
  lastProfileUuid: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly token = signal<string | null>(this.load('token'));
  readonly username = signal<string | null>(this.load('username'));
  readonly setupStep = signal<SetupStep>(this.loadNum('setupStep') as SetupStep ?? SetupStep.NOT_STARTED);
  readonly lastProfileUuid = signal<string | null>(this.load('lastProfileUuid'));

  get isAuthenticated(): boolean {
    return this.token() !== null;
  }

  async login(username: string, password: string): Promise<AuthResponse> {
    const res = await firstValueFrom(
      this.http.post<AuthResponse>('/api/v1/auth/login', { username, password }),
    );
    this.setSession(res);
    return res;
  }

  async register(username: string, password: string): Promise<AuthResponse> {
    const res = await firstValueFrom(
      this.http.post<AuthResponse>('/api/v1/auth/register', { username, password }),
    );
    this.setSession(res);
    return res;
  }

  async updateSetupStep(step: SetupStep): Promise<void> {
    await firstValueFrom(
      this.http.put<{ setupStep: number }>('/api/v1/auth/setup-step', { setupStep: step }),
    );
    this.setupStep.set(step);
    localStorage.setItem('setupStep', String(step));
  }

  async updateLastProfile(profileUuid: string | null): Promise<void> {
    await firstValueFrom(
      this.http.put<{ lastProfileUuid: string | null }>('/api/v1/auth/last-profile', { profileUuid }),
    );
    this.lastProfileUuid.set(profileUuid);
    if (profileUuid == null) localStorage.removeItem('lastProfileUuid');
    else localStorage.setItem('lastProfileUuid', profileUuid);
  }

  logout(): void {
    this.token.set(null);
    this.username.set(null);
    this.setupStep.set(SetupStep.NOT_STARTED);
    this.lastProfileUuid.set(null);
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('setupStep');
    localStorage.removeItem('lastProfileUuid');
    this.router.navigate(['/login']);
  }

  private setSession(res: AuthResponse): void {
    this.token.set(res.token);
    this.username.set(res.username);
    this.setupStep.set(res.setupStep as SetupStep);
    this.lastProfileUuid.set(res.lastProfileUuid);
    localStorage.setItem('token', res.token);
    localStorage.setItem('username', res.username);
    localStorage.setItem('setupStep', String(res.setupStep));
    if (res.lastProfileUuid == null) localStorage.removeItem('lastProfileUuid');
    else localStorage.setItem('lastProfileUuid', res.lastProfileUuid);
  }

  private load(key: string): string | null {
    return localStorage.getItem(key);
  }

  private loadNum(key: string): number | null {
    const v = localStorage.getItem(key);
    return v != null ? Number(v) : null;
  }
}
