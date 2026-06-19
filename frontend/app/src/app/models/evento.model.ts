import {
  InsumoEventoRequest,
  InsumoEventoResponse
} from './insumo-evento.model';

export type EventoId = number | string;

export type StatusEvento =
  | 'agendado'
  | 'em_andamento'
  | 'finalizado'
  | 'cancelado'
  | 'adiado';

export type StatusInscricao =
  | 'nao_aberta'
  | 'aberta'
  | 'encerrada'
  | 'lotada';

export interface EventoCadastroRequest {
  nome: string;
  descricao: string;
  local: string;
  imagemUrl?: string;
  dataInicio: string;
  dataFim: string;
  dataInicioInscricoes: string;
  capacidadeParticipantes: number;
  insumos?: InsumoEventoRequest[];
}

export interface EventoResponse {
  id: number;
  nome: string;
  descricao: string;
  local: string;
  imagemUrl?: string;
  dataHora?: string;
  dataInicio: string;
  dataFim: string;
  dataInicioInscricoes: string;
  capacidadeParticipantes: number;
  statusEvento: StatusEvento;
  statusInscricao: StatusInscricao;
  quantidadeInscritos?: number;
  organizadorId?: number;
  nomeOrganizador?: string;
  organizadorNome?: string;
  participantes?: number;
  insumos?: InsumoEventoResponse[];
  dataCancelamento?: string;
  motivoCancelamento?: string;
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
  statusEvento?: StatusEvento;
  statusInscricao?: StatusInscricao;
  capacidadeParticipantes?: number;
}

export interface EventosResultado {
  eventos: Evento[];
}
