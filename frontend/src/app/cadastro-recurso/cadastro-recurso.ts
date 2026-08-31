import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { ResourceService, ResourceRequest } from '../services/resource.service';

@Component({
  selector: 'app-cadastro-recurso',
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './cadastro-recurso.html',
  styleUrl: './cadastro-recurso.css',
})
export class CadastroRecurso implements OnInit {
  recursoData: ResourceRequest = {
    nome: '',
    descricao: '',
    capacidade: undefined,
    tipo: 'LABORATORIO',
    statusFuncionamento: true,
  };

  // controla se estamos editando ou criando
  modoEdicao = false;
  recursoId: number | null = null;

  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;
  isLoadingDados = false;

  private resourceService = inject(ResourceService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);

  get descricaoLength(): number {
    return this.recursoData.descricao?.length ?? 0;
  }

  ngOnInit(): void {
    // se tiver um :id na rota, é modo edicao. senao é cadastro normal
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.modoEdicao = true;
      this.recursoId = Number(idParam);
      this.carregarRecurso(this.recursoId);
    }
  }

  private carregarRecurso(id: number): void {
    this.isLoadingDados = true;
    this.resourceService.buscarRecursoPorId(id).subscribe({
      next: (recurso) => {
        this.isLoadingDados = false;
        // preenche o form com os dados do recurso existente
        this.recursoData = {
          nome: recurso.nome,
          descricao: recurso.descricao,
          capacidade: recurso.capacidade,
          tipo: recurso.tipo,
          statusFuncionamento: recurso.statusFuncionamento,
        };
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoadingDados = false;
        if (err.status === 404) {
          this.errorMessage = 'Recurso não encontrado.';
        } else {
          this.errorMessage = 'Erro ao carregar os dados do recurso.';
        }
        console.error('Erro ao carregar recurso para edição', err);
        this.cdr.detectChanges();
      },
    });
  }

  onSubmit() {
    this.successMessage = null;
    this.errorMessage = null;
    this.isLoading = true;

    if (this.modoEdicao && this.recursoId !== null) {
      // modo edição: chama PUT /api/resources/{id}
      this.resourceService.editarRecurso(this.recursoId, this.recursoData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = `Recurso "${response.nome}" atualizado com sucesso!`;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isLoading = false;
          if (err.status === 403) {
            this.errorMessage = 'Sem permissão para editar recursos. Apenas ADMINs podem realizar esta ação.';
          } else if (err.status === 404) {
            this.errorMessage = 'Recurso não encontrado.';
          } else if (err.status === 400) {
            this.errorMessage = 'Dados inválidos. Verifique os campos e tente novamente.';
          } else {
            this.errorMessage = 'Erro ao atualizar recurso. Tente novamente mais tarde.';
          }
          console.error('Erro ao editar recurso', err);
          this.cdr.detectChanges();
        },
      });
    } else {
      // modo criação: comportamento original inalterado
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
          this.cdr.detectChanges();
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
          this.cdr.detectChanges();
        },
      });
    }
  }
}
