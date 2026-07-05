import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { forkJoin, interval, merge, startWith, Subject, switchMap } from 'rxjs';

import {
  JobOutcome,
  JobStatus,
  JobType,
  WorkJobView,
  WorkQueueSummary,
  WorkTargetView,
} from '../../models/work-queue';

const REFRESH_MS = 3000;
const STATUS_ORDER: JobStatus[] = ['PENDING', 'RUNNING', 'DONE', 'FAILED'];
const TYPE_ORDER: JobType[] = ['CLUB', 'CLUB_EXPORT', 'TT_PROBE', 'TT_FETCH', 'TT_EXPORT'];

const TYPE_LABELS: Record<JobType, string> = {
  CLUB: 'Club',
  CLUB_EXPORT: 'Club export',
  TT_PROBE: 'Time-trial probe',
  TT_FETCH: 'Time-trial fetch',
  TT_EXPORT: 'Time-trial export',
};

const OUTCOME_LABELS: Record<JobOutcome, string> = {
  CLUB_CREATED: 'Club created',
  CHAMPIONSHIP_STARTED: 'Championship started',
  EVENT_ENDED: 'Event ended',
  LEADERBOARDS_UPDATED: 'Leaderboards updated',
  HISTORY_UPDATED: 'History updated',
  DETAILS_REFRESHED: 'Details refreshed',
  NO_CHANGE: 'No change',
  SYNC_DISABLED: 'Sync disabled (failed)',
  TT_PROBED: 'Boards probed',
  TT_FETCHED: 'Board fetched',
  CLUB_EXPORTED: 'Club exported',
  TT_EXPORTED: 'Board exported',
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
  readonly targets = signal<WorkTargetView[]>([]);
  readonly updatedAt = signal('');
  readonly error = signal('');

  readonly statusFilter = signal<StatusFilter>('ALL');
  readonly typeFilter = signal<TypeFilter>('ALL');
  readonly search = signal('');

  /** The "Next due" targets, filtered by the same ref search box as the jobs table. */
  readonly visibleTargets = computed(() => {
    const term = this.search().trim().toLowerCase();
    const all = this.targets();
    return term ? all.filter((t) => t.ref.toLowerCase().includes(term)) : all;
  });

  ngOnInit(): void {
    merge(interval(REFRESH_MS).pipe(startWith(0)), this.reload)
      .pipe(
        switchMap(() =>
          forkJoin({
            summary: this.http.get<WorkQueueSummary>('/api_v2/work-queue/summary'),
            jobs: this.http.get<WorkJobView[]>('/api_v2/work-queue/jobs', { params: this.jobParams() }),
            targets: this.http.get<WorkTargetView[]>('/api_v2/work-queue/targets'),
          }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ summary, jobs, targets }) => {
          this.summary.set(summary);
          this.jobs.set(jobs);
          this.targets.set(targets);
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

  outcomeLabel(outcome: JobOutcome | null): string {
    return outcome ? (OUTCOME_LABELS[outcome] ?? outcome) : '—';
  }

  /** Compact elapsed duration, e.g. "820ms", "4.3s", "1m 12s". */
  formatDuration(ms: number | null): string {
    if (ms == null) {
      return '—';
    }
    if (ms < 1000) {
      return `${ms}ms`;
    }
    const seconds = ms / 1000;
    if (seconds < 60) {
      return `${seconds.toFixed(1)}s`;
    }
    const mins = Math.floor(seconds / 60);
    const rem = Math.round(seconds % 60);
    return `${mins}m ${rem}s`;
  }

  /** What the run moved, e.g. "3 boards · 512 entries · 1 standings"; empty when nothing. */
  jobCounts(job: WorkJobView): string {
    const parts: string[] = [];
    if (job.leaderboardsUpdated) {
      parts.push(
        `${job.leaderboardsUpdated} leaderboard${job.leaderboardsUpdated === 1 ? '' : 's'}`,
      );
    }
    if (job.entriesImported) {
      parts.push(`${job.entriesImported} entries`);
    }
    if (job.standingsUpdated) {
      parts.push(`${job.standingsUpdated} standings`);
    }
    return parts.join(' · ');
  }

  /** Tooltip for the duration cell: queue wait + recovery note. */
  durationTitle(job: WorkJobView): string {
    const bits: string[] = [];
    if (job.waitMs != null) {
      bits.push(`waited ${this.formatDuration(job.waitMs)} in queue`);
    }
    if (job.recovered) {
      bits.push(`recovered after crash (${job.attempts} attempts)`);
    }
    return bits.join(' • ');
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
