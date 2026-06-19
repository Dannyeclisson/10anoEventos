package com.danny.eventos.backend.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusInscricaoConverter implements AttributeConverter<StatusInscricao, String> {

    @Override
    public String convertToDatabaseColumn(StatusInscricao status) {
        return status != null ? status.getValor() : null;
    }

    @Override
    public StatusInscricao convertToEntityAttribute(String valor) {
        return valor != null ? StatusInscricao.fromValor(valor) : null;
    }
}
