import { Component, ElementRef, computed, input, output, signal, viewChild } from '@angular/core';

/** One pickable entry of a {@link SearchSelect}. */
export interface SearchSelectOption {
  value: string;
  label: string;
}

/**
 * A searchable replacement for long native `<select>`s: a text input that filters the option list
 * as you type, with the matches in a capped, scrollable panel underneath. Native selects render the
 * platform picker over hundreds of rows with no way to search — hopeless on mobile, which is where
 * this is aimed. Selection is emitted, never stored: the parent owns the value (usually via the URL)
 * and passes it back in, mirroring how the pages drive all their state through query params.
 */
@Component({
  selector: 'app-search-select',
  templateUrl: './search-select.html',
  styleUrl: './search-select.scss',
})
export class SearchSelect {
  readonly options = input<SearchSelectOption[]>([]);
  readonly value = input('');
  readonly placeholder = input('Search…');

  readonly valueChange = output<string>();

  /** The filter text while the panel is open; the closed input shows the selected label instead. */
  readonly query = signal('');
  readonly open = signal(false);

  private readonly inputEl = viewChild.required<ElementRef<HTMLInputElement>>('inputEl');

  readonly selectedLabel = computed(
    () => this.options().find((o) => o.value === this.value())?.label ?? '',
  );

  /** Options whose label contains the query, case-insensitively; the full list for a blank query. */
  readonly filtered = computed(() => {
    const term = this.query().trim().toLowerCase();
    const options = this.options();
    if (!term) {
      return options;
    }
    return options.filter((o) => o.label.toLowerCase().includes(term));
  });

  // Focus opens with an empty filter so the whole list is scrollable right away; the selected
  // label moves into the placeholder so it stays visible without blocking the search.
  onFocus(): void {
    this.query.set('');
    this.open.set(true);
  }

  onInput(value: string): void {
    this.query.set(value);
    this.open.set(true);
  }

  onBlur(): void {
    this.open.set(false);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      const first = this.filtered()[0];
      if (this.open() && first) {
        this.pick(first);
      }
    } else if (event.key === 'Escape') {
      this.close();
    }
  }

  /** Option rows pick on mousedown (preventDefault'ed in the template) so blur can't eat the tap. */
  pick(option: SearchSelectOption): void {
    this.valueChange.emit(option.value);
    this.close();
  }

  clear(): void {
    this.valueChange.emit('');
    this.close();
  }

  /** Close and drop focus, so the mobile keyboard goes away once a choice is made. */
  private close(): void {
    this.open.set(false);
    this.inputEl().nativeElement.blur();
  }
}
