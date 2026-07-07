import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type {
  CarTo,
  ChampionshipDetailTo,
  ChampionshipEventTo,
  VariantTo,
} from '../../models/acrally';

interface VariantGroup {
  location: string;
  stages: { stage: string; variants: VariantTo[] }[];
}

interface CarGroup {
  group: string;
  cars: CarTo[];
}

/**
 * Championship editor / viewer. Owners build the schedule here — events (with derived open/close
 * dates), the ordered variants each event runs, and the cars each event permits. Non-owners get a
 * read-only view. Every mutation returns the fresh detail aggregate, which we rebind wholesale.
 */
@Component({
  selector: 'app-acrally-championship',
  imports: [DatePipe, FormsModule, RouterLink],
  templateUrl: './acrally-championship.html',
  styleUrl: './acrally-championship.scss',
})
export class AcrallyChampionship implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly clubId = signal('');
  readonly championshipId = signal('');
  readonly detail = signal<ChampionshipDetailTo | null>(null);
  readonly loaded = signal(false);
  readonly notFound = signal(false);
  readonly error = signal('');

  readonly cars = signal<CarTo[]>([]);
  readonly variants = signal<VariantTo[]>([]);

  readonly owner = computed(() => this.detail()?.owner ?? false);
  readonly published = computed(() => this.detail()?.status === 'PUBLISHED');

  /** variantId → catalogue variant, for labelling the ordered selection. */
  private readonly variantById = computed(
    () => new Map(this.variants().map((v) => [v.id, v])),
  );

  readonly variantGroups = computed<VariantGroup[]>(() => {
    const byLocation = new Map<string, Map<string, VariantTo[]>>();
    for (const v of this.variants()) {
      const loc = v.locationName ?? 'Unassigned';
      const stage = v.stageName ?? '—';
      const stages = byLocation.get(loc) ?? new Map<string, VariantTo[]>();
      const list = stages.get(stage) ?? [];
      list.push(v);
      stages.set(stage, list);
      byLocation.set(loc, stages);
    }
    return [...byLocation.entries()]
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([location, stages]) => ({
        location,
        stages: [...stages.entries()]
          .sort((a, b) => a[0].localeCompare(b[0]))
          .map(([stage, variants]) => ({ stage, variants })),
      }));
  });

  readonly carGroups = computed<CarGroup[]>(() => {
    const byGroup = new Map<string, CarTo[]>();
    for (const c of this.cars()) {
      const group = c.groupName ?? 'Other';
      const list = byGroup.get(group) ?? [];
      list.push(c);
      byGroup.set(group, list);
    }
    return [...byGroup.entries()]
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([group, cars]) => ({ group, cars }));
  });

  // --- Inline editor state ---
  readonly editingMeta = signal(false);
  readonly metaName = signal('');
  readonly metaStartDate = signal('');

  readonly addingEvent = signal(false);
  readonly newEventName = signal('');
  readonly newEventGap = signal(0);
  readonly newEventDuration = signal(7);

  readonly editingEventId = signal<string | null>(null);
  readonly editEventName = signal('');
  readonly editEventGap = signal(0);
  readonly editEventDuration = signal(7);

  readonly editingVariantsEventId = signal<string | null>(null);
  readonly editVariantIds = signal<string[]>([]);

  readonly editingCarsEventId = signal<string | null>(null);
  readonly editCarIds = signal<Set<string>>(new Set());

  ngOnInit(): void {
    this.clubId.set(this.route.snapshot.paramMap.get('clubId') ?? '');
    this.championshipId.set(this.route.snapshot.paramMap.get('championshipId') ?? '');
    this.loadCatalogue();
    this.load();
  }

  private load(): void {
    this.http
      .get<ChampionshipDetailTo>(`/acrally-api/championships/${this.championshipId()}`)
      .subscribe({
        next: (detail) => {
          this.detail.set(detail);
          this.loaded.set(true);
        },
        error: (err) => {
          this.notFound.set(err.status === 404 || err.status === 403);
          this.loaded.set(true);
        },
      });
  }

  private loadCatalogue(): void {
    this.http.get<CarTo[]>('/acrally-api/cars').subscribe({ next: (list) => this.cars.set(list) });
    this.http
      .get<VariantTo[]>('/acrally-api/variants')
      .subscribe({ next: (list) => this.variants.set(list) });
  }

  private apply(detail: ChampionshipDetailTo): void {
    this.detail.set(detail);
    this.error.set('');
    this.editingMeta.set(false);
    this.addingEvent.set(false);
    this.editingEventId.set(null);
    this.editingVariantsEventId.set(null);
    this.editingCarsEventId.set(null);
  }

  private fail(): (err: unknown) => void {
    return (err: unknown) => {
      const status = (err as { status?: number }).status;
      this.error.set(status === 403 ? 'Only the club owner can edit this.' : 'Something went wrong.');
    };
  }

  variantLabel(variantId: string): string {
    return this.variantById().get(variantId)?.displayName
      ?? this.variantById().get(variantId)?.rawName
      ?? '(removed variant)';
  }

  // --- Championship meta ---
  startEditMeta(): void {
    const d = this.detail();
    if (!d) return;
    this.metaName.set(d.name);
    this.metaStartDate.set(d.startDate);
    this.editingMeta.set(true);
  }

  saveMeta(): void {
    const d = this.detail();
    if (!d) return;
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/championships/${d.id}`, {
        name: this.metaName(),
        startDate: this.metaStartDate(),
        status: d.status,
      })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  togglePublish(): void {
    const d = this.detail();
    if (!d) return;
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/championships/${d.id}`, {
        name: d.name,
        startDate: d.startDate,
        status: this.published() ? 'DRAFT' : 'PUBLISHED',
      })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  deleteChampionship(): void {
    const d = this.detail();
    if (!d || !confirm('Delete this championship and all its events?')) return;
    this.http.delete(`/acrally-api/championships/${d.id}`).subscribe({
      next: () => this.router.navigate(['/acrally/dashboard/clubs', this.clubId()]),
      error: this.fail(),
    });
  }

  // --- Events ---
  addEvent(): void {
    const d = this.detail();
    if (!d || !this.newEventName().trim()) return;
    this.http
      .post<ChampionshipDetailTo>(`/acrally-api/championships/${d.id}/events`, {
        name: this.newEventName(),
        gapDays: this.newEventGap(),
        durationDays: this.newEventDuration(),
      })
      .subscribe({
        next: (detail) => {
          this.apply(detail);
          this.newEventName.set('');
          this.newEventGap.set(0);
          this.newEventDuration.set(7);
        },
        error: this.fail(),
      });
  }

  startEditEvent(event: ChampionshipEventTo): void {
    this.editEventName.set(event.name);
    this.editEventGap.set(event.gapDays);
    this.editEventDuration.set(event.durationDays);
    this.editingEventId.set(event.id);
  }

  saveEvent(): void {
    const id = this.editingEventId();
    if (!id || !this.editEventName().trim()) return;
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/events/${id}`, {
        name: this.editEventName(),
        gapDays: this.editEventGap(),
        durationDays: this.editEventDuration(),
      })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  deleteEvent(event: ChampionshipEventTo): void {
    if (!confirm(`Delete event “${event.name}”?`)) return;
    this.http
      .delete<ChampionshipDetailTo>(`/acrally-api/events/${event.id}`)
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  moveEvent(event: ChampionshipEventTo, direction: -1 | 1): void {
    const d = this.detail();
    if (!d) return;
    const ids = d.events.map((e) => e.id);
    const from = ids.indexOf(event.id);
    const to = from + direction;
    if (to < 0 || to >= ids.length) return;
    [ids[from], ids[to]] = [ids[to], ids[from]];
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/championships/${d.id}/events/order`, { eventIds: ids })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  // --- Event variants ---
  startEditVariants(event: ChampionshipEventTo): void {
    this.editVariantIds.set(event.variants.map((v) => v.variantId));
    this.editingCarsEventId.set(null);
    this.editingVariantsEventId.set(event.id);
  }

  isVariantSelected(variantId: string): boolean {
    return this.editVariantIds().includes(variantId);
  }

  addVariant(variantId: string): void {
    if (!this.editVariantIds().includes(variantId)) {
      this.editVariantIds.update((ids) => [...ids, variantId]);
    }
  }

  removeVariant(variantId: string): void {
    this.editVariantIds.update((ids) => ids.filter((id) => id !== variantId));
  }

  moveVariant(index: number, direction: -1 | 1): void {
    this.editVariantIds.update((ids) => {
      const to = index + direction;
      if (to < 0 || to >= ids.length) return ids;
      const next = [...ids];
      [next[index], next[to]] = [next[to], next[index]];
      return next;
    });
  }

  saveVariants(): void {
    const id = this.editingVariantsEventId();
    if (!id) return;
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/events/${id}/variants`, {
        variantIds: this.editVariantIds(),
      })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  // --- Event cars ---
  startEditCars(event: ChampionshipEventTo): void {
    this.editCarIds.set(new Set(event.cars.map((c) => c.id)));
    this.editingVariantsEventId.set(null);
    this.editingCarsEventId.set(event.id);
  }

  isCarSelected(carId: string): boolean {
    return this.editCarIds().has(carId);
  }

  toggleCar(carId: string): void {
    this.editCarIds.update((set) => {
      const next = new Set(set);
      next.has(carId) ? next.delete(carId) : next.add(carId);
      return next;
    });
  }

  isGroupFullySelected(group: CarGroup): boolean {
    return group.cars.length > 0 && group.cars.every((c) => this.editCarIds().has(c.id));
  }

  toggleGroup(group: CarGroup): void {
    const allSelected = this.isGroupFullySelected(group);
    this.editCarIds.update((set) => {
      const next = new Set(set);
      for (const c of group.cars) {
        allSelected ? next.delete(c.id) : next.add(c.id);
      }
      return next;
    });
  }

  get allCarsSelected(): boolean {
    return this.cars().length > 0 && this.cars().every((c) => this.editCarIds().has(c.id));
  }

  toggleAllCars(): void {
    const all = this.allCarsSelected;
    this.editCarIds.set(all ? new Set() : new Set(this.cars().map((c) => c.id)));
  }

  saveCars(): void {
    const id = this.editingCarsEventId();
    if (!id) return;
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/events/${id}/cars`, {
        carIds: [...this.editCarIds()],
      })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  cancelEditors(): void {
    this.editingVariantsEventId.set(null);
    this.editingCarsEventId.set(null);
    this.editingEventId.set(null);
  }
}
