import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

import type { StageTo, UpdateVariantRequestTo, VariantCollectResultTo, VariantTo } from '../../models/acrally';

/**
 * Admin: the variant catalogue. "Collect" scans results for distinct raw stage keys and adds any
 * new ones (existing rows untouched). Each variant gets an optional readable display name and can
 * be assigned to a stage; the app resolves the raw key up the chain (variant → stage → location).
 */
@Component({
  selector: 'app-acrally-variants',
  imports: [FormsModule],
  templateUrl: './acrally-variants.html',
})
export class AcrallyVariants implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly base = '/acrally-api/admin/variants';

  readonly variants = signal<VariantTo[]>([]);
  readonly stages = signal<StageTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');
  readonly collecting = signal(false);
  readonly collectMessage = signal('');

  readonly editingId = signal<string | null>(null);
  readonly editDisplayName = signal('');
  readonly editStageId = signal<string>('');
  readonly busy = signal(false);

  ngOnInit(): void {
    this.http.get<StageTo[]>('/acrally-api/admin/stages').subscribe({
      next: (list) => this.stages.set(list),
      error: () => {},
    });
    this.load();
  }

  private load(): void {
    this.http.get<VariantTo[]>(this.base).subscribe({
      next: (list) => {
        this.variants.set(list);
        this.loaded.set(true);
      },
      error: () => {
        this.error.set('Could not load variants.');
        this.loaded.set(true);
      },
    });
  }

  /** A "Location — Stage" label for a stage option in the dropdown. */
  stageLabel(stage: StageTo): string {
    return stage.locationName ? `${stage.locationName} — ${stage.name}` : stage.name;
  }

  collect(): void {
    if (this.collecting()) {
      return;
    }
    this.error.set('');
    this.collectMessage.set('');
    this.collecting.set(true);
    this.http.post<VariantCollectResultTo>(`${this.base}/collect`, {}).subscribe({
      next: (result) => {
        this.variants.set(result.variants);
        this.collectMessage.set(
          result.added === 0
            ? 'No new variants — the catalogue is up to date.'
            : `Collected ${result.added} new variant${result.added === 1 ? '' : 's'}.`,
        );
        this.collecting.set(false);
      },
      error: () => {
        this.error.set('Could not collect variants.');
        this.collecting.set(false);
      },
    });
  }

  startEdit(variant: VariantTo): void {
    this.error.set('');
    this.editingId.set(variant.id);
    this.editDisplayName.set(variant.displayName ?? '');
    this.editStageId.set(variant.stageId ?? '');
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  save(variant: VariantTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    const body: UpdateVariantRequestTo = {
      displayName: this.editDisplayName(),
      stageId: this.editStageId() || null,
    };
    this.http.put<VariantTo>(`${this.base}/${variant.id}`, body).subscribe({
      next: (updated) => {
        this.variants.update((list) => list.map((v) => (v.id === updated.id ? updated : v)));
        this.busy.set(false);
        this.cancelEdit();
      },
      error: (err) => this.fail(err, 'Could not save the variant.'),
    });
  }

  remove(variant: VariantTo): void {
    if (this.busy()) {
      return;
    }
    this.error.set('');
    this.busy.set(true);
    this.http.delete(`${this.base}/${variant.id}`).subscribe({
      next: () => {
        this.variants.update((list) => list.filter((v) => v.id !== variant.id));
        this.busy.set(false);
      },
      error: (err) => this.fail(err, 'Could not delete the variant.'),
    });
  }

  private fail(err: HttpErrorResponse, fallback: string): void {
    this.error.set(err.status === 409 ? err.error?.message ?? 'That action conflicts with existing data.' : fallback);
    this.busy.set(false);
  }
}
