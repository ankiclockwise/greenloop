package com.greenloop.carbon;

import com.greenloop.model.ListingCategory;

/**
 * CO2 emission factors per food category based on WRAP UK and EPA food waste research.
 * Values represent kg of CO2-equivalent saved per kg of food rescued from waste.
 *
 * Unit-to-kg conversion uses USDA food weight averages, since donors enter quantities
 * in natural units ("4 cakes", "10 bottles") not grams.
 */
public final class Co2Calculator {

    // kg CO2e per kg food (source: WRAP UK Food Surplus and Waste in the UK, 2022)
    private static final double PRODUCE_FACTOR  = 2.0;
    private static final double DAIRY_FACTOR    = 3.2;
    private static final double BAKERY_FACTOR   = 1.5;
    private static final double PREPARED_FACTOR = 4.0;
    private static final double BEVERAGE_FACTOR = 1.0;
    private static final double PANTRY_FACTOR   = 2.5;
    private static final double FROZEN_FACTOR   = 3.5;
    private static final double OTHER_FACTOR    = 2.5;

    // kg CO2 saved per km of average petrol car driving avoided (EPA)
    public static final double KM_PER_KG_CO2 = 4.0;

    private Co2Calculator() {}

    /**
     * Computes kg of CO2 saved for a rescued food listing.
     *
     * @param category food category
     * @param quantity number of units
     * @param unit     unit string from the listing (e.g. "piece", "kg", "lbs", "box")
     */
    public static double computeKg(ListingCategory category, int quantity, String unit) {
        double kgPerUnit = kgPerUnit(category, unit);
        double factor = factorFor(category);
        return Math.round(factor * quantity * kgPerUnit * 100.0) / 100.0;
    }

    public static double toKmEquivalent(double co2Kg) {
        return Math.round(co2Kg * KM_PER_KG_CO2 * 10.0) / 10.0;
    }

    /**
     * Converts a unit string to kg per unit.
     * Exact units (kg, lbs, g) are converted precisely.
     * Piece-like units fall back to a per-category average weight (USDA estimates).
     */
    private static double kgPerUnit(ListingCategory category, String unit) {
        if (unit == null) return pieceWeightKg(category);

        return switch (unit.toLowerCase().trim()) {
            case "kg", "kilogram", "kilograms"    -> 1.0;
            case "lbs", "lb", "pound", "pounds"   -> 0.453;
            case "g", "gram", "grams"              -> 0.001;
            case "oz", "ounce", "ounces"           -> 0.0283;
            case "dozen"                           -> pieceWeightKg(category) * 12;
            case "box", "pack", "package", "bag"  -> 0.5;
            case "carton"                          -> 0.4;
            case "bottle", "can", "jar"            -> 0.35;
            case "tray"                            -> 0.6;
            // piece / item / unit / portion → category-specific average
            default                                -> pieceWeightKg(category);
        };
    }

    /**
     * Average weight per piece/item for each food category (USDA food weight database).
     */
    private static double pieceWeightKg(ListingCategory category) {
        if (category == null) return 0.3;
        return switch (category) {
            case PRODUCE  -> 0.3;   // avg fruit/veg item (apple ~180g, banana ~120g)
            case DAIRY    -> 0.5;   // avg carton/pack (milk 500ml, yogurt 150g)
            case BAKERY   -> 0.15;  // avg baked item (roll ~80g, cake slice ~100g)
            case PREPARED -> 0.4;   // avg meal container
            case BEVERAGE -> 0.35;  // avg bottle/can (330ml can, 500ml bottle)
            case PANTRY   -> 0.4;   // avg can/jar (400g tin)
            case FROZEN   -> 0.3;   // avg frozen portion
            default       -> 0.3;
        };
    }

    private static double factorFor(ListingCategory category) {
        if (category == null) return OTHER_FACTOR;
        return switch (category) {
            case PRODUCE  -> PRODUCE_FACTOR;
            case DAIRY    -> DAIRY_FACTOR;
            case BAKERY   -> BAKERY_FACTOR;
            case PREPARED -> PREPARED_FACTOR;
            case BEVERAGE -> BEVERAGE_FACTOR;
            case PANTRY   -> PANTRY_FACTOR;
            case FROZEN   -> FROZEN_FACTOR;
            default       -> OTHER_FACTOR;
        };
    }
}
