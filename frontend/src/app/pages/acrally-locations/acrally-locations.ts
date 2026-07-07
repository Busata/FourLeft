import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import type { LocationRequestTo, LocationTo } from '../../models/acrally';

/** Admin CRUD for rally locations (name + nation). A location with stages can't be deleted. */
@Component({
  selector: 'app-acrally-locations',
  imports: [FormsModule],
  templateUrl: './acrally-locations.html',
})
export class AcrallyLocations implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly base = '/acrally-api/admin/locations';

  readonly locations = signal<LocationTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');

  readonly creating = signal(false);
  readonly newName = signal('');
  readonly newNation = signal('');

  readonly editingId = signal<string | null>(null);
  readonly editName = signal('');
  readonly editNation = signal('');
  readonly busy = signal(false);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.http.get<LocationTo[]>(this.base).subscribe({
      next: (list) => {
        this.locations.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load locations.');
        this.loaded.set(true);
      },
    });
  }

  toggleCreate(): void {
    this.error.set('');
    this.newName.set('');
    this.newNation.set('');
    this.creating.update((open) => !open);
  }

  create(): void {
    if (this.busy() || !this.newName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: LocationRequestTo = { name: this.newName(), nation: this.newNation() };
    this.http.post<LocationTo>(this.base, body).subscribe({
      next: (created) => {
        this.locations.update((list) => [...list, created].sort((a, b) => a.name.localeCompare(b.name)));
        this.busy.set(false);
        this.toggleCreate();
      },
      error: (err) => this.fail(err, 'Could not create the location.'),
    });
  }

  startEdit(location: LocationTo): void {
    this.error.set('');
    this.editingId.set(location.id);
    this.editName.set(location.name);
    this.editNation.set(location.nation ?? '');
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  save(location: LocationTo): void {
    if (this.busy() || !this.editName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: LocationRequestTo = { name: this.editName(), nation: this.editNation() };
    this.http.put<LocationTo>(`${this.base}/${location.id}`, body).subscribe({
      next: (updated) => {
        this.locations.update((list) =>
          list.map((l) => (l.id === updated.id ? updated : l)).sort((a, b) => a.name.localeCompare(b.name)),
        );
        this.busy.set(false);
        this.cancelEdit();
      },
      error: (err) => this.fail(err, 'Could not save the location.'),
    });
  }

  remove(location: LocationTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    this.http.delete(`${this.base}/${location.id}`).subscribe({
      next: () => {
        this.locations.update((list) => list.filter((l) => l.id !== location.id));
        this.busy.set(false);
      },
      error: (err) => this.fail(err, 'Could not delete the location.'),
    });
  }

  private fail(err: HttpErrorResponse, fallback: string): void {
    this.error.set(err.status === 409 ? err.error?.message ?? 'That action conflicts with existing data.' : fallback);
    this.busy.set(false);
  }
}
