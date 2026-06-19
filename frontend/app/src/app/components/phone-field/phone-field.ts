import {
  ChangeDetectionStrategy,
  Component,
  Input,
  forwardRef
} from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormControl,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
  ValidationErrors,
  Validator
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-phone-field',
  standalone: true,
  imports: [MatFormFieldModule, MatInputModule, ReactiveFormsModule],
  templateUrl: './phone-field.html',
  styleUrl: './phone-field.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PhoneFieldComponent),
      multi: true
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => PhoneFieldComponent),
      multi: true
    }
  ]
})
export class PhoneFieldComponent implements ControlValueAccessor, Validator {
  @Input() label = 'Telefone *';
  @Input() requiredError = 'Telefone obrigatorio';
  @Input() invalidError = 'Telefone invalido';

  readonly control = new FormControl<string>('', { nonNullable: true });
  readonly errorStateMatcher: ErrorStateMatcher = {
    isErrorState: () => {
      const parentControl = this.parentControl;
      return !!(
        parentControl?.invalid &&
        (parentControl.touched || parentControl.dirty)
      );
    }
  };

  private parentControl: AbstractControl<string> | null = null;
  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  get showRequiredError(): boolean {
    return this.hasParentError('required');
  }

  get showInvalidError(): boolean {
    return this.hasParentError('telefoneInvalido');
  }

  writeValue(value: string | null): void {
    this.control.setValue(this.maskPhone(value || ''), { emitEvent: false });
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

  validate(control: AbstractControl<string>): ValidationErrors | null {
    this.parentControl = control;
    const phone = this.onlyDigits(control.value || '');

    if (!phone) {
      return null;
    }

    if (phone.length !== 10 && phone.length !== 11) {
      return { telefoneInvalido: true };
    }

    const numberWithoutAreaCode = phone.slice(2);

    if (numberWithoutAreaCode.length === 8) {
      return /^(\d)\1{7}$/.test(numberWithoutAreaCode)
        ? { telefoneInvalido: true }
        : null;
    }

    if (numberWithoutAreaCode[0] !== '9') {
      return { telefoneInvalido: true };
    }

    if (/^(\d)\1{8}$/.test(numberWithoutAreaCode)) {
      return { telefoneInvalido: true };
    }

    return /^(\d)\1{7}$/.test(numberWithoutAreaCode.slice(1))
      ? { telefoneInvalido: true }
      : null;
  }

  applyPhoneMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    const maskedValue = this.maskPhone(input.value);
    const digits = this.onlyDigits(maskedValue);

    input.value = maskedValue;
    this.control.setValue(maskedValue, { emitEvent: false });
    this.onChange(digits);
  }

  markTouched(): void {
    this.control.markAsTouched();
    this.onTouched();
  }

  private hasParentError(errorName: string): boolean {
    const parentControl = this.parentControl;
    return !!(
      parentControl?.hasError(errorName) &&
      (parentControl.touched || parentControl.dirty)
    );
  }

  private maskPhone(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 11);

    if (digits.length <= 2) {
      return digits.length ? `(${digits}` : '';
    }

    const areaCode = digits.slice(0, 2);
    const phoneNumber = digits.slice(2);

    if (phoneNumber.length <= 4) {
      return `(${areaCode}) ${phoneNumber}`;
    }

    if (phoneNumber.length <= 8) {
      return `(${areaCode}) ${phoneNumber.slice(0, 4)}-${phoneNumber.slice(4)}`;
    }

    return `(${areaCode}) ${phoneNumber.slice(0, 5)}-${phoneNumber.slice(5)}`;
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }
}
