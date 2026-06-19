import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiService } from '../services/api.service';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
}

export interface AuthSession {
  accessToken: string;
  username: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'ecom-ng.auth';

  token = signal<string|null>(null);
  username = signal<string|null>(null);
  roles = signal<string[]>([]);

  constructor(
    private http: HttpClient,
    private api: ApiService,
  ) {
    this.cargarSesion();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.api.buildUrl('/auth/login'), credentials)
      .pipe(
        tap(response => this.guardarSesion({
          accessToken: response.accessToken,
          username: response.username,
          roles: response.roles ?? [],
        })),
      );
  }

  logout() {
    localStorage.removeItem(this.storageKey);
    this.token.set(null);
    this.username.set(null);
    this.roles.set([]);
  }

  isAuthenticated(): boolean {
    return !!this.token();
  }

  hasRole(role: string): boolean {
    return this.roles().includes(role);
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  private guardarSesion(session: AuthSession) {
    localStorage.setItem(this.storageKey, JSON.stringify(session));
    this.token.set(session.accessToken);
    this.username.set(session.username);
    this.roles.set(session.roles);
  }

  private cargarSesion() {
    const rawSession = localStorage.getItem(this.storageKey);
    if (!rawSession) return;

    const session = JSON.parse(rawSession) as AuthSession;
    this.token.set(session.accessToken);
    this.username.set(session.username);
    this.roles.set(session.roles ?? []);
  }
}
