package com.danny.eventos.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelarInscricaoRequestDTO {

    @NotNull(message = "Usuario e obrigatorio")
    private Long usuarioId;
}
