import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, debounceTime, EMPTY, startWith, Subject, switchMap, tap } from 'rxjs';

import { TimeTrialPage } from '../../models/time-trial';

const PAGE_SIZE = 50;

@Component({
  selector: 'app-time-trials',
  templateUrl: './time-trials.html',
  styleUrl: './time-trials.scss',
})
export class TimeTrials implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload = new Subject<void>();
  private readonly searchInput = new Subject<string>();

  readonly search = signal('');
  readonly page = signal(0);
  readonly data = signal<TimeTrialPage | null>(null);
  readonly error = signal('');
  readonly loading = signal(false);

  readonly items = computed(() => this.data()?.items ?? []);
  readonly total = computed(() => this.data()?.total ?? 0);
  readonly totalPages = computed(() => this.data()?.totalPages ?? 0);

  ngOnInit(): void {
    // Debounce the search box, then reset to the first page and refetch.
    this.searchInput
      .pipe(debounceTime(250), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.search.set(value.trim());
        this.page.set(0);
        this.reload.next();
      });

    this.reload
      .pipe(
        startWith(void 0),
        tap(() => this.loading.set(true)),
        switchMap(() =>
          this.http.get<TimeTrialPage>('/api_v2/time-trials', { params: this.params() }).pipe(
            catchError(() => {
              this.error.set('Could not reach the time-trials API.');
              this.loading.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.data.set(page);
        this.error.set('');
        this.loading.set(false);
      });
  }

  private params(): HttpParams {
    let params = new HttpParams().set('page', String(this.page())).set('size', String(PAGE_SIZE));
    const term = this.search();
    if (term) {
      params = params.set('search', term);
    }
    return params;
  }

  onSearch(value: string): void {
    this.searchInput.next(value);
  }

  prev(): void {
    if (this.page() > 0) {
      this.page.update((n) => n - 1);
      this.reload.next();
    }
  }

  next(): void {
    if (this.page() < this.totalPages() - 1) {
      this.page.update((n) => n + 1);
      this.reload.next();
    }
  }

  surfaceLabel(surface: number): string {
    return surface === 1 ? 'Wet' : 'Dry';
  }

  validLabel(valid: boolean | null): string {
    if (valid == null) {
      return 'Unknown';
    }
    return valid ? 'Valid' : 'Invalid';
  }
}
