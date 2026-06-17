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
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';
import { EventCardComponent } from '../../components/event-card/event-card';
import { Evento } from '../../models/evento.model';
import { AuthService } from '../../services/auth.service';
import { EventoService } from '../../services/evento.service';
import { UsuarioEventoService } from '../../services/usuario-evento.service';

@Component({
  selector: 'app-eventos',
  standalone: true,
  imports: [
    EventCardComponent,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './eventos.html',
  styleUrl: './eventos.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EventosComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  eventos: Evento[] = [];
  loading = true;
  errorMessage = '';
  eventosComVinculo = new Set<string>();

  constructor(
    private readonly authService: AuthService,
    private readonly eventoService: EventoService,
    private readonly usuarioEventoService: UsuarioEventoService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadEvents();
    this.loadEventosComVinculo();
  }

  loadEvents(): void {
    this.loading = true;
    this.errorMessage = '';
    this.eventoService
      .listar()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.eventos = result.eventos;
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

  participate(evento: Evento): void {
    void this.router.navigate(['/eventos', evento.id]);
  }

  viewDetails(evento: Evento): void {
    void this.router.navigate(['/eventos', evento.id]);
  }

  temVinculo(evento: Evento): boolean {
    return this.eventosComVinculo.has(String(evento.id));
  }

  isConfirmed(_evento: Evento): boolean {
    return false;
  }

  private loadEventosComVinculo(): void {
    const usuarioAtual = this.authService.getUsuarioAtual();
    const usuario$ = usuarioAtual
      ? of(usuarioAtual)
      : this.authService.me().pipe(catchError(() => of(null)));

    usuario$
      .pipe(
        switchMap((usuario) => {
          if (!usuario) {
            return of([]);
          }

          return this.usuarioEventoService
            .listarEventosDoUsuario(usuario.id)
            .pipe(catchError(() => of([])));
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((relacoes) => {
        this.eventosComVinculo = new Set(
          relacoes.map((relacao) => String(relacao.eventoId))
        );
        this.cdr.markForCheck();
      });
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Nao foi possivel conectar ao backend. Verifique se a API esta rodando em http://localhost:8080.';
      }

      return error.error?.message || 'Nao foi possivel carregar os eventos.';
    }

    return 'Nao foi possivel carregar os eventos.';
  }
}
