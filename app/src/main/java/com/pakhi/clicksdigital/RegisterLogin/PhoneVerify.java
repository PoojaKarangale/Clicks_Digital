package com.pakhi.clicksdigital.RegisterLogin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Profile.SetProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.ToastClass;

import java.util.concurrent.TimeUnit;

public class PhoneVerify extends AppCompatActivity implements View.OnClickListener {
    String                   previousActivity;
    // DatabaseReference userRef;
    String                   userId;
    UserDatabase             ud;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    private FirebaseAuth firebaseAuth;
    private String       number, verificationCode;
    private EditText get_code;
    private Button   btn_verify, resend_otp;
    private PhoneAuthProvider.ForceResendingToken                 token;
    private ProgressBar                                           loading_bar;
    private TextView                                              verify_number;
    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            //Getting the code sent by SMS
            String code=phoneAuthCredential.getSmsCode();
            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code

            if (code != null) {
                get_code.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            } else {
                get_code.setVisibility(View.INVISIBLE);
                signInWithCredential(phoneAuthCredential);
            }
            showToast("Phone verified automatically");
        }


        @Override
        public void onVerificationFailed(FirebaseException e) {
            loading_bar.setVisibility(View.VISIBLE);
            btn_verify.setVisibility(View.INVISIBLE);
            resend_otp.setVisibility(View.VISIBLE);
            showToast("Please check your INTERNET connection and click RESEND OTP\n" + e.getMessage());

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.e("this", "code sent");
            //storing the verification id that is sent to the user
            btn_verify.setVisibility(View.VISIBLE);
            loading_bar.setVisibility(View.INVISIBLE);
            resend_otp.setVisibility(View.VISIBLE);
            showToast("Code Sent");
            verificationCode=s;
            token=forceResendingToken;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);

        number=getIntent().getStringExtra(Const.MO_NUMBER);
        previousActivity=getIntent().getStringExtra(Const.prevActivity);
        String phoneNumberWithoutSpecialChar=number.replaceAll("[ -()/]", "");
        pref=SharedPreference.getInstance();
        rootRef=FirebaseDatabaseInstance.getInstance();
        // userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // userId = FirebaseAuth.getInstance().getUid();

        initializingFields();
        Log.d("phoneVerify", " ----------------------" + phoneNumberWithoutSpecialChar + "----------" + number);

        verify_number.setText("Verify " + number);
        sendVerificationCode(number);

        initializeListeners();

    }

    private void initializingFields() {
        loading_bar=findViewById(R.id.loading_bar);
        firebaseAuth=FirebaseAuth.getInstance();
        btn_verify=findViewById(R.id.btn_verify);
        get_code=findViewById(R.id.get_code);
        verify_number=findViewById(R.id.verify_number);
        resend_otp=findViewById(R.id.resend_otp);
    }

    private void initializeListeners() {
        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
        btn_verify.setOnClickListener(this);
        resend_otp.setOnClickListener(this);
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
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationCode, code);
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

                            if (previousActivity.equals("changeNumber")) {
                                updateUserData();
                            }
                            //user verifies mobile number we can store it in shared preferences now!!!
                            saveDataToSharedPreferences();
                            updateUi();
                            // setUserToSetProfileActivity();
                            //finish();
                        } else {
                            loading_bar.setVisibility(View.VISIBLE);
                            resend_otp.setVisibility(View.VISIBLE);

                            showToast("Please check your INTERNET connection and click RESEND OTP\n" + task.getException().getMessage());
                           /* Toast.makeText(PhoneVerify.this, "Please check your INTERNET connection and click RESEND OTP", Toast.LENGTH_SHORT).show();
                            Toast.makeText(PhoneVerify.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();*/
                        }
                    }
                });
    }

    private void updateUi() {
        if (pref.getData(SharedPreference.userState, getApplicationContext()) != null
                && pref.getData(SharedPreference.userState, getApplicationContext()).equals(Const.verifiedUserState)) {
            checkUserOnline();
        } else if (pref.getData(SharedPreference.userState, getApplicationContext()) != null
                && pref.getData(SharedPreference.userState, getApplicationContext()).equals(Const.profileStoredUserStored)) {
            sendUserToStartActivity();
        }
        finish();
    }

    private void checkUserOnline() {
        // rootRef.getUserRef()
        String currentUserID=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        rootRef.getUserRef().child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child(Const.USER_NAME).exists())) {
                    sendUserToStartActivity();
                } else {
                    setUserToSetProfileActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void showToast(String s) {
        ToastClass.makeText(getApplicationContext(), s);
    }

    private void updateUserData() {
        rootRef.getUserRef().child(userId).
                child(Const.USER_DETAILS).
                child(Const.MO_NUMBER).
                setValue(number).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ud=new UserDatabase(PhoneVerify.this);
                        String[] key={"number"};
                        String[] value={number};
                        boolean isUpdated=ud.updateData(key, value, userId);
                        if (isUpdated) {
                            showToast("Mobile number is updated");

                        } else {
                            showToast("Failed to update");
                        }
                    }
                });
    }

    private void saveDataToSharedPreferences() {
        SharedPreference pref=SharedPreference.getInstance();
        pref.saveData(SharedPreference.phone, number.replaceAll(" ", ""), getApplicationContext());
        pref.saveData(SharedPreference.logging, Const.loggedIn, getApplicationContext());
        pref.saveData(SharedPreference.currentUserId, FirebaseAuth.getInstance().getUid(), getApplicationContext());
        pref.saveData(SharedPreference.userState, Const.verifiedUserState, getApplicationContext());
    }

    void setUserToSetProfileActivity() {
        Intent resIntent=new Intent(PhoneVerify.this, SetProfileActivity.class);
        resIntent.putExtra("PreviousActivity", "PhoneVerify");
        resIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(resIntent);
    }

    private void sendUserToStartActivity() {

        Intent intent=new Intent(PhoneVerify.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_verify:
                String code=get_code.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    get_code.setError("Enter valid code");
                    get_code.requestFocus();
                    return;
                }
                //verifying the code entered manually
                verifyVerificationCode(code);
                break;
            case R.id.resend_otp:
                sendVerificationCode(number);
                break;

        }
    }

}
