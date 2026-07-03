import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { TtPlayerProfile } from '../../models/time-trial-board';

/**
 * A player's time-trial profile: every board they have a stored time on (reverse lookup by display
 * name). Driven by the `?name=` query param so it's linkable straight from a board's entry.
 */
@Component({
  selector: 'app-time-trials-profile',
  imports: [RouterLink],
  templateUrl: './time-trials-profile.html',
  styleUrl: './time-trials-profile.scss',
})
export class TimeTrialsProfile implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);

  readonly name = signal('');
  readonly profile = signal<TtPlayerProfile | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const name = (params.get('name') ?? '').trim();
      this.name.set(name);
      if (name) {
        this.load(name);
      } else {
        this.profile.set(null);
        this.error.set('');
      }
    });
  }

  private load(name: string): void {
    this.loading.set(true);
    this.error.set('');
    const params = new HttpParams().set('name', name);
    this.http
      .get<TtPlayerProfile>('/api_v2/time-trials/player', { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Could not load this player’s records.');
          this.loading.set(false);
        },
      });
  }

  surfaceLabel(surfaceCondition: number): string {
    return surfaceCondition === 1 ? 'Wet' : 'Dry';
  }

  /** "00:10:23.1400000" → "10:23.140" (drops a zero hours component, trims to milliseconds). */
  formatTime(raw: string | null): string {
    if (!raw) {
      return '—';
    }
    const parts = raw.split(':');
    if (parts.length !== 3) {
      return raw;
    }
    const [hh, mm, ss] = parts;
    const [sec, frac = ''] = ss.split('.');
    const secs = `${sec}.${(frac + '000').slice(0, 3)}`;
    return parseInt(hh, 10) > 0 ? `${parseInt(hh, 10)}:${mm}:${secs}` : `${mm}:${secs}`;
  }
}
