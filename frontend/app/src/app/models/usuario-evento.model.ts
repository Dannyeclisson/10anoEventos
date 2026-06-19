export enum TipoRelacaoEvento {
  CANCELADO = 0,
  PARTICIPANTE = 1,
  COLABORADOR = 2,
  ORGANIZADOR = 3
}

export interface ParticiparEventoRequest {
  usuarioId: number;
  tipoRelacao: TipoRelacaoEvento.PARTICIPANTE | TipoRelacaoEvento.COLABORADOR;
  insumoIds?: number[];
}

export interface UsuarioEventoResponse {
  id: number;
  eventoId: number;
  nomeEvento: string;
  usuarioId: number;
  nomeUsuario: string;
  tipoRelacao: TipoRelacaoEvento;
  descricaoTipoRelacao:
    | 'CANCELADO'
    | 'ORGANIZADOR'
    | 'COLABORADOR'
    | 'PARTICIPANTE';
}
