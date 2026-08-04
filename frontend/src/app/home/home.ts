import { Component, inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  template: `
    <div style="padding: 2rem; font-family: Arial, sans-serif;">
      <h2>Bem-vindo ao sistema</h2>
      <p>Você já está autenticado.</p>
      <button (click)="logout()">Sair</button>
    </div>
  `
})
export class Home {
  private authService = inject(AuthService);

  logout(): void {
    this.authService.logout();
  }
}
