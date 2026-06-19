import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ParticiparEventoRequest,
  TipoRelacaoEvento,
  UsuarioEventoResponse
} from '../models/usuario-evento.model';

@Injectable({ providedIn: 'root' })
export class UsuarioEventoService {
  private readonly eventosApiUrl = 'http://localhost:8080/api/eventos';
  private readonly usuariosApiUrl = 'http://localhost:8080/api/usuarios';

  constructor(private readonly http: HttpClient) {}

  participarEvento(
    eventoId: number,
    tipoRelacao: TipoRelacaoEvento.PARTICIPANTE | TipoRelacaoEvento.COLABORADOR,
    usuarioId: number,
    insumoIds?: number[]
  ): Observable<UsuarioEventoResponse> {
    const payload: ParticiparEventoRequest = {
      // Temporario enquanto o backend ainda exige usuarioId no payload.
      usuarioId,
      tipoRelacao,
      ...(insumoIds?.length ? { insumoIds } : {})
    };

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

  cancelarInscricao(
    eventoId: number,
    usuarioId: number
  ): Observable<UsuarioEventoResponse> {
    return this.http.patch<UsuarioEventoResponse>(
      `${this.eventosApiUrl}/${eventoId}/inscricao/cancelar`,
      {
        // Temporario enquanto o backend ainda exige usuarioId no payload.
        usuarioId
      },
      { withCredentials: true }
    );
  }
}
