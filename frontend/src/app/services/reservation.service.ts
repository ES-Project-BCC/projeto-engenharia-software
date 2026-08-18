import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
    status: 'PENDENTE' | 'CONFIRMADA' | 'RECUSADA';
}

@Injectable({ providedIn: 'root' })
export class ReservationService {
    private apiUrl = 'http://localhost:8080';
    private http = inject(HttpClient);

    criarReserva(reserva: ReservationRequest): Observable<ReservationResponse> {
        return this.http.post<ReservationResponse>(`${this.apiUrl}/api/reservations`, reserva);
    }
}