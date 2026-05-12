package com.danny.eventos.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventoRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @NotBlank(message = "Local é obrigatório")
    private String local;

    @NotBlank(message = "Data/hora é obrigatória")
    private String dataHora;

    @NotNull(message = "Organizador é obrigatório")
    private Long organizadorId;
}