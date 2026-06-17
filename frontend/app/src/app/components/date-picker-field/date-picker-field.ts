import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ControlValueAccessor,
  FormControl,
  NgControl,
  ReactiveFormsModule
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-date-picker-field',
  standalone: true,
  imports: [
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule
  ],
  templateUrl: './date-picker-field.html',
  styleUrl: './date-picker-field.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DatePickerFieldComponent implements ControlValueAccessor {
  private readonly ngControl = inject(NgControl, {
    optional: true,
    self: true
  });

  @Input() label = 'Data';
  @Input() hint = 'DD/MM/AAAA';
  @Input() requiredError = 'Data obrigatoria';

  readonly control = new FormControl<Date | null>(null);

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor() {
    if (this.ngControl) {
      this.ngControl.valueAccessor = this;
    }

    this.control.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((value) => this.onChange(this.formatDate(value)));
  }

  get showRequiredError(): boolean {
    const parentControl = this.ngControl?.control;
    return !!(
      parentControl?.hasError('required') &&
      (parentControl.touched || parentControl.dirty)
    );
  }

  writeValue(value: string | Date | null): void {
    this.control.setValue(this.parseDate(value), { emitEvent: false });
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.control.disable({ emitEvent: false });
      return;
    }

    this.control.enable({ emitEvent: false });
  }

  markTouched(): void {
    this.onTouched();
  }

  private parseDate(value: string | Date | null): Date | null {
    if (!value) {
      return null;
    }

    if (value instanceof Date) {
      return value;
    }

    const [year, month, day] = value.split('-').map(Number);
    if (!year || !month || !day) {
      return null;
    }

    return new Date(year, month - 1, day);
  }

  private formatDate(value: Date | null): string {
    if (!value) {
      return '';
    }

    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
