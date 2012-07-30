package net.opgenorth.gallonstolitres;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class activity_main extends Activity {
    private TextView _canadianPrice;
    private EditText _usdPerGallon;
    private TextView _exchangeRate;

    private ConvertUSGasPriceToCanadian _converter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute(new String[]{GetRoyalBankOfCanadaExchangeRate.EXCHANGE_RATE_URL});


        _converter = new ConvertUSGasPriceToCanadian();
        setContentView(R.layout.main);

        _usdPerGallon = (EditText) findViewById(R.id.gallons);
        _canadianPrice = (TextView) findViewById(R.id.litres);
        _exchangeRate = (TextView) findViewById(R.id.exchange_rate);

        _usdPerGallon.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    updateCentsPerLitre();
                }
                return false;
            }
        });

    }

    private void updateCentsPerLitre() {
        _exchangeRate.setText(_converter.getExchangeRate());

        String txt = _usdPerGallon.getText().toString();
        try {
            double usdPerGallon = Double.parseDouble(txt);
            _canadianPrice.setText(_converter.getCanadianPrice(usdPerGallon));
        } catch (NumberFormatException nfe) {
            _canadianPrice.setText("0");
        }
    }


    private class DownloadWebPageTask extends AsyncTask<String, Void, Document> {


        @Override
        protected Document doInBackground(String... urls) {
            Document page = null;
            String urlString = urls[0];
            try {
                page = Jsoup.connect(urlString).get();
            } catch (Exception e) {
                Log.e(Globals.TAG, "Problem getting the page at" + urlString, e);
                page = null;
            }
            return page;
        }

        @Override
        protected void onPostExecute(Document document) {
            if (document == null) {
                Log.w(Globals.TAG, "There is no document to scrape the exchange rate from.");
                return;
            }
            IGetExchangeRate rer = new GetRoyalBankOfCanadaExchangeRate();
            Double exchangeRate = rer.getExchangeRate(document);

            _converter = new ConvertUSGasPriceToCanadian(exchangeRate);
            updateCentsPerLitre();
            Log.v(Globals.TAG, "Got a new exchange rate: " + _converter.getExchangeRate());

        }
    }
}
