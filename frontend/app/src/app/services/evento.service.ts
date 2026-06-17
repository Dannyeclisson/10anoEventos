import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map, of } from 'rxjs';
import {
  Evento,
  EventoCadastroRequest,
  EventoId,
  EventoResponse,
  EventosResultado
} from '../models/evento.model';

const STORAGE_KEY = '10anos-eventos-confirmados';
const CANCELLATIONS_KEY = '10anos-eventos-desistencias';
const FALLBACK_IMAGE = '/assets/event-community.svg';

@Injectable({ providedIn: 'root' })
export class EventoService {
  private readonly apiUrl = 'http://localhost:8080/api/eventos';
  private readonly confirmadosSubject = new BehaviorSubject<Evento[]>(
    this.readConfirmedEvents()
  );
  private readonly desistenciasSubject = new BehaviorSubject<number>(
    this.readCancellations()
  );

  readonly confirmados$ = this.confirmadosSubject.asObservable();
  readonly desistencias$ = this.desistenciasSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  cadastrarEvento(payload: EventoCadastroRequest): Observable<EventoResponse> {
    return this.http.post<EventoResponse>(this.apiUrl, payload, {
      withCredentials: true
    });
  }

  listarEventos(): Observable<EventoResponse[]> {
    return this.http.get<EventoResponse[]>(this.apiUrl);
  }

  buscarEventoPorId(id: number): Observable<EventoResponse> {
    return this.http.get<EventoResponse>(`${this.apiUrl}/${id}`);
  }

  listar(): Observable<EventosResultado> {
    return this.listarEventos().pipe(
      map((response) => ({
        eventos: response.map((item, index) => this.toCardEvent(item, index))
      }))
    );
  }

  listarMeusEventos(): Observable<Evento[]> {
    return of(this.confirmadosSubject.value);
  }

  confirmarParticipacao(evento: Evento | EventoResponse): void {
    const cardEvent = this.isCardEvent(evento)
      ? evento
      : this.toCardEvent(evento, 0);
    const atuais = this.confirmadosSubject.value;

    if (atuais.some((item) => String(item.id) === String(cardEvent.id))) {
      return;
    }

    this.updateConfirmed([
      ...atuais,
      { ...cardEvent, status: 'Confirmado' }
    ]);
  }

  confirmar(evento: Evento): void {
    this.confirmarParticipacao(evento);
  }

  desistirParticipacao(eventoId: EventoId): void {
    const atualizados = this.confirmadosSubject.value.filter(
      (evento) => String(evento.id) !== String(eventoId)
    );

    if (atualizados.length === this.confirmadosSubject.value.length) {
      return;
    }

    this.updateConfirmed(atualizados);
    const total = this.desistenciasSubject.value + 1;
    this.desistenciasSubject.next(total);
    this.writeStorage(CANCELLATIONS_KEY, total);
  }

  desistir(eventoId: EventoId): void {
    this.desistirParticipacao(eventoId);
  }

  estaConfirmado(eventoId: EventoId): boolean {
    return this.confirmadosSubject.value.some(
      (evento) => String(evento.id) === String(eventoId)
    );
  }

  private toCardEvent(evento: EventoResponse, index: number): Evento {
    return {
      id: evento.id,
      nome: evento.nome || `Evento comunitario ${index + 1}`,
      descricao:
        evento.descricao ||
        'Conecte-se com pessoas, parceiros e iniciativas da comunidade.',
      local: evento.local || 'Local a confirmar',
      data:
        evento.dataHora ||
        new Date(Date.now() + (index + 1) * 86400000).toISOString(),
      organizadorNome: evento.organizadorNome || 'Organizacao comunitaria',
      participantes: evento.participantes ?? 0,
      imagem: this.fallbackImage(index),
      categoria: 'Comunidade',
      status: 'Disponivel'
    };
  }

  private isCardEvent(evento: Evento | EventoResponse): evento is Evento {
    return 'data' in evento && 'imagem' in evento;
  }

  private fallbackImage(index: number): string {
    const images = [
      '/assets/event-fair.svg',
      '/assets/event-volunteer.svg',
      '/assets/event-culture.svg',
      '/assets/event-workshop.svg',
      '/assets/event-donation.svg'
    ];

    return images[index % images.length] || FALLBACK_IMAGE;
  }

  private updateConfirmed(eventos: Evento[]): void {
    this.confirmadosSubject.next(eventos);
    this.writeStorage(STORAGE_KEY, eventos);
  }

  private readConfirmedEvents(): Evento[] {
    const value = this.readStorage(STORAGE_KEY);
    return Array.isArray(value) ? (value as Evento[]) : [];
  }

  private readCancellations(): number {
    const value = this.readStorage(CANCELLATIONS_KEY);
    return typeof value === 'number' ? value : 0;
  }

  private readStorage(key: string): unknown {
    try {
      const value = localStorage.getItem(key);
      return value ? JSON.parse(value) : null;
    } catch {
      return null;
    }
  }

  private writeStorage(key: string, value: unknown): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // The app keeps working when browser storage is unavailable.
    }
  }
}
