package com.greenloop.tests.impact;

import com.greenloop.impact.ImpactService;
import com.greenloop.model.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImpactServiceStaticTest {

    @ParameterizedTest
    @CsvSource({
        "CONSUMER,    retail_user",
        "RETAILER,    store",
        "DINING_HALL, diner",
        "DONOR,       store",
        "ADMIN,       store"
    })
    void toFrontendType_mapsCorrectly(UserRole role, String expected) {
        assertEquals(expected, ImpactService.toFrontendType(role));
    }
}
