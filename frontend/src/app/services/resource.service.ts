import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ResourceRequest {
  nome: string;
  descricao: string;
  capacidade?: number;
  tipo: 'LABORATORIO' | 'EQUIPAMENTO';
  statusFuncionamento?: boolean;
}

export interface ResourceResponse {
  id: number;
  nome: string;
  descricao: string;
  capacidade: number;
  tipo: 'LABORATORIO' | 'EQUIPAMENTO';
  statusFuncionamento: boolean;
}

export interface AvailabilityResponse {
  id: number;
  nome: string;
  tipo: 'LABORATORIO' | 'EQUIPAMENTO';
  descricao: string;
  capacidade: number;
  disponivel: boolean;
}

@Injectable({ providedIn: 'root' })
export class ResourceService {
  private apiUrl = 'http://localhost:8080';
  private http = inject(HttpClient);

  criarRecurso(recurso: ResourceRequest): Observable<ResourceResponse> {
    return this.http.post<ResourceResponse>(`${this.apiUrl}/api/resources`, recurso);
  }

  listarRecursos(): Observable<ResourceResponse[]> {
    return this.http.get<ResourceResponse[]>(`${this.apiUrl}/api/resources`);
  }

  // busca os recursos disponiveis no periodo informado (US06)
  consultarDisponibilidade(
    data: string,
    horarioInicio: string,
    horarioFim: string
  ): Observable<AvailabilityResponse[]> {
    const params = new HttpParams()
      .set('data', data)
      .set('horarioInicio', horarioInicio)
      .set('horarioFim', horarioFim);
    return this.http.get<AvailabilityResponse[]>(
      `${this.apiUrl}/api/resources/disponibilidade`,
      { params }
    );
  }
}