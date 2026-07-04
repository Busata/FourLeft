import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { ChannelConfiguration, ScoringAnchors, ScoringAnchorEntry, ScoringStrategy } from '../../models/channel-configuration';
import { SlideToggle } from '../../shared/slide-toggle/slide-toggle';

type ScoringRow = FormGroup<{
  position: FormControl<number | null>;
  points: FormControl<number | null>;
}>;

// A POINT_ANCHOR row is either an anchor (position + points) or a decrease (position + decrease per position).
type AnchorRow = FormGroup<{
  kind: FormControl<'anchor' | 'decrease'>;
  position: FormControl<number | null>;
  points: FormControl<number | null>;
  decrease: FormControl<number | null>;
}>;

const DEFAULT_FLOOR = 1;

@Component({
  selector: 'app-channel-config',
  imports: [ReactiveFormsModule, SlideToggle],
  templateUrl: './channel-config.html',
})
export class ChannelConfig implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);

  private readonly requestId = this.route.snapshot.paramMap.get('requestId') ?? '';
  private readonly base = `/api_v2/configuration/channel/${this.requestId}`;

  readonly loaded = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly config = signal<ChannelConfiguration | null>(null);
  // Mirrors the customScoringEnabled control so the template can reveal the table reactively.
  readonly customScoringOn = signal(false);
  // Mirrors the scoringStrategy control so the template can switch between the two editors reactively.
  readonly scoringStrategySig = signal<ScoringStrategy>('LOOKUP_TABLE');

  readonly form = new FormGroup({
    clubId: new FormControl<string>('', { nonNullable: true }),
    autopostingEnabled: new FormControl<boolean>(true, { nonNullable: true }),
    requiresTracking: new FormControl<boolean>(false, { nonNullable: true }),
    customScoringEnabled: new FormControl<boolean>(false, { nonNullable: true }),
    scoringStrategy: new FormControl<ScoringStrategy>('LOOKUP_TABLE', { nonNullable: true }),
    scoringTable: new FormArray<ScoringRow>([]),
    scoringFloor: new FormControl<number>(DEFAULT_FLOOR, { nonNullable: true }),
    scoringAnchors: new FormArray<AnchorRow>([]),
  });

  get scoringTable(): FormArray<ScoringRow> {
    return this.form.controls.scoringTable;
  }

  get scoringAnchors(): FormArray<AnchorRow> {
    return this.form.controls.scoringAnchors;
  }

  ngOnInit(): void {
    this.form.controls.customScoringEnabled.valueChanges.subscribe((on) => this.customScoringOn.set(on));
    this.form.controls.scoringStrategy.valueChanges.subscribe((s) => this.scoringStrategySig.set(s));

    this.http.get<ChannelConfiguration>(this.base).subscribe({
      next: (config) => this.apply(config),
      error: () => this.error.set('Could not load channel configuration.'),
    });
  }

  addRow(position: number | null = null, points: number | null = null): void {
    this.scoringTable.push(
      new FormGroup({
        position: new FormControl<number | null>(position),
        points: new FormControl<number | null>(points),
      }),
    );
  }

  removeRow(index: number): void {
    this.scoringTable.removeAt(index);
  }

  addAnchor(position: number | null = null, points: number | null = null): void {
    this.scoringAnchors.push(
      new FormGroup({
        kind: new FormControl<'anchor' | 'decrease'>('anchor', { nonNullable: true }),
        position: new FormControl<number | null>(position),
        points: new FormControl<number | null>(points),
        decrease: new FormControl<number | null>(null),
      }),
    );
  }

  addDecrease(position: number | null = null, decrease: number | null = null): void {
    this.scoringAnchors.push(
      new FormGroup({
        kind: new FormControl<'anchor' | 'decrease'>('decrease', { nonNullable: true }),
        position: new FormControl<number | null>(position),
        points: new FormControl<number | null>(null),
        decrease: new FormControl<number | null>(decrease),
      }),
    );
  }

  removeAnchorRow(index: number): void {
    this.scoringAnchors.removeAt(index);
  }

  create(): void {
    this.http
      .post<ChannelConfiguration>(this.base, {
        clubId: this.form.controls.clubId.value,
        autopostingEnabled: this.form.controls.autopostingEnabled.value,
        requiresTracking: this.form.controls.requiresTracking.value,
      })
      .subscribe({
        next: (config) => {
          this.apply(config);
          this.flash('Configuration created.');
        },
        error: () => this.flash('Could not create configuration.'),
      });
  }

  save(): void {
    this.http
      .put<ChannelConfiguration>(this.base, {
        autopostingEnabled: this.form.controls.autopostingEnabled.value,
        requiresTracking: this.form.controls.requiresTracking.value,
        customScoringEnabled: this.form.controls.customScoringEnabled.value,
        scoringStrategy: this.form.controls.scoringStrategy.value,
        scoringTable: this.rowsToTable(),
        scoringAnchors: this.rowsToAnchors(),
      })
      .subscribe({
        next: (config) => {
          this.apply(config);
          this.flash('Configuration saved.');
        },
        error: () => this.flash('Could not save configuration.'),
      });
  }

  remove(): void {
    this.http.delete<ChannelConfiguration>(this.base).subscribe({
      next: (config) => {
        this.apply(config);
        this.flash('Configuration removed.');
      },
      error: () => this.flash('Could not remove configuration.'),
    });
  }

  // Build the position->points map the backend expects, dropping incomplete rows.
  private rowsToTable(): Record<string, number> {
    const table: Record<string, number> = {};
    for (const row of this.scoringTable.controls) {
      const position = row.controls.position.value;
      const points = row.controls.points.value;
      if (position != null && points != null) {
        table[String(position)] = points;
      }
    }
    return table;
  }

  // Build the anchor definition the backend expects, dropping incomplete rows and sorting by position.
  private rowsToAnchors(): ScoringAnchors {
    const entries: ScoringAnchorEntry[] = [];
    for (const row of this.scoringAnchors.controls) {
      const position = row.controls.position.value;
      if (position == null) {
        continue;
      }
      if (row.controls.kind.value === 'anchor') {
        const points = row.controls.points.value;
        if (points != null) {
          entries.push({ position, points });
        }
      } else {
        const decrease = row.controls.decrease.value;
        if (decrease != null) {
          entries.push({ position, decrease });
        }
      }
    }
    entries.sort((a, b) => a.position - b.position);
    return { floor: this.form.controls.scoringFloor.value, entries };
  }

  private apply(config: ChannelConfiguration | null): void {
    if (!config) {
      this.error.set('This configuration link is invalid or has expired.');
      return;
    }
    this.config.set(config);

    this.scoringTable.clear();
    const entries = Object.entries(config.scoringTable ?? {}).sort((a, b) => Number(a[0]) - Number(b[0]));
    for (const [position, points] of entries) {
      this.addRow(Number(position), points);
    }

    this.scoringAnchors.clear();
    const anchors = config.scoringAnchors;
    this.form.controls.scoringFloor.setValue(anchors?.floor ?? DEFAULT_FLOOR);
    for (const entry of [...(anchors?.entries ?? [])].sort((a, b) => a.position - b.position)) {
      if (entry.points != null) {
        this.addAnchor(entry.position, entry.points);
      } else {
        this.addDecrease(entry.position, entry.decrease ?? null);
      }
    }

    this.form.controls.clubId.setValue(config.clubId ?? '');
    this.form.controls.autopostingEnabled.setValue(config.autopostingEnabled ?? true);
    this.form.controls.requiresTracking.setValue(config.requiresTracking ?? false);
    this.form.controls.customScoringEnabled.setValue(config.customScoringEnabled ?? false);
    this.form.controls.scoringStrategy.setValue(config.scoringStrategy ?? 'LOOKUP_TABLE');
    this.customScoringOn.set(config.customScoringEnabled ?? false);
    this.scoringStrategySig.set(config.scoringStrategy ?? 'LOOKUP_TABLE');

    this.loaded.set(true);
  }

  private flash(message: string): void {
    this.notice.set(message);
    setTimeout(() => this.notice.set(''), 4000);
  }
}
