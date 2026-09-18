import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  AuthRequest,
  AuthResponse,
  TwoFactorType
} from '../../shared/models/auth.model';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'jwt_token';

  private readonly apiUrl =
    'http://localhost:8080/api/auth';

  constructor(
    private http: HttpClient,
    private router: Router
  ) { }

  login(
    credentials: AuthRequest
  ): Observable<AuthResponse> {

    return this.http
      .post<AuthResponse>(
        `${this.apiUrl}/login`,
        credentials
      )
      .pipe(
        tap(response => {
          if (response.token) {
            this.setToken(response.token);
          }

        })
      );
  }

  verifyTwoFactor(
    challengeId: string,
    code: string,
    type: TwoFactorType
  ): Observable<AuthResponse> {

    const headers =
      new HttpHeaders({
        'X-2FA-CODE': code,
        'X-2FA-TYPE': type
      });

    return this.http
      .post<AuthResponse>(
        `${this.apiUrl}/2fa/verify`,
        {
          challengeId
        },
        {
          headers
        }
      )
      .pipe(
        tap(response => {

          if (response.token) {
            this.setToken(response.token);
          }

        })
      );
  }

  logout(): void {

    localStorage.removeItem(
      this.TOKEN_KEY
    );

    this.router.navigate([
      '/login'
    ]);
  }

  private setToken(token: string): void {

    localStorage.setItem(
      this.TOKEN_KEY,
      token
    );
  }

  getToken(): string | null {

    return localStorage.getItem(
      this.TOKEN_KEY
    );
  }

  isAuthenticated(): boolean {

    const token =
      this.getToken();

    if (!token) {
      return false;
    }

    try {

      const payload =
        this.decodeToken(token);

      return payload.exp * 1000
        > Date.now();

    } catch {

      return false;
    }
  }

  getUsername(): string | null {

    const payload =
      this.getPayload();

    return payload?.sub ?? null;
  }

  getRoles(): string[] {

    const payload =
      this.getPayload();

    return payload?.roles ?? [];
  }

  hasRole(role: string): boolean {

    return this
      .getRoles()
      .includes(
        `ROLE_${role}`
      );
  }

  private getPayload(): any | null {

    const token =
      this.getToken();

    if (!token) {
      return null;
    }

    try {

      return this.decodeToken(token);

    } catch {

      return null;
    }
  }

  private decodeToken(token: string): any {

    const payload =
      token.split('.')[1];

    const base64 =
      payload
        .replace(/-/g, '+')
        .replace(/_/g, '/');

    const decoded =
      decodeURIComponent(
        atob(base64)
          .split('')
          .map(
            c =>
              '%' +
              (
                '00' +
                c.charCodeAt(0)
                  .toString(16)
              ).slice(-2)
          )
          .join('')
      );

    return JSON.parse(decoded);
  }
}