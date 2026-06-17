package com.danny.eventos.backend.dto;

import com.danny.eventos.backend.model.StatusInsumo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InsumoEventoRequestDTO {

    @NotBlank(message = "Categoria do insumo e obrigatoria")
    @Size(max = 100, message = "Categoria deve ter no maximo 100 caracteres")
    private String categoria;

    @NotBlank(message = "Nome do insumo e obrigatorio")
    @Size(max = 150, message = "Nome do insumo deve ter no maximo 150 caracteres")
    private String nome;

    @NotNull(message = "Quantidade do insumo e obrigatoria")
    @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
    private BigDecimal quantidade;

    @NotBlank(message = "Unidade de medida do insumo e obrigatoria")
    @Size(max = 50, message = "Unidade de medida deve ter no maximo 50 caracteres")
    private String unidadeMedida;

    private String observacoes;

    @NotNull(message = "Status do insumo e obrigatorio")
    private StatusInsumo status;
}
