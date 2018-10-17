package com.example.nielstiben.cryptocurrencywatcher.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nielstiben.cryptocurrencywatcher.R;
import com.example.nielstiben.cryptocurrencywatcher.listener.CurrencyListener;

/**
 * TODO: document your custom view class.
 */
public class CurrencyView extends LinearLayout {
    private TextView tvAlias;
    private EditText etAmount;

    private String currencyCode;
    private TextWatcher watcher;
    private CurrencyListener listener;

    public CurrencyView(Context context) {
        super(context);
        init(context);
    }

    public CurrencyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CurrencyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CurrencyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * Initialization function
     *
     * @param context Application context
     */
    private void init(Context context) {
        inflate(context, R.layout.currency_view, this);

        // Find UI elements
        CardView cvCurrencyCard = findViewById(R.id.cv_currency_card);
        tvAlias = findViewById(R.id.tv_alias);
        etAmount = findViewById(R.id.et_amount);

        // Initiate listener for input events
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputText = etAmount.getText().toString();
                listener.onInput();
            }
        };
        etAmount.addTextChangedListener(watcher);

        // Initiate listener if card is clicked
        cvCurrencyCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCardClick();
            }
        });
    }

    // Setters and getters
    public void setListener(CurrencyListener listener) {
        this.listener = listener;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getAlias() {
        return tvAlias.getText().toString();
    }

    public void setAlias(String currencyText) {
        tvAlias.setText(currencyText);
    }

    public float getAmount() {
        if (!etAmount.getText().toString().isEmpty())
            return Float.parseFloat(etAmount.getText().toString());
        return 0;
    }

    public void setAmount(float amount) {
        etAmount.removeTextChangedListener(watcher);
        etAmount.setText(String.valueOf(amount));
        etAmount.addTextChangedListener(watcher);
    }
}
