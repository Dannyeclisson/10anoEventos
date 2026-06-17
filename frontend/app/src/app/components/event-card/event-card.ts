import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { Evento } from '../../models/evento.model';

@Component({
  selector: 'app-event-card',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule
  ],
  templateUrl: './event-card.html',
  styleUrl: './event-card.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EventCardComponent {
  @Input({ required: true }) evento!: Evento;
  @Input() modo: 'disponivel' | 'confirmado' = 'disponivel';
  @Input() textoAcao = 'Participar';
  @Input() exibirStatus = false;
  @Input() exibirParticipantes = true;
  @Input() acaoDesabilitada = false;

  @Output() visualizar = new EventEmitter<Evento>();
  @Output() acao = new EventEmitter<Evento>();

  handleImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.onerror = null;
    image.src = '/assets/event-community.svg';
  }
}
