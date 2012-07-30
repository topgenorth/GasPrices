package net.opgenorth.gallonstolitres;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class GetRoyalBankOfCanadaExchangeRate implements IGetExchangeRate {

    public static final String EXCHANGE_RATE_URL = "http://www.rbcroyalbank.com/rates/cashrates.html";

    @Override
    public double getExchangeRate(Document webPage) {

        double exchangeRate = 0.0;
        Elements tableRows = webPage.select("table.outlines > tbody > tr");
        Element usRow = tableRows.get(1);

        Node exchangeRateCell = usRow.childNode(4);

        String exchangeRateString = exchangeRateCell.childNode(0).toString();

        exchangeRate = Double.parseDouble(exchangeRateString);

        return exchangeRate;
    }
}
