package com.example.nielstiben.cryptocurrencywatcher.listener;

import java.util.HashMap;

public interface RequestCurrenciesListener {
    void onCurrenciesReceived(HashMap<String, String> currencies);
    void onCurrenciesErrorReceived(String errorMessage);
}
