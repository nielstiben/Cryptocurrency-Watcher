package com.example.nielstiben.cryptocurrencywatcher.misc;
import com.example.nielstiben.cryptocurrencywatcher.listener.RequestExchangeRateListener;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Asynchronous task for receiving the latest exchange rate
 */
public class RequestExchangeRateRunnable implements Runnable {
    private String host;
    private String currency1;
    private String currency2;
    private RequestExchangeRateListener listener;

    public RequestExchangeRateRunnable(String host, String currency1, String currency2, RequestExchangeRateListener listener) {
        this.host = host;
        this.currency1 = currency1;
        this.currency2 = currency2;
        this.listener = listener;
    }

    @Override
    public void run() {
        // Build request URL
        String requestURL = host + "ticker/" + currency1 + "-" + currency2;

        // Create Http Request
        HttpHandler sh = new HttpHandler();
        String jsonString = "";

        // Parse JSON
        try {
            jsonString = sh.makeServiceCall(requestURL);
            if (!jsonString.isEmpty()) {
                JSONObject jsonObj = new JSONObject(jsonString);

                float exchangeRate = Float.parseFloat(jsonObj.getJSONObject("ticker").getString("price")); // "Exchange rate"
                long exchangeRateUpdated = jsonObj.getLong("timestamp") * 1000; // "Updated on..."

                listener.onExchangeRateReceived(exchangeRate, exchangeRateUpdated); // Callback to main method
            }
        } catch (IOException e) {
            // Connection related issue
            listener.onExchangeRateErrorReceived("No internet connection");

        } catch (JSONException e) {
            // JSON related issue
            listener.onExchangeRateErrorReceived("API server error");
        }
    }
}