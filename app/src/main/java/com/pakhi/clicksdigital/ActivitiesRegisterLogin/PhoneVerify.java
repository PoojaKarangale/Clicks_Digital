package com.pakhi.clicksdigital.ActivitiesRegisterLogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.ActivitiesProfile.SetProfileActivity;
import com.pakhi.clicksdigital.R;

import java.util.concurrent.TimeUnit;

public class PhoneVerify extends AppCompatActivity {
    //annotationProcessor 'com.github.bumptech.glide:compiler:4.8.0'
    private FirebaseAuth firebaseAuth;
    private String number, verificationCode;
    private EditText get_code;
    private Button btn_verify, resend_otp;
    private PhoneAuthProvider.ForceResendingToken token;
    private ProgressBar loading_bar;
    private TextView verify_number;
    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();
            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code

            if (code != null) {
                Toast.makeText(PhoneVerify.this, "Phone verified automatically", Toast.LENGTH_SHORT).show();
                get_code.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            } else {
                get_code.setVisibility(View.INVISIBLE);
                Toast.makeText(PhoneVerify.this, "Phone verified automatically", Toast.LENGTH_SHORT).show();
                signInWithCredential(phoneAuthCredential);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            loading_bar.setVisibility(View.VISIBLE);
            btn_verify.setVisibility(View.INVISIBLE);
            resend_otp.setVisibility(View.VISIBLE);
            Toast.makeText(PhoneVerify.this, "Please check your INTERNET connection and click RESEND OTP\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.e("this", "code sent");
            //storing the verification id that is sent to the user
            btn_verify.setVisibility(View.VISIBLE);
            loading_bar.setVisibility(View.INVISIBLE);
            resend_otp.setVisibility(View.VISIBLE);
            Toast.makeText(PhoneVerify.this, "Code Sent", Toast.LENGTH_SHORT).show();
            verificationCode = s;
            token = forceResendingToken;
        }
    };

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

        number = getIntent().getStringExtra("PhoneNumber");
        String phoneNumberWithoutSpecialChar = number.replaceAll("[ -()/]", "");

        Log.d("phoneVerify", " ----------------------" + phoneNumberWithoutSpecialChar + "----------" + number);
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Const.SHARED_PREF, 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();
        //number=pref.getString("PhoneNumber","");
        editor.putString("PhoneNumber", phoneNumberWithoutSpecialChar);
        editor.commit();

        verify_number.setText("Verify " + number);
        sendVerificationCode(number);

        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = get_code.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    get_code.setError("Enter valid code");
                    get_code.requestFocus();
                    return;
                }

                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

        //re-send otp in any case
        resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode(number);
            }
        });
    }

    //the method is sending verification code
    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, code);
        //signing the user
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loading_bar.setVisibility(View.VISIBLE);
                            btn_verify.setVisibility(View.INVISIBLE);
                            resend_otp.setVisibility(View.INVISIBLE);
                            Intent resIntent = new Intent(PhoneVerify.this, SetProfileActivity.class);
                            //  resIntent.putExtra("PhoneNumber", number);
                            resIntent.putExtra("PreviousActivity", "PhoneVerify");
                            resIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(resIntent);
                            finish();
                        } else {
                            loading_bar.setVisibility(View.VISIBLE);
                            resend_otp.setVisibility(View.VISIBLE);
                            Toast.makeText(PhoneVerify.this, "Please check your INTERNET connection and click RESEND OTP", Toast.LENGTH_SHORT).show();
                            Toast.makeText(PhoneVerify.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
