package com.danny.eventos.backend.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusInscricao {
    NAO_ABERTA("nao_aberta"),
    ABERTA("aberta"),
    ENCERRADA("encerrada"),
    LOTADA("lotada");

    private final String valor;

    StatusInscricao(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    public static StatusInscricao fromValor(String valor) {
        for (StatusInscricao status : values()) {
            if (status.valor.equalsIgnoreCase(valor)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status de inscricao invalido: " + valor);
    }
}
