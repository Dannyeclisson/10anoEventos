package com.danny.eventos.backend.dto;

import com.danny.eventos.backend.model.StatusInsumo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@Schema(description = "Detalhes do insumo vinculado ao evento")
public class InsumoEventoResponseDTO {

    @Schema(example = "10")
    private Long id;
    @Schema(example = "1")
    private Long eventoId;
    @Schema(example = "Bebidas")
    private String categoria;
    @Schema(example = "Água mineral")
    private String nome;
    @Schema(example = "100")
    private BigDecimal quantidade;
    @Schema(example = "garrafas")
    private String unidadeMedida;
    private String observacoes;
    @Schema(example = "PENDENTE")
    private StatusInsumo status;
    @Schema(description = "Usuário responsável pelo insumo, quando houver", example = "2")
    private Long responsavelId;
    @Schema(example = "João Souza")
    private String nomeResponsavel;
}
