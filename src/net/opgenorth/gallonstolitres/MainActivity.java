package net.opgenorth.gallonstolitres;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainActivity extends Activity {
    private TextView _canadianPriceTextView;
    private EditText _usdPerGallonEditText;
    private TextView _exchangeRateTextView;
    private GetExchangeRateTask _task;
    private NotificationManager _notificationManager = null;

    private ConvertUSGasPriceToCanadian _converter;
    private static final String[] EXCHANGE_RATE_URL = new String[]{GetRoyalBankOfCanadaExchangeRate.EXCHANGE_RATE_URL};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        setContentView(R.layout.layout_main);
        _converter = new ConvertUSGasPriceToCanadian(getLastExchangeRate());
        _usdPerGallonEditText = (EditText) findViewById(R.id.gallons);
        _canadianPriceTextView = (TextView) findViewById(R.id.litres);
        _exchangeRateTextView = (TextView) findViewById(R.id.exchange_rate);

        _task = (GetExchangeRateTask) getLastNonConfigurationInstance();
        if (_task == null) {
            _task = new GetExchangeRateTask(this);
            _task.execute(EXCHANGE_RATE_URL);
        } else {
            _task.attach(this);
        }
        updateCentsPerLitre();

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

    private double getLastExchangeRate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean saveADefault = false;
        double exchangeRate = Globals.DEFAULT_EXCHANGE_RATE;
        try {
            exchangeRate = new Double(prefs.getFloat("exchange_rate", 1.0f));

        } catch (Exception e) {
            Log.e(Globals.TAG, "There was a problem trying to get the exchange rate - assuming a default of par.", e);
            saveADefault = true;
        }

        if (saveADefault) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("exchange_rate", 1.0f);
            editor.commit();
        }
        return exchangeRate;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void downloadExchangeRate() {
        if (_task.hasRun()) {
            _task = new GetExchangeRateTask(this);
        }
        if (_task.isRunning()) {
            Log.d(Globals.TAG, "Already trying to download the exchange rates.");
            return;
        }
        _task.execute(EXCHANGE_RATE_URL);

    }

    private static class GetExchangeRateTask extends AsyncTask<String, Void, Document> {

        public static final int NOTIFY_ME_ID = 1001;
        private MainActivity _activity;
        private boolean _hasRun = false;
        private boolean _isRunning = false;

        private GetExchangeRateTask(MainActivity activity) {
            this._activity = activity;
        }


        @Override
        protected void onPreExecute() {
            _isRunning = true;
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
            _isRunning = false;
            double exchangeRate = 1.0;
            boolean didParseExchangeRate = false;
            if (document == null) {
                Log.w(Globals.TAG, "There is no document to scrape the exchange rate from.");
            } else {
                try {
                    IGetExchangeRate rer = new GetRoyalBankOfCanadaExchangeRate();
                    exchangeRate = rer.getExchangeRate(document);
                    didParseExchangeRate = true;
                    Log.v(Globals.TAG, "Got a new exchange rate: " + _activity._converter.getExchangeRate());
                } catch (Exception ex) {
                    Log.e(Globals.TAG, "There was a problem trying to parse the exchange rate from the web page.", ex);
                }
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_activity);

            String msg;
            if (didParseExchangeRate) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat("exchange_rate", new Float(exchangeRate));
                editor.commit();
                Log.d(Globals.TAG, "Updated the shared preferences with the new exchange rate.");
                msg = "New exchange rate : " + exchangeRate;


            } else {
                Log.w(Globals.TAG, "Could not get the exchange rate, defaulting to the previous exchange rate.");
                exchangeRate = new Double(prefs.getFloat("exchange_rate", 1.0f));
                msg = "Error retrieving currency";
            }

            postNotificationOfPriceChange(msg);

            _activity._converter.setExchangeRate(exchangeRate);
            _activity.updateCentsPerLitre();
        }

        private void postNotificationOfPriceChange(String msg) {
            Context ctx = _activity.getApplicationContext();
            Intent notificationIntent = new Intent(_activity, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(_activity, 0, notificationIntent, 0);
            Notification note = new Notification(R.drawable.ic_stat_currency_downloaded, msg, System.currentTimeMillis());
            note.setLatestEventInfo(ctx, "Gas Prices Exchange Rate Updated", msg, contentIntent);
            note.flags |= Notification.FLAG_AUTO_CANCEL;
            _activity._notificationManager.notify(NOTIFY_ME_ID, note);
        }

        public void attach(MainActivity activity) {
            _activity = activity;
        }

        public void detach() {
            _activity = null;
        }

        public boolean hasRun() {
            return _hasRun;
        }

        public boolean isRunning() {
            return _isRunning;
        }
    }
}
