package com.greenloop.tests.analytics;

import com.greenloop.analytics.PeakSlotDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeakSlotDtoTest {

    @ParameterizedTest
    @CsvSource({
        "0,  '12 AM'",
        "1,  '1 AM'",
        "6,  '6 AM'",
        "11, '11 AM'",
        "12, '12 PM'",
        "13, '1 PM'",
        "18, '6 PM'",
        "23, '11 PM'"
    })
    void formatHour_returnsCorrectLabel(int hour, String expectedLabel) {
        PeakSlotDto dto = new PeakSlotDto(hour, 5L);
        assertEquals(expectedLabel, dto.getLabel());
        assertEquals(hour, dto.getHour());
        assertEquals(5L, dto.getCount());
    }
}
