package net.opgenorth.gallonstolitres;

import java.text.DecimalFormat;

public class ConvertUSGasPriceToCanadian {
    private static final DecimalFormat _gasPriceFormatter = new DecimalFormat("###0");
    private static final DecimalFormat _exchangeRateFormatter = new DecimalFormat("0.0000");

    private double _exchangeRate;

    public ConvertUSGasPriceToCanadian(double exchangeRate) {
        _exchangeRate = exchangeRate;
    }

    public String getCanadianPrice(double usdPerGallons) {

        double cadPerGallon = usdPerGallons * _exchangeRate;
        double cadPerLitre = cadPerGallon / Globals.LITRES_PER_GALLON;
        return _gasPriceFormatter.format(cadPerLitre * 100) + " cents / litre";
    }

    public String getFormattedExchangeRate() {
        return "1 USD = " + _exchangeRateFormatter.format(_exchangeRate) + " CAD";
    }

    public Double getExchangeRate() {
        return _exchangeRate;
    }

    public void setExchangeRate(Double exchangeRate) {
        _exchangeRate = exchangeRate;
    }
}
