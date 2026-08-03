import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080';
  private http = inject(HttpClient);
  private router = inject(Router);

  login(credentials: any): Observable<any> {
    return this.http.post<{token: string}>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        // Salva o token no navegador se a api retornar sucesso
        if (response?.token) {
          localStorage.setItem('jwt_token', response.token);
        }
      })
    );
  }

  registro(dados: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/registro`, dados);
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

  getRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    
    try {
      // O JWT tem 3 partes. O payload é a do meio (índice 1). Decodificamos de base64.
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      
      // Procura o campo role no json (Spring Boot costuma usar role ou authorities)
      return decoded.role || decoded.roles || decoded.authorities || null;
    } catch (e) {
      console.error('Erro ao decodificar token JWT', e);
      return null;
    }
  }
}
