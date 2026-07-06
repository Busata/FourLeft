import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import type { AgentReleaseTo, ApiKeyTo, LinkedIdentityTo, SteamProfileTo } from '../../models/acrally';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-acrally-account',
  imports: [DatePipe, RouterLink],
  templateUrl: './acrally-account.html',
})
export class AcrallyAccount implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly user = this.auth.currentUser;
  readonly identities = signal<LinkedIdentityTo[]>([]);
  readonly steam = computed(() => this.identities().find((i) => i.provider === 'STEAM') ?? null);
  readonly steamProfile = signal<SteamProfileTo | null>(null);
  readonly keys = signal<ApiKeyTo[]>([]);
  readonly agent = signal<AgentReleaseTo | null>(null);

  readonly notice = signal('');
  readonly error = signal('');

  ngOnInit(): void {
    const flag = this.route.snapshot.queryParamMap.get('steam');
    if (flag === 'linked') {
      this.notice.set('Steam account linked.');
    } else if (flag === 'error') {
      this.error.set('Could not link that Steam account — it may already be linked to another account.');
    }
    this.loadIdentities();
    this.loadSteamProfile();
    this.loadKeys();
    this.loadAgent();
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
