import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

import {
  ChannelConfiguration,
  EventRestriction,
  RestrictionDisplayMode,
  RestrictionScoringMode,
  RestrictionTargetChampionship,
  RestrictionTargetEvent,
  RestrictionTargets,
  ScoringAnchors,
  ScoringAnchorEntry,
  ScoringStrategy,
} from '../../models/channel-configuration';
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

// One restriction rule: a target (championship, or one of its events), the two violation modes and
// the allowed-vehicle set. An empty eventId means the whole championship.
type RestrictionRow = FormGroup<{
  championshipId: FormControl<string>;
  eventId: FormControl<string>;
  displayMode: FormControl<RestrictionDisplayMode>;
  scoringMode: FormControl<RestrictionScoringMode>;
  penaltyPoints: FormControl<number | null>;
  allowedVehicles: FormControl<string[]>;
}>;

const DEFAULT_FLOOR = 1;

// How far the preview expands positions before giving up on reaching the floor (e.g. decrease of 0).
// Club 146's real definition only floors at position 1001, so this needs headroom beyond that.
const PREVIEW_CAP = 2000;

// One row of the preview leaderboard: a finishing position (or the "N+" floor tail) and its points.
interface PreviewRow {
  label: string;
  points: number;
}

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
  // Live expansion of the anchor definition to points per position, recomputed on every form change.
  readonly anchorPreview = signal<PreviewRow[]>([]);
  readonly anchorPreviewTruncated = signal(false);
  // The club's championships/events a restriction can target, and the vehicles seen on its boards.
  readonly restrictionTargets = signal<RestrictionTargetChampionship[]>([]);
  readonly vehicles = signal<string[]>([]);
  // The pickers are static per club; fetch them once, not on every save round-trip.
  private pickersLoaded = false;

  readonly form = new FormGroup({
    clubId: new FormControl<string>('', { nonNullable: true }),
    autopostingEnabled: new FormControl<boolean>(true, { nonNullable: true }),
    requiresTracking: new FormControl<boolean>(false, { nonNullable: true }),
    customScoringEnabled: new FormControl<boolean>(false, { nonNullable: true }),
    scoringStrategy: new FormControl<ScoringStrategy>('LOOKUP_TABLE', { nonNullable: true }),
    scoringTable: new FormArray<ScoringRow>([]),
    scoringFloor: new FormControl<number>(DEFAULT_FLOOR, { nonNullable: true }),
    scoringAnchors: new FormArray<AnchorRow>([]),
    eventRestrictions: new FormArray<RestrictionRow>([]),
  });

  get scoringTable(): FormArray<ScoringRow> {
    return this.form.controls.scoringTable;
  }

  get scoringAnchors(): FormArray<AnchorRow> {
    return this.form.controls.scoringAnchors;
  }

  get eventRestrictions(): FormArray<RestrictionRow> {
    return this.form.controls.eventRestrictions;
  }

  ngOnInit(): void {
    this.form.controls.customScoringEnabled.valueChanges.subscribe((on) => this.customScoringOn.set(on));
    this.form.controls.scoringStrategy.valueChanges.subscribe((s) => this.scoringStrategySig.set(s));
    this.form.valueChanges.subscribe(() => this.updateAnchorPreview());

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

  addRestriction(rule: EventRestriction | null = null): void {
    // Only open championships are offered, so there's usually exactly one — preselect it.
    const targets = this.restrictionTargets();
    const defaultChampionshipId = targets.length === 1 ? targets[0].id : '';
    const row: RestrictionRow = new FormGroup({
      championshipId: new FormControl<string>(rule?.championshipId ?? defaultChampionshipId, { nonNullable: true }),
      eventId: new FormControl<string>(rule?.eventId ?? '', { nonNullable: true }),
      displayMode: new FormControl<RestrictionDisplayMode>(rule?.displayMode ?? 'WARN', { nonNullable: true }),
      scoringMode: new FormControl<RestrictionScoringMode>(rule?.scoringMode ?? 'EXCLUDE', { nonNullable: true }),
      penaltyPoints: new FormControl<number | null>(rule?.penaltyPoints ?? null),
      allowedVehicles: new FormControl<string[]>(rule?.allowedVehicles ?? [], { nonNullable: true }),
    });
    // A rule targets one championship's scope; picking another one invalidates the event choice.
    row.controls.championshipId.valueChanges.subscribe(() => row.controls.eventId.setValue(''));
    this.eventRestrictions.push(row);
  }

  removeRestriction(index: number): void {
    this.eventRestrictions.removeAt(index);
  }

  toggleVehicle(row: RestrictionRow, vehicle: string): void {
    const current = row.controls.allowedVehicles.value;
    row.controls.allowedVehicles.setValue(
      current.includes(vehicle) ? current.filter((v) => v !== vehicle) : [...current, vehicle],
    );
  }

  eventsFor(row: RestrictionRow): RestrictionTargetEvent[] {
    return this.restrictionTargets().find((ch) => ch.id === row.controls.championshipId.value)?.events ?? [];
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
        eventRestrictions: this.rowsToRestrictions(),
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

  // Build the restriction rules the backend expects, dropping rows without a target or vehicles.
  private rowsToRestrictions(): EventRestriction[] {
    const rules: EventRestriction[] = [];
    for (const row of this.eventRestrictions.controls) {
      const championshipId = row.controls.championshipId.value;
      const eventId = row.controls.eventId.value;
      const allowedVehicles = row.controls.allowedVehicles.value;
      if (!championshipId || allowedVehicles.length === 0) {
        continue;
      }
      const scoringMode = row.controls.scoringMode.value;
      rules.push({
        type: 'VEHICLE_ALLOWLIST',
        championshipId,
        eventId: eventId || null,
        displayMode: row.controls.displayMode.value,
        scoringMode,
        penaltyPoints: scoringMode === 'PENALTY' ? (row.controls.penaltyPoints.value ?? 0) : null,
        allowedVehicles,
      });
    }
    return rules;
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

  // Mirrors the backend's ScoringService.anchorPoints: walk positions in hundredths of a point
  // (integer math), where an anchor sets the running value and stops any active decrease, a decrease
  // keeps subtracting until overridden, and anything else scores the floor (which also clamps).
  private updateAnchorPreview(): void {
    const { floor, entries } = this.rowsToAnchors();
    if (entries.length === 0) {
      this.anchorPreview.set([]);
      this.anchorPreviewTruncated.set(false);
      return;
    }

    const roundHundredths = (h: number) => (h >= 0 ? Math.floor((h + 50) / 100) : -Math.floor((-h + 50) / 100));

    const expanded: number[] = [];
    let runningHundredths: number | null = null;
    let stepHundredths = 0;
    let decreaseActive = false;
    let entryIndex = 0;

    for (let p = 1; p <= PREVIEW_CAP; p++) {
      let handled = false;

      while (entryIndex < entries.length && entries[entryIndex].position === p) {
        const entry = entries[entryIndex++];
        if (entry.points != null) {
          runningHundredths = entry.points * 100;
          decreaseActive = false;
        } else {
          stepHundredths = Math.round((entry.decrease ?? 0) * 100);
          decreaseActive = true;
          if (runningHundredths != null) {
            runningHundredths -= stepHundredths;
          }
        }
        handled = true;
      }

      if (!handled && decreaseActive && runningHundredths != null) {
        runningHundredths -= stepHundredths;
        handled = true;
      }

      expanded.push(handled && runningHundredths != null ? Math.max(roundHundredths(runningHundredths), floor) : floor);
    }

    // Everything past the last position that beats the floor scores the floor forever; collapse it into one row.
    let lastAboveFloor = -1;
    for (let i = 0; i < expanded.length; i++) {
      if (expanded[i] !== floor) {
        lastAboveFloor = i;
      }
    }

    const rows: PreviewRow[] = [];
    for (let i = 0; i <= lastAboveFloor; i++) {
      rows.push({ label: `${i + 1}`, points: expanded[i] });
    }

    const truncated = lastAboveFloor === PREVIEW_CAP - 1;
    if (!truncated) {
      rows.push({ label: `${lastAboveFloor + 2}+`, points: floor });
    }
    this.anchorPreview.set(rows);
    this.anchorPreviewTruncated.set(truncated);
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

    this.eventRestrictions.clear();
    for (const rule of config.eventRestrictions ?? []) {
      this.addRestriction(rule);
    }
    if (config.configured && !this.pickersLoaded) {
      this.pickersLoaded = true;
      this.http.get<RestrictionTargets>(`${this.base}/restriction-targets`).subscribe({
        next: (targets) => this.restrictionTargets.set(targets?.championships ?? []),
        error: () => this.restrictionTargets.set([]),
      });
      this.http.get<string[]>(`${this.base}/vehicles`).subscribe({
        next: (vehicles) => this.vehicles.set(vehicles ?? []),
        error: () => this.vehicles.set([]),
      });
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
