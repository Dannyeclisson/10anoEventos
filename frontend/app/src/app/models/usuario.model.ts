export interface UsuarioCadastroRequest {
  nome: string;
  email: string;
  senha: string;
  dataNascimento: string;
  cpf: string;
  telefone: string;
}

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  dataNascimento: string;
  cpf: string;
  telefone: string;
}

export interface UsuarioPerfil extends UsuarioResponse {
  foto: string;
}
