import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../services/auth';

/**
 * "AC Rally" dashboard shell: a sub-tab bar over a nested outlet. Tabs are data-driven —
 * add an entry here plus a matching child route to grow the section. The "Admin" tab (a
 * nested section with its own sub-tabs) is appended only for administrators (the route's
 * adminGuard is the real gate).
 */
@Component({
  selector: 'app-acrally-dashboard',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './acrally-dashboard.html',
  styleUrl: './acrally-dashboard.scss',
})
export class AcrallyDashboard {
  private readonly auth = inject(AuthService);

  readonly tabs = computed(() => {
    const tabs = [
      { path: 'clubs', label: 'Clubs' },
      { path: 'stats', label: 'Personal stats' },
      { path: 'account', label: 'Account' },
    ];
    if (this.auth.isAdmin()) {
      tabs.push({ path: 'admin', label: 'Admin' });
    }
    return tabs;
  });
}
