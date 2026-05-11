package com.greenloop.integrationtest;

import com.greenloop.carbon.Co2Calculator;
import com.greenloop.model.ListingCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Co2CalculatorTest {

    @Test
    void bakery_5_pieces_returns_positive_co2() {
        double result = Co2Calculator.computeKg(ListingCategory.BAKERY, 5, "piece");
        // 5 * 0.15 kg/piece * 1.5 factor = 1.125
        assertEquals(1.13, result, 0.01);
    }

    @Test
    void produce_2_kg_returns_correct_co2() {
        double result = Co2Calculator.computeKg(ListingCategory.PRODUCE, 2, "kg");
        // 2 * 1.0 kg/kg * 2.0 factor = 4.0
        assertEquals(4.0, result, 0.01);
    }

    @Test
    void dairy_1_lbs_returns_correct_co2() {
        double result = Co2Calculator.computeKg(ListingCategory.DAIRY, 1, "lbs");
        // 1 * 0.453 kg/lbs * 3.2 factor = 1.4496
        assertEquals(1.45, result, 0.01);
    }

    @Test
    void beverage_6_bottles_returns_correct_co2() {
        double result = Co2Calculator.computeKg(ListingCategory.BEVERAGE, 6, "bottle");
        // 6 * 0.35 kg/bottle * 1.0 factor = 2.1
        assertEquals(2.1, result, 0.01);
    }

    @Test
    void null_unit_falls_back_to_piece_weight() {
        double withNull = Co2Calculator.computeKg(ListingCategory.BAKERY, 3, null);
        double withPiece = Co2Calculator.computeKg(ListingCategory.BAKERY, 3, "piece");
        assertEquals(withPiece, withNull, 0.001);
    }

    @Test
    void zero_quantity_returns_zero() {
        double result = Co2Calculator.computeKg(ListingCategory.PRODUCE, 0, "kg");
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void to_km_equivalent_scales_correctly() {
        double km = Co2Calculator.toKmEquivalent(1.0);
        assertEquals(4.0, km, 0.01);
    }

    @Test
    void null_category_uses_other_factor() {
        double result = Co2Calculator.computeKg(null, 2, "kg");
        // 2 * 1.0 * 2.5 (OTHER_FACTOR) = 5.0
        assertEquals(5.0, result, 0.01);
    }
}
