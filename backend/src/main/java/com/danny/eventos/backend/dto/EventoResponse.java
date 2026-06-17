package com.danny.eventos.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EventoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private String local;
    private String dataHora;
    private Long organizadorId;
    private String organizadorNome;
    private Long participantes;
    private List<InsumoEventoResponseDTO> insumos;
}
