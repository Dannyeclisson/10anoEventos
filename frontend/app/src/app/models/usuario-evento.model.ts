export type TipoRelacaoEvento = 1 | 2 | 3;

export interface ParticiparEventoRequest {
  usuarioId: number;
  tipoRelacao: 2 | 3;
  insumoIds?: number[];
}

export interface UsuarioEventoResponse {
  id: number;
  eventoId: number;
  nomeEvento: string;
  usuarioId: number;
  nomeUsuario: string;
  tipoRelacao: TipoRelacaoEvento;
  descricaoTipoRelacao: 'ORGANIZADOR' | 'COLABORADOR' | 'PARTICIPANTE';
}
