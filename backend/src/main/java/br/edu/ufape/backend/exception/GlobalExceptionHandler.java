package br.edu.ufape.backend.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centraliza a formatacao de respostas de erro da API, para o frontend
 * sempre receber um formato previsivel, em vez do JSON de erro padrao
 * (verboso e inconsistente) que o Spring gera sozinho.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Disparada automaticamente quando um @Valid falha (ex.: email invalido,
     * campo obrigatorio vazio) em RegisterRequest/LoginRequest.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Dados invalidos");
        body.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Cobre os erros lancados manualmente no AuthService via
     * ResponseStatusException (email duplicado -> 409, credenciais
     * invalidas -> 401), mantendo o mesmo formato de resposta.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}