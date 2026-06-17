package com.danny.eventos.backend.model;

import java.util.Arrays;

public enum TipoRelacaoEvento {
    ORGANIZADOR(1),
    COLABORADOR(2),
    PARTICIPANTE(3);

    private final int codigo;

    TipoRelacaoEvento(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static TipoRelacaoEvento fromCodigo(Integer codigo) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.codigo == codigo)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de relacao invalido"));
    }
}
