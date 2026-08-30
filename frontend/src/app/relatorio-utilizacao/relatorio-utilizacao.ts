import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  ReportService,
  ResourceUsageReport,
  UsageReportResponse,
} from '../services/report.service';

@Component({
  selector: 'app-relatorio-utilizacao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './relatorio-utilizacao.html',
  styleUrl: './relatorio-utilizacao.css',
})
export class RelatorioUtilizacao {
  private reportService = inject(ReportService);

  dataInicio = '';
  dataFim = '';
  relatorio: UsageReportResponse | null = null;
  carregando = false;
  consultado = false;
  erro = '';

  get reservasPorRecurso(): ResourceUsageReport[] {
    return [...(this.relatorio?.reservasPorRecurso ?? [])].sort(
      (primeiro, segundo) => segundo.totalReservas - primeiro.totalReservas,
    );
  }

  get maiorTotalPorDia(): number {
    return Math.max(...(this.relatorio?.distribuicaoPorDiaSemana ?? []).map((dia) => dia.totalReservas), 1);
  }

  consultar(): void {
    this.erro = '';
    this.consultado = false;

    if (!this.dataInicio || !this.dataFim) {
      this.erro = 'Preencha as datas de início e fim antes de consultar.';
      return;
    }

    if (this.dataFim < this.dataInicio) {
      this.erro = 'A data de fim deve ser igual ou posterior à data de início.';
      return;
    }

    this.carregando = true;
    this.relatorio = null;

    this.reportService.consultarRelatorioUtilizacao(this.dataInicio, this.dataFim).subscribe({
      next: (relatorio) => {
        this.relatorio = relatorio;
        this.consultado = true;
        this.carregando = false;
      },
      error: () => {
        this.erro = 'Não foi possível gerar o relatório. Tente novamente.';
        this.carregando = false;
      },
    });
  }

  limpar(): void {
    this.dataInicio = '';
    this.dataFim = '';
    this.relatorio = null;
    this.consultado = false;
    this.erro = '';
  }

  larguraBarra(totalReservas: number): string {
    return `${(totalReservas / this.maiorTotalPorDia) * 100}%`;
  }
}