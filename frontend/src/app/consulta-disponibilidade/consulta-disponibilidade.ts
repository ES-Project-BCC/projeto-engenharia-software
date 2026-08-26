import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  ResourceService,
  AvailabilityResponse,
} from '../services/resource.service';

@Component({
  selector: 'app-consulta-disponibilidade',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './consulta-disponibilidade.html',
  styleUrl: './consulta-disponibilidade.css',
})
export class ConsultaDisponibilidade {
  private resourceService = inject(ResourceService);
  private cdr = inject(ChangeDetectorRef);

  // campos do formulario
  data = '';
  horarioInicio = '';
  horarioFim = '';

  // variaveis de estado pra controlar a UI
  resultados: AvailabilityResponse[] = [];
  carregando = false;
  erro = '';
  consultado = false;

  get disponiveis() {
    return this.resultados.filter((r) => r.disponivel);
  }

  get indisponiveis() {
    return this.resultados.filter((r) => !r.disponivel);
  }

  consultar() {
    this.erro = '';
    this.consultado = false;

    if (!this.data || !this.horarioInicio || !this.horarioFim) {
      this.erro = 'Preencha todos os campos antes de consultar.';
      return;
    }
    if (this.horarioFim <= this.horarioInicio) {
      this.erro = 'O horário de fim deve ser posterior ao horário de início.';
      return;
    }

    this.carregando = true;
    this.resultados = [];

    this.resourceService
      .consultarDisponibilidade(this.data, this.horarioInicio, this.horarioFim)
      .subscribe({
        next: (data) => {
          this.resultados = data;
          this.consultado = true;
          this.carregando = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.carregando = false;
          if (err.status === 400) {
            this.erro = 'Horário inválido. Verifique os campos e tente novamente.';
          } else {
            this.erro = 'Erro ao consultar disponibilidade. Tente novamente.';
          }
          this.cdr.detectChanges();
        },
      });
  }

  limpar() {
    this.data = '';
    this.horarioInicio = '';
    this.horarioFim = '';
    this.resultados = [];
    this.erro = '';
    this.consultado = false;
  }

  tipoLabel(tipo: string): string {
    return tipo === 'LABORATORIO' ? 'Laboratório' : 'Equipamento';
  }
}
