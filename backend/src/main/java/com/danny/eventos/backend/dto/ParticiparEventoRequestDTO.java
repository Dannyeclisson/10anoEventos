package com.danny.eventos.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParticiparEventoRequestDTO {

    @NotNull(message = "Usuario e obrigatorio")
    private Long usuarioId;

    @NotNull(message = "Tipo de relacao e obrigatorio")
    private Integer tipoRelacao;

    private List<Long> insumoIds;
}
