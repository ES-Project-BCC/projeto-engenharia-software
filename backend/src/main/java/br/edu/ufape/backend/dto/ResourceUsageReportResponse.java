package br.edu.ufape.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUsageReportResponse {
    private Long resourceId;
    private String resourceNome;
    private long totalReservas;
}
