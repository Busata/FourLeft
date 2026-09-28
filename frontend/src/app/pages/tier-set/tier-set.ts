import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { catchError, debounceTime, distinctUntilChanged, map, Observable, of, Subject, switchMap } from 'rxjs';

import { Tier, TierPlayer, TierSet } from '../../models/tier-set';

// What a tier's player box is searching for; the tier id keeps each box's dropdown to itself.
interface PlayerQuery {
  tierId: string;
  q: string;
}

/**
 * Edit page for a tier set, reached through a private link from /fourleft tiers or the CLI. Every change is
 * saved immediately and the backend answers with the whole set, which simply replaces the local copy.
 */
@Component({
  selector: 'app-tier-set',
  templateUrl: './tier-set.html',
  styleUrl: './tier-set.scss',
})
export class TierSetPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  private readonly linkId = this.route.snapshot.paramMap.get('linkId') ?? '';
  private readonly base = `/api_v2/tier-sets/link/${this.linkId}`;

  readonly loaded = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly tierSet = signal<TierSet | null>(null);

  readonly name = signal('');
  readonly newTierLabel = signal('');
  // The tier whose label is being edited, and the draft label.
  readonly editingTierId = signal<string | null>(null);
  readonly editingLabel = signal('');

  // Player boxes: typed text per tier, and the one dropdown that's open.
  readonly playerQueries = signal<Record<string, string>>({});
  readonly suggestTierId = signal<string | null>(null);
  readonly suggestions = signal<TierPlayer[]>([]);
  private readonly playerQuery$ = new Subject<PlayerQuery>();

  ngOnInit(): void {
    this.playerQuery$
      .pipe(
        debounceTime(180),
        distinctUntilChanged((a, b) => a.tierId === b.tierId && a.q === b.q),
        switchMap((query) => this.suggest(query.q).pipe(map((players) => ({ query, players })))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ query, players }) => {
        if (this.suggestTierId() === query.tierId) {
          this.suggestions.set(players);
        }
      });

    this.http.get<TierSet>(this.base).subscribe({
      next: (tierSet) => {
        this.apply(tierSet);
        this.loaded.set(true);
      },
      error: () => this.error.set('This link is not valid (anymore). Ask for a new one with /fourleft tiers edit.'),
    });
  }

  rename(): void {
    this.save(this.http.put<TierSet>(this.base, { name: this.name() }), 'Name saved.');
  }

  addTier(): void {
    const label = this.newTierLabel().trim();
    if (!label) {
      return;
    }
    this.save(this.http.post<TierSet>(`${this.base}/tiers`, { label }), `Tier "${label}" added.`, () =>
      this.newTierLabel.set(''),
    );
  }

  startRelabel(tier: Tier): void {
    this.editingTierId.set(tier.id);
    this.editingLabel.set(tier.label);
  }

  relabel(tier: Tier): void {
    this.save(this.http.put<TierSet>(`${this.base}/tiers/${tier.id}`, { label: this.editingLabel() }), 'Label saved.', () =>
      this.editingTierId.set(null),
    );
  }

  removeTier(tier: Tier): void {
    const players = tier.players.length;
    if (players && !confirm(`Remove "${tier.label}"? Its ${players} player(s) become unassigned.`)) {
      return;
    }
    this.save(this.http.delete<TierSet>(`${this.base}/tiers/${tier.id}`), `Tier "${tier.label}" removed.`);
  }

  move(index: number, delta: number): void {
    const ids = (this.tierSet()?.tiers ?? []).map((tier) => tier.id);
    const target = index + delta;
    if (target < 0 || target >= ids.length) {
      return;
    }
    [ids[index], ids[target]] = [ids[target], ids[index]];
    this.save(this.http.put<TierSet>(`${this.base}/tiers/order`, { tierIds: ids }));
  }

  onPlayerInput(tierId: string, q: string): void {
    this.playerQueries.update((queries) => ({ ...queries, [tierId]: q }));
    if (this.suggestTierId() !== tierId) {
      this.suggestions.set([]);
    }
    this.suggestTierId.set(tierId);
    this.playerQuery$.next({ tierId, q });
  }

  closeSuggestions(tierId: string): void {
    if (this.suggestTierId() === tierId) {
      this.suggestTierId.set(null);
    }
  }

  assign(tier: Tier, player: TierPlayer): void {
    const from = this.tierOf(player.playerId);
    if (from?.id === tier.id) {
      this.flash(`${player.displayName} is already in ${tier.label}.`);
      return;
    }
    const message = from
      ? `${player.displayName} moved from ${from.label} to ${tier.label}.`
      : `${player.displayName} added to ${tier.label}.`;
    this.save(
      this.http.put<TierSet>(`${this.base}/players`, { tierId: tier.id, ...player }),
      message,
      () => {
        this.playerQueries.update((queries) => ({ ...queries, [tier.id]: '' }));
        this.suggestTierId.set(null);
        this.suggestions.set([]);
      },
    );
  }

  /** Moves a player to the tier above (-1) or below (+1) the one at tierIndex. */
  shift(player: TierPlayer, tierIndex: number, delta: number): void {
    const target = this.tierSet()?.tiers[tierIndex + delta];
    if (target) {
      this.assign(target, player);
    }
  }

  unassign(player: TierPlayer): void {
    this.save(
      this.http.delete<TierSet>(`${this.base}/players/${encodeURIComponent(player.playerId)}`),
      `${player.displayName} removed.`,
    );
  }

  /** The tier a player currently sits in, to flag suggestions that would move them. */
  tierOf(playerId: string): Tier | undefined {
    return this.tierSet()?.tiers.find((tier) => tier.players.some((p) => p.playerId === playerId));
  }

  playerCount(): number {
    return (this.tierSet()?.tiers ?? []).reduce((sum, tier) => sum + tier.players.length, 0);
  }

  private suggest(q: string): Observable<TierPlayer[]> {
    const trimmed = q.trim();
    if (trimmed.length < 2) {
      return of([]);
    }
    const params = new HttpParams().set('q', trimmed);
    return this.http.get<TierPlayer[]>(`${this.base}/players/suggest`, { params }).pipe(catchError(() => of([])));
  }

  private save(request: Observable<TierSet>, message = '', done?: () => void): void {
    request.subscribe({
      next: (tierSet) => {
        this.apply(tierSet);
        done?.();
        if (message) {
          this.flash(message);
        }
      },
      error: (err: HttpErrorResponse) => this.flash(this.describe(err)),
    });
  }

  private describe(err: HttpErrorResponse): string {
    switch (err.status) {
      case 400:
        return 'Names and labels must be 1-100 characters.';
      case 409:
        return 'Another tier set in this server already has that name.';
      case 404:
        return 'That tier no longer exists; reload the page.';
      default:
        return 'Saving failed, please try again.';
    }
  }

  private apply(tierSet: TierSet): void {
    this.tierSet.set(tierSet);
    this.name.set(tierSet.name);
  }

  private flash(message: string): void {
    this.notice.set(message);
    setTimeout(() => this.notice.set(''), 4000);
  }
}
