import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { catchError, debounceTime, distinctUntilChanged, Observable, of, Subject, switchMap } from 'rxjs';

import {
  ControllerType,
  PeripheralType,
  Platform,
  Profile,
  ProfileClaimResult,
  ProfileClaimState,
  ProfileDispute,
  ProfilePage,
} from '../../models/profile';
import { TierPlayer } from '../../models/tier-set';
import { SlideToggle } from '../../shared/slide-toggle/slide-toggle';

/**
 * Private profile link from /wrc profile. Either edits the racenet account the discord user holds, or — when they
 * haven't picked one yet — lets them search for it and claim it. Claiming an account someone else holds needs an
 * explicit confirm to dispute it; ?claim=<ssid> (from the bot) starts that claim right away.
 */
@Component({
  selector: 'app-profile-editor',
  imports: [DatePipe, ReactiveFormsModule, SlideToggle],
  templateUrl: './profile-editor.html',
  styleUrl: './profile-editor.scss',
})
export class ProfileEditor implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  private readonly requestId = this.route.snapshot.paramMap.get('requestId') ?? '';
  private readonly initialClaim = this.route.snapshot.queryParamMap.get('claim');
  private last: Profile | null = null;

  readonly loaded = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly claimState = signal<ProfileClaimState | null>(null);
  // The account this user disputes, if any.
  readonly disputing = signal<ProfileDispute | null>(null);

  // Picking a racenet account (no profile on this link yet).
  readonly picking = signal(false);
  readonly query = signal('');
  readonly suggestions = signal<TierPlayer[]>([]);
  readonly showSuggestions = signal(false);
  readonly claimMessage = signal('');
  // A held account the user may dispute, awaiting their confirm.
  readonly disputeCandidate = signal<{ playerId: string; racenet: string } | null>(null);
  private readonly query$ = new Subject<string>();

  readonly form = new FormGroup({
    id: new FormControl<string>('', { nonNullable: true }),
    displayName: new FormControl<string>('', { nonNullable: true }),
    platform: new FormControl<Platform>('UNKNOWN', { nonNullable: true }),
    controller: new FormControl<ControllerType>('UNKNOWN', { nonNullable: true }),
    peripheral: new FormControl<PeripheralType>('UNKNOWN', { nonNullable: true }),
    racenet: new FormControl<string>({ value: '', disabled: true }, { nonNullable: true }),
    trackDiscord: new FormControl<boolean>(false, { nonNullable: true }),
    claimState: new FormControl<ProfileClaimState>('CLAIMED', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.query$
      .pipe(
        debounceTime(180),
        distinctUntilChanged(),
        switchMap((q) => this.suggest(q)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((players) => this.suggestions.set(players));

    this.load();
  }

  onQuery(q: string): void {
    this.query.set(q);
    this.showSuggestions.set(true);
    this.query$.next(q);
  }

  claim(player: TierPlayer): void {
    this.showSuggestions.set(false);
    this.claimAccount(player.playerId);
  }

  confirmDispute(): void {
    const candidate = this.disputeCandidate();
    if (!candidate) {
      return;
    }
    this.http
      .post<ProfileClaimResult>(`/api_v2/profile/${this.requestId}/dispute`, { playerId: candidate.playerId })
      .subscribe({
        next: (result) => this.handle(result),
        error: () => this.claimMessage.set('Could not dispute that account, please try again.'),
      });
  }

  withdrawDispute(): void {
    const disputing = this.disputing();
    if (!disputing || !confirm(`Withdraw your dispute of ${disputing.racenet}?`)) {
      return;
    }
    this.http.post<ProfilePage>(`/api_v2/profile/${this.requestId}/withdraw`, {}).subscribe({
      next: (page) => {
        this.disputing.set(page.disputing);
        this.claimMessage.set('');
        this.flash(`Dispute of ${disputing.racenet} withdrawn.`);
      },
      error: () => this.flash('Could not withdraw the dispute, please try again.'),
    });
  }

  cancelDispute(): void {
    this.disputeCandidate.set(null);
    this.claimMessage.set('');
  }

  private claimAccount(playerId: string): void {
    this.claimMessage.set('');
    this.disputeCandidate.set(null);
    this.http.post<ProfileClaimResult>(`/api_v2/profile/${this.requestId}/claim`, { playerId }).subscribe({
      next: (result) => this.handle(result),
      error: () => this.claimMessage.set('Could not claim that account, please try again.'),
    });
  }

  private handle(result: ProfileClaimResult): void {
    const name = result.racenet ?? 'That account';
    this.disputeCandidate.set(null);
    switch (result.outcome) {
      case 'LINKED':
        this.claimMessage.set('');
        this.flash(`${name} is now linked to your discord account.`);
        this.load();
        break;
      case 'CONFIRM_DISPUTE':
        this.claimMessage.set('');
        this.disputeCandidate.set({ playerId: result.playerId ?? '', racenet: name });
        break;
      case 'DISPUTED':
        this.claimMessage.set('');
        this.load();
        break;
      case 'ALREADY_DISPUTED':
        this.claimMessage.set(`${name} is claimed by someone else and already disputed. Stuck? Contact @busata.`);
        break;
      case 'DISPUTE_LIMIT':
        this.claimMessage.set(
          `${name} is claimed by someone else, and you already have an open dispute on another account. Stuck? Contact @busata.`,
        );
        break;
      case 'VERIFIED_BY_OTHER':
        this.claimMessage.set(`${name} has been verified by someone else. If that's wrong, contact @busata.`);
        break;
      default:
        this.claimMessage.set('Could not claim that account, please try again.');
    }
  }

  save(): void {
    this.http
      .post<Profile>(`/api_v2/profile/${this.requestId}`, this.form.getRawValue())
      .subscribe({
        next: (profile) => {
          this.apply(profile);
          this.flash('Profile updated.');
        },
        error: () => this.flash('Could not save profile.'),
      });
  }

  reset(): void {
    if (this.last) {
      this.apply(this.last);
    }
  }

  private load(): void {
    this.http.get<ProfilePage>(`/api_v2/profile/${this.requestId}`).subscribe({
      next: (page) => {
        this.disputing.set(page.disputing);
        if (page.profile) {
          this.apply(page.profile);
          this.picking.set(false);
        } else {
          const first = !this.loaded();
          this.picking.set(true);
          if (first && this.initialClaim) {
            this.claimAccount(this.initialClaim);
          }
        }
        this.loaded.set(true);
      },
      error: () => this.error.set('This link is not valid (anymore). Ask for a new one with /wrc profile.'),
    });
  }

  private suggest(q: string): Observable<TierPlayer[]> {
    const trimmed = q.trim();
    if (trimmed.length < 2) {
      return of([]);
    }
    const params = new HttpParams().set('q', trimmed);
    return this.http
      .get<TierPlayer[]>(`/api_v2/profile/${this.requestId}/suggest`, { params })
      .pipe(catchError(() => of([])));
  }

  private apply(profile: Profile): void {
    this.last = profile;
    this.claimState.set(profile.claimState);
    this.form.reset(profile);
  }

  private flash(message: string): void {
    this.notice.set(message);
    setTimeout(() => this.notice.set(''), 4000);
  }
}
