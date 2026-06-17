import { ChangeDetectionStrategy, Component, Input, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ControlValueAccessor,
  FormControl,
  NgControl,
  ReactiveFormsModule
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTimepickerModule } from '@angular/material/timepicker';

@Component({
  selector: 'app-time-picker-field',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatTimepickerModule,
    ReactiveFormsModule
  ],
  templateUrl: './time-picker-field.html',
  styleUrl: './time-picker-field.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TimePickerFieldComponent implements ControlValueAccessor {
  private readonly ngControl = inject(NgControl, {
    optional: true,
    self: true
  });

  @Input() label = 'Horario';
  @Input() hint = '';
  @Input() requiredError = 'Horario obrigatorio';

  readonly control = new FormControl<Date | null>(null);

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor() {
    if (this.ngControl) {
      this.ngControl.valueAccessor = this;
    }

    this.control.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((value) => this.onChange(this.formatTime(value)));
  }

  get showRequiredError(): boolean {
    const parentControl = this.ngControl?.control;
    return !!(
      parentControl?.hasError('required') &&
      (parentControl.touched || parentControl.dirty)
    );
  }

  writeValue(value: string | Date | null): void {
    this.control.setValue(this.parseTime(value), { emitEvent: false });
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

  private parseTime(value: string | Date | null): Date | null {
    if (!value) {
      return null;
    }

    if (value instanceof Date) {
      return value;
    }

    const [hours, minutes] = value.split(':').map(Number);
    if (!Number.isFinite(hours) || !Number.isFinite(minutes)) {
      return null;
    }

    const date = new Date();
    date.setHours(hours, minutes, 0, 0);
    return date;
  }

  private formatTime(value: Date | null): string {
    if (!value) {
      return '';
    }

    const hours = String(value.getHours()).padStart(2, '0');
    const minutes = String(value.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }
}
