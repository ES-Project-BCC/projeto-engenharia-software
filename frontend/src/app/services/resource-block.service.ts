import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ResourceBlockRequest {
  resourceId: number;
  inicio: string;
  fim: string;
  motivo: string;
}

export interface ResourceBlockResponse {
  id: number;
  resourceId: number;
  resourceNome: string;
  inicio: string;
  fim: string;
  motivo: string;
}

@Injectable({ providedIn: 'root' })
export class ResourceBlockService {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);

  criarBloqueio(bloqueio: ResourceBlockRequest): Observable<ResourceBlockResponse> {
    return this.http.post<ResourceBlockResponse>(`${this.apiUrl}/api/resource-blocks`, bloqueio);
  }

  listarBloqueios(resourceId: number): Observable<ResourceBlockResponse[]> {
    const params = new HttpParams().set('resourceId', resourceId.toString());
    return this.http.get<ResourceBlockResponse[]>(`${this.apiUrl}/api/resource-blocks`, { params });
  }

  removerBloqueio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/api/resource-blocks/${id}`);
  }
}
