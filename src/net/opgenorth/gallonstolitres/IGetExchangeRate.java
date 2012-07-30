package net.opgenorth.gallonstolitres;

import org.jsoup.nodes.Document;

/**
 * Created with IntelliJ IDEA.
 * User: tom
 * Date: 30/07/12
 * Time: 6:38 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IGetExchangeRate {
    double getExchangeRate(Document webPage);
}
