import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type { ChampionshipDetailTo, ChampionshipTo, ClubTo } from '../../models/acrally';
import { AcrallyChampionshipView } from '../../shared/acrally-championship-view/acrally-championship-view';

/**
 * A single club. Its championships render inline — each one expands in place into the compact
 * championship view (events, stages, boards), so members never need to leave the page. Owners get
 * the schedule form here and a "Manage" link per championship into the editor.
 */
@Component({
  selector: 'app-acrally-club-detail',
  imports: [DatePipe, ReactiveFormsModule, RouterLink, AcrallyChampionshipView],
  templateUrl: './acrally-club-detail.html',
  styleUrl: './acrally-club-detail.scss',
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

  // Inline championship details, fetched lazily as blocks expand.
  readonly expandedChamps = signal<Set<string>>(new Set());
  private readonly details = signal<Map<string, ChampionshipDetailTo>>(new Map());
  private readonly loadingDetails = signal<Set<string>>(new Set());

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
          // The first published championship opens by itself — that's what a visitor came for.
          const first = list.find((c) => c.status === 'PUBLISHED') ?? (list.length === 1 ? list[0] : undefined);
          if (first) {
            this.expandChampionship(first.id);
          }
        },
        error: () => this.loaded.set(true),
      });
  }

  // --- Inline championship blocks ---
  isChampExpanded(id: string): boolean {
    return this.expandedChamps().has(id);
  }

  toggleChampionship(id: string): void {
    if (this.expandedChamps().has(id)) {
      this.expandedChamps.update((s) => {
        const next = new Set(s);
        next.delete(id);
        return next;
      });
    } else {
      this.expandChampionship(id);
    }
  }

  private expandChampionship(id: string): void {
    this.expandedChamps.update((s) => new Set(s).add(id));
    if (!this.details().has(id) && !this.loadingDetails().has(id)) {
      this.loadingDetails.update((s) => new Set(s).add(id));
      this.http.get<ChampionshipDetailTo>(`/acrally-api/championships/${id}`).subscribe({
        next: (detail) => {
          this.details.update((m) => new Map(m).set(id, detail));
          this.clearLoadingDetail(id);
        },
        error: () => this.clearLoadingDetail(id),
      });
    }
  }

  private clearLoadingDetail(id: string): void {
    this.loadingDetails.update((s) => {
      const next = new Set(s);
      next.delete(id);
      return next;
    });
  }

  detailFor(id: string): ChampionshipDetailTo | undefined {
    return this.details().get(id);
  }

  isDetailLoading(id: string): boolean {
    return this.loadingDetails().has(id);
  }

  // --- Scheduling (owner) ---
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
