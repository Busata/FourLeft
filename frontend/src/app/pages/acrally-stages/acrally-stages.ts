import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import type { LocationTo, StageRequestTo, StageTo } from '../../models/acrally';

/**
 * Admin CRUD for stages. Each stage can be assigned to a location. A stage with variants assigned
 * to it can't be deleted.
 */
@Component({
  selector: 'app-acrally-stages',
  imports: [FormsModule],
  templateUrl: './acrally-stages.html',
})
export class AcrallyStages implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly base = '/acrally-api/admin/stages';

  readonly stages = signal<StageTo[]>([]);
  readonly locations = signal<LocationTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');

  readonly creating = signal(false);
  readonly newName = signal('');
  readonly newLocationId = signal<string>('');

  readonly editingId = signal<string | null>(null);
  readonly editName = signal('');
  readonly editLocationId = signal<string>('');
  readonly busy = signal(false);

  ngOnInit(): void {
    this.http.get<LocationTo[]>('/acrally-api/admin/locations').subscribe({
      next: (list) => this.locations.set(list),
      error: () => {},
    });
    this.load();
  }

  private load(): void {
    this.http.get<StageTo[]>(this.base).subscribe({
      next: (list) => {
        this.stages.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load stages.');
        this.loaded.set(true);
      },
    });
  }

  toggleCreate(): void {
    this.error.set('');
    this.newName.set('');
    this.newLocationId.set('');
    this.creating.update((open) => !open);
  }

  create(): void {
    if (this.busy() || !this.newName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: StageRequestTo = { name: this.newName(), locationId: this.newLocationId() || null };
    this.http.post<StageTo>(this.base, body).subscribe({
      next: (created) => {
        this.stages.update((list) => [...list, created].sort((a, b) => a.name.localeCompare(b.name)));
        this.busy.set(false);
        this.toggleCreate();
      },
      error: (err) => this.fail(err, 'Could not create the stage.'),
    });
  }

  startEdit(stage: StageTo): void {
    this.error.set('');
    this.editingId.set(stage.id);
    this.editName.set(stage.name);
    this.editLocationId.set(stage.locationId ?? '');
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  save(stage: StageTo): void {
    if (this.busy() || !this.editName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: StageRequestTo = { name: this.editName(), locationId: this.editLocationId() || null };
    this.http.put<StageTo>(`${this.base}/${stage.id}`, body).subscribe({
      next: (updated) => {
        this.stages.update((list) =>
          list.map((s) => (s.id === updated.id ? updated : s)).sort((a, b) => a.name.localeCompare(b.name)),
        );
        this.busy.set(false);
        this.cancelEdit();
      },
      error: (err) => this.fail(err, 'Could not save the stage.'),
    });
  }

  remove(stage: StageTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    this.http.delete(`${this.base}/${stage.id}`).subscribe({
      next: () => {
        this.stages.update((list) => list.filter((s) => s.id !== stage.id));
        this.busy.set(false);
      },
      error: (err) => this.fail(err, 'Could not delete the stage.'),
    });
  }

  private fail(err: HttpErrorResponse, fallback: string): void {
    this.error.set(err.status === 409 ? err.error?.message ?? 'That action conflicts with existing data.' : fallback);
    this.busy.set(false);
  }
}
