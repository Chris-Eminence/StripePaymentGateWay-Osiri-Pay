package com.example.osiriapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import javax.security.auth.callback.Callback;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CheckoutActivityJava extends AppCompatActivity {

    public Stripe stripe;
    //10.0.2.2 is the Android emulator,s alias to localhost
    private static final String BACKEND_URL = "http://10.0.2.2:4242/";

    private final OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        //Configure the SDK with your Stripe publishable key, so it can make request to Stripe
        Stripe stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_51IEfIWJeopeZj83SxvRnIcFtpz16vdmO9T86E2DZLqMSt79IwZiHHUSceNJoIPPQyKatAgUYmZyUUulSJLWk7EtY00RRGb14Gz")

        );
        startCheckout();
    }

    private void startCheckout() {
        //Create a paymentIntent by calling the server endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = "{"
                + "\"currency\":\"usd\","
                + "\"items\":["
                + "{\"id\":\"photo_subscription\"}"
                + "]"
                + "}";

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));

        //Hook the button with to the Card widget and stripe instance
        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener((View view) -> {
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                stripe.confirmPayment(this, confirmParams);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handle the result of the stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));

    }

    static final class PayCallback implements Callback, okhttp3.Callback {
        @NonNull
        private final WeakReference<CheckoutActivityJava> activityRef;

        PayCallback(@NonNull CheckoutActivityJava activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;

            }

            activity.runOnUiThread(() ->
                    Toast.makeText(activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show());
        }

        //  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response)
                throws IOException {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;
            }
            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG).show()
                );

            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).toString(), type
        );
        paymentIntentClientSecret = responseMap.get("clientSecret");
    }

    private static final class PaymentResultCallback implements ApiResultCallback<PaymentIntentResult> {
        @NonNull
        private final WeakReference<CheckoutActivityJava> activityRef;

        private PaymentResultCallback(@NonNull CheckoutActivityJava activity) {
            activityRef = new WeakReference<>(activity);
        }


        @Override
        public void onSuccess(@NotNull PaymentIntentResult result) {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;
            }
            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                //payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert(
                        "Payment Completed",
                        gson.toJson(paymentIntent)
                );
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                //Payment failed - allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }
        }

        @Override
        public void onError(@NotNull Exception e) {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;

            }
            //Payment failed - allow retrying using a different payment method
            activity.displayAlert("Error", e.toString());
        }
    }

    private void displayAlert (@NonNull String title,
                               @Nullable String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);

        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

}
