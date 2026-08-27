import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ReservationService, ReservationRequest } from '../services/reservation.service';
import { ResourceService, ResourceResponse } from '../services/resource.service';
import { debounceTime, Subject } from 'rxjs';

@Component({
  selector: 'app-solicitar-reserva',
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './solicitar-reserva.html',
  styleUrl: './solicitar-reserva.css',
})
export class SolicitarReserva implements OnInit {
  private reservationService = inject(ReservationService);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);

  recurso: ResourceResponse | null = null;
  isLoadingRecurso = true;

  // estados do envio
  isLoading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  // estado de disponibilidade (#88)
  isCheckingDisponibilidade = false;
  horarioIndisponivel = false;

  private disponibilidadeSubject = new Subject<void>();

  reservaData: ReservationRequest = {
    resourceId: 0,
    data: '',
    horarioInicio: '',
    horarioFim: ''
  };

  ngOnInit(): void {
    // pega o id do recurso da url
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.reservaData.resourceId = id;

    // busca os recursos e filtra pelo id pra mostrar o nome
    this.resourceService.listarRecursos().subscribe({
      next: (recursos) => {
        this.recurso = recursos.find(r => r.id === id) ?? null;
        this.isLoadingRecurso = false;
        if (!this.recurso) {
          this.errorMessage = 'Recurso não encontrado ou não disponível.';
        }
      },
      error: () => {
        this.isLoadingRecurso = false;
        this.errorMessage = 'Erro ao carregar o recurso. Tente novamente.';
      }
    });

    // debounce pra nao chamar a api a cada tecla (#88)
    this.disponibilidadeSubject.pipe(debounceTime(500)).subscribe(() => {
      this.verificarDisponibilidade();
    });
  }

  onCampoHorarioChange(): void {
    const { data, horarioInicio, horarioFim } = this.reservaData;
    if (data && horarioInicio && horarioFim) {
      this.disponibilidadeSubject.next();
    } else {
      this.horarioIndisponivel = false;
    }
  }

  private verificarDisponibilidade(): void {
    const { data, horarioInicio, horarioFim, resourceId } = this.reservaData;
    this.isCheckingDisponibilidade = true;

    this.resourceService.consultarDisponibilidade(data, horarioInicio, horarioFim).subscribe({
      next: (recursos) => {
        const recursoAtual = recursos.find(r => r.id === resourceId);
        this.horarioIndisponivel = recursoAtual ? !recursoAtual.disponivel : false;
        this.isCheckingDisponibilidade = false;
      },
      error: () => {
        this.isCheckingDisponibilidade = false;
        this.horarioIndisponivel = false;
      }
    });
  }

  onSubmit() {
    // limpa mensagens anteriores antes de tentar de novo
    this.successMessage = null;
    this.errorMessage = null;
    this.isLoading = true;

    this.reservationService.criarReserva(this.reservaData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.horarioIndisponivel = false;
        // feedback de confirmacao com os detalhes da reserva (task #49)
        this.successMessage =
          `Reserva solicitada com sucesso! 📅 ${response.data} das ${response.horarioInicio} às ${response.horarioFim}. Status: ${response.status}.`;
        // limpa os campos de data/hora, mas mantém o recurso
        this.reservaData = {
          resourceId: this.recurso?.id ?? 0,
          data: '',
          horarioInicio: '',
          horarioFim: ''
        };
      },
      error: (err) => {
        this.isLoading = false;
        const mensagemBackend = err?.error?.message ?? '';

        if (err.status === 409) {
          if (mensagemBackend.toLowerCase().includes('bloqueado') || mensagemBackend.toLowerCase().includes('manutenção') || mensagemBackend.toLowerCase().includes('restrição administrativa')) {
            this.errorMessage = 'Este recurso está bloqueado neste período por manutenção ou restrição administrativa.';
          } else {
            this.errorMessage = 'Horário indisponível. Já existe uma reserva nesse período para este recurso.';
          }
        } else if (err.status === 400) {
          this.errorMessage = 'Dados inválidos. Verifique se o horário de fim é maior que o de início.';
        } else if (err.status === 403) {
          this.errorMessage = 'Sem permissão para realizar reservas. Faça login novamente.';
        } else {
          this.errorMessage = 'Erro ao solicitar reserva. Tente novamente mais tarde.';
        }
        console.error('Erro ao criar reserva', err);
      }
    });
  }
}