import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ResourceService, ResourceResponse } from '../services/resource.service';
import { AuthService } from '../services/auth.service';
import { ResourceBlockResponse, ResourceBlockService } from '../services/resource-block.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-resource-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './resource-list.html',
  styleUrl: './resource-list.css',
})
export class ResourceList implements OnInit {
  private resourceService = inject(ResourceService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  private resourceBlockService = inject(ResourceBlockService);

  recursos: ResourceResponse[] = [];
  isLoading = true;
  errorMessage: string | null = null;
  bloqueiosAtivos = new Map<number, ResourceBlockResponse>();

  ngOnInit(): void {
    this.resourceService.listarRecursos().subscribe({
      next: (response) => {
        // só mostra os recursos que estão funcionando (disponíveis pra reserva)
        this.recursos = response.filter(r => r.statusFuncionamento);
        forkJoin(this.recursos.map((recurso) => this.resourceBlockService.listarBloqueios(recurso.id))).subscribe({
          next: (bloqueios) => {
            const agora = Date.now();
            this.recursos.forEach((recurso, index) => {
              const bloqueio = bloqueios[index].find((item) =>
                new Date(item.inicio).getTime() <= agora && new Date(item.fim).getTime() > agora
              );
              if (bloqueio) {
                this.bloqueiosAtivos.set(recurso.id, bloqueio);
              }
            });
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.isLoading = false;
            this.errorMessage = 'Erro ao carregar os bloqueios. Tente novamente mais tarde.';
            console.error('Erro ao listar bloqueios', err);
            this.cdr.detectChanges();
          },
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Erro ao carregar os recursos. Tente novamente mais tarde.';
        console.error('Erro ao listar recursos', err);
        this.cdr.detectChanges();
      },
    });
  }

  // usado no template pra decidir se mostra o botao de editar
  isAdmin(): boolean {
    return this.authService.getRole() === 'ADMIN';
  }

  bloqueioDoRecurso(resourceId: number): ResourceBlockResponse | undefined {
    return this.bloqueiosAtivos.get(resourceId);
  }
}