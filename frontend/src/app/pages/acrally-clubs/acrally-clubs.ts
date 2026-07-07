import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import type { ClubTo } from '../../models/acrally';

@Component({
  selector: 'app-acrally-clubs',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './acrally-clubs.html',
  styleUrl: './acrally-clubs.scss',
})
export class AcrallyClubs implements OnInit {
  private readonly http = inject(HttpClient);

  readonly clubs = signal<ClubTo[]>([]);
  readonly clubsLoaded = signal(false);
  readonly myClubs = computed(() => this.clubs().filter((c) => c.member));
  /** Clubs the user hasn't joined — "All clubs" excludes the ones already under "My clubs". */
  readonly otherClubs = computed(() => this.clubs().filter((c) => !c.member));

  readonly creating = signal(false);
  readonly submitting = signal(false);
  readonly error = signal('');

  readonly form = new FormGroup({
    name: new FormControl<string>('', { nonNullable: true }),
    description: new FormControl<string>('', { nonNullable: true }),
    socialLink: new FormControl<string>('', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.loadClubs();
  }

  private loadClubs(): void {
    this.http.get<ClubTo[]>('/acrally-api/clubs').subscribe({
      next: (list) => {
        this.clubs.set(list);
        this.clubsLoaded.set(true);
      },
      error: () => this.clubsLoaded.set(true),
    });
  }

  toggleCreate(): void {
    this.error.set('');
    this.creating.update((open) => !open);
  }

  submit(): void {
    if (this.submitting()) {
      return;
    }
    this.error.set('');
    this.submitting.set(true);
    this.http.post<ClubTo>('/acrally-api/clubs', this.form.getRawValue()).subscribe({
      next: (club) => {
        this.clubs.update((list) => [club, ...list]);
        this.form.reset();
        this.creating.set(false);
        this.submitting.set(false);
      },
      error: (err) => {
        if (err.status === 409) {
          this.error.set('A club with that name already exists.');
        } else if (err.status === 400) {
          this.error.set('Enter a club name (a social link must be a http(s) URL).');
        } else {
          this.error.set('Could not create the club.');
        }
        this.submitting.set(false);
      },
    });
  }

  join(club: ClubTo): void {
    this.setMembership(club, true);
    this.http.post(`/acrally-api/clubs/${club.id}/join`, {}).subscribe({
      error: () => this.setMembership(club, false),
    });
  }

  leave(club: ClubTo): void {
    this.setMembership(club, false);
    this.http.post(`/acrally-api/clubs/${club.id}/leave`, {}).subscribe({
      error: () => this.setMembership(club, true),
    });
  }

  /** Optimistically flip membership + member count for a club in the list. */
  private setMembership(club: ClubTo, member: boolean): void {
    if (club.member === member) {
      return;
    }
    this.clubs.update((list) =>
      list.map((c) =>
        c.id === club.id
          ? { ...c, member, memberCount: c.memberCount + (member ? 1 : -1) }
          : c,
      ),
    );
  }
}
