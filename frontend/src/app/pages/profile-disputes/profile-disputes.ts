import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe, NgTemplateOutlet } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { DiscordUserInfo, ProfileDisputeAdmin } from '../../models/profile-dispute-admin';

/**
 * Operator page listing open profile disputes, reached through an expiring admin link (posted to the admin log
 * channel on every new dispute, or minted with the CLI). Each dispute is resolved by keeping the holder (dismiss)
 * or handing the racenet account to the disputer (transfer).
 */
@Component({
  selector: 'app-profile-disputes',
  imports: [DatePipe, NgTemplateOutlet],
  templateUrl: './profile-disputes.html',
  styleUrl: './profile-disputes.scss',
})
export class ProfileDisputes implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);

  private readonly base = `/api_v2/admin/${this.route.snapshot.paramMap.get('linkId') ?? ''}/disputes`;

  readonly loaded = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly disputes = signal<ProfileDisputeAdmin[]>([]);

  ngOnInit(): void {
    this.http.get<ProfileDisputeAdmin[]>(this.base).subscribe({
      next: (disputes) => {
        this.disputes.set(disputes);
        this.loaded.set(true);
      },
      error: () => this.error.set('This link is not valid (anymore). Generate a new one with the CLI.'),
    });
  }

  dismiss(dispute: ProfileDisputeAdmin): void {
    if (confirm(`Dismiss the dispute? ${this.name(dispute.holder)} keeps ${dispute.racenet}.`)) {
      this.resolve(this.http.post<ProfileDisputeAdmin[]>(`${this.base}/${encodeURIComponent(dispute.playerId)}/dismiss`, {}), 'Dispute dismissed.');
    }
  }

  transfer(dispute: ProfileDisputeAdmin): void {
    if (confirm(`Give ${dispute.racenet} to ${this.name(dispute.disputer)}? ${this.name(dispute.holder)} loses it.`)) {
      this.resolve(this.http.post<ProfileDisputeAdmin[]>(`${this.base}/${encodeURIComponent(dispute.playerId)}/transfer`, {}), `${dispute.racenet} transferred.`);
    }
  }

  copyMention(user: DiscordUserInfo): void {
    navigator.clipboard.writeText(`<@${user.id}>`).then(
      () => this.flash('Mention copied; paste it in a server they are in.'),
      () => this.flash('Could not copy.'),
    );
  }

  name(user: DiscordUserInfo | null): string {
    if (!user) {
      return 'nobody';
    }
    return user.globalName ?? user.username ?? user.id;
  }

  private resolve(request: Observable<ProfileDisputeAdmin[]>, message: string): void {
    request.subscribe({
      next: (disputes) => {
        this.disputes.set(disputes);
        this.flash(message);
      },
      error: () => this.flash('That did not work; reload the page, the dispute may already be resolved.'),
    });
  }

  private flash(message: string): void {
    this.notice.set(message);
    setTimeout(() => this.notice.set(''), 4000);
  }
}
