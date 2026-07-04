import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';

import {
  TtCatalog,
  TtClass,
  TtEntryPage,
  TtRally,
  TtStage,
  TtSurface,
} from '../../models/time-trial-board';

const PAGE_SIZE = 50;

/** The currently drilled-to stage + surface, plus the car classes available there. */
interface Selection {
  locationId: number;
  location: string;
  routeId: number;
  route: string;
  surfaceCondition: number;
  classes: TtClass[];
}

/** Where a combination id sits in the catalog — used to restore a linked board. */
interface CatalogHit {
  rally: TtRally;
  stage: TtStage;
  surface: TtSurface;
  cls: TtClass;
}

@Component({
  selector: 'app-time-trials-boards',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './time-trials-boards.html',
  styleUrl: './time-trials-boards.scss',
})
export class TimeTrialsBoards implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly catalog = signal<TtCatalog | null>(null);
  readonly catalogError = signal('');

  // Sidebar search over rally + stage names.
  readonly search = signal('');
  readonly searching = computed(() => this.search().trim().length > 0);

  /**
   * Rallies filtered by the search term (matches rally name, or prunes to matching stages). Empty
   * search returns the full tree. While searching, the tree is force-expanded (see isRallyOpen).
   */
  readonly filteredRallies = computed(() => {
    const catalog = this.catalog();
    if (!catalog) {
      return [];
    }
    const term = this.search().trim().toLowerCase();
    if (!term) {
      return catalog.rallies;
    }
    const result: TtRally[] = [];
    for (const rally of catalog.rallies) {
      if (rally.location.toLowerCase().includes(term)) {
        result.push(rally); // rally name matches → keep all its stages
        continue;
      }
      const stages = rally.stages.filter((s) => s.route.toLowerCase().includes(term));
      if (stages.length) {
        result.push({ ...rally, stages });
      }
    }
    return result;
  });

  // Sidebar expansion state (rallies by locationId, stages by routeId) — local UI, not in the URL.
  readonly expandedRallies = signal<ReadonlySet<number>>(new Set());
  readonly expandedStages = signal<ReadonlySet<number>>(new Set());

  // Current drill-down + class pick. Driven by the URL (see applyFromUrl); the combination id is
  // derived from the two.
  readonly selection = signal<Selection | null>(null);
  readonly selectedClassId = signal<number | null>(null);

  readonly combinationId = computed(() => {
    const sel = this.selection();
    const classId = this.selectedClassId();
    if (!sel || classId == null) {
      return null;
    }
    return sel.classes.find((c) => c.vehicleClassId === classId)?.combinationId ?? null;
  });

  // Entries table.
  readonly entryPage = signal<TtEntryPage | null>(null);
  readonly loadingEntries = signal(false);
  readonly entriesError = signal('');
  readonly page = signal(0);
  readonly expandedRank = signal<number | null>(null);

  readonly totalPages = computed(() => this.entryPage()?.totalPages ?? 0);

  ngOnInit(): void {
    this.http
      .get<TtCatalog>('/api_v2/time-trials/catalog')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (catalog) => {
          this.catalog.set(catalog);
          // No board linked → nudge discovery by opening the first rally.
          if (!this.route.snapshot.queryParamMap.get('board') && catalog.rallies[0]) {
            this.expandedRallies.set(new Set([catalog.rallies[0].locationId]));
          }
          // The URL is the source of truth for the selection — react to it (including the current
          // value on subscribe, which restores a linked/bookmarked board once the catalog is here).
          this.route.queryParamMap
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((params) => this.applyFromUrl(params));
        },
        error: () => this.catalogError.set('Could not load the time-trial catalog.'),
      });
  }

  /** Resolve the URL (?board=&page=) against the catalog into the shown selection + entries. */
  private applyFromUrl(params: ParamMap): void {
    const catalog = this.catalog();
    if (!catalog) {
      return;
    }

    const board = params.get('board');
    const page = Math.max(0, Number.parseInt(params.get('page') ?? '0', 10) || 0);
    const hit = board ? findInCatalog(catalog, board) : null;

    if (!hit) {
      this.selection.set(null);
      this.selectedClassId.set(null);
      this.entryPage.set(null);
      return;
    }

    // Open the tree down to the linked board so it reads as selected.
    this.expandedRallies.update((s) => new Set(s).add(hit.rally.locationId));
    this.expandedStages.update((s) => new Set(s).add(hit.stage.routeId));

    this.selection.set({
      locationId: hit.rally.locationId,
      location: hit.rally.location,
      routeId: hit.stage.routeId,
      route: hit.stage.route,
      surfaceCondition: hit.surface.surfaceCondition,
      classes: hit.surface.classes,
    });
    this.selectedClassId.set(hit.cls.vehicleClassId);
    this.page.set(page);
    this.loadEntries();
  }

  // --- sidebar ------------------------------------------------------------

  toggleRally(rally: TtRally): void {
    this.expandedRallies.set(toggle(this.expandedRallies(), rally.locationId));
  }

  toggleStage(stage: TtStage): void {
    this.expandedStages.set(toggle(this.expandedStages(), stage.routeId));
  }

  onSearch(value: string): void {
    this.search.set(value);
  }

  // While searching, the (already pruned) tree is fully expanded so matches show without clicking.
  isRallyOpen(rally: TtRally): boolean {
    return this.searching() || this.expandedRallies().has(rally.locationId);
  }

  isStageOpen(stage: TtStage): boolean {
    return this.searching() || this.expandedStages().has(stage.routeId);
  }

  /** Pick a stage+surface leaf. Keeps the current class if it exists here, else the first one. */
  selectSurface(rally: TtRally, stage: TtStage, surface: TtSurface): void {
    const keep = surface.classes.some((c) => c.vehicleClassId === this.selectedClassId());
    const classId = keep ? this.selectedClassId() : (surface.classes[0]?.vehicleClassId ?? null);
    const combinationId = surface.classes.find((c) => c.vehicleClassId === classId)?.combinationId;
    if (combinationId) {
      this.navigateToBoard(combinationId, 0, false);
    }
  }

  isSurfaceSelected(stage: TtStage, surface: TtSurface): boolean {
    const sel = this.selection();
    return !!sel && sel.routeId === stage.routeId && sel.surfaceCondition === surface.surfaceCondition;
  }

  // --- class selector -----------------------------------------------------

  selectClass(classId: number): void {
    const sel = this.selection();
    const combinationId = sel?.classes.find((c) => c.vehicleClassId === classId)?.combinationId;
    if (combinationId && classId !== this.selectedClassId()) {
      this.navigateToBoard(combinationId, 0, false);
    }
  }

  // --- entries ------------------------------------------------------------

  private loadEntries(): void {
    const combinationId = this.combinationId();
    if (!combinationId) {
      return;
    }
    this.loadingEntries.set(true);
    this.entriesError.set('');
    this.expandedRank.set(null);

    const params = new HttpParams().set('page', this.page()).set('size', PAGE_SIZE);
    this.http
      .get<TtEntryPage>(`/api_v2/time-trials/boards/${combinationId}/entries`, { params })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.entryPage.set(result);
          this.loadingEntries.set(false);
        },
        error: () => {
          this.entriesError.set('Could not load this board’s entries.');
          this.loadingEntries.set(false);
        },
      });
  }

  prev(): void {
    const combinationId = this.combinationId();
    if (combinationId && this.page() > 0) {
      this.navigateToBoard(combinationId, this.page() - 1, true);
    }
  }

  next(): void {
    const combinationId = this.combinationId();
    if (combinationId && this.page() < this.totalPages() - 1) {
      this.navigateToBoard(combinationId, this.page() + 1, true);
    }
  }

  /**
   * Push the selection into the URL; {@link applyFromUrl} then drives the view. Paging replaces the
   * history entry (so Back returns to the previous board, not page-by-page); picking a board pushes.
   */
  private navigateToBoard(combinationId: string, page: number, replaceUrl: boolean): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { board: combinationId, page: page > 0 ? page : null },
      queryParamsHandling: 'merge',
      replaceUrl,
    });
  }

  toggleSplits(rank: number | null): void {
    if (rank == null) {
      return;
    }
    this.expandedRank.set(this.expandedRank() === rank ? null : rank);
  }

  // --- formatting ---------------------------------------------------------

  surfaceLabel(surfaceCondition: number): string {
    return surfaceCondition === 1 ? 'Wet' : 'Dry';
  }

  /** Rally location badge; the numeric location id maps to Codemasters' event logo assets. */
  locationBadgeUrl(locationId: number): string {
    return `https://ecdn.codemasters.com/ecdn/Racenet/PROD/WRC2023/stats/event_location_logos/${locationId}.png`;
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
    const secs = trimFraction(ss);
    return parseInt(hh, 10) > 0 ? `${parseInt(hh, 10)}:${mm}:${secs}` : `${mm}:${secs}`;
  }

  /** Gap to the leader as "+2.147"; blank for the leader / a zero gap. */
  formatDiff(raw: string | null, rank: number | null): string {
    if (rank === 1 || !raw || raw === '00:00:00') {
      return '';
    }
    const parts = raw.split(':');
    if (parts.length !== 3) {
      return raw;
    }
    const [hh, mm, ss] = parts;
    const h = parseInt(hh, 10);
    const m = parseInt(mm, 10);
    const secs = trimFraction(ss);
    if (h > 0) {
      return `+${h}:${mm}:${secs}`;
    }
    return m > 0 ? `+${m}:${secs}` : `+${secs}`;
  }
}

/** Locate a combination id in the catalog tree. */
function findInCatalog(catalog: TtCatalog, combinationId: string): CatalogHit | null {
  for (const rally of catalog.rallies) {
    for (const stage of rally.stages) {
      for (const surface of stage.surfaces) {
        const cls = surface.classes.find((c) => c.combinationId === combinationId);
        if (cls) {
          return { rally, stage, surface, cls };
        }
      }
    }
  }
  return null;
}

/** Return a new set with `value` toggled — signals need a fresh reference to notify. */
function toggle(set: ReadonlySet<number>, value: number): ReadonlySet<number> {
  const next = new Set(set);
  if (!next.delete(value)) {
    next.add(value);
  }
  return next;
}

/** "23.1400000" → "23.140"; "00" → "00.000". */
function trimFraction(ss: string): string {
  const [sec, frac = ''] = ss.split('.');
  return `${sec}.${(frac + '000').slice(0, 3)}`;
}
