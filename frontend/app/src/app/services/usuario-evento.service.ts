import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ParticiparEventoRequest,
  UsuarioEventoResponse
} from '../models/usuario-evento.model';

@Injectable({ providedIn: 'root' })
export class UsuarioEventoService {
  private readonly eventosApiUrl = 'http://localhost:8080/api/eventos';
  private readonly usuariosApiUrl = 'http://localhost:8080/api/usuarios';

  constructor(private readonly http: HttpClient) {}

  participarEvento(
    eventoId: number,
    payload: ParticiparEventoRequest
  ): Observable<UsuarioEventoResponse> {
    return this.http.post<UsuarioEventoResponse>(
      `${this.eventosApiUrl}/${eventoId}/participacoes`,
      payload,
      { withCredentials: true }
    );
  }

  listarParticipacoesPorEvento(
    eventoId: number
  ): Observable<UsuarioEventoResponse[]> {
    return this.http.get<UsuarioEventoResponse[]>(
      `${this.eventosApiUrl}/${eventoId}/participacoes`,
      { withCredentials: true }
    );
  }

  listarEventosDoUsuario(usuarioId: number): Observable<UsuarioEventoResponse[]> {
    return this.http.get<UsuarioEventoResponse[]>(
      `${this.usuariosApiUrl}/${usuarioId}/eventos`,
      { withCredentials: true }
    );
  }

  removerParticipacao(eventoId: number, usuarioId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.eventosApiUrl}/${eventoId}/participacoes/${usuarioId}`,
      { withCredentials: true }
    );
  }
}
