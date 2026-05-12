package com.danny.eventos.backend.dto;

import lombok.Builder;
import lombok.Getter;

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
}