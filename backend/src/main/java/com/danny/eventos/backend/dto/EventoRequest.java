package com.danny.eventos.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EventoRequest {

    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @NotBlank(message = "Descricao e obrigatoria")
    private String descricao;

    @NotBlank(message = "Local e obrigatorio")
    private String local;

    private String imagemUrl;

    private String dataHora;

    @NotBlank(message = "Data de inicio e obrigatoria")
    private String dataInicio;

    @NotBlank(message = "Data de fim e obrigatoria")
    private String dataFim;

    @NotBlank(message = "Data de inicio das inscricoes e obrigatoria")
    private String dataInicioInscricoes;

    @NotNull(message = "Capacidade de participantes e obrigatoria")
    private Integer capacidadeParticipantes;

    @Valid
    private List<InsumoEventoRequestDTO> insumos;
}
