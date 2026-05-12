import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Evento {
  id: number;
  nome: string;
  descricao: string;
  local: string;
  dataHora: string;
  organizadorId: number;
  organizadorNome: string;
}

@Injectable({
  providedIn: 'root'
})
export class EventoService {

  private api = 'http://localhost:8080/api/eventos';

  constructor(private http: HttpClient) {}

  listar(): Observable<Evento[]> {
    return this.http.get<Evento[]>(this.api);
  }

  criar(evento: any): Observable<Evento> {
    return this.http.post<Evento>(this.api, evento);
  }
}