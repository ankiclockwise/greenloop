package com.greenloop.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ListingStatusConverter implements AttributeConverter<ListingStatus, String> {

    @Override
    public String convertToDatabaseColumn(ListingStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public ListingStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return ListingStatus.valueOf(dbValue.toUpperCase());
    }
}