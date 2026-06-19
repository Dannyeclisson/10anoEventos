package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados opcionais do cancelamento do evento")
public class CancelarEventoRequestDTO {

    @Schema(description = "Motivo opcional do cancelamento", example = "Evento cancelado por indisponibilidade do local")
    private String motivo;
}
