package com.danny.eventos.backend.model;

import java.util.Arrays;

public enum TipoRelacaoEvento {
    CANCELADO(0),
    PARTICIPANTE(1),
    COLABORADOR(2),
    ORGANIZADOR(3);

    private final int codigo;

    TipoRelacaoEvento(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static TipoRelacaoEvento fromCodigo(Integer codigo) {
        if (codigo == null) {
            throw new IllegalArgumentException("Tipo de relacao e obrigatorio");
        }

        return Arrays.stream(values())
                .filter(tipo -> tipo.codigo == codigo)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de relacao invalido: " + codigo));
    }
}
