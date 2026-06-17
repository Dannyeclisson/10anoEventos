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

    @NotBlank(message = "Data/hora e obrigatoria")
    private String dataHora;

    @NotNull(message = "Organizador e obrigatorio")
    private Long organizadorId;

    @Valid
    private List<InsumoEventoRequestDTO> insumos;
}
