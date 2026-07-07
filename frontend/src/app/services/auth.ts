import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of, tap } from 'rxjs';

import type { AuthUserTo } from '../models/acrally';

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
  readonly isAdmin = computed(() => !!this.user()?.admin);

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

  /**
   * Signing in is a full-page round-trip through Steam (the backend 302s to Steam and
   * back), not an XHR — navigate to this URL to start it. `redirect` is the in-app path
   * to land on afterwards.
   */
  steamSignInUrl(redirect?: string): string {
    return redirect
      ? `${this.base}/steam/start?redirect=${encodeURIComponent(redirect)}`
      : `${this.base}/steam/start`;
  }

  updateDisplayName(displayName: string): Observable<AuthUserTo> {
    return this.http
      .post<AuthUserTo>('/acrally-api/account/display-name', { displayName })
      .pipe(tap((user) => this.user.set(user)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.base}/logout`, {})
      .pipe(tap(() => this.user.set(null)));
  }
}
