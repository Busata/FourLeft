import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-acrally-login',
  templateUrl: './acrally-login.html',
})
export class AcrallyLogin implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // Where to go after sign-in — used by the device-pairing link to return to approval.
  // Carried through the Steam round-trip by the backend (validated same-origin there).
  private readonly redirect = this.route.snapshot.queryParamMap.get('redirect') || '/acrally/dashboard';

  /** Full-page navigation target — the backend 302s to Steam and Steam 302s back. */
  readonly steamHref = this.auth.steamSignInUrl(this.redirect);

  readonly error = signal('');

  ngOnInit(): void {
    // A failed Steam round-trip lands back here with ?steam=<outcome>.
    const flag = this.route.snapshot.queryParamMap.get('steam');
    if (flag === 'banned') {
      this.error.set('This account is banned.');
    } else if (flag === 'error') {
      this.error.set('Steam sign-in failed — please try again.');
    }

    // Primes the session/XSRF state; if already signed in, skip straight on.
    this.auth.loadMe().subscribe(() => {
      if (this.auth.isAuthenticated()) {
        this.router.navigateByUrl(this.redirect);
      }
    });
  }
}
