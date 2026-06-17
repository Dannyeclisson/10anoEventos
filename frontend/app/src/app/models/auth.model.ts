import { UsuarioResponse } from './usuario.model';

export interface LoginRequest {
  email: string;
  senha: string;
}

export type UsuarioLogado = UsuarioResponse;
