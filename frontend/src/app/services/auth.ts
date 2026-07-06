import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of, tap } from 'rxjs';

import type { AuthUserTo, LoginRequestTo, RegisterRequestTo } from '../models/acrally';

/**
 * Session-backed auth for the ACRally module. The browser talks to /acrally-api with the
 * HttpOnly session cookie; Angular's built-in XSRF interceptor echoes the XSRF-TOKEN cookie
 * (primed by any GET, e.g. loadMe) as X-XSRF-TOKEN on mutating requests.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly base = '/acrally-api/auth';

  /** undefined = not yet resolved, null = anonymous, object = signed in. */
  private readonly user = signal<AuthUserTo | null | undefined>(undefined);

  readonly currentUser = this.user.asReadonly();
  readonly resolved = computed(() => this.user() !== undefined);
  readonly isAuthenticated = computed(() => !!this.user());

  /** Resolves current session; also primes the XSRF cookie for later POSTs. */
  loadMe(): Observable<AuthUserTo | null> {
    return this.http.get<AuthUserTo>(`${this.base}/me`).pipe(
      tap((user) => this.user.set(user)),
      catchError(() => {
        this.user.set(null);
        return of(null);
      }),
    );
  }

  register(body: RegisterRequestTo): Observable<AuthUserTo> {
    return this.http
      .post<AuthUserTo>(`${this.base}/register`, body)
      .pipe(tap((user) => this.user.set(user)));
  }

  login(body: LoginRequestTo): Observable<AuthUserTo> {
    return this.http
      .post<AuthUserTo>(`${this.base}/login`, body)
      .pipe(tap((user) => this.user.set(user)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.base}/logout`, {})
      .pipe(tap(() => this.user.set(null)));
  }
}
