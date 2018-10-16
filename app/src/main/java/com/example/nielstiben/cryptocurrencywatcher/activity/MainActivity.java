package com.example.nielstiben.cryptocurrencywatcher.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nielstiben.cryptocurrencywatcher.R;
import com.example.nielstiben.cryptocurrencywatcher.listener.CurrencyListener;
import com.example.nielstiben.cryptocurrencywatcher.misc.HttpHandler;
import com.example.nielstiben.cryptocurrencywatcher.view.CurrencyView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String host = "https://api.cryptonator.com/api/";
    TextView tvUpdatedOn;

    float exchangeRate; // 1 EUR = ?? BTC
    CurrencyView firstCurrencyView;
    CurrencyView secondCurrencyView;
    long updatedOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load views
        firstCurrencyView = findViewById(R.id.cv_first);
        secondCurrencyView = findViewById(R.id.cv_second);
        tvUpdatedOn = findViewById(R.id.tv_updated_on);

        loadPreferences(); // Load last-used settings
        new RequestData().execute(); // Update exchange rate

        // Add listeners to both cards
        firstCurrencyView.setListener(new CurrencyListener() {
            @Override
            public void onCardClick() {
                new RequestData().execute(); // Retrieve exchange rate
            }

            @Override
            public void onInput() {
                updateResults();
            }
        });
        secondCurrencyView.setListener(new CurrencyListener() {
            @Override
            public void onCardClick() {
                new RequestData().execute(); // Retrieve exchange rate
            }

            @Override
            public void onInput() {
                updateResults();
            }
        });
    }


    /**
     * Updates the currency cards based on the user input and exhange rate
     */
    private void updateResults() {
        // Set update label
        PrettyTime p = new PrettyTime();
        if (updatedOn >= 0)
            tvUpdatedOn.setText("Updated " + p.format(new Date(updatedOn)));

        // Calculate and set result
        if (firstCurrencyView.hasFocus()) {
            // Plot result in second CurrencyView
            float multiplier = firstCurrencyView.getAmount();

            secondCurrencyView.setAmount(multiplier * exchangeRate);
        } else {
            // Plot result in first CurrencyView
            float divider = secondCurrencyView.getAmount();
            float result = exchangeRate / divider;
            if (!Float.isInfinite(result)) firstCurrencyView.setAmount(divider / exchangeRate);
            else firstCurrencyView.setAmount(0);
        }

        savePreferences();
    }

    private class RequestData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Build request URL
            String requestURL = host;
            requestURL += "ticker/";
            requestURL += firstCurrencyView.getCurrencyCode(); // Append first currency
            requestURL += "-";
            requestURL += secondCurrencyView.getCurrencyCode(); // Append second currency

            HttpHandler sh = new HttpHandler();
            String jsonString = "";
            try {
                jsonString = sh.makeServiceCall(requestURL);
                if (!jsonString.isEmpty()) {
                    // Parse JSON and get price
                    JSONObject jsonObj = new JSONObject(jsonString);
                    updatedOn = jsonObj.getLong("timestamp") * 1000;
                    exchangeRate = Float.parseFloat(jsonObj.getJSONObject("ticker").getString("price")); // Update rate
                }
            } catch (IOException e) {
                System.err.println(e.toString());
                runOnUiThread(new Toaster("No internet connection"));

            } catch (JSONException e) {
                runOnUiThread(new Toaster("JSON parsing issues"));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateResults();
        }
    }

    private class Toaster implements Runnable {
        String message;

        public Toaster(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method in order to load the last used settings from the phone's Shared Preferences
     */
    private void loadPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Get the values from the Shared Preferences
        String firstCurrencyCode = prefs.getString("first_currency_code", "btc");
        String firstCurrencyAlias = prefs.getString("first_currency_alias", "Bitcoin");
        float firstCurrencyAmount = prefs.getFloat("first_currency_amount", 1);

        String secondCurrencyCode = prefs.getString("second_currency_code", "eur");
        String secondCurrencyAlias = prefs.getString("second_currency_alias", "Euro");
        float secondCurrencyAmount = prefs.getFloat("second_currency_amount", 5600);

        updatedOn = prefs.getLong("updated_on", -1);
        exchangeRate = prefs.getFloat("exchange_rate", 0);


        // Apply the result
        firstCurrencyView.setCurrencyCode(firstCurrencyCode);
        firstCurrencyView.setAlias(firstCurrencyAlias);
        firstCurrencyView.setAmount(firstCurrencyAmount);

        secondCurrencyView.setCurrencyCode(secondCurrencyCode);
        secondCurrencyView.setAlias(secondCurrencyAlias);
        secondCurrencyView.setAmount(secondCurrencyAmount);
    }

    /**
     * Method for saving the current settings to the phone's Shared Preferences
     */
    private void savePreferences() {

        // Get the values from the Views
        String firstCurrencyCode = firstCurrencyView.getCurrencyCode();
        String firstCurrencyAlias = firstCurrencyView.getAlias();
        float firstCurrencyAmount = firstCurrencyView.getAmount();

        String secondCurrencyCode = secondCurrencyView.getCurrencyCode();
        String secondCurrencyAlias = secondCurrencyView.getAlias();
        float secondCurrencyAmount = secondCurrencyView.getAmount();

        // Save to shared preferences
        SharedPreferences.Editor prefsEditor = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
        prefsEditor.putString("first_currency_code", firstCurrencyCode);
        prefsEditor.putString("first_currency_alias", firstCurrencyAlias);
        prefsEditor.putFloat("first_currency_amount", firstCurrencyAmount);
        prefsEditor.putString("second_currency_code", secondCurrencyCode);
        prefsEditor.putString("second_currency_alias", secondCurrencyAlias);
        prefsEditor.putFloat("second_currency_amount", secondCurrencyAmount);

        prefsEditor.putLong("updated_on", updatedOn);
        prefsEditor.putFloat("exchange_rate", exchangeRate);
        prefsEditor.apply();
    }


}
