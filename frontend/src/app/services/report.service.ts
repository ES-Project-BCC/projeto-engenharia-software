import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ResourceUsageReport {
  recursoId: number;
  recursoNome: string;
  totalReservas: number;
}

export interface WeekdayUsageReport {
  diaSemana: string;
  totalReservas: number;
}

export interface UsageReportResponse {
  recursoMaisUtilizado: ResourceUsageReport | null;
  recursoMenosUtilizado: ResourceUsageReport | null;
  totalReservas: number;
  reservasPorRecurso: ResourceUsageReport[];
  distribuicaoPorDiaSemana: WeekdayUsageReport[];
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);

  consultarRelatorioUtilizacao(dataInicio: string, dataFim: string): Observable<UsageReportResponse> {
    const params = new HttpParams()
      .set('dataInicio', dataInicio)
      .set('dataFim', dataFim);

    return this.http.get<UsageReportResponse>(`${this.apiUrl}/api/reports/utilizacao`, { params });
  }
}