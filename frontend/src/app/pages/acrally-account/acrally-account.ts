import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import type { AgentReleaseTo, ApiKeyTo, LinkedIdentityTo, SteamProfileTo } from '../../models/acrally';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-acrally-account',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  templateUrl: './acrally-account.html',
})
export class AcrallyAccount implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly identities = signal<LinkedIdentityTo[]>([]);
  readonly steam = computed(() => this.identities().find((i) => i.provider === 'STEAM') ?? null);
  readonly steamProfile = signal<SteamProfileTo | null>(null);
  readonly keys = signal<ApiKeyTo[]>([]);
  readonly agent = signal<AgentReleaseTo | null>(null);

  readonly notice = signal('');
  readonly error = signal('');

  // Display name is the only editable profile field; identity itself is the Steam anchor.
  readonly editingName = signal(false);
  readonly savingName = signal(false);
  readonly nameControl = new FormControl<string>('', { nonNullable: true });

  ngOnInit(): void {
    this.loadIdentities();
    this.loadSteamProfile();
    this.loadKeys();
    this.loadAgent();
  }

  startEditName(): void {
    this.nameControl.setValue(this.user()?.displayName ?? '');
    this.error.set('');
    this.editingName.set(true);
  }

  cancelEditName(): void {
    this.editingName.set(false);
  }

  saveName(): void {
    if (this.savingName()) {
      return;
    }
    this.savingName.set(true);
    this.auth.updateDisplayName(this.nameControl.value.trim()).subscribe({
      next: () => {
        this.savingName.set(false);
        this.editingName.set(false);
        this.notice.set('Display name updated.');
      },
      error: (err) => {
        this.savingName.set(false);
        this.error.set(
          err.status === 409
            ? 'This display name is taken.'
            : err.error?.message || 'Could not update the display name.',
        );
      },
    });
  }

  private loadAgent(): void {
    // Static release manifest served by the reverse proxy; url points at the current signed exe.
    this.http.get<AgentReleaseTo>('/acrally-agent/latest.json').subscribe({
      next: (rel) => this.agent.set(rel ?? null),
      error: () => {},
    });
  }

  private loadKeys(): void {
    this.http.get<ApiKeyTo[]>('/acrally-api/account/keys').subscribe({
      next: (list) => this.keys.set(list),
      error: () => {},
    });
  }

  revokeKey(id: string): void {
    this.http.post(`/acrally-api/account/keys/${id}/revoke`, {}).subscribe({
      next: () => this.loadKeys(),
      error: () => this.error.set('Could not revoke that key.'),
    });
  }

  private loadIdentities(): void {
    this.http.get<LinkedIdentityTo[]>('/acrally-api/account/identities').subscribe({
      next: (list) => this.identities.set(list),
      error: () => this.error.set('Could not load linked accounts.'),
    });
  }

  private loadSteamProfile(): void {
    // 204 (no linked/fetched profile) arrives as a null body — leaves the signal null.
    this.http.get<SteamProfileTo>('/acrally-api/account/steam').subscribe({
      next: (profile) => this.steamProfile.set(profile ?? null),
      error: () => {},
    });
  }

  hasBan(profile: SteamProfileTo): boolean {
    return profile.vacBanned || profile.gameBanCount > 0 || profile.communityBanned;
  }

  logout(): void {
    this.auth.logout().subscribe({
      next: () => this.router.navigateByUrl('/acrally/login'),
    });
  }
}
