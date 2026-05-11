package com.greenloop.tests.model;

import com.greenloop.model.ListingCategory;
import com.greenloop.model.ListingCategoryConverter;
import com.greenloop.model.ListingStatus;
import com.greenloop.model.ListingStatusConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

public class ListingConverterTest {

    private final ListingCategoryConverter categoryConverter = new ListingCategoryConverter();
    private final ListingStatusConverter statusConverter = new ListingStatusConverter();

    // --- ListingCategoryConverter ---

    @ParameterizedTest
    @EnumSource(ListingCategory.class)
    void categoryConverter_roundTrip(ListingCategory category) {
        String dbValue = categoryConverter.convertToDatabaseColumn(category);
        ListingCategory back = categoryConverter.convertToEntityAttribute(dbValue);
        assertEquals(category, back);
    }

    @Test
    void categoryConverter_nullToDb_returnsNull() {
        assertNull(categoryConverter.convertToDatabaseColumn(null));
    }

    @Test
    void categoryConverter_nullFromDb_returnsNull() {
        assertNull(categoryConverter.convertToEntityAttribute(null));
    }

    @Test
    void categoryConverter_storesLowerCase() {
        String dbValue = categoryConverter.convertToDatabaseColumn(ListingCategory.PRODUCE);
        assertEquals("produce", dbValue);
    }

    @Test
    void categoryConverter_readsUpperCase() {
        assertEquals(ListingCategory.BAKERY, categoryConverter.convertToEntityAttribute("BAKERY"));
    }

    // --- ListingStatusConverter ---

    @ParameterizedTest
    @EnumSource(ListingStatus.class)
    void statusConverter_roundTrip(ListingStatus status) {
        String dbValue = statusConverter.convertToDatabaseColumn(status);
        ListingStatus back = statusConverter.convertToEntityAttribute(dbValue);
        assertEquals(status, back);
    }

    @Test
    void statusConverter_nullToDb_returnsNull() {
        assertNull(statusConverter.convertToDatabaseColumn(null));
    }

    @Test
    void statusConverter_nullFromDb_returnsNull() {
        assertNull(statusConverter.convertToEntityAttribute(null));
    }

    @Test
    void statusConverter_storesLowerCase() {
        String dbValue = statusConverter.convertToDatabaseColumn(ListingStatus.AVAILABLE);
        assertEquals("available", dbValue);
    }
}
