import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return authService.me().pipe(
    map(() => true),
    catchError(() =>
      of(router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url }
      }))
    )
  );
};
