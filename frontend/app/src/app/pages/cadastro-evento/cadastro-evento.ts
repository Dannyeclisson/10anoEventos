import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { TimePickerFieldComponent } from '../../components/time-picker-field/time-picker-field';
import { EventoCadastroRequest, EventoResponse } from '../../models/evento.model';
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
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDatepickerModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatNativeDateModule,
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
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly eventoService = inject(EventoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  loading = false;
  loadingEvento = false;
  loadErrorMessage = '';
  usuarioNome = '';
  editMode = false;
  eventoId: number | null = null;
  readonly categoriasInsumo = CATEGORIAS_INSUMO;
  readonly unidadesMedida = UNIDADES_MEDIDA;
  readonly statusInsumo = STATUS_INSUMO;

  readonly form = this.fb.group({
    nome: ['', Validators.required],
    descricao: ['', Validators.required],
    local: ['', Validators.required],
    dataInicio: [null as Date | null, Validators.required],
    dataFim: [null as Date | null, Validators.required],
    horaInicio: ['', Validators.required],
    horaFim: ['', Validators.required],
    dataInicioInscricoes: ['', Validators.required],
    capacidadeParticipantes: [2, [
      Validators.required,
      Validators.min(2),
      Validators.pattern(/^\d+$/)
    ]],
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
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.editMode = Number.isFinite(id) && id > 0;
    this.eventoId = this.editMode ? id : null;

    const usuarioAtual = this.authService.getUsuarioAtual();
    if (usuarioAtual) {
      this.usuarioNome = usuarioAtual.nome;
      this.loadEventoParaEdicao();
      return;
    }

    this.authService.me().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (usuario) => {
        this.usuarioNome = usuario.nome;
        this.loadEventoParaEdicao();
        this.cdr.markForCheck();
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

    this.loading = true;
    const raw = this.form.getRawValue();
    const insumos = this.getInsumosPayload();
    const dataInicio = this.buildDateTime(raw.dataInicio, raw.horaInicio);
    const dataFim = this.buildDateTime(raw.dataFim, raw.horaFim);
    const dataInicioInscricoes = this.normalizeDateTimeLocal(
      raw.dataInicioInscricoes
    );

    if (!dataInicio || !dataFim || !dataInicioInscricoes) {
      this.loading = false;
      this.snackBar.open('Informe as datas e horarios obrigatorios.', 'Fechar', {
        duration: 4000
      });
      return;
    }

    const validationMessage = this.validateBusinessDates(
      dataInicio,
      dataFim,
      dataInicioInscricoes,
      Number(raw.capacidadeParticipantes)
    );

    if (validationMessage) {
      this.loading = false;
      this.snackBar.open(validationMessage, 'Fechar', {
        duration: 4500
      });
      return;
    }

    const payload: EventoCadastroRequest = {
      nome: raw.nome || '',
      descricao: raw.descricao || '',
      local: raw.local || '',
      dataInicio,
      dataFim,
      dataInicioInscricoes,
      capacidadeParticipantes: Number(raw.capacidadeParticipantes),
      ...(insumos.length ? { insumos } : {})
    };

    const request$ = this.editMode && this.eventoId
      ? this.eventoService.editarEvento(this.eventoId, payload)
      : this.eventoService.cadastrarEvento(payload);

    request$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.loading = false;
        this.cdr.markForCheck();
        this.snackBar.open(this.editMode ? 'Evento atualizado com sucesso.' : 'Evento cadastrado com sucesso.', 'Ver eventos', {
          duration: 4500
        }).onAction().subscribe(() => {
          void this.router.navigate(['/eventos']);
        });
        void this.router.navigate(['/eventos']);
      },
      error: (error: unknown) => {
        this.loading = false;
        this.snackBar.open(this.getSubmitErrorMessage(error), 'Fechar', {
          duration: 5500
        });
        this.cdr.markForCheck();
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

  private loadEventoParaEdicao(): void {
    if (!this.editMode || !this.eventoId) {
      return;
    }

    this.loadingEvento = true;
    this.loadErrorMessage = '';
    this.eventoService.buscarEventoParaEdicao(this.eventoId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: (evento) => {
        this.loadingEvento = false;
        this.patchEvento(evento);
        this.cdr.markForCheck();
      },
      error: (error: unknown) => {
        this.loadingEvento = false;
        this.loadErrorMessage = this.getLoadEditErrorMessage(error);
        this.snackBar.open(this.loadErrorMessage, 'Fechar', {
          duration: 5000
        });
        this.cdr.markForCheck();
      }
    });
  }

  retryLoadEvento(): void {
    this.loadEventoParaEdicao();
  }

  voltarAosEventos(): void {
    void this.router.navigate(['/eventos']);
  }

  private patchEvento(evento: EventoResponse): void {
    this.form.patchValue({
      nome: evento.nome,
      descricao: evento.descricao,
      local: evento.local,
      dataInicio: this.parseLocalDate(evento.dataInicio),
      dataFim: this.parseLocalDate(evento.dataFim),
      horaInicio: this.extractTime(evento.dataInicio),
      horaFim: this.extractTime(evento.dataFim),
      dataInicioInscricoes: this.toDateTimeLocalInput(evento.dataInicioInscricoes),
      capacidadeParticipantes: evento.capacidadeParticipantes
    });

    this.insumos.clear();
    for (const insumo of evento.insumos || []) {
      this.insumos.push(this.createInsumoGroup(insumo));
    }
  }

  private buildDateTime(date: Date | null | undefined, time: string | null | undefined): string {
    if (!date || !time) {
      return '';
    }

    return `${this.formatDateOnly(date)}T${time}:00`;
  }

  private normalizeDateTimeLocal(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    return value.length === 16 ? `${value}:00` : value;
  }

  private validateBusinessDates(
    dataInicio: string,
    dataFim: string,
    dataInicioInscricoes: string,
    capacidade: number
  ): string {
    const inicio = new Date(dataInicio);
    const fim = new Date(dataFim);
    const inscricoes = new Date(dataInicioInscricoes);
    const agora = new Date();

    if (inicio <= agora) {
      return 'Data de inicio do evento deve ser futura.';
    }

    if (fim <= inicio) {
      return 'Data de fim deve ser posterior ao inicio.';
    }

    if (!this.editMode && inscricoes < new Date(agora.getTime() - 5000)) {
      return 'Abertura das inscricoes deve ser agora ou no futuro.';
    }

    if (inscricoes >= inicio) {
      return 'Abertura das inscricoes deve ser anterior ao inicio do evento.';
    }

    if (!Number.isInteger(capacidade) || capacidade <= 1) {
      return 'Capacidade maxima deve ser um numero inteiro maior que 1.';
    }

    return '';
  }

  private formatDateOnly(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private parseLocalDate(value: string): Date {
    const [datePart] = value.split('T');
    const [year, month, day] = datePart.split('-').map(Number);

    return new Date(year, month - 1, day);
  }

  private extractTime(value: string): string {
    return value.split('T')[1]?.slice(0, 5) || '00:00';
  }

  private toDateTimeLocalInput(value: string): string {
    return value.slice(0, 16);
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

  private getLoadEditErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Não foi possível carregar a edição do evento.';
      }

      if (error.status === 403) {
        return 'Você não tem permissão para editar este evento.';
      }

      if (error.status === 404) {
        return 'Evento não encontrado.';
      }

      return 'Não foi possível carregar a edição do evento.';
    }

    return 'Não foi possível carregar a edição do evento.';
  }

  private getSubmitErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 403) {
        return 'Você não tem permissão para editar este evento.';
      }

      if (error.status === 404) {
        return 'Evento não encontrado.';
      }

      if (error.status === 400 && error.error?.message) {
        return error.error.message;
      }
    }

    return this.editMode
      ? 'Não foi possível atualizar o evento.'
      : 'Não foi possível cadastrar o evento.';
  }
}
