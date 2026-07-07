import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  private readonly router = inject(Router);

  /** Mobile navigation drawer; the toggle is hidden on desktop where links show inline. */
  readonly menuOpen = signal(false);

  /**
   * True while the AC Rally section is active. Drives the "not affiliated" disclaimer banner —
   * AC Rally is a self-hosted community effort, unconnected to the official game.
   */
  readonly acRallyActive = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects.startsWith('/acrally')),
      startWith(this.router.url.startsWith('/acrally')),
    ),
    { initialValue: this.router.url.startsWith('/acrally') },
  );

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }
}
