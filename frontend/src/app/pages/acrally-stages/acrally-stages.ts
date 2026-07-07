import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import type { StageNameCollectResultTo, StageNameTo } from '../../models/acrally';

/**
 * Admin: the stage-name catalogue. "Collect" scans results for distinct raw stage identifiers
 * and adds any new ones (existing rows are untouched); each row's readable display name can be
 * edited inline. The display name is what the app renders for that stage in results.
 */
@Component({
  selector: 'app-acrally-stages',
  imports: [FormsModule],
  templateUrl: './acrally-stages.html',
})
export class AcrallyStages implements OnInit {
  private readonly http = inject(HttpClient);

  readonly stages = signal<StageNameTo[]>([]);
  readonly loaded = signal(false);
  readonly error = signal('');
  readonly collecting = signal(false);
  readonly collectMessage = signal('');

  /** The id of the row currently being edited, plus the working draft of its display name. */
  readonly editingId = signal<string | null>(null);
  readonly draft = signal('');
  readonly saving = signal(false);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.http.get<StageNameTo[]>('/acrally-api/admin/stage-names').subscribe({
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

  collect(): void {
    if (this.collecting()) {
      return;
    }
    this.error.set('');
    this.collectMessage.set('');
    this.collecting.set(true);
    this.http.post<StageNameCollectResultTo>('/acrally-api/admin/stage-names/collect', {}).subscribe({
      next: (result) => {
        this.stages.set(result.stages);
        this.collectMessage.set(
          result.added === 0
            ? 'No new stages — the catalogue is up to date.'
            : `Collected ${result.added} new stage${result.added === 1 ? '' : 's'}.`,
        );
        this.collecting.set(false);
      },
      error: () => {
        this.error.set('Could not collect stages.');
        this.collecting.set(false);
      },
    });
  }

  startEdit(stage: StageNameTo): void {
    this.editingId.set(stage.id);
    this.draft.set(stage.displayName ?? '');
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.draft.set('');
  }

  save(stage: StageNameTo): void {
    if (this.saving()) {
      return;
    }
    this.saving.set(true);
    this.http
      .put<StageNameTo>(`/acrally-api/admin/stage-names/${stage.id}`, { displayName: this.draft() })
      .subscribe({
        next: (updated) => {
          this.stages.update((list) => list.map((s) => (s.id === updated.id ? updated : s)));
          this.saving.set(false);
          this.cancelEdit();
        },
        error: () => {
          this.error.set('Could not save the display name.');
          this.saving.set(false);
        },
      });
  }
}
