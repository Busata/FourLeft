import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { ChannelConfiguration } from '../../models/channel-configuration';

@Component({
  selector: 'app-channel-config',
  templateUrl: './channel-config.html',
})
export class ChannelConfig implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);

  private readonly requestId = this.route.snapshot.paramMap.get('requestId') ?? '';

  readonly loaded = signal(false);
  readonly error = signal('');
  readonly config = signal<ChannelConfiguration | null>(null);

  ngOnInit(): void {
    this.http.get<ChannelConfiguration>(`/api_v2/configuration/channel/${this.requestId}`).subscribe({
      next: (config) => {
        if (config) {
          this.config.set(config);
          this.loaded.set(true);
        } else {
          this.error.set('This configuration link is invalid or has expired.');
        }
      },
      error: () => this.error.set('Could not load channel configuration.'),
    });
  }
}
