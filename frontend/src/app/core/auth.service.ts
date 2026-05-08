import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { ApiConfigService } from './api-config.service';
import { AuthResponse, LoginRequest, RegisterRequest, User } from './models';

const STORAGE_KEY = 'papeleria-session';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiConfig = inject(ApiConfigService);

  private readonly sessionSubject = new BehaviorSubject<AuthResponse | null>(this.loadSession());
  readonly session$ = this.sessionSubject.asObservable();

  get token(): string | null {
    return this.sessionSubject.value?.token ?? null;
  }

  get user(): User | null {
    return this.sessionSubject.value?.user ?? null;
  }

  get isAuthenticated(): boolean {
    return this.token !== null;
  }

  get isAdmin(): boolean {
    return this.user?.role === 'ADMIN';
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiConfig.authApiUrl}/auth/login`, payload)
      .pipe(tap((session) => this.persistSession(session)));
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiConfig.authApiUrl}/auth/register`, payload)
      .pipe(tap((session) => this.persistSession(session)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.sessionSubject.next(null);
    void this.router.navigate(['/login']);
  }

  private loadSession(): AuthResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as AuthResponse;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }

  private persistSession(session: AuthResponse): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    this.sessionSubject.next(session);
  }
}
