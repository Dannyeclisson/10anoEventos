package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Dados para confirmar participação em evento")
public class ParticiparEventoRequestDTO {

    @Schema(description = "Identificador do usuário", example = "2")
    @NotNull(message = "Usuario e obrigatorio")
    private Long usuarioId;

    @Schema(
            description = "Tipo da relação: 1 PARTICIPANTE ou 2 COLABORADOR. O código 3 ORGANIZADOR é reservado ao criador do evento.",
            allowableValues = {"1", "2"},
            example = "1"
    )
    @NotNull(message = "Tipo de relacao e obrigatorio")
    private Integer tipoRelacao;

    @Schema(description = "Insumos assumidos pelo colaborador", example = "[10, 11]")
    private List<Long> insumoIds;
}
