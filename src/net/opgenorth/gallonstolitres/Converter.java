package net.opgenorth.gallonstolitres;

public class Converter {
    // http://www.rbcroyalbank.com/rates/cashrates.html
    public static final double USD = 1.0291;

    public static final double LITRES_PER_GALLON = 3.7854;
    public static final double GALLONS_PER_LITRE = 0.264172;

    public static double toLitres(double gallons) {
        double litres = gallons * LITRES_PER_GALLON;
        return litres;
    }

    public static double toGallons(double litres) {
        return litres * LITRES_PER_GALLON;
    }

    public static double convertToCanadianDollars(double usDollars) {
        return usDollars * USD;
    }
}
