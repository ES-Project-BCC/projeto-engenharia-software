package br.edu.ufape.backend.controller;

import br.edu.ufape.backend.dto.NotificationResponse;
import br.edu.ufape.backend.service.NotificationService;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> listarMinhasNotificacoes(
            @PageableDefault(size = 10, sort = "criadaEm") Pageable pageable) {
        return ResponseEntity.ok(notificationService.listarMinhasNotificacoes(pageable));
    }

    @PatchMapping("/{id}/ler")
    public ResponseEntity<NotificationResponse> marcarComoLida(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarComoLida(id));
    }

    @GetMapping("/nao-lidas/contagem")
    public ResponseEntity<Map<String, Long>> contarNaoLidas() {
        return ResponseEntity.ok(Map.of("total", notificationService.contarNaoLidas()));
    }
}