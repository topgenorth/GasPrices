package net.opgenorth.gallonstolitres;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class activity_main extends Activity {
    private TextView _canadianPriceTextView;
    private EditText _usdPerGallonEditText;
    private TextView _exchangeRateTextView;
    private GetExchangeRateTask _task;

    private ConvertUSGasPriceToCanadian _converter;
    private static final String[] EXCHANGE_RATE_URL = new String[]{GetRoyalBankOfCanadaExchangeRate.EXCHANGE_RATE_URL};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _converter = new ConvertUSGasPriceToCanadian();
        _task = (GetExchangeRateTask) getLastNonConfigurationInstance();
        if (_task == null) {
            _task = new GetExchangeRateTask(this);
            _task.execute(EXCHANGE_RATE_URL);
        } else {
            _task.attach(this);
        }

        setContentView(R.layout.layout_main);

        _usdPerGallonEditText = (EditText) findViewById(R.id.gallons);
        _canadianPriceTextView = (TextView) findViewById(R.id.litres);
        _exchangeRateTextView = (TextView) findViewById(R.id.exchange_rate);

        _usdPerGallonEditText.setOnKeyListener(new View.OnKeyListener() {
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
        _exchangeRateTextView.setText(_converter.getFormattedExchangeRate());

        String txt = _usdPerGallonEditText.getText().toString();
        try {
            double usdPerGallon = Double.parseDouble(txt);
            _canadianPriceTextView.setText(_converter.getCanadianPrice(usdPerGallon));
        } catch (NumberFormatException nfe) {
            _canadianPriceTextView.setText("0");
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        _task.detach();
        return (_task);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_exchange_rage:
                Log.d(Globals.TAG, "User requested that a new exchange rate be downloaded.");
                downloadExchangeRate();
                return true;
            case R.id.preferences:
                startActivity(new Intent(this, EditPreferences.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void downloadExchangeRate() {
        if (_task.hasRun()) {
            _task = new GetExchangeRateTask(this);
        }
        _task.execute(EXCHANGE_RATE_URL);

    }

    private static class GetExchangeRateTask extends AsyncTask<String, Void, Document> {
        private activity_main _activity;
        private boolean _hasRun = false;

        private GetExchangeRateTask(activity_main activity) {
            this._activity = activity;

        }

        @Override
        protected Document doInBackground(String... urls) {
            Document page;
            String urlString = urls[0];
            try {
                page = Jsoup.connect(urlString).get();
            } catch (Exception e) {
                Log.e(Globals.TAG, "Problem getting the page at " + urlString, e);
                page = null;
            }
            return page;
        }

        @Override
        protected void onPostExecute(Document document) {

            _hasRun = true;
            if (document == null) {
                Log.w(Globals.TAG, "There is no document to scrape the exchange rate from.");
                return;
            }

            IGetExchangeRate rer = new GetRoyalBankOfCanadaExchangeRate();
            Double exchangeRate = rer.getExchangeRate(document);
            _activity._converter.setExchangeRate(exchangeRate);
            _activity.updateCentsPerLitre();
            Log.v(Globals.TAG, "Got a new exchange rate: " + _activity._converter.getExchangeRate());

        }

        public void attach(activity_main activity) {
            _activity = activity;
        }

        public void detach() {
            _activity = null;
        }

        public boolean hasRun() {
            return _hasRun;
        }
    }
}
