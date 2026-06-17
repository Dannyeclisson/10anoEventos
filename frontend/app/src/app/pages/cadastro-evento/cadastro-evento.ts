import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import {
  FormBuilder,
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule, MatChipInputEvent } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { DatePickerFieldComponent } from '../../components/date-picker-field/date-picker-field';
import { TimePickerFieldComponent } from '../../components/time-picker-field/time-picker-field';
import { EventoCadastroRequest } from '../../models/evento.model';
import {
  InsumoEventoRequest,
  StatusInsumo
} from '../../models/insumo-evento.model';
import { AuthService } from '../../services/auth.service';
import { EventoService } from '../../services/evento.service';

type InsumoEventoForm = FormGroup<{
  categoria: FormControl<string>;
  nome: FormControl<string>;
  quantidade: FormControl<number>;
  unidadeMedida: FormControl<string>;
  observacoes: FormControl<string>;
  status: FormControl<StatusInsumo>;
}>;

const CATEGORIAS_INSUMO = [
  'Alimentos',
  'Bebidas',
  'Mobiliario',
  'Utensilios e descartaveis',
  'Decoracao',
  'Equipamentos',
  'Limpeza',
  'Entretenimento',
  'Seguranca',
  'Outros'
] as const;

const UNIDADES_MEDIDA = [
  'unidades',
  'kg',
  'g',
  'litros',
  'ml',
  'garrafas',
  'caixas',
  'pacotes',
  'sacos',
  'metros'
] as const;

const STATUS_INSUMO: StatusInsumo[] = [
  'PENDENTE',
  'COMPRADO',
  'ALUGADO',
  'ENTREGUE',
  'CONFIRMADO',
  'CANCELADO'
];

@Component({
  selector: 'app-cadastro-evento',
  standalone: true,
  imports: [
    DatePickerFieldComponent,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
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
  readonly categoriasInsumo = CATEGORIAS_INSUMO;
  readonly unidadesMedida = UNIDADES_MEDIDA;
  readonly statusInsumo = STATUS_INSUMO;

  readonly form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    descricao: ['', Validators.required],
    local: ['', Validators.required],
    dataEvento: ['', Validators.required],
    horarioEvento: ['', Validators.required],
    insumos: this.fb.array<InsumoEventoForm>([])
  });

  readonly insumoForm = this.fb.nonNullable.group({
    categoria: ['Alimentos', Validators.required],
    nome: ['', Validators.required],
    quantidade: [1, [Validators.required, Validators.min(0.01)]],
    unidadeMedida: ['unidades', Validators.required],
    observacoes: [''],
    status: ['PENDENTE' as StatusInsumo, Validators.required]
  });

  get insumos(): FormArray<InsumoEventoForm> {
    return this.form.controls.insumos;
  }

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
    const insumos = this.getInsumosPayload();
    const payload: EventoCadastroRequest = {
      nome: raw.nome,
      descricao: raw.descricao,
      local: raw.local,
      dataHora: this.buildDateTime(raw.dataEvento, raw.horarioEvento),
      organizadorId: this.organizadorId,
      ...(insumos.length ? { insumos } : {})
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

  adicionarInsumo(): void {
    if (this.insumoForm.invalid) {
      this.insumoForm.markAllAsTouched();
      this.snackBar.open('Preencha os dados obrigatorios do insumo.', 'Fechar', {
        duration: 3500
      });
      return;
    }

    this.insumos.push(this.createInsumoGroup(this.insumoForm.getRawValue()));
    this.insumoForm.reset({
      categoria: 'Alimentos',
      nome: '',
      quantidade: 1,
      unidadeMedida: 'unidades',
      observacoes: '',
      status: 'PENDENTE'
    });
  }

  adicionarInsumoViaChip(event: MatChipInputEvent): void {
    const nome = (event.value || '').trim();
    event.chipInput?.clear();

    if (!nome) {
      return;
    }

    if (!this.insumoForm.controls.nome.value) {
      this.insumoForm.patchValue({ nome });
    }

    this.adicionarInsumo();
  }

  removerInsumo(index: number): void {
    this.insumos.removeAt(index);
  }

  getInsumoValue(insumo: InsumoEventoForm): InsumoEventoRequest {
    return insumo.getRawValue();
  }

  private buildDateTime(date: string, time: string): string {
    return `${date}T${time}:00`;
  }

  private createInsumoGroup(insumo: InsumoEventoRequest): InsumoEventoForm {
    return this.fb.nonNullable.group({
      categoria: [insumo.categoria, Validators.required],
      nome: [insumo.nome, Validators.required],
      quantidade: [insumo.quantidade, [Validators.required, Validators.min(0.01)]],
      unidadeMedida: [insumo.unidadeMedida, Validators.required],
      observacoes: [insumo.observacoes || ''],
      status: [insumo.status, Validators.required]
    });
  }

  private getInsumosPayload(): InsumoEventoRequest[] {
    return this.insumos.controls.map((control) => control.getRawValue());
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
