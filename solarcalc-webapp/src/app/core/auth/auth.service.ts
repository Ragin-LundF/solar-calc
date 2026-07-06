import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Router } from '@angular/router';

export interface AuthResponse {
  token: string;
  username: string;
  expiresInSeconds: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly token = signal<string | null>(this.load('token'));
  readonly username = signal<string | null>(this.load('username'));

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

  logout(): void {
    this.token.set(null);
    this.username.set(null);
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }

  private setSession(res: AuthResponse): void {
    this.token.set(res.token);
    this.username.set(res.username);
    localStorage.setItem('token', res.token);
    localStorage.setItem('username', res.username);
  }

  private load(key: string): string | null {
    return localStorage.getItem(key);
  }
}
