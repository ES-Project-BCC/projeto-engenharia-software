import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ResourceBlockRequest, ResourceBlockService } from '../services/resource-block.service';
import { ResourceResponse, ResourceService } from '../services/resource.service';

@Component({
  selector: 'app-bloquear-recurso',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './bloquear-recurso.html',
  styleUrl: './bloquear-recurso.css',
})
export class BloquearRecurso implements OnInit {
  bloqueioData: ResourceBlockRequest = this.novoBloqueio();
  recursos: ResourceResponse[] = [];
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;
  isLoadingRecursos = false;

  private resourceService = inject(ResourceService);
  private resourceBlockService = inject(ResourceBlockService);

  ngOnInit(): void {
    this.carregarRecursos();
  }

  onSubmit(): void {
    this.successMessage = null;
    this.errorMessage = null;
    this.isLoading = true;

    this.resourceBlockService.criarBloqueio(this.bloqueioData).subscribe({
      next: (bloqueio) => {
        this.isLoading = false;
        this.successMessage = `Recurso "${bloqueio.resourceNome}" bloqueado com sucesso.`;
        this.bloqueioData = this.novoBloqueio();
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 400) {
          this.errorMessage = 'Dados inválidos. Verifique os campos e tente novamente.';
        } else if (err.status === 403) {
          this.errorMessage = 'Sem permissão para bloquear recursos. Apenas ADMINs podem realizar esta ação.';
        } else if (err.status === 404) {
          this.errorMessage = 'Recurso não encontrado.';
        } else {
          this.errorMessage = 'Erro ao bloquear recurso. Tente novamente mais tarde.';
        }
        console.error('Erro ao criar bloqueio', err);
      },
    });
  }

  private carregarRecursos(): void {
    this.isLoadingRecursos = true;
    this.resourceService.listarRecursos().subscribe({
      next: (recursos) => {
        this.recursos = recursos;
        this.isLoadingRecursos = false;
      },
      error: (err) => {
        this.isLoadingRecursos = false;
        this.errorMessage = 'Erro ao carregar os recursos. Tente novamente mais tarde.';
        console.error('Erro ao carregar recursos para bloqueio', err);
      },
    });
  }

  private novoBloqueio(): ResourceBlockRequest {
    return {
      resourceId: 0,
      inicio: '',
      fim: '',
      motivo: '',
    };
  }
}