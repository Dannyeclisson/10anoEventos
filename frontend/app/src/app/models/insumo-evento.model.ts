export type StatusInsumo =
  | 'PENDENTE'
  | 'COMPRADO'
  | 'ALUGADO'
  | 'ENTREGUE'
  | 'CONFIRMADO'
  | 'CANCELADO';

export interface InsumoEventoRequest {
  categoria: string;
  nome: string;
  quantidade: number;
  unidadeMedida: string;
  observacoes?: string;
  status: StatusInsumo;
}

export interface InsumoEventoResponse {
  id: number;
  eventoId: number;
  categoria: string;
  nome: string;
  quantidade: number;
  unidadeMedida: string;
  observacoes?: string;
  status: StatusInsumo;
  responsavelId?: number;
  nomeResponsavel?: string;
}
