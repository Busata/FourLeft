import { Component, forwardRef, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-slide-toggle',
  templateUrl: './slide-toggle.html',
  styleUrl: './slide-toggle.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SlideToggle),
      multi: true,
    },
  ],
})
export class SlideToggle implements ControlValueAccessor {
  readonly active = signal(false);
  readonly disabled = signal(false);

  private onChange: (value: boolean) => void = () => {};
  private onTouched: () => void = () => {};

  writeValue(value: boolean): void {
    this.active.set(!!value);
  }

  registerOnChange(fn: (value: boolean) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
  }

  toggle(): void {
    if (this.disabled()) {
      return;
    }
    this.active.update((value) => !value);
    this.onChange(this.active());
    this.onTouched();
  }
}
