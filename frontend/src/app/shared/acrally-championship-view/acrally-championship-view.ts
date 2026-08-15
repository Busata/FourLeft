import { Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

import { AuthService } from '../../services/auth';

import type {
  CarTo,
  ChampionshipDetailTo,
  ChampionshipEventTo,
  EventDnfTo,
  EventLeaderboardTo,
  EventVariantTo,
  LeaderboardEntryTo,
  StageBoardTo,
} from '../../models/acrally';

/**
 * Read-only, compact renderer of a championship's events. Each event is a collapsible card whose
 * stage tabs double as the itinerary and the leaderboard switcher: "Overall" plus one tab per
 * stage, with a single board shown at a time. Used inline on the club page and as the viewer on
 * the championship page — owners build the schedule in the championship editor instead.
 */
@Component({
  selector: 'app-acrally-championship-view',
  imports: [DatePipe],
  templateUrl: './acrally-championship-view.html',
  styleUrl: './acrally-championship-view.scss',
})
export class AcrallyChampionshipView {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);

  readonly detail = input.required<ChampionshipDetailTo>();

  /**
   * Who may see and act on the DNF panel: the club owner, or a platform admin in any club —
   * the same rule the endpoint enforces. Ordinary members never see it.
   */
  readonly canModerate = computed(() => this.detail().owner || this.auth.isAdmin());

  readonly expanded = signal<Set<string>>(new Set());
  private readonly boards = signal<Map<string, EventLeaderboardTo>>(new Map());
  private readonly loadingBoards = signal<Set<string>>(new Set());
  /** eventId → selected board tab: 'overall' or a variantId. Unset falls back to the default. */
  private readonly tabs = signal<Map<string, string>>(new Map());
  /** Events whose car list is expanded past the +N overflow. */
  private readonly carsOpen = signal<Set<string>>(new Set());

  /** Board rows whose checkpoint splits are expanded, keyed event:variant:user. */
  private readonly splitsOpen = signal<Set<string>>(new Set());

  /** Owner-only DNF panel: eventId → its DNFs, fetched when the tab is first opened. */
  private readonly dnfs = signal<Map<string, EventDnfTo[]>>(new Map());
  private readonly loadingDnfs = signal<Set<string>>(new Set());
  /** Arms with a revert in flight, so the button can't be double-fired. */
  private readonly reverting = signal<Set<string>>(new Set());
  readonly dnfError = signal('');

  /** Car chips shown before collapsing the rest behind "+N more". */
  private readonly carLimit = 6;

  constructor() {
    // A (new) detail arriving resets the expansion to the events worth looking at right now.
    effect(() => {
      const detail = this.detail();
      untracked(() => this.initExpansion(detail));
    });
  }

  /**
   * Open events expand by default; with none open, fall back to the freshest results (the last
   * closed event) and finally to the first upcoming one, so the card that matters is already open.
   */
  private initExpansion(detail: ChampionshipDetailTo): void {
    let ids = detail.events.filter((e) => this.phase(e) === 'open').map((e) => e.id);
    if (ids.length === 0) {
      const lastClosed = [...detail.events].reverse().find((e) => this.phase(e) === 'closed');
      const firstUpcoming = detail.events.find((e) => this.phase(e) === 'upcoming');
      const pick = lastClosed ?? firstUpcoming;
      ids = pick ? [pick.id] : [];
    }
    this.expanded.set(new Set(ids));
    for (const id of ids) {
      this.fetchBoard(id);
      this.fetchDnfs(id);
    }
  }

  // --- Event cards ---
  isExpanded(eventId: string): boolean {
    return this.expanded().has(eventId);
  }

  toggle(eventId: string): void {
    const open = new Set(this.expanded());
    if (open.has(eventId)) {
      open.delete(eventId);
    } else {
      open.add(eventId);
      this.fetchBoard(eventId);
      this.fetchDnfs(eventId);
    }
    this.expanded.set(open);
  }

  /** open | upcoming | closed, from the event's derived window against now. */
  phase(event: ChampionshipEventTo): 'open' | 'upcoming' | 'closed' {
    const now = Date.now();
    if (now < new Date(event.opensAt).getTime()) return 'upcoming';
    if (now < new Date(event.closesAt).getTime()) return 'open';
    return 'closed';
  }

  phaseLabel(event: ChampionshipEventTo): string {
    return { open: 'Open', upcoming: 'Upcoming', closed: 'Closed' }[this.phase(event)];
  }

  /** Status-pill modifier for the phase badge. */
  phaseClass(event: ChampionshipEventTo): string {
    return {
      open: 'status-pill--live',
      upcoming: 'status-pill--draft',
      closed: 'status-pill--muted',
    }[this.phase(event)];
  }

  /** "closes in 2d" / "opens in 5h" — the actionable time left, next to the absolute window. */
  relPhrase(event: ChampionshipEventTo): string {
    const now = Date.now();
    const opens = new Date(event.opensAt).getTime();
    const closes = new Date(event.closesAt).getTime();
    if (now < opens) return `opens ${this.rel(opens - now)}`;
    if (now < closes) return `closes ${this.rel(closes - now)}`;
    return '';
  }

  private rel(ms: number): string {
    const hours = Math.floor(ms / 3_600_000);
    if (hours < 1) return 'soon';
    if (hours < 48) return `in ${hours}h`;
    return `in ${Math.floor(hours / 24)}d`;
  }

  /** Collapsed-card summary, e.g. "3 stages · 5 cars". */
  summary(event: ChampionshipEventTo): string {
    const stages = `${event.variants.length} ${event.variants.length === 1 ? 'stage' : 'stages'}`;
    const cars = event.cars.length === 0
      ? 'any car'
      : `${event.cars.length} ${event.cars.length === 1 ? 'car' : 'cars'}`;
    return `${stages} · ${cars}`;
  }

  /** Full stage label in "Location - Stage - Alias" order (the alias alone is usually "Variant X"). */
  stageFullLabel(v: EventVariantTo): string {
    return [v.locationName, v.stageName, v.label].filter((p) => !!p && p.trim()).join(' - ');
  }

  // --- Board tabs ---
  /** The selected tab, defaulting to Overall for multi-stage events and the lone stage otherwise. */
  tab(event: ChampionshipEventTo): string {
    const chosen = this.tabs().get(event.id);
    if (chosen) return chosen;
    return event.variants.length > 1 ? 'overall' : (event.variants[0]?.variantId ?? '');
  }

  setTab(eventId: string, tab: string): void {
    this.tabs.update((m) => new Map(m).set(eventId, tab));
  }

  selectedVariant(event: ChampionshipEventTo): EventVariantTo | undefined {
    return event.variants.find((v) => v.variantId === this.tab(event));
  }

  // --- Leaderboards (fetched lazily per expanded event, cached) ---
  board(eventId: string): EventLeaderboardTo | undefined {
    return this.boards().get(eventId);
  }

  isBoardLoading(eventId: string): boolean {
    return this.loadingBoards().has(eventId);
  }

  selectedBoard(event: ChampionshipEventTo): StageBoardTo | undefined {
    return this.board(event.id)?.stages.find((s) => s.variantId === this.tab(event));
  }

  private fetchBoard(eventId: string): void {
    if (this.boards().has(eventId) || this.loadingBoards().has(eventId)) return;
    this.loadingBoards.update((s) => new Set(s).add(eventId));
    this.http.get<EventLeaderboardTo>(`/acrally-api/events/${eventId}/leaderboard`).subscribe({
      next: (board) => {
        this.boards.update((m) => new Map(m).set(eventId, board));
        this.clearBoardLoading(eventId);
      },
      error: () => this.clearBoardLoading(eventId),
    });
  }

  private clearBoardLoading(eventId: string): void {
    this.loadingBoards.update((s) => {
      const next = new Set(s);
      next.delete(eventId);
      return next;
    });
  }

  // --- DNFs (club owner / admin only) ---
  /**
   * A DNF spends a driver's one shot at a stage, and the server can't tell a bail-out from a
   * crashed game or a dead agent. The club owner — or an admin — sees them all here and can hand
   * a shot back.
   */
  dnfList(eventId: string): EventDnfTo[] {
    return this.dnfs().get(eventId) ?? [];
  }

  /** DNFs still standing — what the tab badge counts, so a handled event stops nagging. */
  openDnfCount(eventId: string): number {
    return this.dnfList(eventId).filter((d) => !d.revertedAt).length;
  }

  isDnfLoading(eventId: string): boolean {
    return this.loadingDnfs().has(eventId);
  }

  isReverting(armId: string): boolean {
    return this.reverting().has(armId);
  }

  /** "quit / restart / crash" vs "armed, never ran" — the two ways a shot is lost. */
  causeLabel(dnf: EventDnfTo): string {
    return dnf.cause === 'EXPIRED' ? 'armed, never ran' : 'quit / restart / crash';
  }

  revert(eventId: string, dnf: EventDnfTo): void {
    if (this.isReverting(dnf.armId)) return;
    this.dnfError.set('');
    this.reverting.update((s) => new Set(s).add(dnf.armId));
    this.http
      .post<EventDnfTo[]>(`/acrally-api/events/${eventId}/dnfs/${dnf.armId}/revert`, {})
      .subscribe({
        next: (list) => {
          this.dnfs.update((m) => new Map(m).set(eventId, list));
          this.clearReverting(dnf.armId);
        },
        error: () => {
          this.dnfError.set(`Could not revert ${dnf.driver}'s DNF.`);
          this.clearReverting(dnf.armId);
        },
      });
  }

  /** Moderator-only fetch; the endpoint 403s for everyone else, so don't even ask. */
  private fetchDnfs(eventId: string): void {
    if (!this.canModerate()) return;
    if (this.dnfs().has(eventId) || this.loadingDnfs().has(eventId)) return;
    this.loadingDnfs.update((s) => new Set(s).add(eventId));
    this.http.get<EventDnfTo[]>(`/acrally-api/events/${eventId}/dnfs`).subscribe({
      next: (list) => {
        this.dnfs.update((m) => new Map(m).set(eventId, list));
        this.clearDnfLoading(eventId);
      },
      error: () => this.clearDnfLoading(eventId),
    });
  }

  private clearDnfLoading(eventId: string): void {
    this.loadingDnfs.update((s) => {
      const next = new Set(s);
      next.delete(eventId);
      return next;
    });
  }

  private clearReverting(armId: string): void {
    this.reverting.update((s) => {
      const next = new Set(s);
      next.delete(armId);
      return next;
    });
  }

  // --- Checkpoint splits (per board row, expandable) ---
  hasSplits(e: LeaderboardEntryTo): boolean {
    return (e.checkpointsMs?.length ?? 0) > 0;
  }

  splitsKey(eventId: string, variantId: string, e: LeaderboardEntryTo): string {
    return `${eventId}:${variantId}:${e.userId}`;
  }

  splitsShown(key: string): boolean {
    return this.splitsOpen().has(key);
  }

  toggleSplits(key: string): void {
    this.splitsOpen.update((s) => {
      const next = new Set(s);
      next.has(key) ? next.delete(key) : next.add(key);
      return next;
    });
  }

  /**
   * The row splits are compared against: the best-ranked entry that has the same number of
   * checkpoints (usually the leader). Undefined for the reference row itself and when nobody
   * comparable exists — those rows show sector durations instead of deltas.
   */
  splitRef(sb: StageBoardTo, e: LeaderboardEntryTo): LeaderboardEntryTo | undefined {
    const ref = sb.entries.find(
      (c) => this.hasSplits(c) && c.checkpointsMs.length === e.checkpointsMs.length,
    );
    return ref && ref.userId !== e.userId ? ref : undefined;
  }

  /** Checkpoint label: CP1…CPn-1, "Fin" for the finish line. */
  cpLabel(e: LeaderboardEntryTo, i: number): string {
    return i === e.checkpointsMs.length - 1 ? 'Fin' : `CP${i + 1}`;
  }

  /** Duration of sector i (cumulative difference). */
  sectorMs(e: LeaderboardEntryTo, i: number): number {
    return e.checkpointsMs[i] - (i > 0 ? e.checkpointsMs[i - 1] : 0);
  }

  /** Signed gap to the reference at checkpoint i (negative = ahead). */
  cpDelta(ref: LeaderboardEntryTo, e: LeaderboardEntryTo, i: number): number {
    return e.checkpointsMs[i] - ref.checkpointsMs[i];
  }

  /** "+1.234" / "−0.850" — signed split gap in seconds (splits stay sub-minute readable). */
  formatSignedGap(ms: number): string {
    const sign = ms < 0 ? '−' : '+';
    return `${sign}${(Math.abs(ms) / 1000).toFixed(3)}`;
  }

  /** A sector's duration, e.g. "57.623" or "1:24.872" (shown when there is nobody to compare to). */
  formatSector(ms: number): string {
    return ms >= 60_000 ? this.formatTime(ms) : `${(ms / 1000).toFixed(3)}`;
  }

  // --- Cars ---
  visibleCars(event: ChampionshipEventTo): CarTo[] {
    if (event.cars.length <= this.carLimit || this.carsOpen().has(event.id)) return event.cars;
    // Show one fewer than the limit so the "+N more" chip takes the last slot.
    return event.cars.slice(0, this.carLimit - 1);
  }

  overflowCount(event: ChampionshipEventTo): number {
    return event.cars.length - this.visibleCars(event).length;
  }

  carsExpanded(event: ChampionshipEventTo): boolean {
    return event.cars.length > this.carLimit && this.carsOpen().has(event.id);
  }

  toggleCars(eventId: string): void {
    this.carsOpen.update((s) => {
      const next = new Set(s);
      next.has(eventId) ? next.delete(eventId) : next.add(eventId);
      return next;
    });
  }

  // --- Formatting ---
  /** Podium tint for the top three ranks. */
  podClass(rank: number): string {
    if (rank === 1) return 'pod--gold';
    if (rank === 2) return 'pod--silver';
    if (rank === 3) return 'pod--bronze';
    return '';
  }

  /** ms → m:ss.mmm (penalised totals), matching the personal-stats formatting. */
  formatTime(ms: number): string {
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    const millis = ms % 1000;
    return `${minutes}:${seconds.toString().padStart(2, '0')}.${millis.toString().padStart(3, '0')}`;
  }

  /** Penalty seconds baked into a total, e.g. "+2.0s". Only shown when there is a penalty. */
  formatPenalty(ms: number): string {
    return `+${(ms / 1000).toFixed(1)}s`;
  }

  /** Gap behind the leader, e.g. "+1.234" or "+1:02.345". Empty for the leader / no gap. */
  formatDiff(gapMs: number): string {
    if (gapMs <= 0) {
      return '';
    }
    const totalSeconds = Math.floor(gapMs / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    const millis = gapMs % 1000;
    const body = minutes > 0
      ? `${minutes}:${seconds.toString().padStart(2, '0')}.${millis.toString().padStart(3, '0')}`
      : `${seconds}.${millis.toString().padStart(3, '0')}`;
    return `+${body}`;
  }
}
