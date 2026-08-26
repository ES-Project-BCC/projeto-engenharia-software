import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ReservationRequest {
    resourceId: number;
    data: string;
    horarioInicio: string;
    horarioFim: string;
}

export interface ReservationResponse {
    id: number;
    resourceId: number;
    data: string;
    horarioInicio: string;
    horarioFim: string;
    status: 'PENDENTE' | 'CONFIRMADA' | 'RECUSADA' | 'CANCELADA';
}

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
}

export interface MinhaReservaResponse {
    id: number;
    resourceId: number;
    resourceNome: string;
    resourceTipo: 'LABORATORIO' | 'EQUIPAMENTO';
    data: string;
    horarioInicio: string;
    horarioFim: string;
    status: 'PENDENTE' | 'CONFIRMADA' | 'RECUSADA' | 'CANCELADA';
}

@Injectable({ providedIn: 'root' })
export class ReservationService {
    // usa a URL definida no environment (localhost em dev, Render em producao)
    private apiUrl = environment.apiUrl;
    private http = inject(HttpClient);

    criarReserva(reserva: ReservationRequest): Observable<ReservationResponse> {
        return this.http.post<ReservationResponse>(`${this.apiUrl}/api/reservations`, reserva);
    }

    // lista as reservas do usuario autenticado com paginacao (US08, task #96)
    listarMinhasReservas(page: number, size: number): Observable<Page<MinhaReservaResponse>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<MinhaReservaResponse>>(`${this.apiUrl}/api/reservations/me`, { params });
    }

    cancelarReserva(id: number): Observable<ReservationResponse> {
        return this.http.patch<ReservationResponse>(`${this.apiUrl}/api/reservations/${id}/cancelar`, {});
    }
}