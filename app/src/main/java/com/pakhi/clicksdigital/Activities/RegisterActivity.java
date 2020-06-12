package com.pakhi.clicksdigital.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.pakhi.clicksdigital.R;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    DatabaseReference RootRef;
    private String number;
    private CountryCodePicker ccp;
    private EditText mobileNo_reg;
    private Button verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mobileNo_reg = (EditText) findViewById(R.id.mobileNo_reg);
        verify = findViewById(R.id.verify);
        ccp = findViewById(R.id.ccp);

        firebaseAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF, 0); // 0 - for private mode
        final SharedPreferences.Editor editor = pref.edit();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ccp.registerCarrierNumberEditText(mobileNo_reg);
                String num = mobileNo_reg.getText().toString().trim();

                number = ccp.getDefaultCountryCodeWithPlus() + "" + num;

                // editor.putString("PhoneNumber", number);
                //  editor.commit();

                Log.d("RegisterActivity", number + "----------------------------");
                if (TextUtils.isEmpty(num)) {
                    mobileNo_reg.setFocusable(true);
                    mobileNo_reg.setError("mobile number is required");
                    // return;
                } else {

                    Intent i = new Intent(RegisterActivity.this, PhoneVerify.class);
                    i.putExtra("PhoneNumber", number);
                    startActivity(i);
                    //finish();
                }
            }
        });

    }

    //talk about this
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {

            VerifyUserExistance();

        }
    }

    private void VerifyUserExistance() {
        String currentUserID = firebaseAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child(Constants.USER_NAME).exists())) {
                      //sendUserToStartActivity();
                    if(dataSnapshot.child("groups").exists()){
                        sendUserToStartActivity();
                    }
                    else{
                        sendUserToJoinGroupActivity();
                    }
                } else {
                     SendUserToSetProfileActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToJoinGroupActivity() {
        Intent intent = new Intent(RegisterActivity.this, JoinGroupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void sendUserToStartActivity() {

        Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void SendUserToSetProfileActivity() {
        Intent intent = new Intent(RegisterActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


}