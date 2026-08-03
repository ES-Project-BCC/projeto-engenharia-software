import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-registro',
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro {
  registroData = {
    nome: '',
    email: '',
    password: '',
    role: 'USER'
  };

  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit() {
    this.authService.registro(this.registroData).subscribe({
      next: () => {
        // Se o backend salvar o usuário, avisa que deu certo e joga pra tela de login
        alert('Registro efetuado com sucesso! Faça login.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Erro no registro', err);
        alert('Falha no registro. Tente novamente.');
      }
    });
  }
}
