import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Registro } from './registro/registro';
import { Home } from './home/home';
import { Admin } from './admin/admin';
import { CadastroRecurso } from './cadastro-recurso/cadastro-recurso';
import { ResourceList } from './resource-list/resource-list';
import { SolicitarReserva } from './solicitar-reserva/solicitar-reserva';
import { ConsultaDisponibilidade } from './consulta-disponibilidade/consulta-disponibilidade';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'home', component: Home, canActivate: [authGuard] },
  {
    path: 'admin',
    component: Admin,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'cadastro-recurso',
    component: CadastroRecurso,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'recursos',
    component: ResourceList,
    canActivate: [authGuard]
  },
  {
    path: 'reserva/:id',
    component: SolicitarReserva,
    canActivate: [authGuard]
  },
  {
    path: 'disponibilidade',
    component: ConsultaDisponibilidade,
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '/login' }
];
