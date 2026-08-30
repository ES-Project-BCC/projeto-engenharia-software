import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);

  consultarRelatorioUtilizacao(dataInicio: string, dataFim: string): Observable<unknown> {
    const params = new HttpParams()
      .set('dataInicio', dataInicio)
      .set('dataFim', dataFim);

    return this.http.get<unknown>(`${this.apiUrl}/api/reports/utilizacao`, { params });
  }
}