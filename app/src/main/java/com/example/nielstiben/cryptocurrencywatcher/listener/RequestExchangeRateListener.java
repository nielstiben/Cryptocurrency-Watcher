package com.example.nielstiben.cryptocurrencywatcher.listener;

public interface RequestExchangeRateListener {
    void onExchangeRateReceived(float rate, long timestamp);
    void onExchangeRateErrorReceived(String errorMessage);
}
