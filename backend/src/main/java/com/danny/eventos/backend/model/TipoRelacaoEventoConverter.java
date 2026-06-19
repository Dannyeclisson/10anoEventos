package com.danny.eventos.backend.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoRelacaoEventoConverter implements AttributeConverter<TipoRelacaoEvento, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TipoRelacaoEvento tipo) {
        return tipo != null ? tipo.getCodigo() : null;
    }

    @Override
    public TipoRelacaoEvento convertToEntityAttribute(Integer codigo) {
        return codigo != null ? TipoRelacaoEvento.fromCodigo(codigo) : null;
    }
}
