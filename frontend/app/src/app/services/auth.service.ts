import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, UsuarioLogado } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private readonly usuarioLogadoSubject =
    new BehaviorSubject<UsuarioLogado | null>(null);

  readonly usuarioLogado$ = this.usuarioLogadoSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginRequest): Observable<UsuarioLogado> {
    return this.http
      .post<UsuarioLogado>(`${this.apiUrl}/login`, payload, {
        withCredentials: true
      })
      .pipe(tap((usuario) => this.usuarioLogadoSubject.next(usuario)));
  }

  logout(): Observable<void> {
    return this.http
      .post<void>(`${this.apiUrl}/logout`, null, {
        withCredentials: true
      })
      .pipe(tap(() => this.clearSession()));
  }

  me(): Observable<UsuarioLogado> {
    return this.http
      .get<UsuarioLogado>(`${this.apiUrl}/me`, {
        withCredentials: true
      })
      .pipe(tap((usuario) => this.usuarioLogadoSubject.next(usuario)));
  }

  isAuthenticated(): boolean {
    return this.usuarioLogadoSubject.value !== null;
  }

  getUsuarioLogado(): Observable<UsuarioLogado | null> {
    return this.usuarioLogado$;
  }

  getUsuarioAtual(): UsuarioLogado | null {
    return this.usuarioLogadoSubject.value;
  }

  clearSession(): void {
    this.usuarioLogadoSubject.next(null);
  }
}
