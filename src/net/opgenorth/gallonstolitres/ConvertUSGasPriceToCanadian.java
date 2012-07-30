package net.opgenorth.gallonstolitres;

import java.text.DecimalFormat;

public class ConvertUSGasPriceToCanadian {
    // http://www.rbcroyalbank.com/rates/cashrates.html
    public static final double DEFAULT_EXCHANGE_RATE = 1.0291;

    public static final double LITRES_PER_GALLON = 3.7854;
    public static final double GALLONS_PER_LITRE = 0.264172;

    private static final DecimalFormat _gasPriceFormatter = new DecimalFormat("###0");
    private static final DecimalFormat _exchangeRateFormatter = new DecimalFormat("0.0000");

    private double _exchangeRate;

    public ConvertUSGasPriceToCanadian() {
        _exchangeRate = DEFAULT_EXCHANGE_RATE;
    }

    public ConvertUSGasPriceToCanadian(double exchangeRate) {
        _exchangeRate = exchangeRate;
    }

    public String getCanadianPrice(double usdPerGallons) {

        double cadPerGallon = usdPerGallons * _exchangeRate;
        double cadPerLitre = cadPerGallon / LITRES_PER_GALLON;
        return _gasPriceFormatter.format(cadPerLitre * 100) + " cents / litre";

    }

    public String getExchangeRate() {
        return "1 USD = " + _exchangeRateFormatter.format(_exchangeRate) + " CAD";
    }
}
