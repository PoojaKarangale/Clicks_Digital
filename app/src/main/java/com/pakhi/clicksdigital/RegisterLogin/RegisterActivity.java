package com.pakhi.clicksdigital.RegisterLogin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.JoinGroup.JoinGroupActivity;
import com.pakhi.clicksdigital.Profile.SetProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth     firebaseAuth;
    // DatabaseReference RootRef;
    SharedPreference pref;
    private String            number;
    private CountryCodePicker ccp;
    private EditText          mobileNo_reg;

    FirebaseDatabaseInstance rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pref=SharedPreference.getInstance();
        rootRef=FirebaseDatabaseInstance.getInstance();
        mobileNo_reg=(EditText) findViewById(R.id.mobileNo_reg);
        Button verify=findViewById(R.id.verify);
        ccp=findViewById(R.id.ccp);


        firebaseAuth=FirebaseAuth.getInstance();
        // RootRef = FirebaseDatabase.getInstance().getReference();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ccp.registerCarrierNumberEditText(mobileNo_reg);
                String num=mobileNo_reg.getText().toString().trim();

                number=ccp.getDefaultCountryCodeWithPlus() + "" + num;

                if (TextUtils.isEmpty(num)) {
                    mobileNo_reg.setFocusable(true);
                    mobileNo_reg.setError("mobile number is required");
                    // return;
                } else {
                    sendUserToPhoneVerify();
                    //finish();
                }
            }
        });
    }

    private void sendUserToPhoneVerify() {
        Intent i=new Intent(RegisterActivity.this, PhoneVerify.class);
        i.putExtra(Const.MO_NUMBER, number);
        i.putExtra(Const.prevActivity, Const.registerActivity);
        startActivity(i);
    }

    //talk about this
    @Override
    protected void onStart() {
        super.onStart();
        VerifyUserExistance();

      /*  if (firebaseAuth.getCurrentUser() != null) {

            VerifyUserExistance();

        }*/
    }

    private void VerifyUserExistance() {

     /*   if (pref.getData(SharedPreference.userState, getApplicationContext()) != null
                && pref.getData(SharedPreference.userState, getApplicationContext()).equals(Const.profileStoredUserStored)){
            sendUserToStartActivity();
        }else if (pref.getData(SharedPreference.userState, getApplicationContext()) != null
                && pref.getData(SharedPreference.userState, getApplicationContext()).equals(Const.verifiedUserState)) {
            checkUserOnline();
        }*/


        if (pref.getData(SharedPreference.logging, getApplicationContext()) != null
                && pref.getData(SharedPreference.logging, getApplicationContext()).equals(Const.loggedIn)) {

            if (pref.getData(SharedPreference.isProfileSet, getApplicationContext()) != null
                    && pref.getData(SharedPreference.isProfileSet, getApplicationContext()).equals(Const.profileSet)) {

                sendUserToStartActivity();
            } else {
                checkUserOnline();
                // SendUserToSetProfileActivity();
            }
        }

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
                    SendUserToSetProfileActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToJoinGroupActivity() {
        Intent intent=new Intent(RegisterActivity.this, JoinGroupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void sendUserToStartActivity() {

        Intent intent=new Intent(RegisterActivity.this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void SendUserToSetProfileActivity() {
        Intent intent=new Intent(RegisterActivity.this, SetProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("PreviousActivity", "RegisterActivity");
        startActivity(intent);
        finish();
    }

}