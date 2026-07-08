import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type {
  CarTo,
  ChampionshipDetailTo,
  ChampionshipEventTo,
  EventVariantTo,
  VariantTo,
} from '../../models/acrally';
import { AcrallyChampionshipView } from '../../shared/acrally-championship-view/acrally-championship-view';

interface VariantGroup {
  location: string;
  stages: { stage: string; variants: VariantTo[] }[];
}

interface CarGroup {
  group: string;
  cars: CarTo[];
}

/**
 * Championship editor. Owners of a draft build the schedule here — events (with derived open/close
 * dates), the ordered variants each event runs, and the cars each event permits. Once published
 * (or for non-owners) the page swaps to the shared compact championship view; the club page embeds
 * the same view inline. Every mutation returns the fresh detail aggregate, rebound wholesale.
 */
@Component({
  selector: 'app-acrally-championship',
  imports: [DatePipe, FormsModule, NgTemplateOutlet, RouterLink, AcrallyChampionshipView],
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
  /** Editing is locked once published — a published championship is read-only bar the unpublish. */
  readonly canEdit = computed(() => this.owner() && !this.published());

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
  readonly metaStartsAt = signal('');

  readonly addingEvent = signal(false);
  readonly newEventGap = signal(0);
  readonly newEventDuration = signal(7);

  readonly editingEventId = signal<string | null>(null);
  readonly editEventGap = signal(0);
  readonly editEventDuration = signal(7);

  readonly editingVariantsEventId = signal<string | null>(null);
  readonly editVariantIds = signal<string[]>([]);

  readonly editingCarsEventId = signal<string | null>(null);
  readonly editCarIds = signal<Set<string>>(new Set());

  // --- Event collapse (editor only): all events expand by default while building ---
  readonly expandedEvents = signal<Set<string>>(new Set());

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
          this.initEventState(detail);
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
    // A freshly added event should be open for its stage/car setup.
    const known = new Set((this.detail()?.events ?? []).map((e) => e.id));
    const expanded = new Set(this.expandedEvents());
    for (const event of detail.events) {
      if (!known.has(event.id)) {
        expanded.add(event.id);
      }
    }
    this.expandedEvents.set(expanded);
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

  /** Full stage label in "Location - Stage - Alias" order (the alias alone is usually just "Variant X"). */
  stageFullLabel(v: EventVariantTo): string {
    return [v.locationName, v.stageName, v.label].filter((p) => !!p && p.trim()).join(' - ');
  }

  // --- Event collapse (editor only; the read-only view manages its own) ---
  /** Start with every event expanded so the owner can build the draft without extra clicks. */
  private initEventState(detail: ChampionshipDetailTo): void {
    if (!this.canEdit()) return;
    this.expandedEvents.set(new Set(detail.events.map((event) => event.id)));
  }

  isEventExpanded(eventId: string): boolean {
    return this.expandedEvents().has(eventId);
  }

  toggleEvent(eventId: string): void {
    const open = new Set(this.expandedEvents());
    open.has(eventId) ? open.delete(eventId) : open.add(eventId);
    this.expandedEvents.set(open);
  }

  /** open | upcoming | closed, from the event's derived window against now. */
  eventPhase(event: ChampionshipEventTo): 'open' | 'upcoming' | 'closed' {
    const now = Date.now();
    if (now < new Date(event.opensAt).getTime()) return 'upcoming';
    if (now < new Date(event.closesAt).getTime()) return 'open';
    return 'closed';
  }

  eventPhaseLabel(event: ChampionshipEventTo): string {
    return { open: 'Open', upcoming: 'Upcoming', closed: 'Closed' }[this.eventPhase(event)];
  }

  /** Status-pill modifier for the phase badge. */
  eventPhaseClass(event: ChampionshipEventTo): string {
    return {
      open: 'status-pill--live',
      upcoming: 'status-pill--draft',
      closed: 'status-pill--muted',
    }[this.eventPhase(event)];
  }

  /** One-line summary shown as the collapsed event's title, e.g. "3 stages · 5 cars". */
  eventSummary(event: ChampionshipEventTo): string {
    const stages = `${event.variants.length} ${event.variants.length === 1 ? 'stage' : 'stages'}`;
    const cars = event.cars.length === 0
      ? 'any car'
      : `${event.cars.length} ${event.cars.length === 1 ? 'car' : 'cars'}`;
    return `${stages} · ${cars}`;
  }

  // --- Championship meta ---
  startEditMeta(): void {
    const d = this.detail();
    if (!d) return;
    this.metaName.set(d.name);
    // datetime-local wants "yyyy-MM-ddTHH:mm"; the server sends full ISO with seconds.
    this.metaStartsAt.set(d.startsAt.slice(0, 16));
    this.addingEvent.set(false);
    this.editingMeta.set(true);
  }

  saveMeta(): void {
    const d = this.detail();
    if (!d) return;
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/championships/${d.id}`, {
        name: this.metaName(),
        startsAt: this.metaStartsAt(),
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
        startsAt: d.startsAt,
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
  /**
   * Open the add-event form. It reuses the same variant/car picker state as the per-event editors
   * (only one editor is ever open at a time), so a new event can be fully configured — stages and
   * cars included — before it's created, with no follow-up edit.
   */
  startAddEvent(): void {
    this.newEventGap.set(0);
    this.newEventDuration.set(7);
    this.editVariantIds.set([]);
    this.editCarIds.set(new Set());
    this.editingMeta.set(false);
    this.editingEventId.set(null);
    this.editingVariantsEventId.set(null);
    this.editingCarsEventId.set(null);
    this.addingEvent.set(true);
  }

  addEvent(): void {
    const d = this.detail();
    if (!d) return;
    this.http
      .post<ChampionshipDetailTo>(`/acrally-api/championships/${d.id}/events`, {
        gapDays: this.newEventGap(),
        durationDays: this.newEventDuration(),
        variantIds: this.editVariantIds(),
        carIds: [...this.editCarIds()],
      })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  startEditEvent(event: ChampionshipEventTo): void {
    this.editEventGap.set(event.gapDays);
    this.editEventDuration.set(event.durationDays);
    this.addingEvent.set(false);
    this.editingEventId.set(event.id);
  }

  saveEvent(): void {
    const id = this.editingEventId();
    if (!id) return;
    this.http
      .put<ChampionshipDetailTo>(`/acrally-api/events/${id}`, {
        gapDays: this.editEventGap(),
        durationDays: this.editEventDuration(),
      })
      .subscribe({ next: (detail) => this.apply(detail), error: this.fail() });
  }

  deleteEvent(event: ChampionshipEventTo): void {
    if (!confirm(`Delete event “${event.label}”?`)) return;
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
    this.addingEvent.set(false);
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
    this.addingEvent.set(false);
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
