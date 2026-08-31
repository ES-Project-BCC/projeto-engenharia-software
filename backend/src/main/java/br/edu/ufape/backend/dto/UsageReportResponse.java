package br.edu.ufape.backend.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageReportResponse {
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private List<ResourceUsageReportResponse> reservasPorRecurso;
    private ResourceUsageReportResponse recursoMaisUtilizado;
    private ResourceUsageReportResponse recursoMenosUtilizado;
    private Map<DayOfWeek, Long> reservasPorDiaSemana;
}
