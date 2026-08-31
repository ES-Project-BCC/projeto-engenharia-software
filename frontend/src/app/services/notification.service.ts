import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface NotificationResponse {
  id: number;
  reservationId: number;
  mensagem: string;
  criadaEm: string;
  lida: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface UnreadCount {
  total: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = environment.apiUrl + '/api/notifications';

  constructor(private http: HttpClient) {}

  listarNotificacoes(page: number = 0, size: number = 10): Observable<Page<NotificationResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<Page<NotificationResponse>>(this.apiUrl, { params });
  }

  marcarComoLida(id: number): Observable<NotificationResponse> {
    return this.http.patch<NotificationResponse>(`${this.apiUrl}/${id}/ler`, {});
  }

  contarNaoLidas(): Observable<UnreadCount> {
    return this.http.get<UnreadCount>(`${this.apiUrl}/nao-lidas/contagem`);
  }
}
