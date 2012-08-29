package net.opgenorth.gallonstolitres;

import org.jsoup.nodes.Document;


public interface IGetExchangeRate {
    double getExchangeRate(Document webPage);
}
