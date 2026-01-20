package br.gov.mt.seplag.backendapi.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoArtistaConverter implements AttributeConverter<TipoArtista, Integer> {
    @Override
    public Integer convertToDatabaseColumn(TipoArtista attribute) {
        return attribute == null ? null : attribute.getCodigo();
    }
    @Override
    public TipoArtista convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : TipoArtista.fromCodigo(dbData);
    }
}
