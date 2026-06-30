import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

import { ControllerType, PeripheralType, Platform, Profile } from '../../models/profile';
import { SlideToggle } from '../../shared/slide-toggle/slide-toggle';

@Component({
  selector: 'app-profile-editor',
  imports: [ReactiveFormsModule, SlideToggle],
  templateUrl: './profile-editor.html',
})
export class ProfileEditor implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);

  private readonly requestId = this.route.snapshot.paramMap.get('requestId') ?? '';
  private last: Profile | null = null;

  readonly loaded = signal(false);
  readonly error = signal('');
  readonly notice = signal('');

  readonly form = new FormGroup({
    id: new FormControl<string>('', { nonNullable: true }),
    displayName: new FormControl<string>('', { nonNullable: true }),
    platform: new FormControl<Platform>('UNKNOWN', { nonNullable: true }),
    controller: new FormControl<ControllerType>('UNKNOWN', { nonNullable: true }),
    peripheral: new FormControl<PeripheralType>('UNKNOWN', { nonNullable: true }),
    racenet: new FormControl<string>({ value: '', disabled: true }, { nonNullable: true }),
    trackDiscord: new FormControl<boolean>(false, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.http.get<Profile>(`/api_v2/profile/${this.requestId}`).subscribe({
      next: (profile) => {
        this.apply(profile);
        this.loaded.set(true);
      },
      error: () => this.error.set('Could not load profile.'),
    });
  }

  save(): void {
    this.http
      .post<Profile>(`/api_v2/profile/${this.requestId}`, this.form.getRawValue())
      .subscribe({
        next: (profile) => {
          this.apply(profile);
          this.flash('Profile updated.');
        },
        error: () => this.flash('Could not save profile.'),
      });
  }

  reset(): void {
    if (this.last) {
      this.apply(this.last);
    }
  }

  private apply(profile: Profile): void {
    this.last = profile;
    this.form.reset(profile);
  }

  private flash(message: string): void {
    this.notice.set(message);
    setTimeout(() => this.notice.set(''), 4000);
  }
}
