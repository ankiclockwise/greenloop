package com.greenloop.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ListingCategoryConverter implements AttributeConverter<ListingCategory, String> {

    @Override
    public String convertToDatabaseColumn(ListingCategory category) {
        return category == null ? null : category.name().toLowerCase();
    }

    @Override
    public ListingCategory convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return ListingCategory.valueOf(dbValue.toUpperCase());
    }
}