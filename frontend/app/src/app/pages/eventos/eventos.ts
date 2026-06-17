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
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { EventCardComponent } from '../../components/event-card/event-card';
import { Evento } from '../../models/evento.model';
import { EventoService } from '../../services/evento.service';

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
  confirmados = new Set<string>();

  constructor(
    private readonly eventoService: EventoService,
    private readonly snackBar: MatSnackBar,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.eventoService.confirmados$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((eventos) => {
        this.confirmados = new Set(eventos.map((evento) => String(evento.id)));
        this.cdr.markForCheck();
      });
  }

  ngOnInit(): void {
    this.loadEvents();
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
    this.eventoService.confirmarParticipacao(evento);
    this.snackBar.open(
      `Participacao confirmada em "${evento.nome}".`,
      'Ver meus eventos',
      { duration: 4500 }
    ).onAction().subscribe(() => {
      void this.router.navigate(['/meus-eventos']);
    });
  }

  viewDetails(evento: Evento): void {
    this.snackBar.open(
      `${evento.nome}: ${evento.local}, em breve com detalhes completos.`,
      'Fechar',
      { duration: 4500 }
    );
  }

  isConfirmed(evento: Evento): boolean {
    return this.confirmados.has(String(evento.id));
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
