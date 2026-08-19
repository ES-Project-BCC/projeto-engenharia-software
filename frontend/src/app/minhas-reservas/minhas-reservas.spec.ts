import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { MinhasReservas } from './minhas-reservas';
import { ReservationService, MinhaReservaResponse } from '../services/reservation.service';

const makeReserva = (overrides: Partial<MinhaReservaResponse> = {}): MinhaReservaResponse => ({
  id: 1,
  resourceId: 1,
  resourceNome: 'Lab 01',
  resourceTipo: 'LABORATORIO',
  data: '2099-12-31',
  horarioInicio: '08:00:00',
  horarioFim: '10:00:00',
  status: 'PENDENTE',
  ...overrides,
});

const makePage = (content: MinhaReservaResponse[], totalPages = 1) => ({
  content,
  totalPages,
  totalElements: content.length,
  number: 0,
  size: 10,
  first: true,
  last: true,
});

describe('MinhasReservas', () => {
  let component: MinhasReservas;
  let fixture: ComponentFixture<MinhasReservas>;
  let mockService: { listarMinhasReservas: ReturnType<typeof vi.fn>; cancelarReserva: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    mockService = {
      listarMinhasReservas: vi.fn().mockReturnValue(of(makePage([]))),
      cancelarReserva: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [MinhasReservas],
      providers: [
        { provide: ReservationService, useValue: mockService },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MinhasReservas);
    component = fixture.componentInstance;
  });

  // #98 — paginação
  describe('issue #98 — paginação', () => {
    it('deve iniciar na página 0', () => {
      expect(component.currentPage).toBe(0);
    });

    it('irParaPagina deve chamar o service com a página correta', () => {
      mockService.listarMinhasReservas.mockReturnValue(of(makePage([], 3)));
      component.totalPages = 3;
      component.irParaPagina(2);
      expect(mockService.listarMinhasReservas).toHaveBeenCalledWith(2, component.pageSize);
      expect(component.currentPage).toBe(2);
    });

    it('irParaPagina não deve navegar para página inválida', () => {
      mockService.listarMinhasReservas.mockReturnValue(of(makePage([], 2)));
      component.totalPages = 2;
      component.currentPage = 0;
      component.irParaPagina(-1);
      expect(component.currentPage).toBe(0);
      component.irParaPagina(2);
      expect(component.currentPage).toBe(0);
    });
  });

  // #99 — estado vazio/erro
  describe('issue #99 — estado vazio e erro', () => {
    it('deve exibir estado vazio quando não há reservas', async () => {
      mockService.listarMinhasReservas.mockReturnValue(of(makePage([])));
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();
      const el = fixture.nativeElement.querySelector('.empty-state');
      expect(el).toBeTruthy();
    });

    it('deve exibir mensagem de erro quando o service falha', async () => {
      mockService.listarMinhasReservas.mockReturnValue(throwError(() => new Error('500')));
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();
      expect(component.errorMessage).toBeTruthy();
      const el = fixture.nativeElement.querySelector('.alert-error');
      expect(el).toBeTruthy();
    });
  });

  // #107 — cancelarReserva no service
  describe('issue #107 — cancelarReserva', () => {
    it('deve chamar cancelarReserva com o id correto', () => {
      const reserva = makeReserva({ id: 42 });
      mockService.cancelarReserva.mockReturnValue(of({
        id: 42, resourceId: 1, data: '2099-12-31',
        horarioInicio: '08:00', horarioFim: '10:00', status: 'CANCELADA',
      }));
      mockService.listarMinhasReservas.mockReturnValue(of(makePage([])));
      component.cancelar(reserva);
      expect(mockService.cancelarReserva).toHaveBeenCalledWith(42);
    });

    it('deve exibir mensagem de sucesso após cancelar', () => {
      const reserva = makeReserva({ id: 5, resourceNome: 'Lab 01' });
      mockService.cancelarReserva.mockReturnValue(of({
        id: 5, resourceId: 1, data: '2099-12-31',
        horarioInicio: '08:00', horarioFim: '10:00', status: 'CANCELADA',
      }));
      mockService.listarMinhasReservas.mockReturnValue(of(makePage([])));
      component.cancelar(reserva);
      expect(component.successMessage).toContain('Lab 01');
    });

    it('deve exibir erro se cancelamento falhar', () => {
      const reserva = makeReserva({ id: 5 });
      mockService.cancelarReserva.mockReturnValue(throwError(() => new Error('403')));
      component.cancelar(reserva);
      expect(component.errorMessage).toBeTruthy();
      expect(component.cancelandoId).toBeNull();
    });
  });

  // #108 — botão cancelar com regras de exibição
  describe('issue #108 — botão cancelar', () => {
    it('deve permitir cancelar reserva futura PENDENTE', () => {
      expect(component.podeCancel(makeReserva({ status: 'PENDENTE', data: '2099-12-31' }))).toBe(true);
    });

    it('deve permitir cancelar reserva futura CONFIRMADA', () => {
      expect(component.podeCancel(makeReserva({ status: 'CONFIRMADA', data: '2099-12-31' }))).toBe(true);
    });

    it('não deve permitir cancelar reserva RECUSADA', () => {
      expect(component.podeCancel(makeReserva({ status: 'RECUSADA', data: '2099-12-31' }))).toBe(false);
    });

    it('não deve permitir cancelar reserva CANCELADA', () => {
      expect(component.podeCancel(makeReserva({ status: 'CANCELADA', data: '2099-12-31' }))).toBe(false);
    });

    it('não deve permitir cancelar reserva passada', () => {
      expect(component.podeCancel(makeReserva({ status: 'PENDENTE', data: '2000-01-01' }))).toBe(false);
    });
  });
});
