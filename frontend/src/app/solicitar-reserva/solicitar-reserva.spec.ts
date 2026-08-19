import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SolicitarReserva } from './solicitar-reserva';
import { ReservationService } from '../services/reservation.service';
import { ResourceService } from '../services/resource.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { vi, describe, it, expect, beforeEach } from 'vitest';

describe('SolicitarReserva', () => {
  let component: SolicitarReserva;
  let fixture: ComponentFixture<SolicitarReserva>;

  const criarReservaMock = vi.fn();
  const listarRecursosMock = vi.fn();
  const consultarDisponibilidadeMock = vi.fn();

  const recursoMock = {
    id: 1,
    nome: 'Lab 01',
    descricao: 'Laboratório de informática',
    capacidade: 30,
    tipo: 'LABORATORIO' as const,
    statusFuncionamento: true
  };

  beforeEach(async () => {
    criarReservaMock.mockReset();
    listarRecursosMock.mockReset();
    consultarDisponibilidadeMock.mockReset();

    listarRecursosMock.mockReturnValue(of([recursoMock]));
    consultarDisponibilidadeMock.mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [SolicitarReserva],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        {
          provide: ReservationService,
          useValue: { criarReserva: criarReservaMock }
        },
        {
          provide: ResourceService,
          useValue: {
            listarRecursos: listarRecursosMock,
            consultarDisponibilidade: consultarDisponibilidadeMock
          }
        },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '1' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SolicitarReserva);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve exibir mensagem de erro 409 quando horário estiver indisponível', () => {
    criarReservaMock.mockReturnValue(throwError(() => ({ status: 409 })));

    component.reservaData = {
      resourceId: 1,
      data: '2026-09-01',
      horarioInicio: '08:00',
      horarioFim: '10:00'
    };

    component.onSubmit();

    expect(component.errorMessage).toBe(
      'Horário indisponível. Já existe uma reserva nesse período para este recurso.'
    );
  });

  it('deve exibir mensagem de sucesso ao criar reserva', () => {
    criarReservaMock.mockReturnValue(of({
      id: 1,
      resourceId: 1,
      data: '2026-09-01',
      horarioInicio: '08:00',
      horarioFim: '10:00',
      status: 'PENDENTE' as const
    }));

    component.reservaData = {
      resourceId: 1,
      data: '2026-09-01',
      horarioInicio: '08:00',
      horarioFim: '10:00'
    };

    component.onSubmit();

    expect(component.successMessage).toContain('Reserva solicitada com sucesso');
  });

  it('deve exibir mensagem de erro 400 para dados inválidos', () => {
    criarReservaMock.mockReturnValue(throwError(() => ({ status: 400 })));

    component.onSubmit();

    expect(component.errorMessage).toBe(
      'Dados inválidos. Verifique se o horário de fim é maior que o de início.'
    );
  });
});