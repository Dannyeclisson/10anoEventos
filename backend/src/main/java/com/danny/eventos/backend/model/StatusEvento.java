package com.danny.eventos.backend.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusEvento {
    AGENDADO("agendado"),
    EM_ANDAMENTO("em_andamento"),
    FINALIZADO("finalizado"),
    CANCELADO("cancelado"),
    ADIADO("adiado");

    private final String valor;

    StatusEvento(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    public static StatusEvento fromValor(String valor) {
        for (StatusEvento status : values()) {
            if (status.valor.equalsIgnoreCase(valor)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status do evento invalido: " + valor);
    }
}
