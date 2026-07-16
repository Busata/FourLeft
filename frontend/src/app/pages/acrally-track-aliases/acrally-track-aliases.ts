import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import type {
  CreateTrackAliasRequestTo,
  TrackAliasCollectResultTo,
  TrackAliasTo,
  UpdateTrackAliasRequestTo,
  VariantTo,
} from '../../models/acrally';

/**
 * Admin: track aliases. "Collect" scans sessions for the distinct track names live telemetry
 * reports and adds any new ones. Each alias is assigned to a variant so the server knows, the
 * moment a run starts, whether it is the armed stage (an armed run is final — restart or quit is
 * a DNF); an unassigned name errs toward binding, so assign these promptly.
 */
@Component({
  selector: 'app-acrally-track-aliases',
  imports: [FormsModule],
  templateUrl: './acrally-track-aliases.html',
})
export class AcrallyTrackAliases implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly base = '/acrally-api/admin/track-aliases';

  readonly aliases = signal<TrackAliasTo[]>([]);
  readonly variants = signal<VariantTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');
  readonly collecting = signal(false);
  readonly collectMessage = signal('');

  readonly editingId = signal<string | null>(null);
  readonly editVariantId = signal<string>('');
  readonly busy = signal(false);

  readonly creating = signal(false);
  readonly newRawName = signal('');
  readonly newVariantId = signal<string>('');

  /** Variants sorted by their readable label for the picker. */
  readonly variantOptions = computed(() =>
    this.variants()
      .map((v) => ({ id: v.id, label: this.variantLabel(v) }))
      .sort((a, b) => a.label.localeCompare(b.label)),
  );

  ngOnInit(): void {
    this.http.get<VariantTo[]>('/acrally-api/admin/variants').subscribe({
      next: (list) => this.variants.set(list),
      error: () => {},
    });
    this.load();
  }

  private variantLabel(v: VariantTo): string {
    const parts = [v.locationName, v.stageName, v.displayName ?? v.rawName].filter(Boolean);
    return parts.join(' - ');
  }

  private load(): void {
    this.http.get<TrackAliasTo[]>(this.base).subscribe({
      next: (list) => {
        this.aliases.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load track aliases.');
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
    this.http.post<TrackAliasCollectResultTo>(`${this.base}/collect`, {}).subscribe({
      next: (result) => {
        this.aliases.set(result.aliases);
        this.collectMessage.set(
          result.added === 0
            ? 'No new track names — everything telemetry has reported is already listed.'
            : `Collected ${result.added} new track name${result.added === 1 ? '' : 's'}.`,
        );
        this.collecting.set(false);
      },
      error: () => {
        this.error.set('Could not collect track aliases.');
        this.collecting.set(false);
      },
    });
  }

  toggleCreate(): void {
    this.error.set('');
    this.newRawName.set('');
    this.newVariantId.set('');
    this.creating.update((open) => !open);
  }

  create(): void {
    if (this.busy() || !this.newRawName().trim()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: CreateTrackAliasRequestTo = {
      rawName: this.newRawName().trim(),
      variantId: this.newVariantId() || null,
    };
    this.http.post<TrackAliasTo>(this.base, body).subscribe({
      next: (created) => {
        this.aliases.update((list) => [...list, created].sort((a, b) => a.rawName.localeCompare(b.rawName)));
        this.busy.set(false);
        this.toggleCreate();
      },
      error: (err) => this.fail(err, 'Could not add the track alias.'),
    });
  }

  startEdit(alias: TrackAliasTo): void {
    this.error.set('');
    this.editingId.set(alias.id);
    this.editVariantId.set(alias.variantId ?? '');
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  save(alias: TrackAliasTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: UpdateTrackAliasRequestTo = { variantId: this.editVariantId() || null };
    this.http.put<TrackAliasTo>(`${this.base}/${alias.id}`, body).subscribe({
      next: (updated) => {
        this.aliases.update((list) => list.map((a) => (a.id === updated.id ? updated : a)));
        this.busy.set(false);
        this.cancelEdit();
      },
      error: (err) => this.fail(err, 'Could not save the track alias.'),
    });
  }

  remove(alias: TrackAliasTo): void {
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
      error: (err) => this.fail(err, 'Could not delete the track alias.'),
    });
  }

  private fail(err: HttpErrorResponse, fallback: string): void {
    this.error.set(err.status === 409 ? err.error?.message ?? 'That action conflicts with existing data.' : fallback);
    this.busy.set(false);
  }
}
