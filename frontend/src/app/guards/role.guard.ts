import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  // Pega as roles permitidas que definimos lá no app.routes.ts
  const expectedRoles = route.data['roles'] as string[];
  const userRole = authService.getRole();

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  // Verifica se a role do usuario ta na lista de permitidas da rota
  if (expectedRoles?.includes(userRole as string)) {
    return true;
  }

  alert('Acesso negado: Você não tem permissão para acessar esta funcionalidade.');
  router.navigate(['/login']);
  return false;
};
