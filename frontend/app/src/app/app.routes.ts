import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./layout/main-layout/main-layout').then(
        (component) => component.MainLayoutComponent
      ),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'home' },
      {
        path: 'login',
        loadComponent: () =>
          import('./pages/login/login').then(
            (component) => component.LoginComponent
          )
      },
      {
        path: 'home',
        loadComponent: () =>
          import('./pages/home/home').then(
            (component) => component.HomeComponent
          )
      },
      {
        path: 'eventos',
        loadComponent: () =>
          import('./pages/eventos/eventos').then(
            (component) => component.EventosComponent
          )
      },
      {
        path: 'eventos/:id',
        loadComponent: () =>
          import('./pages/evento-detalhe/evento-detalhe').then(
            (component) => component.EventoDetalheComponent
          )
      },
      {
        path: 'meus-eventos',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/meus-eventos/meus-eventos').then(
            (component) => component.MeusEventosComponent
          )
      },
      {
        path: 'perfil',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/perfil/perfil').then(
            (component) => component.PerfilComponent
          )
      },
      {
        path: 'cadastro-usuario',
        loadComponent: () =>
          import('./pages/cadastro-usuario/cadastro-usuario').then(
            (component) => component.CadastroUsuarioComponent
          )
      },
      {
        path: 'cadastro-evento',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./pages/cadastro-evento/cadastro-evento').then(
            (component) => component.CadastroEventoComponent
          )
      }
    ]
  },
  { path: '**', redirectTo: 'home' }
];
