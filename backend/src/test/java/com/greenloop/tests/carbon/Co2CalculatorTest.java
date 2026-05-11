package com.greenloop.tests.carbon;

import com.greenloop.carbon.Co2Calculator;
import com.greenloop.model.ListingCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class Co2CalculatorTest {

    @Test
    void computeKg_produce_kg_unit() {
        // PRODUCE factor=2.0, kg unit=1.0 kg/unit, qty=5 → 2.0 * 5 * 1.0 = 10.0
        double result = Co2Calculator.computeKg(ListingCategory.PRODUCE, 5, "kg");
        assertEquals(10.0, result);
    }

    @Test
    void computeKg_dairy_lbs_unit() {
        // DAIRY factor=3.2, lbs=0.453 kg/unit, qty=2 → 3.2 * 2 * 0.453 = 2.9 (rounded)
        double result = Co2Calculator.computeKg(ListingCategory.DAIRY, 2, "lbs");
        assertEquals(Math.round(3.2 * 2 * 0.453 * 100.0) / 100.0, result);
    }

    @Test
    void computeKg_bakery_piece_unit_uses_category_weight() {
        // BAKERY factor=1.5, piece weight=0.15 kg, qty=10 → 1.5 * 10 * 0.15 = 2.25
        double result = Co2Calculator.computeKg(ListingCategory.BAKERY, 10, "piece");
        assertEquals(2.25, result);
    }

    @Test
    void computeKg_prepared_box_unit() {
        // PREPARED factor=4.0, box=0.5 kg/unit, qty=3 → 4.0 * 3 * 0.5 = 6.0
        double result = Co2Calculator.computeKg(ListingCategory.PREPARED, 3, "box");
        assertEquals(6.0, result);
    }

    @Test
    void computeKg_beverage_bottle_unit() {
        // BEVERAGE factor=1.0, bottle=0.35 kg/unit, qty=6 → 1.0 * 6 * 0.35 = 2.1
        double result = Co2Calculator.computeKg(ListingCategory.BEVERAGE, 6, "bottle");
        assertEquals(2.1, result);
    }

    @Test
    void computeKg_nullCategory_usesDefaultFactor() {
        // null category → OTHER factor=2.5, null unit → pieceWeight default=0.3, qty=1
        double result = Co2Calculator.computeKg(null, 1, null);
        assertEquals(Math.round(2.5 * 1 * 0.3 * 100.0) / 100.0, result);
    }

    @Test
    void computeKg_nullUnit_fallsBackToPieceWeight() {
        // PRODUCE factor=2.0, null unit → pieceWeight=0.3, qty=4 → 2.0 * 4 * 0.3 = 2.4
        double result = Co2Calculator.computeKg(ListingCategory.PRODUCE, 4, null);
        assertEquals(2.4, result);
    }

    @ParameterizedTest
    @CsvSource({
        "kg,        1.0",
        "kilogram,  1.0",
        "lbs,       0.453",
        "pound,     0.453",
        "g,         0.001",
        "oz,        0.0283",
        "box,       0.5",
        "pack,      0.5",
        "bag,       0.5",
        "carton,    0.4",
        "bottle,    0.35",
        "can,       0.35",
        "jar,       0.35",
        "tray,      0.6"
    })
    void computeKg_unitConversions(String unit, double expectedKgPerUnit) {
        // PRODUCE factor=2.0, qty=1 → result = 2.0 * 1 * expectedKgPerUnit
        double result = Co2Calculator.computeKg(ListingCategory.PRODUCE, 1, unit);
        assertEquals(Math.round(2.0 * expectedKgPerUnit * 100.0) / 100.0, result);
    }

    @Test
    void computeKg_dozen_unit_is_twelvePieces() {
        // PRODUCE factor=2.0, dozen = pieceWeight(PRODUCE)*12 = 0.3*12 = 3.6, qty=1
        double result = Co2Calculator.computeKg(ListingCategory.PRODUCE, 1, "dozen");
        assertEquals(Math.round(2.0 * 3.6 * 100.0) / 100.0, result);
    }

    @Test
    void toKmEquivalent_correctConversion() {
        // 10 kg CO2 * 4.0 km/kg = 40.0 km
        assertEquals(40.0, Co2Calculator.toKmEquivalent(10.0));
    }

    @Test
    void toKmEquivalent_zero() {
        assertEquals(0.0, Co2Calculator.toKmEquivalent(0.0));
    }

    @Test
    void toKmEquivalent_roundsToOneDecimal() {
        // 1.05 * 4.0 = 4.2
        assertEquals(4.2, Co2Calculator.toKmEquivalent(1.05));
    }

    @Test
    void computeKg_allCategories_returnPositive() {
        for (ListingCategory cat : ListingCategory.values()) {
            double result = Co2Calculator.computeKg(cat, 1, "kg");
            assertTrue(result > 0, "Expected positive CO2 for category: " + cat);
        }
    }
}
