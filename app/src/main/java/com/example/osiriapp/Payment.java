package com.example.osiriapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Payment extends AppCompatActivity {

    public EditText amountED;
    public String amountInNumber;
    public int naira = 381;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        amountED = findViewById(R.id.amountEditTextBox);

    }

    public void done(View view) {

        int dollarAmount = Integer.parseInt(amountED.getText().toString());

        int convert = dollarAmount * naira;

        Intent intent = new Intent(getApplicationContext(), CheckoutActivityJava.class);
        amountInNumber = amountED.getText().toString() ;
        intent.putExtra("converted_amount", convert);
        intent.putExtra("amount_in", amountInNumber);
        startActivity(intent);
        finish();

    }
}