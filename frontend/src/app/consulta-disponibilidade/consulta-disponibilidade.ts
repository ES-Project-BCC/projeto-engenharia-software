import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import {
  ResourceService,
  AvailabilityResponse,
} from '../services/resource.service';
import { ResourceBlockResponse, ResourceBlockService } from '../services/resource-block.service';

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
  bloqueiosPorRecurso = new Map<number, ResourceBlockResponse>();

  private resourceBlockService = inject(ResourceBlockService);

  get disponiveis() {
    return this.resultados.filter((r) => r.disponivel && !this.bloqueioDoRecurso(r.id));
  }

  get indisponiveis() {
    return this.resultados.filter((r) => !r.disponivel || this.bloqueioDoRecurso(r.id));
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
    this.bloqueiosPorRecurso.clear();

    this.resourceService
      .consultarDisponibilidade(this.data, this.horarioInicio, this.horarioFim)
      .subscribe({
        next: (data) => {
          this.resultados = data;
          
          if (data.length === 0) {
            this.consultado = true;
            this.carregando = false;
            this.cdr.detectChanges();
            return;
          }

          forkJoin(data.map((recurso) => this.resourceBlockService.listarBloqueios(recurso.id))).subscribe({
            next: (bloqueios) => {
              data.forEach((recurso, index) => {
                const bloqueio = bloqueios[index].find((item) =>
                  this.sobrepoePeriodo(item.inicio, item.fim, `${this.data}T${this.horarioInicio}`, `${this.data}T${this.horarioFim}`)
                );
                if (bloqueio) {
                  this.bloqueiosPorRecurso.set(recurso.id, bloqueio);
                }
              });
              this.consultado = true;
              this.carregando = false;
              this.cdr.detectChanges();
            },
            error: () => {
              this.carregando = false;
              this.erro = 'Erro ao consultar os bloqueios dos recursos. Tente novamente.';
              this.cdr.detectChanges();
            },
          });
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
    this.bloqueiosPorRecurso.clear();
  }

  tipoLabel(tipo: string): string {
    return tipo === 'LABORATORIO' ? 'Laboratório' : 'Equipamento';
  }

  bloqueioDoRecurso(resourceId: number): ResourceBlockResponse | undefined {
    return this.bloqueiosPorRecurso.get(resourceId);
  }

  private sobrepoePeriodo(inicio: string, fim: string, periodoInicio: string, periodoFim: string): boolean {
    return new Date(inicio).getTime() < new Date(periodoFim).getTime()
      && new Date(fim).getTime() > new Date(periodoInicio).getTime();
  }
}
