import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-acrally-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './acrally-login.html',
})
export class AcrallyLogin implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // Where to go after sign-in — used by the device-pairing link to return to approval.
  private readonly redirect = this.route.snapshot.queryParamMap.get('redirect') || '/acrally/account';

  readonly error = signal('');
  readonly submitting = signal(false);

  readonly form = new FormGroup({
    email: new FormControl<string>('', { nonNullable: true }),
    password: new FormControl<string>('', { nonNullable: true }),
  });

  ngOnInit(): void {
    // Primes the session/XSRF state; if already signed in, skip straight on.
    this.auth.loadMe().subscribe(() => {
      if (this.auth.isAuthenticated()) {
        this.router.navigateByUrl(this.redirect);
      }
    });
  }

  submit(): void {
    if (this.submitting()) {
      return;
    }
    this.error.set('');
    this.submitting.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl(this.redirect),
      error: (err) => {
        this.error.set(err.status === 401 ? 'Invalid email or password.' : 'Could not sign in.');
        this.submitting.set(false);
      },
    });
  }
}
