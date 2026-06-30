import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { forkJoin, interval, startWith, switchMap } from 'rxjs';

import {
  ImportJobStatus,
  ImportJobView,
  ImportQueueSummary,
  ImportTargetView,
} from '../../models/import-queue';

const REFRESH_MS = 3000;
const STATUS_ORDER: ImportJobStatus[] = ['PENDING', 'RUNNING', 'DONE', 'FAILED'];

@Component({
  selector: 'app-import-queue',
  templateUrl: './import-queue.html',
  styleUrl: './import-queue.scss',
})
export class ImportQueue implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  readonly statusOrder = STATUS_ORDER;

  readonly summary = signal<ImportQueueSummary | null>(null);
  readonly targets = signal<ImportTargetView[]>([]);
  readonly jobs = signal<ImportJobView[]>([]);
  readonly updatedAt = signal('');
  readonly error = signal('');

  ngOnInit(): void {
    interval(REFRESH_MS)
      .pipe(
        startWith(0),
        switchMap(() =>
          forkJoin({
            summary: this.http.get<ImportQueueSummary>('/api_v2/import-queue/summary'),
            targets: this.http.get<ImportTargetView[]>('/api_v2/import-queue/targets'),
            jobs: this.http.get<ImportJobView[]>('/api_v2/import-queue/jobs?limit=100'),
          }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ summary, targets, jobs }) => {
          this.summary.set(summary);
          this.targets.set(targets);
          this.jobs.set(jobs);
          this.updatedAt.set(new Date().toLocaleTimeString());
          this.error.set('');
        },
        error: () => this.error.set('Could not reach the import queue API.'),
      });
  }

  count(status: ImportJobStatus): number {
    return this.summary()?.jobCountsByStatus?.[status] ?? 0;
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
