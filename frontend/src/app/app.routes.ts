import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Registro } from './registro/registro';
import { Home } from './home/home';
import { Admin } from './admin/admin';
import { CadastroRecurso } from './cadastro-recurso/cadastro-recurso';
import { ResourceList } from './resource-list/resource-list';
import { SolicitarReserva } from './solicitar-reserva/solicitar-reserva';
import { ConsultaDisponibilidade } from './consulta-disponibilidade/consulta-disponibilidade';
import { MinhasReservas } from './minhas-reservas/minhas-reservas';
import { ReservasPorRecurso } from './reservas-por-recurso/reservas-por-recurso';
import { BloquearRecurso } from './bloquear-recurso/bloquear-recurso';
import { RelatorioUtilizacao } from './relatorio-utilizacao/relatorio-utilizacao';
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
    path: 'bloquear-recurso',
    component: BloquearRecurso,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'relatorio-utilizacao',
    component: RelatorioUtilizacao,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'recursos/:id/bloquear',
    component: BloquearRecurso,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'recursos/:id/editar',
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
  {
    path: 'minhas-reservas',
    component: MinhasReservas,
    canActivate: [authGuard]
  },
  // rota de reservas por recurso (somente admin)
  {
    path: 'recursos/:id/reservas',
    component: ReservasPorRecurso,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  { path: '**', redirectTo: '/login' }
];
