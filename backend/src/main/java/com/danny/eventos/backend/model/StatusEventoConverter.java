package com.danny.eventos.backend.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusEventoConverter implements AttributeConverter<StatusEvento, String> {

    @Override
    public String convertToDatabaseColumn(StatusEvento status) {
        return status != null ? status.getValor() : null;
    }

    @Override
    public StatusEvento convertToEntityAttribute(String valor) {
        return valor != null ? StatusEvento.fromValor(valor) : null;
    }
}
