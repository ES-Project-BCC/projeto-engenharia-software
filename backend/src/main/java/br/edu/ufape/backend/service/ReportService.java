package br.edu.ufape.backend.service;

import br.edu.ufape.backend.dto.ResourceUsageReportResponse;
import br.edu.ufape.backend.dto.UsageReportResponse;
import br.edu.ufape.backend.model.Reservation;
import br.edu.ufape.backend.model.enums.StatusReserva;
import br.edu.ufape.backend.repository.ReservationRepository;
import br.edu.ufape.backend.repository.projection.ResourceUsageProjection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportService {

    private final ReservationRepository reservationRepository;

    public ReportService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public UsageReportResponse gerarRelatorioUtilizacao(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As datas de início e fim são obrigatórias.");
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de fim não pode ser anterior à data de início.");
        }

        // 1. Recursos mais e menos utilizados
        List<ResourceUsageProjection> projections = reservationRepository.countReservationsByResource(dataInicio, dataFim);
        
        List<ResourceUsageReportResponse> reservasPorRecurso = projections.stream()
                .map(p -> new ResourceUsageReportResponse(p.getResourceId(), p.getResourceNome(), p.getTotalReservas()))
                .collect(Collectors.toList());

        ResourceUsageReportResponse maisUtilizado = null;
        ResourceUsageReportResponse menosUtilizado = null;

        if (!reservasPorRecurso.isEmpty()) {
            maisUtilizado = reservasPorRecurso.get(0);
            menosUtilizado = reservasPorRecurso.get(reservasPorRecurso.size() - 1);
        }

        // 2. Distribuição por dia da semana
        List<StatusReserva> statusIgnorados = List.of(StatusReserva.CANCELADA, StatusReserva.RECUSADA);
        List<Reservation> reservasNoPeriodo = reservationRepository.findByDataBetweenAndStatusNotIn(dataInicio, dataFim, statusIgnorados);

        Map<DayOfWeek, Long> reservasPorDiaSemana = reservasNoPeriodo.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getData().getDayOfWeek(),
                        Collectors.counting()
                ));

        return UsageReportResponse.builder()
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .reservasPorRecurso(reservasPorRecurso)
                .recursoMaisUtilizado(maisUtilizado)
                .recursoMenosUtilizado(menosUtilizado)
                .reservasPorDiaSemana(reservasPorDiaSemana)
                .build();
    }
}
