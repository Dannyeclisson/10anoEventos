import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DatePickerFieldComponent } from '../../components/date-picker-field/date-picker-field';
import { UsuarioCadastroRequest } from '../../models/usuario.model';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-cadastro-usuario',
  standalone: true,
  imports: [
    DatePickerFieldComponent,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    ReactiveFormsModule
  ],
  templateUrl: './cadastro-usuario.html',
  styleUrl: './cadastro-usuario.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CadastroUsuarioComponent {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly snackBar = inject(MatSnackBar);

  loading = false;

  readonly form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(8)]],
    dataNascimento: ['', Validators.required],
    cpf: ['', Validators.required],
    telefone: ['', Validators.required]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    const payload: UsuarioCadastroRequest = this.form.getRawValue();

    this.usuarioService.cadastrarUsuario(payload).subscribe({
      next: () => {
        this.loading = false;
        this.form.reset({
          nome: '',
          email: '',
          senha: '',
          dataNascimento: '',
          cpf: '',
          telefone: ''
        });
        this.snackBar.open('Usuario cadastrado com sucesso.', 'Fechar', {
          duration: 4000
        });
      },
      error: (error: unknown) => {
        this.loading = false;
        this.snackBar.open(this.getErrorMessage(error), 'Fechar', {
          duration: 5500
        });
      }
    });
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Nao foi possivel conectar ao backend.';
      }

      const message = String(error.error?.message || '').toLowerCase();
      if (message.includes('email')) {
        return 'Este email ja esta cadastrado.';
      }
      if (message.includes('cpf')) {
        return 'Este CPF ja esta cadastrado.';
      }

      return error.error?.message || 'Nao foi possivel cadastrar o usuario.';
    }

    return 'Nao foi possivel cadastrar o usuario.';
  }
}
