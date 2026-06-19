package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para cancelamento de inscrição")
public class CancelarInscricaoRequestDTO {

    @Schema(description = "Identificador do usuário inscrito", example = "2")
    @NotNull(message = "Usuario e obrigatorio")
    private Long usuarioId;
}
