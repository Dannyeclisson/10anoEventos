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
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  NativeDateAdapter
} from '@angular/material/core';

const BR_DATE_FORMATS = {
  parse: {
    dateInput: 'DD/MM/YYYY'
  },
  display: {
    dateInput: 'input',
    monthYearLabel: { year: 'numeric', month: 'short' },
    dateA11yLabel: { year: 'numeric', month: 'long', day: 'numeric' },
    monthYearA11yLabel: { year: 'numeric', month: 'long' }
  }
};

class BrDateAdapter extends NativeDateAdapter {
  override parse(value: unknown): Date | null {
    if (typeof value !== 'string') {
      return value instanceof Date ? value : super.parse(value);
    }

    const trimmedValue = value.trim();
    const brDate = this.parseBrDate(trimmedValue);
    if (brDate) {
      return brDate;
    }

    return super.parse(trimmedValue);
  }

  override format(date: Date, displayFormat: Object): string {
    if (displayFormat === 'input') {
      return this.formatBrDate(date);
    }

    return super.format(date, displayFormat);
  }

  private parseBrDate(value: string): Date | null {
    const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
    if (!match) {
      return null;
    }

    const day = Number(match[1]);
    const month = Number(match[2]);
    const year = Number(match[3]);
    const date = new Date(year, month - 1, day);

    if (
      date.getFullYear() !== year ||
      date.getMonth() !== month - 1 ||
      date.getDate() !== day
    ) {
      return null;
    }

    return date;
  }

  private formatBrDate(date: Date): string {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  }
}

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
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    { provide: DateAdapter, useClass: BrDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: BR_DATE_FORMATS }
  ]
})
export class DatePickerFieldComponent implements ControlValueAccessor {
  private readonly ngControl = inject(NgControl, {
    optional: true,
    self: true
  });

  @Input() label = 'Data';
  @Input() hint = '';
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

  applyDateMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    const maskedValue = this.maskDate(input.value);
    input.value = maskedValue;

    if (!maskedValue) {
      this.onChange('');
      return;
    }

    if (maskedValue.length < 10) {
      this.onChange('');
    }
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

  private maskDate(value: string): string {
    const digits = value.replace(/\D/g, '').slice(0, 8);

    if (digits.length <= 2) {
      return digits;
    }

    if (digits.length <= 4) {
      return `${digits.slice(0, 2)}/${digits.slice(2)}`;
    }

    return `${digits.slice(0, 2)}/${digits.slice(2, 4)}/${digits.slice(4)}`;
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
