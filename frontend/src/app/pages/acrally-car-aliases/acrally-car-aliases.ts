import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import type {
  CarAliasCollectResultTo,
  CarAliasTo,
  CarTo,
  CreateCarAliasRequestTo,
  UpdateCarAliasRequestTo,
} from '../../models/acrally';

/**
 * Admin: car aliases. "Collect" scans results for the distinct raw car strings the game reports and
 * adds any new ones. Each alias is assigned to a catalogue car so results (and the agent's live
 * check) match on what the game says (e.g. "Lancia Delta Integrale Evo") rather than the catalogue name.
 */
@Component({
  selector: 'app-acrally-car-aliases',
  imports: [FormsModule],
  templateUrl: './acrally-car-aliases.html',
})
export class AcrallyCarAliases implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly base = '/acrally-api/admin/car-aliases';

  readonly aliases = signal<CarAliasTo[]>([]);
  readonly cars = signal<CarTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');
  readonly collecting = signal(false);
  readonly collectMessage = signal('');

  readonly editingId = signal<string | null>(null);
  readonly editCarId = signal<string>('');
  readonly busy = signal(false);

  readonly creating = signal(false);
  readonly newRawName = signal('');
  readonly newCarId = signal<string>('');

  ngOnInit(): void {
    this.http.get<CarTo[]>('/acrally-api/admin/cars').subscribe({
      next: (list) => this.cars.set(list),
      error: () => {},
    });
    this.load();
  }

  private load(): void {
    this.http.get<CarAliasTo[]>(this.base).subscribe({
      next: (list) => {
        this.aliases.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load car aliases.');
        this.loaded.set(true);
      },
    });
  }

  collect(): void {
    if (this.collecting()) {
      return;
    }
    this.error.set('');
    this.collectMessage.set('');
    this.collecting.set(true);
    this.http.post<CarAliasCollectResultTo>(`${this.base}/collect`, {}).subscribe({
      next: (result) => {
        this.aliases.set(result.aliases);
        this.collectMessage.set(
          result.added === 0
            ? 'No new car names — everything the game has reported is already listed.'
            : `Collected ${result.added} new car name${result.added === 1 ? '' : 's'}.`,
        );
        this.collecting.set(false);
      },
      error: () => {
        this.error.set('Could not collect car aliases.');
        this.collecting.set(false);
      },
    });
  }

  toggleCreate(): void {
    this.error.set('');
    this.newRawName.set('');
    this.newCarId.set('');
    this.creating.update((open) => !open);
  }

  create(): void {
    if (this.busy() || !this.newRawName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: CreateCarAliasRequestTo = { rawName: this.newRawName().trim(), carId: this.newCarId() || null };
    this.http.post<CarAliasTo>(this.base, body).subscribe({
      next: (created) => {
        this.aliases.update((list) => [...list, created].sort((a, b) => a.rawName.localeCompare(b.rawName)));
        this.busy.set(false);
        this.toggleCreate();
      },
      error: (err) => this.fail(err, 'Could not add the car alias.'),
    });
  }

  startEdit(alias: CarAliasTo): void {
    this.error.set('');
    this.editingId.set(alias.id);
    this.editCarId.set(alias.carId ?? '');
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  save(alias: CarAliasTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: UpdateCarAliasRequestTo = { carId: this.editCarId() || null };
    this.http.put<CarAliasTo>(`${this.base}/${alias.id}`, body).subscribe({
      next: (updated) => {
        this.aliases.update((list) => list.map((a) => (a.id === updated.id ? updated : a)));
        this.busy.set(false);
        this.cancelEdit();
      },
      error: (err) => this.fail(err, 'Could not save the car alias.'),
    });
  }

  remove(alias: CarAliasTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    this.http.delete(`${this.base}/${alias.id}`).subscribe({
      next: () => {
        this.aliases.update((list) => list.filter((a) => a.id !== alias.id));
        this.busy.set(false);
      },
      error: (err) => this.fail(err, 'Could not delete the car alias.'),
    });
  }

  private fail(err: HttpErrorResponse, fallback: string): void {
    this.error.set(err.status === 409 ? err.error?.message ?? 'That action conflicts with existing data.' : fallback);
    this.busy.set(false);
  }
}
