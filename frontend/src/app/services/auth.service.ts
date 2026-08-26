import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // usa a URL definida no environment (localhost em dev, Render em producao)
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);
  private router = inject(Router);

  login(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/api/auth/login`, credentials).pipe(
      tap(response => {
        if (response?.token) {
          localStorage.setItem('jwt_token', response.token);
        }
      })
    );
  }

  registro(dados: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/auth/register`, dados);
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private decodePayload(): Record<string, any> | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (e) {
      console.error('Erro ao decodificar token JWT', e);
      return null;
    }
  }

  // retorna a role do usuario logado ('ADMIN' ou 'USER'), ou null
  getRole(): string | null {
    const payload = this.decodePayload();
    return payload ? (payload['role'] ?? null) : null;
  }

  // retorna o e-mail do usuario logado
  getEmail(): string | null {
    const payload = this.decodePayload();
    return payload ? (payload['sub'] ?? null) : null;
  }
}
