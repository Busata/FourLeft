import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-status',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './status.html',
  styleUrl: './status.scss',
})
export class Status {}
