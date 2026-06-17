import {
  InsumoEventoRequest,
  InsumoEventoResponse
} from './insumo-evento.model';

export type EventoId = number | string;

export interface EventoCadastroRequest {
  nome: string;
  descricao: string;
  local: string;
  dataHora: string;
  organizadorId: number;
  insumos?: InsumoEventoRequest[];
}

export interface EventoResponse {
  id: number;
  nome: string;
  descricao: string;
  local: string;
  dataHora: string;
  organizadorId?: number;
  organizadorNome?: string;
  participantes?: number;
  insumos?: InsumoEventoResponse[];
}

export interface Evento {
  id: EventoId;
  nome: string;
  descricao: string;
  local: string;
  data: string;
  organizadorNome: string;
  participantes: number;
  imagem: string;
  categoria: string;
  status: 'Disponivel' | 'Confirmado';
}

export interface EventosResultado {
  eventos: Evento[];
}
