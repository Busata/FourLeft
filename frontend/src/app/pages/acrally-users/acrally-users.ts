import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import type { AdminUserTo } from '../../models/acrally';

@Component({
  selector: 'app-acrally-users',
  imports: [DatePipe],
  templateUrl: './acrally-users.html',
})
export class AcrallyUsers implements OnInit {
  private readonly http = inject(HttpClient);

  readonly users = signal<AdminUserTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');
  readonly adminCount = computed(() => this.users().filter((u) => u.admin).length);

  ngOnInit(): void {
    this.http.get<AdminUserTo[]>('/acrally-api/admin/users').subscribe({
      next: (list) => {
        this.users.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load users.');
        this.loaded.set(true);
      },
    });
  }
}
