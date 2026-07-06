import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Router } from '@angular/router';

export interface AuthResponse {
  token: string;
  username: string;
  tenantId: number;
  expiresInSeconds: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly token = signal<string | null>(this.load('token'));
  readonly username = signal<string | null>(this.load('username'));
  readonly tenantId = signal<number | null>(this.loadNum('tenantId'));

  get isAuthenticated(): boolean {
    return this.token() !== null;
  }

  async login(username: string, password: string): Promise<AuthResponse> {
    const res = await firstValueFrom(
      this.http.post<AuthResponse>('/api/auth/login', { username, password }),
    );
    this.setSession(res);
    return res;
  }

  async register(username: string, password: string): Promise<AuthResponse> {
    const res = await firstValueFrom(
      this.http.post<AuthResponse>('/api/auth/register', { username, password }),
    );
    this.setSession(res);
    return res;
  }

  logout(): void {
    this.token.set(null);
    this.username.set(null);
    this.tenantId.set(null);
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('tenantId');
    this.router.navigate(['/login']);
  }

  private setSession(res: AuthResponse): void {
    this.token.set(res.token);
    this.username.set(res.username);
    this.tenantId.set(res.tenantId);
    localStorage.setItem('token', res.token);
    localStorage.setItem('username', res.username);
    localStorage.setItem('tenantId', String(res.tenantId));
  }

  private load(key: string): string | null {
    return localStorage.getItem(key);
  }

  private loadNum(key: string): number | null {
    const v = localStorage.getItem(key);
    return v ? Number(v) : null;
  }
}
