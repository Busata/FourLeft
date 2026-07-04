import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { ChannelConfiguration } from '../../models/channel-configuration';
import { SlideToggle } from '../../shared/slide-toggle/slide-toggle';

@Component({
  selector: 'app-channel-config',
  imports: [ReactiveFormsModule, SlideToggle],
  templateUrl: './channel-config.html',
})
export class ChannelConfig implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);

  private readonly requestId = this.route.snapshot.paramMap.get('requestId') ?? '';
  private readonly base = `/api_v2/configuration/channel/${this.requestId}`;

  readonly loaded = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  readonly config = signal<ChannelConfiguration | null>(null);

  readonly form = new FormGroup({
    clubId: new FormControl<string>('', { nonNullable: true }),
    autopostingEnabled: new FormControl<boolean>(true, { nonNullable: true }),
    requiresTracking: new FormControl<boolean>(false, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.http.get<ChannelConfiguration>(this.base).subscribe({
      next: (config) => this.apply(config),
      error: () => this.error.set('Could not load channel configuration.'),
    });
  }

  create(): void {
    this.http
      .post<ChannelConfiguration>(this.base, {
        clubId: this.form.controls.clubId.value,
        autopostingEnabled: this.form.controls.autopostingEnabled.value,
        requiresTracking: this.form.controls.requiresTracking.value,
      })
      .subscribe({
        next: (config) => {
          this.apply(config);
          this.flash('Configuration created.');
        },
        error: () => this.flash('Could not create configuration.'),
      });
  }

  save(): void {
    this.http
      .put<ChannelConfiguration>(this.base, {
        autopostingEnabled: this.form.controls.autopostingEnabled.value,
        requiresTracking: this.form.controls.requiresTracking.value,
      })
      .subscribe({
        next: (config) => {
          this.apply(config);
          this.flash('Configuration saved.');
        },
        error: () => this.flash('Could not save configuration.'),
      });
  }

  remove(): void {
    this.http.delete<ChannelConfiguration>(this.base).subscribe({
      next: (config) => {
        this.apply(config);
        this.flash('Configuration removed.');
      },
      error: () => this.flash('Could not remove configuration.'),
    });
  }

  private apply(config: ChannelConfiguration | null): void {
    if (!config) {
      this.error.set('This configuration link is invalid or has expired.');
      return;
    }
    this.config.set(config);
    this.form.reset({
      clubId: config.clubId ?? '',
      autopostingEnabled: config.autopostingEnabled ?? true,
      requiresTracking: config.requiresTracking ?? false,
    });
    this.loaded.set(true);
  }

  private flash(message: string): void {
    this.notice.set(message);
    setTimeout(() => this.notice.set(''), 4000);
  }
}
