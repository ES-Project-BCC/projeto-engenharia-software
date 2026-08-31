package br.edu.ufape.backend.repository.projection;

public interface ResourceUsageProjection {
    Long getResourceId();
    String getResourceNome();
    Long getTotalReservas();
}
