import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
}