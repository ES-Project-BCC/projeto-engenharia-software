import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page } from './reservation.service';

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

// DTO usado pelo admin para listar reservas
export interface ReservationAdminResponse {
  id: number;
  userId: number;
  userNome: string;
  userEmail: string;
  data: string;
  horarioInicio: string;
  horarioFim: string;
  status: 'PENDENTE' | 'CONFIRMADA' | 'RECUSADA' | 'CANCELADA';
}

@Injectable({ providedIn: 'root' })
export class ResourceService {
  // usa a URL definida no environment (localhost em dev, Render em producao)
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);

  criarRecurso(recurso: ResourceRequest): Observable<ResourceResponse> {
    return this.http.post<ResourceResponse>(`${this.apiUrl}/api/resources`, recurso);
  }

  // manda o PUT pra atualizar o recurso pelo id
  editarRecurso(id: number, recurso: ResourceRequest): Observable<ResourceResponse> {
    return this.http.put<ResourceResponse>(`${this.apiUrl}/api/resources/${id}`, recurso);
  }

  // busca os dados de um recurso especifico pra preencher o form de edicao
  buscarRecursoPorId(id: number): Observable<ResourceResponse> {
    return this.http.get<ResourceResponse>(`${this.apiUrl}/api/resources/${id}`);
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

  // lista as reservas do recurso (admin)
  listarReservasPorRecurso(resourceId: number, page: number, size: number): Observable<Page<ReservationAdminResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'data,desc');
    return this.http.get<Page<ReservationAdminResponse>>(
      `${this.apiUrl}/api/resources/${resourceId}/reservations`,
      { params }
    );
  }
}