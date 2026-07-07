import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import type { MyResultTo, MySessionTo } from '../../models/acrally';

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
}
