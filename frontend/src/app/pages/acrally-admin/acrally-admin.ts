import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/**
 * "Admin" section shell inside the AC Rally dashboard: a second sub-tab strip over its own
 * nested outlet. The whole section is gated by the adminGuard on the route; the backend
 * /acrally-api/admin/** rules are the real gate.
 */
@Component({
  selector: 'app-acrally-admin',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './acrally-admin.html',
  styleUrl: './acrally-admin.scss',
})
export class AcrallyAdmin {
  readonly tabs = [
    { path: 'users', label: 'Users' },
    { path: 'locations', label: 'Locations' },
    { path: 'stages', label: 'Stages' },
    { path: 'variants', label: 'Variants' },
    { path: 'cars', label: 'Cars' },
    { path: 'car-aliases', label: 'Car aliases' },
  ];
}
