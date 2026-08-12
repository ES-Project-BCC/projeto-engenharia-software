import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ResourceService, ResourceRequest } from '../services/resource.service';

@Component({
  selector: 'app-cadastro-recurso',
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './cadastro-recurso.html',
  styleUrl: './cadastro-recurso.css',
})
export class CadastroRecurso {
  recursoData: ResourceRequest = {
    nome: '',
    descricao: '',
    capacidade: undefined,
    tipo: 'LABORATORIO',
    statusFuncionamento: true,
  };

  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;

  private resourceService = inject(ResourceService);
  private router = inject(Router);

  get descricaoLength(): number {
    return this.recursoData.descricao?.length ?? 0;
  }

  onSubmit() {
    this.successMessage = null;
    this.errorMessage = null;
    this.isLoading = true;

    this.resourceService.criarRecurso(this.recursoData).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = `Recurso "${response.nome}" cadastrado com sucesso! (ID: ${response.id})`;
        // limpando os dados do form pra não ficar na tela dps q cadastra
        this.recursoData = {
          nome: '',
          descricao: '',
          capacidade: undefined,
          tipo: 'LABORATORIO',
          statusFuncionamento: true,
        };
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 403) {
          this.errorMessage = 'Sem permissão para cadastrar recursos. Apenas ADMINs podem realizar esta ação.';
        } else if (err.status === 400) {
          this.errorMessage = 'Dados inválidos. Verifique os campos e tente novamente.';
        } else {
          this.errorMessage = 'Erro ao cadastrar recurso. Tente novamente mais tarde.';
        }
        console.error('Erro ao cadastrar recurso', err);
      },
    });
  }
}
