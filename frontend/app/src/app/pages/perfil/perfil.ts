import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  OnInit,
  inject
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { ProfileInfoCardComponent } from '../../components/profile-info-card/profile-info-card';
import { StatsCardComponent } from '../../components/stats-card/stats-card';
import { Evento } from '../../models/evento.model';
import { UsuarioPerfil, UsuarioResponse } from '../../models/usuario.model';
import { AuthService } from '../../services/auth.service';
import { EventoService } from '../../services/evento.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule,
    MatListModule,
    MatProgressSpinnerModule,
    ProfileInfoCardComponent,
    StatsCardComponent
  ],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PerfilComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  usuario: UsuarioPerfil | null = null;
  loadingUsuario = true;

  confirmados: Evento[] = [];
  desistencias = 0;

  get totalParticipados(): number {
    return this.confirmados.length + this.desistencias;
  }

  constructor(
    private readonly authService: AuthService,
    private readonly eventoService: EventoService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {
    this.eventoService.confirmados$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((eventos) => {
        this.confirmados = eventos;
        this.cdr.markForCheck();
      });

    this.eventoService.desistencias$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((total) => {
        this.desistencias = total;
        this.cdr.markForCheck();
      });
  }

  ngOnInit(): void {
    const usuarioAtual = this.authService.getUsuarioAtual();
    if (usuarioAtual) {
      this.usuario = this.toPerfil(usuarioAtual);
      this.loadingUsuario = false;
      return;
    }

    this.authService.me()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (usuario) => {
          this.usuario = this.toPerfil(usuario);
          this.loadingUsuario = false;
          this.cdr.markForCheck();
        },
        error: () => {
          this.loadingUsuario = false;
          void this.router.navigate(['/login'], {
            queryParams: { returnUrl: '/perfil' }
          });
        }
      });
  }

  private toPerfil(usuario: UsuarioResponse): UsuarioPerfil {
    return {
      ...usuario,
      foto: '/assets/profile-avatar.svg'
    };
  }
}
