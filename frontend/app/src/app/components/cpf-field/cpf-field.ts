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
  NG_VALUE_ACCESSOR,
  NG_VALIDATORS,
  ReactiveFormsModule,
  ValidationErrors,
  Validator
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-cpf-field',
  standalone: true,
  imports: [MatFormFieldModule, MatInputModule, ReactiveFormsModule],
  templateUrl: './cpf-field.html',
  styleUrl: './cpf-field.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CpfFieldComponent),
      multi: true
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => CpfFieldComponent),
      multi: true
    }
  ]
})
export class CpfFieldComponent implements ControlValueAccessor, Validator {
  @Input() label = 'CPF *';
  @Input() requiredError = 'CPF obrigatorio';
  @Input() invalidError = 'CPF invalido';

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
    return this.hasParentError('cpfInvalido');
  }

  writeValue(value: string | null): void {
    this.control.setValue(this.maskCpf(value || ''), { emitEvent: false });
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
    const cpf = this.onlyDigits(control.value || '');

    if (!cpf) {
      return null;
    }

    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
      return { cpfInvalido: true };
    }

    const firstDigit = this.calculateCpfDigit(cpf.slice(0, 9), 10);
    const secondDigit = this.calculateCpfDigit(cpf.slice(0, 10), 11);

    if (firstDigit !== Number(cpf[9]) || secondDigit !== Number(cpf[10])) {
      return { cpfInvalido: true };
    }

    return null;
  }

  applyCpfMask(event: Event): void {
    const input = event.target as HTMLInputElement;
    const maskedValue = this.maskCpf(input.value);
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

  private calculateCpfDigit(numbers: string, initialWeight: number): number {
    const sum = numbers
      .split('')
      .reduce((total, number, index) => total + Number(number) * (initialWeight - index), 0);
    const remainder = (sum * 10) % 11;

    return remainder === 10 ? 0 : remainder;
  }

  private maskCpf(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 11);

    if (digits.length <= 3) {
      return digits;
    }

    if (digits.length <= 6) {
      return `${digits.slice(0, 3)}.${digits.slice(3)}`;
    }

    if (digits.length <= 9) {
      return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`;
    }

    return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }
}
