package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Vínculo entre usuário e evento")
public class UsuarioEventoResponseDTO {

    @Schema(example = "20")
    private Long id;
    @Schema(example = "1")
    private Long eventoId;
    @Schema(example = "Workshop de Tecnologia")
    private String nomeEvento;
    @Schema(example = "2")
    private Long usuarioId;
    @Schema(example = "João Souza")
    private String nomeUsuario;
    @Schema(
            description = "0 CANCELADO, 1 PARTICIPANTE, 2 COLABORADOR, 3 ORGANIZADOR",
            allowableValues = {"0", "1", "2", "3"},
            example = "1"
    )
    private Integer tipoRelacao;
    @Schema(example = "PARTICIPANTE")
    private String descricaoTipoRelacao;
}
