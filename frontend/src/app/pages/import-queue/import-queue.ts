import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { forkJoin, interval, merge, startWith, Subject, switchMap } from 'rxjs';

import {
  ImportJobStatus,
  ImportJobView,
  ImportQueueSummary,
} from '../../models/import-queue';

const REFRESH_MS = 3000;
const STATUS_ORDER: ImportJobStatus[] = ['PENDING', 'RUNNING', 'DONE', 'FAILED'];

type StatusFilter = ImportJobStatus | 'ALL';

@Component({
  selector: 'app-import-queue',
  templateUrl: './import-queue.html',
  styleUrl: './import-queue.scss',
})
export class ImportQueue implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();

  readonly statusOrder = STATUS_ORDER;

  readonly summary = signal<ImportQueueSummary | null>(null);
  readonly jobs = signal<ImportJobView[]>([]);
  readonly updatedAt = signal('');
  readonly error = signal('');

  readonly statusFilter = signal<StatusFilter>('ALL');
  readonly search = signal('');

  ngOnInit(): void {
    merge(interval(REFRESH_MS).pipe(startWith(0)), this.reload)
      .pipe(
        switchMap(() =>
          forkJoin({
            summary: this.http.get<ImportQueueSummary>('/api_v2/import-queue/summary'),
            jobs: this.http.get<ImportJobView[]>('/api_v2/import-queue/jobs', { params: this.jobParams() }),
          }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ summary, jobs }) => {
          this.summary.set(summary);
          this.jobs.set(jobs);
          this.updatedAt.set(new Date().toLocaleTimeString());
          this.error.set('');
        },
        error: () => this.error.set('Could not reach the import queue API.'),
      });
  }

  private jobParams(): HttpParams {
    let params = new HttpParams().set('limit', '100');
    const status = this.statusFilter();
    if (status !== 'ALL') {
      params = params.set('status', status);
    }
    const term = this.search().trim();
    if (term) {
      params = params.set('search', term);
    }
    return params;
  }

  setStatus(status: StatusFilter): void {
    this.statusFilter.set(status);
    this.reload.next();
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.reload.next();
  }

  count(status: ImportJobStatus): number {
    return this.summary()?.jobCountsByStatus?.[status] ?? 0;
  }

  totalCount(): number {
    const counts = this.summary()?.jobCountsByStatus;
    return counts ? Object.values(counts).reduce((a, b) => a + b, 0) : 0;
  }

  /** True when the timestamp is in the future (a job waiting out a retry backoff). */
  isFuture(iso: string | null): boolean {
    return iso != null && new Date(iso).getTime() > Date.now();
  }

  /** Human-friendly absolute + relative time, e.g. "12:30:05 (in 4s)". */
  formatTime(iso: string | null): string {
    if (!iso) {
      return '—';
    }
    const date = new Date(iso);
    const diffSec = Math.round((date.getTime() - Date.now()) / 1000);
    const abs = Math.abs(diffSec);
    const rel =
      abs < 60 ? `${abs}s` : abs < 3600 ? `${Math.round(abs / 60)}m` : `${Math.round(abs / 3600)}h`;
    const when = diffSec >= 0 ? `in ${rel}` : `${rel} ago`;
    return `${date.toLocaleTimeString()} (${when})`;
  }
}
