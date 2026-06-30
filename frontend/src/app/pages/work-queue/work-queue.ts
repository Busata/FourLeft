import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { forkJoin, interval, merge, startWith, Subject, switchMap } from 'rxjs';

import {
  JobStatus,
  JobType,
  WorkJobView,
  WorkQueueSummary,
} from '../../models/work-queue';

const REFRESH_MS = 3000;
const STATUS_ORDER: JobStatus[] = ['PENDING', 'RUNNING', 'DONE', 'FAILED'];
const TYPE_ORDER: JobType[] = ['CLUB', 'TT', 'CONFIG_CLEANUP'];

const TYPE_LABELS: Record<JobType, string> = {
  CLUB: 'Club',
  TT: 'Time trial',
  CONFIG_CLEANUP: 'Config cleanup',
};

type StatusFilter = JobStatus | 'ALL';
type TypeFilter = JobType | 'ALL';

@Component({
  selector: 'app-work-queue',
  templateUrl: './work-queue.html',
  styleUrl: './work-queue.scss',
})
export class WorkQueue implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();

  readonly statusOrder = STATUS_ORDER;
  readonly typeOrder = TYPE_ORDER;
  readonly typeLabels = TYPE_LABELS;

  readonly summary = signal<WorkQueueSummary | null>(null);
  readonly jobs = signal<WorkJobView[]>([]);
  readonly updatedAt = signal('');
  readonly error = signal('');

  readonly statusFilter = signal<StatusFilter>('ALL');
  readonly typeFilter = signal<TypeFilter>('ALL');
  readonly search = signal('');

  ngOnInit(): void {
    merge(interval(REFRESH_MS).pipe(startWith(0)), this.reload)
      .pipe(
        switchMap(() =>
          forkJoin({
            summary: this.http.get<WorkQueueSummary>('/api_v2/work-queue/summary'),
            jobs: this.http.get<WorkJobView[]>('/api_v2/work-queue/jobs', { params: this.jobParams() }),
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
        error: () => this.error.set('Could not reach the work queue API.'),
      });
  }

  private jobParams(): HttpParams {
    let params = new HttpParams().set('limit', '100');
    const status = this.statusFilter();
    if (status !== 'ALL') {
      params = params.set('status', status);
    }
    const type = this.typeFilter();
    if (type !== 'ALL') {
      params = params.set('type', type);
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

  setType(type: TypeFilter): void {
    this.typeFilter.set(type);
    this.reload.next();
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.reload.next();
  }

  count(status: JobStatus): number {
    return this.summary()?.jobCountsByStatus?.[status] ?? 0;
  }

  typeCount(type: JobType): number {
    return this.summary()?.jobCountsByType?.[type] ?? 0;
  }

  typeLabel(type: JobType): string {
    return TYPE_LABELS[type] ?? type;
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
