import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { EmptyStateComponent } from '../../components/empty-state/empty-state';
import { EventCardComponent } from '../../components/event-card/event-card';
import { Evento } from '../../models/evento.model';
import { EventoService } from '../../services/evento.service';

@Component({
  selector: 'app-meus-eventos',
  standalone: true,
  imports: [
    AsyncPipe,
    EmptyStateComponent,
    EventCardComponent,
    MatSnackBarModule
  ],
  templateUrl: './meus-eventos.html',
  styleUrl: './meus-eventos.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeusEventosComponent {
  private readonly eventoService = inject(EventoService);
  readonly confirmados$ = this.eventoService.confirmados$;

  constructor(
    private readonly snackBar: MatSnackBar,
    private readonly router: Router
  ) {}

  cancelParticipation(evento: Evento): void {
    this.eventoService.desistir(evento.id);
    this.snackBar.open(
      `Sua participação em “${evento.nome}” foi cancelada.`,
      'Fechar',
      { duration: 4000 }
    );
  }

  viewDetails(evento: Evento): void {
    this.snackBar.open(
      `${evento.nome}: presença confirmada para ${evento.local}.`,
      'Fechar',
      { duration: 4000 }
    );
  }

  exploreEvents(): void {
    void this.router.navigate(['/eventos']);
  }
}
