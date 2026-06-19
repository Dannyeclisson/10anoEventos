package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "Detalhes do evento")
public class EventoResponse {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "Workshop de Tecnologia")
    private String nome;
    @Schema(example = "Encontro sobre desenvolvimento full stack")
    private String descricao;
    @Schema(example = "Auditório Central")
    private String local;
    private String imagemUrl;
    @Schema(description = "Alias legado de dataInicio", example = "2026-07-20T19:30:00")
    private String dataHora;
    @Schema(example = "2026-07-20T19:30:00")
    private String dataInicio;
    @Schema(example = "2026-07-20T22:30:00")
    private String dataFim;
    @Schema(example = "2026-07-01T08:00:00")
    private String dataInicioInscricoes;
    @Schema(example = "100")
    private Integer capacidadeParticipantes;
    @Schema(allowableValues = {"agendado", "em_andamento", "finalizado", "cancelado", "adiado"}, example = "agendado")
    private String statusEvento;
    @Schema(allowableValues = {"nao_aberta", "aberta", "encerrada", "lotada"}, example = "aberta")
    private String statusInscricao;
    private String dataCancelamento;
    private String motivoCancelamento;
    @Schema(example = "1")
    private Long organizadorId;
    @Schema(example = "Maria da Silva")
    private String organizadorNome;
    @Schema(description = "Quantidade de inscrições ativas", example = "25")
    private Long participantes;
    private List<InsumoEventoResponseDTO> insumos;
}
