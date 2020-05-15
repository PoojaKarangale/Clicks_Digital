package com.pakhi.clicksdigital;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneVerify extends AppCompatActivity {
    //annotationProcessor 'com.github.bumptech.glide:compiler:4.8.0'
    private FirebaseAuth firebaseAuth;
    private String number, verificationCode;
    private EditText get_code;
    private Button btn_verify, resend_otp;
    private PhoneAuthProvider.ForceResendingToken token;
    private ProgressBar loading_bar;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCode = s;
            token = forceResendingToken;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            loading_bar.setVisibility(View.VISIBLE);
            resend_otp.setVisibility(View.INVISIBLE);
            Toast.makeText(PhoneVerify.this, "Please check your INTERNET connection and click RESEND OTP", Toast.LENGTH_SHORT).show();
        }
    };
    private TextView verify_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);

        loading_bar = findViewById(R.id.loading_bar);
        firebaseAuth = FirebaseAuth.getInstance();
        btn_verify = findViewById(R.id.btn_verify);
        get_code = findViewById(R.id.get_code);
        verify_number = findViewById(R.id.verify_number);
        resend_otp = findViewById(R.id.resend_otp);
        resend_otp.setVisibility(View.INVISIBLE);


        number = getIntent().getStringExtra("PhoneNumber");
        verify_number.setText("Verify " + number);

        verifyPhoneNumber(number);

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = get_code.getText().toString().trim();
                if (code.isEmpty() | code.length() < 6) {
                    get_code.setError("Enter OTP...");
                    get_code.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });

        resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPhoneNumber(number);
            }
        });
    }

    private void verifyPhoneNumber(String number) {
        loading_bar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent resIntent = new Intent(PhoneVerify.this, SetProfileActivity.class);
                            resIntent.putExtra("number",number);
                            resIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(resIntent);
                            finish();

                        } else {
                            loading_bar.setVisibility(View.VISIBLE);
                            resend_otp.setVisibility(View.INVISIBLE);
                            Toast.makeText(PhoneVerify.this, "Please check your INTERNET connection and click RESEND OTP", Toast.LENGTH_SHORT).show();
                            Toast.makeText(PhoneVerify.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
