import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';

import {
  ClubChampionshipResult,
  ClubEventResult,
  ClubOverview,
  ClubReference,
  ClubResultEntry,
} from '../../models/club';
import { CompareRow, compareDurationRow, parseDuration } from '../../common/time-format';

/** A club member, distilled from the export for the two comparison pickers. */
interface Member {
  wrcPlayerId: string;
  playerName: string;
}

/** One event both members contested, laid out as a side-by-side time comparison. */
interface CompareEvent {
  eventId: string;
  location: string;
  meta: string;
  aRank: number | null;
  bRank: number | null;
  rows: CompareRow[];
}

/** A championship's shared events — one divider block in the compare view. */
interface CompareChampionship {
  id: string;
  name: string;
  events: CompareEvent[];
}

/**
 * Head-to-head between two members of a club. A `?club=` id loads that club's cached export; two
 * `?a=`/`?b=` member ids (Racenet wrcPlayerId — stable across renames, so links stay valid) pick the
 * pair. Every event both contested is shown side by side, grouped by championship, coloured for
 * whoever was faster — the same layout as the time-trial driver comparison, at event granularity.
 */
@Component({
  selector: 'app-club-compare',
  imports: [],
  templateUrl: './club-compare.html',
  styleUrl: './club-compare.scss',
})
export class ClubCompare implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly clubs = signal<ClubReference[]>([]);

  readonly clubId = signal('');
  readonly overview = signal<ClubOverview | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');

  // The two members under comparison, driven by ?a= / ?b= (Racenet wrcPlayerId).
  readonly a = signal('');
  readonly b = signal('');

  readonly comparing = computed(() => this.a().length > 0 && this.b().length > 0);

  /** Distinct members across every event of the loaded club, sorted by name — the picker options. */
  readonly members = computed<Member[]>(() => {
    const overview = this.overview();
    if (!overview) {
      return [];
    }
    const byId = new Map<string, Member>();
    for (const championship of overview.championships) {
      for (const event of championship.events) {
        for (const entry of event.leaderboardEntries) {
          if (entry.wrcPlayerId && !byId.has(entry.wrcPlayerId)) {
            byId.set(entry.wrcPlayerId, { wrcPlayerId: entry.wrcPlayerId, playerName: entry.playerName });
          }
        }
      }
    }
    return [...byId.values()].sort((a, b) => a.playerName.localeCompare(b.playerName));
  });

  readonly aName = computed(() => this.memberName(this.a()));
  readonly bName = computed(() => this.memberName(this.b()));

  /** Championships (in export order) holding the events both members contested, each event compared. */
  readonly comparison = computed<CompareChampionship[]>(() => {
    const overview = this.overview();
    const aId = this.a();
    const bId = this.b();
    if (!overview || !aId || !bId) {
      return [];
    }
    const result: CompareChampionship[] = [];
    for (const championship of overview.championships) {
      const events: CompareEvent[] = [];
      for (const event of championship.events) {
        const ea = this.entryFor(event, aId);
        const eb = this.entryFor(event, bId);
        if (ea && eb) {
          events.push(this.buildEvent(event, ea, eb));
        }
      }
      if (events.length) {
        result.push({ id: championship.id, name: this.championshipName(championship), events });
      }
    }
    return result;
  });

  /** Total events both members share across all championships. */
  readonly commonCount = computed(() =>
    this.comparison().reduce((sum, championship) => sum + championship.events.length, 0),
  );

  /**
   * Overall head-to-head: on how many shared events each member had the faster accumulated total (the
   * "Total" row, always last), plus dead heats. {@code leader} is 'a', 'b', or 'tie'.
   */
  readonly tally = computed(() => {
    let a = 0;
    let b = 0;
    let ties = 0;
    for (const championship of this.comparison()) {
      for (const event of championship.events) {
        const total = event.rows[event.rows.length - 1];
        if (total?.aWins) {
          a++;
        } else if (total?.bWins) {
          b++;
        } else {
          ties++;
        }
      }
    }
    const leader = a > b ? 'a' : b > a ? 'b' : 'tie';
    return { a, b, ties, total: a + b + ties, leader };
  });

  ngOnInit(): void {
    this.http
      .get<ClubReference[]>('/api_v2/clubs')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (clubs) => this.clubs.set(clubs),
        error: () => this.error.set('Could not load the list of clubs.'),
      });

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const clubId = (params.get('club') ?? '').trim();
      this.a.set((params.get('a') ?? '').trim());
      this.b.set((params.get('b') ?? '').trim());

      if (clubId !== this.clubId()) {
        this.clubId.set(clubId);
        if (clubId) {
          this.load(clubId);
        } else {
          this.overview.set(null);
          this.error.set('');
        }
      }
    });
  }

  private load(clubId: string): void {
    this.loading.set(true);
    this.error.set('');
    this.overview.set(null);
    this.http
      .get<ClubOverview>(`/api_v2/cached/club_summary/${clubId}`)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (overview) => {
          this.overview.set(overview);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('No results are available for this club yet.');
          this.loading.set(false);
        },
      });
  }

  private entryFor(event: ClubEventResult, wrcPlayerId: string): ClubResultEntry | null {
    return event.leaderboardEntries.find((e) => e.wrcPlayerId === wrcPlayerId) ?? null;
  }

  /** Last-stage time then accumulated total (the decider, always last) — mirrors splits → finish. */
  private buildEvent(event: ClubEventResult, a: ClubResultEntry, b: ClubResultEntry): CompareEvent {
    const settings = event.eventSettings;
    const meta = [settings?.weatherSeason, settings?.vehicleClass].filter(Boolean).join(' · ');
    return {
      eventId: event.id,
      location: settings?.location ?? 'Event',
      meta,
      aRank: a.rank,
      bRank: b.rank,
      rows: [
        compareDurationRow('Last stage', parseDuration(a.time), parseDuration(b.time)),
        compareDurationRow('Total', parseDuration(a.timeAccumulated), parseDuration(b.timeAccumulated)),
      ],
    };
  }

  private championshipName(championship: ClubChampionshipResult): string {
    return championship.championshipSettings?.name || championship.string || 'Championship';
  }

  private memberName(wrcPlayerId: string): string {
    if (!wrcPlayerId) {
      return '';
    }
    return this.members().find((m) => m.wrcPlayerId === wrcPlayerId)?.playerName ?? wrcPlayerId;
  }

  // --- selection (URL-driven, so every pick is a shareable link) ------------

  selectClub(clubId: string): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { club: clubId || null, a: null, b: null },
      queryParamsHandling: 'merge',
    });
  }

  selectA(wrcPlayerId: string): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { a: wrcPlayerId || null },
      queryParamsHandling: 'merge',
    });
  }

  selectB(wrcPlayerId: string): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { b: wrcPlayerId || null },
      queryParamsHandling: 'merge',
    });
  }

  clearComparison(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { a: null, b: null },
      queryParamsHandling: 'merge',
    });
  }

  /** Podium tier for an event rank: 'gold' | 'silver' | 'bronze' for 1/2/3, else ''. */
  podium(rank: number | null): string {
    if (rank === 1) return 'gold';
    if (rank === 2) return 'silver';
    if (rank === 3) return 'bronze';
    return '';
  }
}
