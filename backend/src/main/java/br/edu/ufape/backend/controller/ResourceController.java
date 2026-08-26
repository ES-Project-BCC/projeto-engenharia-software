package br.edu.ufape.backend.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufape.backend.dto.AvailabilityRequest;
import br.edu.ufape.backend.dto.AvailabilityResponse;
import br.edu.ufape.backend.dto.ReservationAdminResponse;
import br.edu.ufape.backend.dto.ResourceRequest;
import br.edu.ufape.backend.dto.ResourceResponse;
import br.edu.ufape.backend.service.ReservationService;
import br.edu.ufape.backend.service.ResourceService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final ReservationService reservationService;

    public ResourceController(ResourceService resourceService, ReservationService reservationService) {
        this.resourceService = resourceService;
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ResourceResponse> criarRecurso(@Valid @RequestBody ResourceRequest request) {
        ResourceResponse response = resourceService.criarRecurso(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // endpoint pra editar um recurso existente, so admin pode usar
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> editarRecurso(
            @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {
        ResourceResponse response = resourceService.editarRecurso(id, request);
        return ResponseEntity.ok(response);
    }

    // busca um recurso especifico pelo id, o front usa isso pra montar o form de edicao
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> buscarRecursoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ResourceResponse>> listarRecursos() {
        List<ResourceResponse> response = resourceService.listarRecursos();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/disponibilidade")
    public ResponseEntity<List<AvailabilityResponse>> consultarDisponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horarioInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horarioFim) {

        AvailabilityRequest request = new AvailabilityRequest(data, horarioInicio, horarioFim);
        List<AvailabilityResponse> response = resourceService.consultarDisponibilidade(request);
        return ResponseEntity.ok(response);
    }

    // lista as reservas de um recurso especifico, usado pelo admin (US11)
    @GetMapping("/{id}/reservations")
    public ResponseEntity<Page<ReservationAdminResponse>> listarReservasPorRecurso(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReservationAdminResponse> response = reservationService.listarReservasPorRecurso(id, pageable);
        return ResponseEntity.ok(response);
    }
}