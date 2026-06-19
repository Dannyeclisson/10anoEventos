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
    private String imagemUrl;
    private String dataHora;
    private String dataInicio;
    private String dataFim;
    private String dataInicioInscricoes;
    private Integer capacidadeParticipantes;
    private String statusEvento;
    private String statusInscricao;
    private String dataCancelamento;
    private String motivoCancelamento;
    private Long organizadorId;
    private String organizadorNome;
    private Long participantes;
    private List<InsumoEventoResponseDTO> insumos;
}
