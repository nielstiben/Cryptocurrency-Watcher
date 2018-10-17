package com.example.nielstiben.cryptocurrencywatcher.misc;
import com.example.nielstiben.cryptocurrencywatcher.listener.RequestCurrenciesListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;

/**
 * Runnable for receiving currencies
 */
public class RequestCurrenciesRunnable implements Runnable {
    private String host;
    private RequestCurrenciesListener listener;

    public RequestCurrenciesRunnable(String host, RequestCurrenciesListener listener) {
        this.host = host;
        this.listener = listener;
    }

    @Override
    public void run() {
        // Build request URL
        String requestURL = host + "currencies/";

        // Create Http Request
        HttpHandler sh = new HttpHandler();
        String jsonString = "";

        // Parse JSON
        try {
            jsonString = sh.makeServiceCall(requestURL);
            if (!jsonString.isEmpty()) {
                HashMap<String, String> currencies = new HashMap<>(); // Hashmap for storing the currencies
                JSONArray currenciesArray = new JSONObject(jsonString).getJSONArray("rows");

                // Loop through every currency
                for (int i = 0; i < currenciesArray.length(); i++) {
                    JSONObject currencyObject = currenciesArray.getJSONObject(i);

                    // Substract data
                    String code = currencyObject.getString("code");
                    String name = currencyObject.getString("name");

                    // Add to array
                    currencies.put(code, name);
                }

                // All currencies are added -> callback
                listener.onCurrenciesReceived(currencies);
            }
        } catch (IOException | JSONException e) {
            listener.onCurrenciesErrorReceived("Error receiving currencylist");
        }
    }
}