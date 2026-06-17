package com.danny.eventos.backend.dto;

import com.danny.eventos.backend.model.StatusInsumo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InsumoEventoResponseDTO {

    private Long id;
    private Long eventoId;
    private String categoria;
    private String nome;
    private BigDecimal quantidade;
    private String unidadeMedida;
    private String observacoes;
    private StatusInsumo status;
    private Long responsavelId;
    private String nomeResponsavel;
}
