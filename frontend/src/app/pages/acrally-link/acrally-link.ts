import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type { PairLookupResultTo } from '../../models/acrally';
import { AuthService } from '../../services/auth';

type LinkState = 'loading' | 'idle' | 'ready' | 'approved' | 'denied' | 'needs-steam' | 'error';

@Component({
  selector: 'app-acrally-link',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './acrally-link.html',
})
export class AcrallyLink implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // Plain field (not a signal) so [(ngModel)] two-way binding works on the input.
  codeInput = '';
  readonly pairing = signal<PairLookupResultTo | null>(null);
  readonly state = signal<LinkState>('loading');
  readonly error = signal('');

  ngOnInit(): void {
    const initial = (this.route.snapshot.queryParamMap.get('code') ?? '').trim().toUpperCase();
    this.codeInput = initial;

    this.auth.loadMe().subscribe(() => {
      if (!this.auth.isAuthenticated()) {
        // Bounce through login, preserving the code so we return straight to approval.
        const back = '/acrally/link' + (initial ? `?code=${encodeURIComponent(initial)}` : '');
        this.router.navigate(['/acrally/login'], { queryParams: { redirect: back } });
        return;
      }
      if (initial) {
        this.lookup(initial);
      } else {
        this.state.set('idle');
      }
    });
  }

  submitCode(): void {
    const code = this.codeInput.trim().toUpperCase();
    if (code) {
      this.lookup(code);
    }
  }

  private lookup(code: string): void {
    this.state.set('loading');
    this.error.set('');
    this.http
      .get<PairLookupResultTo>('/acrally-api/agent/pair/lookup', { params: { user_code: code } })
      .subscribe({
        next: (result) => {
          this.pairing.set(result);
          this.state.set('ready');
        },
        error: (err) => {
          this.state.set('error');
          this.error.set(
            err.status === 404
              ? 'Unknown or already-used code.'
              : err.status === 410
                ? 'This code has expired — start pairing again in the app.'
                : 'Could not look up that code.',
          );
        },
      });
  }

  approve(): void {
    const pairing = this.pairing();
    if (!pairing) {
      return;
    }
    this.http.post('/acrally-api/agent/pair/approve', { userCode: pairing.userCode }).subscribe({
      next: () => this.state.set('approved'),
      error: (err) => {
        if (err.status === 403) {
          this.state.set('needs-steam');
        } else {
          this.error.set(err.status === 410 ? 'This code has expired.' : 'Could not authorize the device.');
          this.state.set('error');
        }
      },
    });
  }

  deny(): void {
    const pairing = this.pairing();
    if (!pairing) {
      return;
    }
    this.http.post('/acrally-api/agent/pair/deny', { userCode: pairing.userCode }).subscribe({
      next: () => this.state.set('denied'),
      error: () => this.state.set('denied'),
    });
  }
}
