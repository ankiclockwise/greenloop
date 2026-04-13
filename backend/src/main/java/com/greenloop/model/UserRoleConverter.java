package com.greenloop.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.getDisplayName();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbValue) {
        return UserRole.fromString(dbValue);
    }
}