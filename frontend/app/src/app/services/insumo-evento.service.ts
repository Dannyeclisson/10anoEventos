import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  InsumoEventoRequest,
  InsumoEventoResponse
} from '../models/insumo-evento.model';

@Injectable({ providedIn: 'root' })
export class InsumoEventoService {
  private readonly eventosApiUrl = 'http://localhost:8080/api/eventos';

  constructor(private readonly http: HttpClient) {}

  listarPorEvento(eventoId: number): Observable<InsumoEventoResponse[]> {
    return this.http.get<InsumoEventoResponse[]>(
      `${this.eventosApiUrl}/${eventoId}/insumos`,
      { withCredentials: true }
    );
  }

  adicionarInsumo(
    eventoId: number,
    payload: InsumoEventoRequest
  ): Observable<InsumoEventoResponse> {
    return this.http.post<InsumoEventoResponse>(
      `${this.eventosApiUrl}/${eventoId}/insumos`,
      payload,
      { withCredentials: true }
    );
  }

  atualizarInsumo(
    eventoId: number,
    insumoId: number,
    payload: InsumoEventoRequest
  ): Observable<InsumoEventoResponse> {
    return this.http.put<InsumoEventoResponse>(
      `${this.eventosApiUrl}/${eventoId}/insumos/${insumoId}`,
      payload,
      { withCredentials: true }
    );
  }

  removerInsumo(eventoId: number, insumoId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.eventosApiUrl}/${eventoId}/insumos/${insumoId}`,
      { withCredentials: true }
    );
  }
}
