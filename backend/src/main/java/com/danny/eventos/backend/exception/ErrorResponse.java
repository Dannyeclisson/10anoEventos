package com.danny.eventos.backend.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta padronizada de erro da API")
public class ErrorResponse {

    @Schema(description = "Data e hora do erro", example = "2026-06-19T10:00:00")
    private LocalDateTime timestamp;
    @Schema(description = "Código HTTP", example = "400")
    private int status;
    @Schema(description = "Mensagem legível do erro", example = "CPF inválido")
    private String message;

    public ErrorResponse(LocalDateTime timestamp, int status, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
}
