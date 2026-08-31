import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { NotificationService, NotificationResponse } from '../services/notification.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class HeaderComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  notificationService = inject(NotificationService);
  router = inject(Router);

  naoLidasCount = 0;
  dropdownOpen = false;
  notificacoesRecentes: NotificationResponse[] = [];
  
  private pollingSub?: Subscription;

  ngOnInit() {
    if (this.authService.isAuthenticated()) {
      this.carregarNaoLidas();
      
      // Polling a cada 60s
      this.pollingSub = interval(60000).subscribe(() => {
        if (this.authService.isAuthenticated()) {
          this.carregarNaoLidas();
        }
      });
    }
  }

  ngOnDestroy() {
    if (this.pollingSub) {
      this.pollingSub.unsubscribe();
    }
  }

  carregarNaoLidas() {
    this.notificationService.contarNaoLidas().subscribe({
      next: (res) => this.naoLidasCount = res.total,
      error: (err) => console.error('Erro ao contar notificações', err)
    });
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
    if (this.dropdownOpen) {
      this.carregarNotificacoesRecentes();
    }
  }

  carregarNotificacoesRecentes() {
    this.notificationService.listarNotificacoes(0, 5).subscribe({
      next: (res) => {
        this.notificacoesRecentes = res.content;
      },
      error: (err) => console.error('Erro ao listar notificações', err)
    });
  }

  marcarComoLida(notificacao: NotificationResponse, event: Event) {
    event.stopPropagation();
    if (notificacao.lida) return;
    
    this.notificationService.marcarComoLida(notificacao.id).subscribe({
      next: () => {
        notificacao.lida = true;
        if (this.naoLidasCount > 0) {
          this.naoLidasCount--;
        }
      },
      error: (err) => console.error('Erro ao marcar como lida', err)
    });
  }

  verTodas() {
    this.dropdownOpen = false;
    this.router.navigate(['/notificacoes']);
  }
}
