export type TipoUsuario = 'ORGANIZADOR' | 'PARTICIPANTE' | 'COLABORADOR';

export interface UsuarioCadastroRequest {
  nome: string;
  email: string;
  senha: string;
  dataNascimento: string;
  cpf: string;
  telefone: string;
  tipo: TipoUsuario;
}

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  dataNascimento: string;
  cpf: string;
  telefone: string;
  tipo: TipoUsuario;
}

export interface UsuarioPerfil extends UsuarioResponse {
  foto: string;
}
