import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ResourceService, ResourceResponse } from '../services/resource.service';

@Component({
  selector: 'app-resource-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './resource-list.html',
  styleUrl: './resource-list.css',
})
export class ResourceList implements OnInit {
  private resourceService = inject(ResourceService);

  recursos: ResourceResponse[] = [];
  isLoading = true;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.resourceService.listarRecursos().subscribe({
      next: (response) => {
        // só mostra os recursos que estão funcionando (disponíveis pra reserva)
        this.recursos = response.filter(r => r.statusFuncionamento);
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Erro ao carregar os recursos. Tente novamente mais tarde.';
        console.error('Erro ao listar recursos', err);
      },
    });
  }
}