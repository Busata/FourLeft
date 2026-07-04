import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, Subject, debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { TtPlayerEntry, TtPlayerProfile } from '../../models/time-trial-board';
import {
  CompareRow,
  compareRow,
  formatDiff,
  formatTime,
  percentileBand,
  podium,
  surfaceLabel,
} from '../../common/time-format';

type SortDir = 'asc' | 'desc';

/** One board both players have a time on, laid out for a side-by-side split comparison. */
export interface CompareBoard {
  combinationId: string;
  location: string;
  route: string;
  surfaceCondition: number;
  vehicleClass: string;
  a: TtPlayerEntry;
  b: TtPlayerEntry;
  rows: CompareRow[];
}

/** All common boards for one rally (location) — one row of the compare view. */
export interface CompareRally {
  locationId: number;
  location: string;
  boards: CompareBoard[];
}

/**
 * A player's time-trial profile: every board they have a stored time on (reverse lookup by display
 * name). Driven by the `?name=` query param so it's linkable straight from a board's entry. A second
 * `?vs=` param puts a second player alongside the first: common boards are matched and each stage is
 * shown side by side with per-sector splits, colored for whoever is faster.
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
  private readonly router = inject(Router);

  readonly name = signal('');
  readonly profile = signal<TtPlayerProfile | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');

  // Second player (comparison), driven by the `?vs=` param.
  readonly vs = signal('');
  readonly vsProfile = signal<TtPlayerProfile | null>(null);
  readonly vsLoading = signal(false);
  readonly vsError = signal('');

  /** Bound to the search boxes; may differ from the loaded names until submitted. */
  readonly query = signal('');
  readonly vsQuery = signal('');

  // Autocomplete: suggestion lists and dropdown visibility, per box.
  readonly suggestions = signal<string[]>([]);
  readonly showSuggest = signal(false);
  readonly vsSuggestions = signal<string[]>([]);
  readonly showVsSuggest = signal(false);

  private readonly queryInput$ = new Subject<string>();
  private readonly vsQueryInput$ = new Subject<string>();

  /** Rank ascending puts the driver's best (podium) finishes first. */
  readonly sortDir = signal<SortDir>('asc');

  readonly comparing = computed(() => this.vs().length > 0);

  /** Profile rows sorted by rank; null ranks always sort last regardless of direction. */
  readonly sortedEntries = computed<TtPlayerEntry[]>(() => {
    const p = this.profile();
    if (!p) {
      return [];
    }
    const dir = this.sortDir() === 'asc' ? 1 : -1;
    return [...p.entries].sort((a, b) => {
      if (a.rank == null && b.rank == null) return 0;
      if (a.rank == null) return 1;
      if (b.rank == null) return -1;
      return (a.rank - b.rank) * dir;
    });
  });

  /** Boards both players have a time on, in player A's order, each with a per-sector split comparison. */
  readonly common = computed<CompareBoard[]>(() => {
    const a = this.profile();
    const b = this.vsProfile();
    if (!a || !b) {
      return [];
    }
    const byBoard = new Map(b.entries.map((e) => [e.combinationId, e]));
    const boards: CompareBoard[] = [];
    for (const ea of a.entries) {
      const eb = byBoard.get(ea.combinationId);
      if (!eb) {
        continue;
      }
      boards.push({
        combinationId: ea.combinationId,
        location: ea.location,
        route: ea.route,
        surfaceCondition: ea.surfaceCondition,
        vehicleClass: ea.vehicleClass,
        a: ea,
        b: eb,
        rows: this.buildRows(ea, eb),
      });
    }
    return boards;
  });

  /**
   * Overall head-to-head across the common boards: how many each driver has the faster finish time on
   * (the "Finish" row is always last in each board's rows), plus any dead heats. {@code leader} names
   * whoever is ahead — 'a', 'b', or 'tie'.
   */
  readonly tally = computed(() => {
    let a = 0;
    let b = 0;
    let ties = 0;
    for (const board of this.common()) {
      const finish = board.rows[board.rows.length - 1];
      if (finish?.aWins) {
        a++;
      } else if (finish?.bWins) {
        b++;
      } else {
        ties++;
      }
    }
    const leader = a > b ? 'a' : b > a ? 'b' : 'tie';
    return { a, b, ties, total: a + b + ties, leader };
  });

  /** Common boards grouped by rally, so the compare view shows one rally per row. */
  readonly commonByRally = computed<CompareRally[]>(() => {
    const rallies = new Map<number, CompareRally>();
    for (const board of this.common()) {
      const id = board.a.locationId;
      let rally = rallies.get(id);
      if (!rally) {
        rally = { locationId: id, location: board.location, boards: [] };
        rallies.set(id, rally);
      }
      rally.boards.push(board);
    }
    return [...rallies.values()];
  });

  ngOnInit(): void {
    this.queryInput$
      .pipe(debounceTime(180), distinctUntilChanged(), switchMap((q) => this.suggest(q)), takeUntilDestroyed(this.destroyRef))
      .subscribe((names) => this.suggestions.set(names));
    this.vsQueryInput$
      .pipe(debounceTime(180), distinctUntilChanged(), switchMap((q) => this.suggest(q)), takeUntilDestroyed(this.destroyRef))
      .subscribe((names) => this.vsSuggestions.set(names));

    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const name = (params.get('name') ?? '').trim();
      this.name.set(name);
      this.query.set(name);
      if (name) {
        this.load(name);
      } else {
        this.profile.set(null);
        this.error.set('');
      }

      const vs = (params.get('vs') ?? '').trim();
      this.vs.set(vs);
      this.vsQuery.set(vs);
      if (vs) {
        this.loadVs(vs);
      } else {
        this.vsProfile.set(null);
        this.vsError.set('');
      }
    });
  }

  private load(name: string): void {
    this.loading.set(true);
    this.error.set('');
    this.fetch(name).subscribe({
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

  private loadVs(name: string): void {
    this.vsLoading.set(true);
    this.vsError.set('');
    this.fetch(name).subscribe({
      next: (profile) => {
        this.vsProfile.set(profile);
        this.vsLoading.set(false);
      },
      error: () => {
        this.vsError.set('Could not load the comparison player’s records.');
        this.vsLoading.set(false);
      },
    });
  }

  private fetch(name: string) {
    const params = new HttpParams().set('name', name);
    return this.http
      .get<TtPlayerProfile>('/api_v2/time-trials/player', { params })
      .pipe(takeUntilDestroyed(this.destroyRef));
  }

  /** Zip both players' splits into per-sector rows plus a finish-total row, tagging the faster side. */
  private buildRows(a: TtPlayerEntry, b: TtPlayerEntry): CompareRow[] {
    const as = a.splits ?? [];
    const bs = b.splits ?? [];
    const rows: CompareRow[] = [];
    const sectors = Math.max(as.length, bs.length);
    for (let i = 0; i < sectors; i++) {
      rows.push(compareRow(`Split ${i + 1}`, as[i] ?? null, bs[i] ?? null));
    }
    rows.push(compareRow('Finish', a.time, b.time));
    return rows;
  }

  /** Fetch name suggestions for {@code q} (empty for short/blank input); errors degrade to no list. */
  private suggest(q: string): Observable<string[]> {
    const trimmed = q.trim();
    if (trimmed.length < 2) {
      return of([]);
    }
    const params = new HttpParams().set('q', trimmed);
    return this.http
      .get<string[]>('/api_v2/time-trials/players/suggest', { params })
      .pipe(catchError(() => of([])));
  }

  onQueryInput(value: string): void {
    this.query.set(value);
    this.showSuggest.set(true);
    this.queryInput$.next(value);
  }

  onVsQueryInput(value: string): void {
    this.vsQuery.set(value);
    this.showVsSuggest.set(true);
    this.vsQueryInput$.next(value);
  }

  pickSuggestion(name: string): void {
    this.query.set(name);
    this.showSuggest.set(false);
    this.submitSearch();
  }

  pickVsSuggestion(name: string): void {
    this.vsQuery.set(name);
    this.showVsSuggest.set(false);
    this.submitVs();
  }

  /** Look up the typed name by pushing it to the `?name=` param, which drives the load. */
  submitSearch(): void {
    this.showSuggest.set(false);
    const name = this.query().trim();
    if (name === this.name()) {
      return;
    }
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { name: name || null },
      queryParamsHandling: 'merge',
    });
  }

  /** Set (or update) the comparison player via the `?vs=` param. */
  submitVs(): void {
    this.showVsSuggest.set(false);
    const vs = this.vsQuery().trim();
    if (vs === this.vs()) {
      return;
    }
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { vs: vs || null },
      queryParamsHandling: 'merge',
    });
  }

  /** Drop the comparison, returning to the single-player profile. */
  clearVs(): void {
    this.vsQuery.set('');
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { vs: null },
      queryParamsHandling: 'merge',
    });
  }

  /** Toggle the rank sort direction. */
  toggleSort(): void {
    this.sortDir.update((d) => (d === 'asc' ? 'desc' : 'asc'));
  }

  sortIndicator(): string {
    return this.sortDir() === 'asc' ? '▲' : '▼';
  }

  // Template-facing formatters — shared with the club-compare view (see common/time-format).
  podium = podium;
  surfaceLabel = surfaceLabel;
  formatTime = formatTime;
  formatDiff = formatDiff;
  percentile = percentileBand;
}
