package net.opgenorth.gallonstolitres;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

public class activity_main extends Activity {


    private TextView _canadianPrice;
    private EditText _usdPerGallon;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        _usdPerGallon = (EditText) findViewById(R.id.gallons);
        _canadianPrice = (TextView) findViewById(R.id.litres);

        _usdPerGallon.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    String txt = _usdPerGallon.getText().toString();
                    // This is the Canadian cost of 1 gallon
                    try {
                    double cad = Converter.convertToCanadianDollars(Double.parseDouble(txt));
                    double litres = Converter.toLitres(1.0);

                    double cadPerLitre = cad/litres;
                    DecimalFormat f = new DecimalFormat("###0");

                    _canadianPrice.setText(f.format(cadPerLitre*100) + " cents per litre.");
                    }
                    catch (NumberFormatException nfe)
                    {
                        _canadianPrice.setText("0");
                    }
                }

                return false;
            }
        });

    }
}
