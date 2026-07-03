import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';

import { TimeTrialCombination } from '../../models/time-trial';

const PAGE_SIZE = 50;

/** Columns that can be sorted; 'default' keeps the server's grouped order (location → stage → surface → class). */
type SortKey =
  | 'default'
  | 'location'
  | 'route'
  | 'surface'
  | 'vehicleClass'
  | 'exists'
  | 'totalEntries'
  | 'changedEntries'
  | 'probedAt';
type SortDir = 'asc' | 'desc';

// How to read a sort value from a row, and the natural default direction when the column is picked.
const SORTERS: Record<Exclude<SortKey, 'default'>, { value: (c: TimeTrialCombination) => number | string | null; dir: SortDir }> = {
  location: { value: (c) => c.location, dir: 'asc' },
  route: { value: (c) => c.route, dir: 'asc' },
  surface: { value: (c) => c.surfaceCondition, dir: 'asc' },
  vehicleClass: { value: (c) => c.vehicleClass, dir: 'asc' },
  exists: { value: (c) => (c.valid == null ? null : c.valid ? 1 : 0), dir: 'desc' },
  totalEntries: { value: (c) => c.totalEntries, dir: 'desc' },
  changedEntries: { value: (c) => c.changedEntries, dir: 'desc' },
  probedAt: { value: (c) => (c.probedAt ? Date.parse(c.probedAt) : null), dir: 'desc' },
};

@Component({
  selector: 'app-time-trials',
  templateUrl: './time-trials.html',
  styleUrl: './time-trials.scss',
})
export class TimeTrials implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  readonly all = signal<TimeTrialCombination[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly search = signal('');
  readonly sortKey = signal<SortKey>('default');
  readonly sortDir = signal<SortDir>('asc');
  readonly page = signal(0);

  readonly pageSize = PAGE_SIZE;

  /** Search terms split on whitespace — every term must match some column (AND across terms). */
  private readonly terms = computed(() =>
    this.search().toLowerCase().split(/\s+/).filter((t) => t.length > 0),
  );

  readonly filtered = computed(() => {
    const terms = this.terms();
    const rows = this.all();
    if (terms.length === 0) {
      return rows;
    }
    return rows.filter((c) => {
      const haystack = `${c.id} ${c.location} ${c.route} ${c.vehicleClass} ${this.surfaceLabel(
        c.surfaceCondition,
      )}`.toLowerCase();
      return terms.every((t) => haystack.includes(t));
    });
  });

  readonly sorted = computed(() => {
    const key = this.sortKey();
    const rows = this.filtered();
    if (key === 'default') {
      return rows;
    }
    const read = SORTERS[key].value;
    const dir = this.sortDir() === 'asc' ? 1 : -1;
    return [...rows].sort((a, b) => {
      const va = read(a);
      const vb = read(b);
      // Never-probed values sort last regardless of direction.
      if (va == null && vb == null) return 0;
      if (va == null) return 1;
      if (vb == null) return -1;
      if (va < vb) return -1 * dir;
      if (va > vb) return 1 * dir;
      return 0;
    });
  });

  readonly total = computed(() => this.filtered().length);
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.total() / PAGE_SIZE)));
  readonly visible = computed(() => {
    const start = this.page() * PAGE_SIZE;
    return this.sorted().slice(start, start + PAGE_SIZE);
  });

  ngOnInit(): void {
    this.http
      .get<TimeTrialCombination[]>('/api_v2/time-trials')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rows) => {
          this.all.set(rows);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Could not reach the time-trials API.');
          this.loading.set(false);
        },
      });
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.page.set(0);
  }

  /** Click a column: toggle direction if already active, else switch to it at its natural default direction. */
  setSort(key: Exclude<SortKey, 'default'>): void {
    if (this.sortKey() === key) {
      this.sortDir.update((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortKey.set(key);
      this.sortDir.set(SORTERS[key].dir);
    }
    this.page.set(0);
  }

  sortIndicator(key: Exclude<SortKey, 'default'>): string {
    if (this.sortKey() !== key) {
      return '';
    }
    return this.sortDir() === 'asc' ? '▲' : '▼';
  }

  prev(): void {
    if (this.page() > 0) {
      this.page.update((n) => n - 1);
    }
  }

  next(): void {
    if (this.page() < this.totalPages() - 1) {
      this.page.update((n) => n + 1);
    }
  }

  surfaceLabel(surface: number): string {
    return surface === 1 ? 'Wet' : 'Dry';
  }

  validLabel(valid: boolean | null): string {
    if (valid == null) {
      return 'Unknown';
    }
    return valid ? 'Yes' : 'No';
  }

  probedLabel(iso: string | null): string {
    return iso ? new Date(iso).toLocaleDateString() : '—';
  }
}
