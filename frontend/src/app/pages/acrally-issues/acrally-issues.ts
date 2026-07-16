import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import type { AdminIssueReportTo } from '../../models/acrally';

/**
 * Admin list of issue reports submitted from the companion agent. Downloads are plain links to the
 * admin API (session cookie carries auth); delete removes a handled report and its attachments.
 */
@Component({
  selector: 'app-acrally-issues',
  imports: [DatePipe],
  templateUrl: './acrally-issues.html',
})
export class AcrallyIssues implements OnInit {
  private readonly http = inject(HttpClient);

  readonly issues = signal<AdminIssueReportTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');
  readonly busy = signal(false);

  ngOnInit(): void {
    this.http.get<AdminIssueReportTo[]>('/acrally-api/admin/issues').subscribe({
      next: (list) => {
        this.issues.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load issue reports.');
        this.loaded.set(true);
      },
    });
  }

  remove(issue: AdminIssueReportTo): void {
    if (!confirm(`Delete the report from ${issue.userDisplayName}? The attachments are deleted with it.`)) {
      return;
    }
    this.busy.set(true);
    this.http.delete(`/acrally-api/admin/issues/${issue.id}`).subscribe({
      next: () => {
        this.issues.update((list) => list.filter((i) => i.id !== issue.id));
        this.busy.set(false);
      },
      error: () => {
        this.error.set('Could not delete the report.');
        this.busy.set(false);
      },
    });
  }

  size(bytes: number): string {
    if (bytes >= 1024 * 1024) {
      return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    }
    if (bytes >= 1024) {
      return `${Math.round(bytes / 1024)} KB`;
    }
    return `${bytes} B`;
  }
}
