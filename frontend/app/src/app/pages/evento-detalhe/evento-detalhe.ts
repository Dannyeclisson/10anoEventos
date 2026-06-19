import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventoResponse } from '../../models/evento.model';
import { InsumoEventoResponse } from '../../models/insumo-evento.model';
import { TipoRelacaoEvento, UsuarioEventoResponse } from '../../models/usuario-evento.model';
import { AuthService } from '../../services/auth.service';
import { EventoService } from '../../services/evento.service';
import { UsuarioEventoService } from '../../services/usuario-evento.service';

interface InsumosPorCategoria {
  categoria: string;
  insumos: InsumoEventoResponse[];
}

@Component({
  selector: 'app-evento-detalhe',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDividerModule,
    MatIconModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatSnackBarModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './evento-detalhe.html',
  styleUrl: './evento-detalhe.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EventoDetalheComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly eventoService = inject(EventoService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly usuarioEventoService = inject(UsuarioEventoService);

  evento: EventoResponse | null = null;
  participacoes: UsuarioEventoResponse[] = [];
  insumosAgrupados: InsumosPorCategoria[] = [];
  loading = true;
  submitting = false;
  errorMessage = '';
  usuarioId: number | null = null;
  readonly TipoRelacaoEvento = TipoRelacaoEvento;

  readonly form = this.fb.group({
    tipoRelacao: this.fb.control<
      | TipoRelacaoEvento.PARTICIPANTE
      | TipoRelacaoEvento.COLABORADOR
      | null
    >(null, Validators.required)
  });
  private readonly insumosSelecionados = new Set<number>();

  ngOnInit(): void {
    this.form.controls.tipoRelacao.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((tipoRelacao) => {
        if (tipoRelacao !== TipoRelacaoEvento.COLABORADOR) {
          this.insumosSelecionados.clear();
          this.cdr.markForCheck();
        }
      });
    this.resolveUsuarioAtual();
    this.loadEvento();
  }

  confirmarParticipacao(): void {
    if (!this.evento) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.snackBar.open('Selecione como deseja participar.', 'Fechar', {
        duration: 3500
      });
      return;
    }

    if (!this.usuarioId) {
      void this.router.navigate(['/login'], {
        queryParams: { returnUrl: `/eventos/${this.evento.id}` }
      });
      return;
    }

    if (!this.podeInscrever()) {
      this.snackBar.open(this.getInscricaoBloqueadaMessage(), 'Fechar', {
        duration: 4000
      });
      return;
    }

    const tipoRelacao = this.form.controls.tipoRelacao.value;
    if (
      tipoRelacao !== TipoRelacaoEvento.PARTICIPANTE &&
      tipoRelacao !== TipoRelacaoEvento.COLABORADOR
    ) {
      this.snackBar.open('Tipo de participacao invalido.', 'Fechar', {
        duration: 3500
      });
      return;
    }

    const insumoIds = Array.from(this.insumosSelecionados);
    if (tipoRelacao === TipoRelacaoEvento.COLABORADOR && !insumoIds.length) {
      this.snackBar.open('Selecione ao menos um insumo para colaborar.', 'Fechar', {
        duration: 4000
      });
      return;
    }

    this.submitting = true;
    this.usuarioEventoService
      .participarEvento(
        this.evento.id,
        tipoRelacao,
        this.usuarioId,
        tipoRelacao === TipoRelacaoEvento.COLABORADOR ? insumoIds : undefined
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting = false;
          this.form.reset({ tipoRelacao: null });
          this.insumosSelecionados.clear();
          this.snackBar.open('Participação confirmada com sucesso.', 'Fechar', {
            duration: 4000
          });
          this.loadEvento();
          this.loadParticipacoes(this.evento?.id);
          this.cdr.markForCheck();
        },
        error: () => {
          this.submitting = false;
          this.snackBar.open('Não foi possível confirmar a participação.', 'Fechar', {
            duration: 5000
          });
          this.cdr.markForCheck();
        }
      });
  }

  cancelarInscricao(): void {
    if (!this.evento || !this.usuarioId || !this.relacaoAtiva()) {
      return;
    }

    if (!confirm('Deseja cancelar sua inscricao neste evento?')) {
      return;
    }

    this.submitting = true;
    this.usuarioEventoService
      .cancelarInscricao(this.evento.id, this.usuarioId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting = false;
          this.snackBar.open('Inscrição cancelada com sucesso.', 'Fechar', {
            duration: 4000
          });
          this.loadEvento();
          this.loadParticipacoes(this.evento?.id);
          this.cdr.markForCheck();
        },
        error: () => {
          this.submitting = false;
          this.snackBar.open('Não foi possível cancelar a inscrição.', 'Fechar', {
            duration: 5000
          });
          this.cdr.markForCheck();
        }
      });
  }

  editarEvento(): void {
    if (this.evento) {
      void this.router.navigate(['/eventos', this.evento.id, 'editar']);
    }
  }

  cancelarEvento(): void {
    if (!this.evento || !this.usuarioId || !this.ehOrganizador()) {
      return;
    }

    const motivo = prompt('Motivo do cancelamento (opcional):') || undefined;
    if (!confirm('Confirma o cancelamento deste evento?')) {
      return;
    }

    this.submitting = true;
    this.eventoService
      .cancelarEvento(this.evento.id, motivo)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.submitting = false;
          this.snackBar.open('Evento cancelado com sucesso.', 'Fechar', {
            duration: 4000
          });
          this.loadEvento();
          this.cdr.markForCheck();
        },
        error: () => {
          this.submitting = false;
          this.snackBar.open('Não foi possível cancelar o evento.', 'Fechar', {
            duration: 5000
          });
          this.cdr.markForCheck();
        }
      });
  }

  usuarioJaVinculado(): boolean {
    return !!this.relacaoAtiva();
  }

  relacaoAtiva(): UsuarioEventoResponse | null {
    if (this.usuarioId === null) {
      return null;
    }

    return this.participacoes.find(
      (relacao) =>
        relacao.usuarioId === this.usuarioId &&
        relacao.tipoRelacao !== TipoRelacaoEvento.CANCELADO
    ) || null;
  }

  ehOrganizador(): boolean {
    return this.usuarioId !== null && (
      this.evento?.organizadorId === this.usuarioId ||
      this.participacoes.some(
        (relacao) =>
          relacao.usuarioId === this.usuarioId &&
          relacao.tipoRelacao === TipoRelacaoEvento.ORGANIZADOR
      )
    );
  }

  getTipoLabel(tipo: TipoRelacaoEvento): string {
    const labels: Record<TipoRelacaoEvento, string> = {
      [TipoRelacaoEvento.CANCELADO]: 'Cancelado',
      [TipoRelacaoEvento.PARTICIPANTE]: 'Participante',
      [TipoRelacaoEvento.COLABORADOR]: 'Colaborador',
      [TipoRelacaoEvento.ORGANIZADOR]: 'Organizador'
    };

    return labels[tipo];
  }

  getStatusEventoLabel(): string {
    const labels = {
      agendado: 'Agendado',
      em_andamento: 'Em andamento',
      finalizado: 'Finalizado',
      cancelado: 'Cancelado',
      adiado: 'Adiado'
    };

    return this.evento ? labels[this.evento.statusEvento] : '';
  }

  getStatusInscricaoLabel(): string {
    const labels = {
      nao_aberta: 'Nao aberta',
      aberta: 'Aberta',
      encerrada: 'Encerrada',
      lotada: 'Lotada'
    };

    return this.evento ? labels[this.evento.statusInscricao] : '';
  }

  getQuantidadeInscritos(): number {
    return this.evento?.quantidadeInscritos ?? this.evento?.participantes ?? 0;
  }

  podeInscrever(): boolean {
    return !!this.evento &&
      !this.ehOrganizador() &&
      !this.usuarioJaVinculado() &&
      this.evento.statusInscricao === 'aberta';
  }

  podeCancelarInscricao(): boolean {
    const relacao = this.relacaoAtiva();
    return !!relacao &&
      relacao.tipoRelacao !== TipoRelacaoEvento.ORGANIZADOR &&
      relacao.tipoRelacao !== TipoRelacaoEvento.CANCELADO;
  }

  podeEditarEvento(): boolean {
    return this.ehOrganizador() &&
      this.evento?.statusEvento !== 'cancelado' &&
      this.evento?.statusEvento !== 'finalizado';
  }

  podeCancelarEvento(): boolean {
    return this.ehOrganizador() &&
      this.evento?.statusEvento !== 'cancelado' &&
      this.evento?.statusEvento !== 'finalizado';
  }

  getInscricaoBloqueadaMessage(): string {
    if (!this.evento) {
      return 'Evento indisponivel.';
    }

    if (this.evento.statusInscricao === 'nao_aberta') {
      return 'Inscricoes ainda nao abertas.';
    }

    if (this.evento.statusInscricao === 'lotada') {
      return 'Evento lotado.';
    }

    if (this.evento.statusEvento === 'cancelado') {
      return 'Evento cancelado.';
    }

    if (this.evento.statusEvento === 'finalizado') {
      return 'Evento finalizado.';
    }

    if (this.evento.statusEvento === 'em_andamento') {
      return 'Evento em andamento.';
    }

    return 'Inscricoes encerradas.';
  }

  colaboradorSelecionado(): boolean {
    return this.form.controls.tipoRelacao.value === TipoRelacaoEvento.COLABORADOR &&
      !this.usuarioJaVinculado();
  }

  insumoSelecionado(insumoId: number): boolean {
    return this.insumosSelecionados.has(insumoId);
  }

  insumoIndisponivel(insumo: InsumoEventoResponse): boolean {
    return !!insumo.responsavelId;
  }

  atualizarSelecaoInsumo(insumoId: number, selected: boolean): void {
    if (selected) {
      this.insumosSelecionados.add(insumoId);
    } else {
      this.insumosSelecionados.delete(insumoId);
    }
  }

  private loadEvento(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.loading = false;
      this.errorMessage = 'Evento invalido.';
      return;
    }

    this.loading = true;
    this.eventoService
      .buscarEventoPorId(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (evento) => {
          this.evento = evento;
          this.insumosAgrupados = this.agruparInsumosPorCategoria(evento.insumos || []);
          this.loading = false;
          this.loadParticipacoes(evento.id);
          this.cdr.markForCheck();
        },
        error: (error: unknown) => {
          this.evento = null;
          this.insumosAgrupados = [];
          this.loading = false;
          this.errorMessage = this.getLoadError(error);
          this.cdr.markForCheck();
        }
      });
  }

  private loadParticipacoes(eventoId?: number): void {
    if (!eventoId) {
      return;
    }

    this.usuarioEventoService
      .listarParticipacoesPorEvento(eventoId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (participacoes) => {
          this.participacoes = participacoes;
          this.cdr.markForCheck();
        },
        error: () => {
          this.participacoes = [];
          this.cdr.markForCheck();
        }
      });
  }

  private resolveUsuarioAtual(): void {
    const usuarioAtual = this.authService.getUsuarioAtual();
    if (usuarioAtual) {
      this.usuarioId = usuarioAtual.id;
      return;
    }

    this.authService
      .me()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (usuario) => {
          this.usuarioId = usuario.id;
          this.cdr.markForCheck();
        },
        error: () => {
          this.usuarioId = null;
          this.cdr.markForCheck();
        }
      });
  }

  private agruparInsumosPorCategoria(
    insumos: InsumoEventoResponse[]
  ): InsumosPorCategoria[] {
    const grupos = new Map<string, InsumoEventoResponse[]>();

    for (const insumo of insumos) {
      const categoria = insumo.categoria || 'Outros';
      grupos.set(categoria, [...(grupos.get(categoria) || []), insumo]);
    }

    return Array.from(grupos.entries()).map(([categoria, itens]) => ({
      categoria,
      insumos: itens
    }));
  }

  private getLoadError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Nao foi possivel conectar ao backend.';
      }

      return error.error?.message || 'Nao foi possivel carregar o evento.';
    }

    return 'Nao foi possivel carregar o evento.';
  }

}
