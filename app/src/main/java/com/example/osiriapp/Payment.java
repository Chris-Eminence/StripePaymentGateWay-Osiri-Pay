package com.example.osiriapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Payment extends AppCompatActivity {

    public EditText amountED;
    public String amountInNumber;
    public double amountConversion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        amountED = findViewById(R.id.amountEditTextBox);
    }

    public void done(View view) {

        String convertAmount = amountED.getText().toString();
        double amountEd = Double.parseDouble(convertAmount);

        double amountConversion = amountEd * 381.00;

        Intent intent = new Intent(Payment.this, CheckoutActivityJava.class);



        amountInNumber = amountED.getText().toString() ;
        intent.putExtra("converted_amount", amountConversion);
        intent.putExtra("amount_in", amountInNumber);
        startActivity(intent);
        finish();

    }
}