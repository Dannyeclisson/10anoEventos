import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    AsyncPipe,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavbarComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly usuario$ = this.authService.getUsuarioLogado();

  readonly publicNavigation = [
    { label: 'Home', path: '/home', icon: 'home' },
    { label: 'Eventos Confirmados', path: '/eventos', icon: 'event_available' },
    { label: 'Login', path: '/login', icon: 'login' },
    { label: 'Cadastrar Usuario', path: '/cadastro-usuario', icon: 'person_add' }
  ];

  readonly privateNavigation = [
    { label: 'Home', path: '/home', icon: 'home' },
    { label: 'Eventos Confirmados', path: '/eventos', icon: 'event_available' },
    { label: 'Meus Eventos', path: '/meus-eventos', icon: 'bookmark' },
    { label: 'Perfil', path: '/perfil', icon: 'person' },
    { label: 'Cadastrar Evento', path: '/cadastro-evento', icon: 'add_circle' }
  ];

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.authService.me().subscribe({ error: () => undefined });
    }
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => void this.router.navigate(['/home']),
      error: () => {
        this.authService.clearSession();
        void this.router.navigate(['/home']);
      }
    });
  }
}
