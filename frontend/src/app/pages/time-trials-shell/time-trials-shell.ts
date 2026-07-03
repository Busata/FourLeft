import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/**
 * Top-level "Time Trials" section shell: a sub-tab bar over a nested outlet. Only "Boards" today;
 * the strip is data-driven so future tabs (e.g. records, activity) are a one-line add + a child route.
 */
@Component({
  selector: 'app-time-trials-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './time-trials-shell.html',
  styleUrl: './time-trials-shell.scss',
})
export class TimeTrialsShell {
  /** Sub-tabs, in order. Add an entry (and a matching child route) to grow the section. */
  readonly tabs = [
    { path: 'boards', label: 'Boards' },
    { path: 'profile', label: 'Profile' },
  ];
}
