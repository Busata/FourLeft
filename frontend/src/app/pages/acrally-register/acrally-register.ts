import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-acrally-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './acrally-register.html',
})
export class AcrallyRegister implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly error = signal('');
  readonly submitting = signal(false);

  readonly form = new FormGroup({
    email: new FormControl<string>('', { nonNullable: true }),
    displayName: new FormControl<string>('', { nonNullable: true }),
    password: new FormControl<string>('', { nonNullable: true }),
  });

  ngOnInit(): void {
    // Prime session/XSRF; a signed-in user has no business on the register page.
    this.auth.loadMe().subscribe(() => {
      if (this.auth.isAuthenticated()) {
        this.router.navigateByUrl('/acrally/account');
      }
    });
  }

  submit(): void {
    if (this.submitting()) {
      return;
    }
    this.error.set('');
    this.submitting.set(true);
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/acrally/account'),
      error: (err) => {
        if (err.status === 409) {
          this.error.set('That email or display name is already taken.');
        } else if (err.status === 400) {
          this.error.set('Enter a valid email, a display name, and a password of at least 8 characters.');
        } else {
          this.error.set('Could not create your account.');
        }
        this.submitting.set(false);
      },
    });
  }
}
