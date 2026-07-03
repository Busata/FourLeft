import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';

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

@Component({
  selector: 'app-time-trials-boards',
  templateUrl: './time-trials-boards.html',
  styleUrl: './time-trials-boards.scss',
})
export class TimeTrialsBoards implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  readonly catalog = signal<TtCatalog | null>(null);
  readonly catalogError = signal('');

  // Sidebar expansion state (rallies by locationId, stages by routeId).
  readonly expandedRallies = signal<ReadonlySet<number>>(new Set());
  readonly expandedStages = signal<ReadonlySet<number>>(new Set());

  // Current drill-down + class pick. The combination id is derived from the two.
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
          // Nudge discovery: open the first rally so the tree reads as explorable.
          const first = catalog.rallies[0];
          if (first) {
            this.expandedRallies.set(new Set([first.locationId]));
          }
        },
        error: () => this.catalogError.set('Could not load the time-trial catalog.'),
      });
  }

  // --- sidebar ------------------------------------------------------------

  toggleRally(rally: TtRally): void {
    this.expandedRallies.set(toggle(this.expandedRallies(), rally.locationId));
  }

  toggleStage(stage: TtStage): void {
    this.expandedStages.set(toggle(this.expandedStages(), stage.routeId));
  }

  isRallyOpen(rally: TtRally): boolean {
    return this.expandedRallies().has(rally.locationId);
  }

  isStageOpen(stage: TtStage): boolean {
    return this.expandedStages().has(stage.routeId);
  }

  /** Pick a stage+surface leaf. Keeps the current class if it exists here, else defaults to the first. */
  selectSurface(rally: TtRally, stage: TtStage, surface: TtSurface): void {
    const sel: Selection = {
      locationId: rally.locationId,
      location: rally.location,
      routeId: stage.routeId,
      route: stage.route,
      surfaceCondition: surface.surfaceCondition,
      classes: surface.classes,
    };
    this.selection.set(sel);

    const keep = surface.classes.some((c) => c.vehicleClassId === this.selectedClassId());
    this.selectedClassId.set(keep ? this.selectedClassId() : (surface.classes[0]?.vehicleClassId ?? null));

    this.page.set(0);
    this.loadEntries();
  }

  isSurfaceSelected(stage: TtStage, surface: TtSurface): boolean {
    const sel = this.selection();
    return !!sel && sel.routeId === stage.routeId && sel.surfaceCondition === surface.surfaceCondition;
  }

  // --- class selector -----------------------------------------------------

  selectClass(classId: number): void {
    if (classId === this.selectedClassId()) {
      return;
    }
    this.selectedClassId.set(classId);
    this.page.set(0);
    this.loadEntries();
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
    if (this.page() > 0) {
      this.page.update((p) => p - 1);
      this.loadEntries();
    }
  }

  next(): void {
    if (this.page() < this.totalPages() - 1) {
      this.page.update((p) => p + 1);
      this.loadEntries();
    }
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
