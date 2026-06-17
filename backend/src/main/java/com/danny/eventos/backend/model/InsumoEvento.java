package com.danny.eventos.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "insumos_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsumoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    @Column(name = "unidade_medida", nullable = false, length = 50)
    private String unidadeMedida;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusInsumo status;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;
}
