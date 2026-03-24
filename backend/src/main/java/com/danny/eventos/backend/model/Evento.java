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

    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "organizador_id", nullable = false)
    private Usuario organizador;
}