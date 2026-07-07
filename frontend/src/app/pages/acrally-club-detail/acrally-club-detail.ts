import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type { ChampionshipDetailTo, ChampionshipTo, ClubTo } from '../../models/acrally';

/**
 * A single club: its championships, and — for the club owner — the affordance to schedule a new one.
 * The first club-detail surface in the app; reached from the clubs list "Open" link.
 */
@Component({
  selector: 'app-acrally-club-detail',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './acrally-club-detail.html',
})
export class AcrallyClubDetail implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly clubId = signal('');
  readonly club = signal<ClubTo | null>(null);
  readonly championships = signal<ChampionshipTo[]>([]);
  readonly loaded = signal(false);

  readonly owner = computed(() => this.club()?.owner ?? false);

  readonly creating = signal(false);
  readonly submitting = signal(false);
  readonly error = signal('');

  readonly form = new FormGroup({
    name: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    startsAt: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
  });

  ngOnInit(): void {
    this.clubId.set(this.route.snapshot.paramMap.get('clubId') ?? '');
    this.load();
  }

  private load(): void {
    // No single-club endpoint; the list carries the name + owner flag we need.
    this.http.get<ClubTo[]>('/acrally-api/clubs').subscribe({
      next: (clubs) => this.club.set(clubs.find((c) => c.id === this.clubId()) ?? null),
    });
    this.http
      .get<ChampionshipTo[]>(`/acrally-api/clubs/${this.clubId()}/championships`)
      .subscribe({
        next: (list) => {
          this.championships.set(list);
          this.loaded.set(true);
        },
        error: () => this.loaded.set(true),
      });
  }

  toggleCreate(): void {
    this.error.set('');
    this.creating.update((open) => !open);
  }

  submit(): void {
    if (this.submitting() || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.error.set('');
    this.submitting.set(true);
    this.http
      .post<ChampionshipDetailTo>(
        `/acrally-api/clubs/${this.clubId()}/championships`,
        this.form.getRawValue(),
      )
      .subscribe({
        next: (championship) => {
          // Straight into the editor to add events.
          this.router.navigate(['championships', championship.id], { relativeTo: this.route });
        },
        error: (err) => {
          this.error.set(
            err.status === 403
              ? 'Only the club owner can schedule championships.'
              : 'Could not create the championship.',
          );
          this.submitting.set(false);
        },
      });
  }
}
