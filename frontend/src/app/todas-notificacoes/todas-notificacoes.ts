import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService, NotificationResponse } from '../services/notification.service';

@Component({
  selector: 'app-todas-notificacoes',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './todas-notificacoes.html',
  styleUrl: './todas-notificacoes.css'
})
export class TodasNotificacoesComponent implements OnInit {
  notificationService = inject(NotificationService);

  notificacoes: NotificationResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  ngOnInit() {
    this.carregarPagina();
  }

  carregarPagina() {
    this.notificationService.listarNotificacoes(this.page, this.size).subscribe({
      next: (res) => {
        this.notificacoes = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
      },
      error: (err) => console.error('Erro ao listar notificações', err)
    });
  }

  marcarComoLida(notif: NotificationResponse) {
    if (notif.lida) return;
    this.notificationService.marcarComoLida(notif.id).subscribe({
      next: () => {
        notif.lida = true;
      },
      error: (err) => console.error('Erro ao marcar como lida', err)
    });
  }

  nextPage() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.carregarPagina();
    }
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.carregarPagina();
    }
  }
}
