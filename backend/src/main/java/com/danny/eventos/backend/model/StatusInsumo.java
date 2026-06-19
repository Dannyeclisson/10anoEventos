package com.danny.eventos.backend.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status do insumo", allowableValues = {
        "PENDENTE", "COMPRADO", "ALUGADO", "ENTREGUE", "CONFIRMADO", "CANCELADO"
})
public enum StatusInsumo {
    PENDENTE,
    COMPRADO,
    ALUGADO,
    ENTREGUE,
    CONFIRMADO,
    CANCELADO
}
