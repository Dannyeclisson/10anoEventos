package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Dados para criação ou atualização de evento")
public class EventoRequest {

    @Schema(example = "Workshop de Tecnologia")
    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @Schema(example = "Encontro sobre desenvolvimento full stack")
    @NotBlank(message = "Descricao e obrigatoria")
    private String descricao;

    @Schema(example = "Auditório Central")
    @NotBlank(message = "Local e obrigatorio")
    private String local;

    @Schema(description = "URL opcional da imagem do evento", example = "https://exemplo.com/evento.jpg")
    private String imagemUrl;

    @Schema(description = "Campo legado. Use dataInicio.", deprecated = true, example = "2026-07-20T19:30:00")
    private String dataHora;

    @Schema(description = "Data e hora local de início em formato ISO-8601", example = "2026-07-20T19:30:00")
    @NotBlank(message = "Data de inicio e obrigatoria")
    private String dataInicio;

    @Schema(description = "Data e hora local de término em formato ISO-8601", example = "2026-07-20T22:30:00")
    @NotBlank(message = "Data de fim e obrigatoria")
    private String dataFim;

    @Schema(description = "Início das inscrições em formato ISO-8601", example = "2026-07-01T08:00:00")
    @NotBlank(message = "Data de inicio das inscricoes e obrigatoria")
    private String dataInicioInscricoes;

    @Schema(description = "Capacidade máxima, maior que um", example = "100", minimum = "2")
    @NotNull(message = "Capacidade de participantes e obrigatoria")
    private Integer capacidadeParticipantes;

    @Schema(description = "Insumos que podem ser cadastrados junto com o evento")
    @Valid
    private List<InsumoEventoRequestDTO> insumos;
}
