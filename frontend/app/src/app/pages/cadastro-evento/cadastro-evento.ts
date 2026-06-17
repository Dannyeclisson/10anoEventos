import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
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
import { Router } from '@angular/router';
import { DatePickerFieldComponent } from '../../components/date-picker-field/date-picker-field';
import { TimePickerFieldComponent } from '../../components/time-picker-field/time-picker-field';
import { EventoCadastroRequest } from '../../models/evento.model';
import { AuthService } from '../../services/auth.service';
import { EventoService } from '../../services/evento.service';

@Component({
  selector: 'app-cadastro-evento',
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
    ReactiveFormsModule,
    TimePickerFieldComponent
  ],
  templateUrl: './cadastro-evento.html',
  styleUrl: './cadastro-evento.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CadastroEventoComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly eventoService = inject(EventoService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  loading = false;
  usuarioNome = '';
  private organizadorId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    descricao: ['', Validators.required],
    local: ['', Validators.required],
    dataEvento: ['', Validators.required],
    horarioEvento: ['', Validators.required]
  });

  ngOnInit(): void {
    const usuarioAtual = this.authService.getUsuarioAtual();
    if (usuarioAtual) {
      this.usuarioNome = usuarioAtual.nome;
      this.organizadorId = usuarioAtual.id;
      return;
    }

    this.authService.me().subscribe({
      next: (usuario) => {
        this.usuarioNome = usuario.nome;
        this.organizadorId = usuario.id;
      },
      error: () => {
        this.snackBar.open('Faca login para cadastrar eventos.', 'Fechar', {
          duration: 4000
        });
        void this.router.navigate(['/login'], {
          queryParams: { returnUrl: '/cadastro-evento' }
        });
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.organizadorId) {
      this.snackBar.open('Nao foi possivel identificar o usuario logado.', 'Fechar', {
        duration: 4000
      });
      return;
    }

    this.loading = true;
    const raw = this.form.getRawValue();
    const payload: EventoCadastroRequest = {
      nome: raw.nome,
      descricao: raw.descricao,
      local: raw.local,
      dataHora: this.buildDateTime(raw.dataEvento, raw.horarioEvento),
      organizadorId: this.organizadorId
    };

    this.eventoService.cadastrarEvento(payload).subscribe({
      next: () => {
        this.loading = false;
        this.snackBar.open('Evento cadastrado com sucesso.', 'Ver eventos', {
          duration: 4500
        }).onAction().subscribe(() => {
          void this.router.navigate(['/eventos']);
        });
        void this.router.navigate(['/eventos']);
      },
      error: (error: unknown) => {
        this.loading = false;
        this.snackBar.open(this.getErrorMessage(error), 'Fechar', {
          duration: 5500
        });
      }
    });
  }

  private buildDateTime(date: string, time: string): string {
    return `${date}T${time}:00`;
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Nao foi possivel conectar ao backend.';
      }

      const message = String(error.error?.message || '').toLowerCase();
      if (message.includes('usuario')) {
        return 'Organizador nao encontrado. Cadastre ou informe um usuario existente.';
      }

      return error.error?.message || 'Nao foi possivel cadastrar o evento.';
    }

    return 'Nao foi possivel cadastrar o evento.';
  }
}
