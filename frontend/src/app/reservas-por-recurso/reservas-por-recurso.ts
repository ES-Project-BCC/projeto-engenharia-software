import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ResourceService, ReservationAdminResponse } from '../services/resource.service';
import { Page } from '../services/reservation.service';

@Component({
  selector: 'app-reservas-por-recurso',
  imports: [CommonModule, RouterLink],
  templateUrl: './reservas-por-recurso.html',
  styleUrl: './reservas-por-recurso.css',
})
export class ReservasPorRecurso implements OnInit {
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);

  resourceId!: number;
  reservas: ReservationAdminResponse[] = [];
  isLoading = true;
  errorMessage: string | null = null;

  // paginacao
  currentPage = 0;
  totalPages = 0;
  pageSize = 10;

  ngOnInit(): void {
    this.resourceId = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarReservas();
  }

  carregarReservas(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.resourceService.listarReservasPorRecurso(this.resourceId, this.currentPage, this.pageSize).subscribe({
      next: (page: Page<ReservationAdminResponse>) => {
        this.reservas = page.content;
        this.totalPages = page.totalPages;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Erro ao carregar reservas. Tente novamente.';
        this.cdr.detectChanges();
      }
    });
  }

  irParaPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPages) {
      this.currentPage = pagina;
      this.carregarReservas();
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'CONFIRMADA': return 'badge-confirmada';
      case 'PENDENTE':   return 'badge-pendente';
      case 'RECUSADA':   return 'badge-recusada';
      case 'CANCELADA':  return 'badge-cancelada';
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'CONFIRMADA': return '✅ Confirmada';
      case 'PENDENTE':   return '⏳ Pendente';
      case 'RECUSADA':   return '❌ Recusada';
      case 'CANCELADA':  return '🚫 Cancelada';
      default: return status;
    }
  }
}
