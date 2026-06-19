package com.danny.eventos.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    private String local;

    @Column(name = "imagem_url")
    private String imagemUrl;

    private LocalDateTime dataHora;

    @Column(name = "status_evento", nullable = false, length = 30, columnDefinition = "varchar(30) default 'agendado'")
    private StatusEvento statusEvento;

    @Column(name = "status_inscricao", nullable = false, length = 30, columnDefinition = "varchar(30) default 'nao_aberta'")
    private StatusInscricao statusInscricao;

    @Column(name = "data_inicio", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime dataFim;

    @Column(name = "data_inicio_inscricoes", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime dataInicioInscricoes;

    @Column(name = "capacidade_participantes", nullable = false, columnDefinition = "integer default 100")
    private Integer capacidadeParticipantes;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;
}
