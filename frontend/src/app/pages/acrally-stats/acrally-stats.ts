import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import type { MyResultTo, MySessionTo } from '../../models/acrally';

const SESSIONS_PAGE_SIZE = 10;

@Component({
  selector: 'app-acrally-stats',
  imports: [DatePipe],
  templateUrl: './acrally-stats.html',
})
export class AcrallyStats implements OnInit {
  private readonly http = inject(HttpClient);

  readonly results = signal<MyResultTo[]>([]);
  readonly sessions = signal<MySessionTo[]>([]);
  readonly loaded = signal(false);

  readonly sessionsPage = signal(0);
  readonly sessionsTotalPages = computed(() =>
    Math.max(1, Math.ceil(this.sessions().length / SESSIONS_PAGE_SIZE)),
  );
  readonly visibleSessions = computed(() => {
    const start = this.sessionsPage() * SESSIONS_PAGE_SIZE;
    return this.sessions().slice(start, start + SESSIONS_PAGE_SIZE);
  });

  ngOnInit(): void {
    this.http.get<MyResultTo[]>('/acrally-api/me/results').subscribe({
      next: (list) => {
        this.results.set(list);
        this.loaded.set(true);
      },
      error: () => this.loaded.set(true),
    });
    this.http.get<MySessionTo[]>('/acrally-api/me/sessions').subscribe({
      next: (list) => this.sessions.set(list),
      error: () => {},
    });
  }

  /** ms → m:ss.mmm (stage times are penalised totals). */
  formatTime(ms: number): string {
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    const millis = ms % 1000;
    return `${minutes}:${seconds.toString().padStart(2, '0')}.${millis.toString().padStart(3, '0')}`;
  }

  formatPenalty(ms: number): string {
    return ms > 0 ? `+${(ms / 1000).toFixed(1)}s` : '—';
  }

  prevSessionsPage(): void {
    if (this.sessionsPage() > 0) {
      this.sessionsPage.update((n) => n - 1);
    }
  }

  nextSessionsPage(): void {
    if (this.sessionsPage() < this.sessionsTotalPages() - 1) {
      this.sessionsPage.update((n) => n + 1);
    }
  }
}
