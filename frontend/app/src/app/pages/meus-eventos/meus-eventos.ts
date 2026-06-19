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
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { EmptyStateComponent } from '../../components/empty-state/empty-state';
import { EventoResponse } from '../../models/evento.model';
import { TipoRelacaoEvento, UsuarioEventoResponse } from '../../models/usuario-evento.model';
import { AuthService } from '../../services/auth.service';
import { EventoService } from '../../services/evento.service';
import { UsuarioEventoService } from '../../services/usuario-evento.service';

interface MeuEventoView {
  relacao: UsuarioEventoResponse;
  evento: EventoResponse | null;
}

@Component({
  selector: 'app-meus-eventos',
  standalone: true,
  imports: [
    DatePipe,
    EmptyStateComponent,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './meus-eventos.html',
  styleUrl: './meus-eventos.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeusEventosComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly eventoService = inject(EventoService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly usuarioEventoService = inject(UsuarioEventoService);
  private readonly router = inject(Router);

  eventos: MeuEventoView[] = [];
  loading = true;
  errorMessage = '';
  private usuarioId: number | null = null;

  ngOnInit(): void {
    this.loadEventos();
  }

  loadEventos(): void {
    this.loading = true;
    this.errorMessage = '';

    this.authService
      .me()
      .pipe(
        switchMap((usuario) => {
          this.usuarioId = usuario.id;
          return this.usuarioEventoService.listarEventosDoUsuario(usuario.id);
        }),
        switchMap((relacoes) => {
          if (!relacoes.length) {
            return of([]);
          }

          return forkJoin(
            relacoes.map((relacao) =>
              this.eventoService.buscarEventoPorId(relacao.eventoId).pipe(
                map((evento) => ({ relacao, evento })),
                catchError(() => of({ relacao, evento: null }))
              )
            )
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (eventos) => {
          this.eventos = eventos;
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: (error: unknown) => {
          this.eventos = [];
          this.loading = false;
          this.errorMessage = this.getErrorMessage(error);
          this.cdr.markForCheck();
        }
      });
  }

  cancelParticipation(item: MeuEventoView): void {
    if (!this.usuarioId || !this.podeDesistir(item)) {
      return;
    }

    if (!confirm('Deseja cancelar sua inscricao neste evento?')) {
      return;
    }

    this.usuarioEventoService
      .cancelarInscricao(item.relacao.eventoId, this.usuarioId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.snackBar.open('Participacao cancelada.', 'Fechar', {
            duration: 4000
          });
          this.loadEventos();
        },
        error: (error: unknown) => {
          this.snackBar.open(this.getErrorMessage(error), 'Fechar', {
            duration: 5000
          });
        }
      });
  }

  viewDetails(item: MeuEventoView): void {
    void this.router.navigate(['/eventos', item.relacao.eventoId]);
  }

  exploreEvents(): void {
    void this.router.navigate(['/eventos']);
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

  podeDesistir(item: MeuEventoView): boolean {
    return item.relacao.tipoRelacao === TipoRelacaoEvento.PARTICIPANTE ||
      item.relacao.tipoRelacao === TipoRelacaoEvento.COLABORADOR;
  }

  getStatusEventoLabel(evento: EventoResponse | null): string {
    if (!evento) {
      return 'Indisponivel';
    }

    const labels = {
      agendado: 'Agendado',
      em_andamento: 'Em andamento',
      finalizado: 'Finalizado',
      cancelado: 'Cancelado',
      adiado: 'Adiado'
    };

    return labels[evento.statusEvento];
  }

  getQuantidadeInscritos(evento: EventoResponse | null): number {
    return evento?.quantidadeInscritos ?? evento?.participantes ?? 0;
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Nao foi possivel conectar ao backend.';
      }

      return error.error?.message || 'Nao foi possivel carregar seus eventos.';
    }

    return 'Nao foi possivel carregar seus eventos.';
  }
}
