package com.example.nielstiben.cryptocurrencywatcher.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nielstiben.cryptocurrencywatcher.R;
import com.example.nielstiben.cryptocurrencywatcher.listener.CurrencyListener;
import com.example.nielstiben.cryptocurrencywatcher.listener.RequestCurrenciesListener;
import com.example.nielstiben.cryptocurrencywatcher.listener.RequestExchangeRateListener;
import com.example.nielstiben.cryptocurrencywatcher.misc.RequestCurrenciesRunnable;
import com.example.nielstiben.cryptocurrencywatcher.misc.RequestExchangeRateRunnable;
import com.example.nielstiben.cryptocurrencywatcher.view.CurrencyView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    String host = "https://api.cryptonator.com/api/";

    // Exchange rate related
    float exchangeRate; // 1 EUR = ?? BTC
    long exchangeRateUpdated;

    // Hashmap containing all currencies (e.g. <"EUR"><"Euro">)
    HashMap<String, String> currencies;
    ArrayAdapter<String> currenciesAdapter;

    // Views
    CurrencyView firstCurrencyView;
    CurrencyView secondCurrencyView;
    TextView tvUpdatedOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load views
        firstCurrencyView = findViewById(R.id.cv_first);
        secondCurrencyView = findViewById(R.id.cv_second);
        tvUpdatedOn = findViewById(R.id.tv_updated_on);

        // Load last-used settings
        loadPreferences();

        // Retrieve data
        updateExchangeRate();
        updateCurrencies();

        // Reload UI
        new UpdateUIThread().run();

        // Add listeners to "Left" card
        firstCurrencyView.setListener(new CurrencyListener() {
            @Override
            public void onCardClick() {
                // Card itself is clicked => show 'change currency' list
                showSelectCurrencyDialog(0);
            }

            @Override
            public void onInput() {
                // New user input => just recalculate & refresh view
                new UpdateUIThread().run();
            }
        });

        // Add listener to "Right" card
        secondCurrencyView.setListener(new CurrencyListener() {
            @Override
            public void onCardClick() {
                // Card itself is clicked => show 'change currency' list
                showSelectCurrencyDialog(1);
            }

            @Override
            public void onInput() {
                // New user input => just recalculate & refresh view
                new UpdateUIThread().run();
            }
        });

        // Assign action to "Update" Button
        findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExchangeRate(); // Retrieve exchange rate for this coin
                new UpdateUIThread().run(); // Reload UI

                // Also try to load the currencies if none are there yet
                if(currencies.size() == 0) updateCurrencies();
            }
        });
    }

    /**
     * Method for showing the dialog containing all currencies
     * @param chozenCurrency first or second currency-box?
     */
    private void showSelectCurrencyDialog(final int chozenCurrency) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle("Select currency:");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(currenciesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strCurrency = currenciesAdapter.getItem(which);
                String currencyCode = Objects.requireNonNull(strCurrency).split(" ")[0];
                String alias = currencies.get(currencyCode);

                // Apply currency
                if (chozenCurrency == 0) {
                    if(!secondCurrencyView.getCurrencyCode().equals(currencyCode)){
                        firstCurrencyView.setCurrencyCode(currencyCode);
                        firstCurrencyView.setAlias(alias);
                    }else{
                        runOnUiThread(new ToasterThread("Coin is already loaded!"));
                    }

                } else {
                    if(!firstCurrencyView.getCurrencyCode().equals(currencyCode)){
                        secondCurrencyView.setCurrencyCode(currencyCode);
                        secondCurrencyView.setAlias(alias);
                    }else{
                        runOnUiThread(new ToasterThread("Coin is already loaded!"));
                    }
                }

                updateExchangeRate(); // Retrieve exchange rate for this coin
                new UpdateUIThread().run(); // Reload UI


            }
        });
        builderSingle.show();
    }

    /**
     * Method for updating the exchange rate of the current currency
     */
    private void updateExchangeRate() {
        String currency1 = firstCurrencyView.getCurrencyCode();
        String currency2 = secondCurrencyView.getCurrencyCode();

        // Start the thread
        new Thread(new RequestExchangeRateRunnable(host, currency1, currency2, new RequestExchangeRateListener() {
            @Override
            public void onExchangeRateReceived(float rate, long timestamp) {
                // Exchange rate received -> Show it!
                exchangeRate = rate;
                exchangeRateUpdated = timestamp;
                runOnUiThread(new UpdateUIThread());
            }

            @Override
            public void onExchangeRateErrorReceived(String errorMessage) {
                // Error occurred -> Toast it!
                runOnUiThread(new ToasterThread(errorMessage));
            }
        })).start();

    }

    /**
     * Method for updating the list of all the currencies
     */
    private void updateCurrencies() {
        new Thread(new RequestCurrenciesRunnable(host, new RequestCurrenciesListener() {
            @Override
            public void onCurrenciesReceived(HashMap<String, String> c) {
                currencies = c;
                String currencyTexts[] = new String[currencies.size()];                 // Create array

                // Fill this array
                int i = 0;
                for (Map.Entry<String, String> entry : currencies.entrySet()) {
                    if (!entry.getKey().equals(entry.getValue()))
                        currencyTexts[i] = entry.getKey() + " (" + entry.getValue() + ")";
                    else currencyTexts[i] = entry.getKey();
                    i++;
                }
                Arrays.sort(currencyTexts); // Sort this array
                currenciesAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice); // Initialize adapter list
                currenciesAdapter.addAll(currencyTexts);                 // Add Array as data source
            }

            @Override
            public void onCurrenciesErrorReceived(String errorMessage) {
                // Error occurred -> Toast it!
//                runOnUiThread(new ToasterThread(errorMessage));
            }
        })).start();
    }

    /**
     * Updates the currency cards based on the user input and exchange rate
     */
    private class UpdateUIThread extends Thread {
        @Override
        public void run() {
            super.run();
            // Set update label
            PrettyTime p = new PrettyTime();
            if (exchangeRateUpdated >= 0) {
                String updatedText = "Updated " + p.format(new Date(exchangeRateUpdated));
                tvUpdatedOn.setText(updatedText);
            }

            // Calculate and set result
            if (firstCurrencyView.hasFocus()) {
                // Focus on left panel
                float multiplier = firstCurrencyView.getAmount();
                secondCurrencyView.setAmount(multiplier * exchangeRate);
            } else {
                // Focus on right panel
                float divider = secondCurrencyView.getAmount();
                float result = exchangeRate / divider;
                if (!Float.isInfinite(result)) firstCurrencyView.setAmount(divider / exchangeRate);
                else firstCurrencyView.setAmount(0);
            }

            savePreferences(); // Save preferences to phone's memory

        }
    }

    /**
     * Toaster
     */
    private class ToasterThread extends Thread {
        String toastText;

        private ToasterThread(String toastText) {
            this.toastText = toastText;
        }

        @Override
        public void run() {
            super.run();
            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
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

        exchangeRateUpdated = prefs.getLong("updated_on", -1);
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

        prefsEditor.putLong("updated_on", exchangeRateUpdated);
        prefsEditor.putFloat("exchange_rate", exchangeRate);
        prefsEditor.apply();
    }
}