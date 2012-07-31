package net.opgenorth.gallonstolitres;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class GetRoyalBankOfCanadaExchangeRate implements IGetExchangeRate {

    public static final String EXCHANGE_RATE_URL = "http://www.rbcroyalbank.com/rates/cashrates.html";

    @Override
    public double getExchangeRate(Document webPage) {

        if (webPage == null)
            throw new NullPointerException("There is no Document that can be scraped for exchange rate information.");

        double exchangeRate = 0.0;
        Elements tableRows = webPage.select("table.outlines > tbody > tr");

        if (tableRows.size() == 0)
            throw new IndexOutOfBoundsException("There aren't enough table rows in the HTML document - cannot scrape the exchange rate.");

        Element usRow = tableRows.get(1);

        Node exchangeRateCell = usRow.childNode(4);

        String exchangeRateString = exchangeRateCell.childNode(0).toString();

        exchangeRate = Double.parseDouble(exchangeRateString);

        return exchangeRate;
    }
}
