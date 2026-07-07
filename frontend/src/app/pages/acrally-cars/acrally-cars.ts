import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import type { CarRequestTo, CarTo } from '../../models/acrally';

/** Admin CRUD for the car catalogue: name, year, group and class. */
@Component({
  selector: 'app-acrally-cars',
  imports: [FormsModule],
  templateUrl: './acrally-cars.html',
})
export class AcrallyCars implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly base = '/acrally-api/admin/cars';

  readonly cars = signal<CarTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');

  readonly creating = signal(false);
  readonly newName = signal('');
  readonly newYear = signal<number | null>(null);
  readonly newGroup = signal('');
  readonly newClass = signal('');

  readonly editingId = signal<string | null>(null);
  readonly editName = signal('');
  readonly editYear = signal<number | null>(null);
  readonly editGroup = signal('');
  readonly editClass = signal('');
  readonly busy = signal(false);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.http.get<CarTo[]>(this.base).subscribe({
      next: (list) => {
        this.cars.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load cars.');
        this.loaded.set(true);
      },
    });
  }

  toggleCreate(): void {
    this.error.set('');
    this.newName.set('');
    this.newYear.set(null);
    this.newGroup.set('');
    this.newClass.set('');
    this.creating.update((open) => !open);
  }

  create(): void {
    if (this.busy() || !this.newName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    this.http.post<CarTo>(this.base, this.body(this.newName(), this.newYear(), this.newGroup(), this.newClass())).subscribe({
      next: (created) => {
        this.cars.update((list) => [...list, created].sort((a, b) => a.name.localeCompare(b.name)));
        this.busy.set(false);
        this.toggleCreate();
      },
      error: (err) => this.fail(err, 'Could not create the car.'),
    });
  }

  startEdit(car: CarTo): void {
    this.error.set('');
    this.editingId.set(car.id);
    this.editName.set(car.name);
    this.editYear.set(car.year);
    this.editGroup.set(car.groupName ?? '');
    this.editClass.set(car.className ?? '');
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  save(car: CarTo): void {
    if (this.busy() || !this.editName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    this.http
      .put<CarTo>(`${this.base}/${car.id}`, this.body(this.editName(), this.editYear(), this.editGroup(), this.editClass()))
      .subscribe({
        next: (updated) => {
          this.cars.update((list) =>
            list.map((c) => (c.id === updated.id ? updated : c)).sort((a, b) => a.name.localeCompare(b.name)),
          );
          this.busy.set(false);
          this.cancelEdit();
        },
        error: (err) => this.fail(err, 'Could not save the car.'),
      });
  }

  remove(car: CarTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    this.http.delete(`${this.base}/${car.id}`).subscribe({
      next: () => {
        this.cars.update((list) => list.filter((c) => c.id !== car.id));
        this.busy.set(false);
      },
      error: (err) => this.fail(err, 'Could not delete the car.'),
    });
  }

  private body(name: string, year: number | null, group: string, className: string): CarRequestTo {
    return {
      name,
      year: year ?? null,
      groupName: group.trim() || null,
      className: className.trim() || null,
    };
  }

  private fail(err: HttpErrorResponse, fallback: string): void {
    this.error.set(err.status === 409 ? err.error?.message ?? 'That action conflicts with existing data.' : fallback);
    this.busy.set(false);
  }
}
