package com.pakhi.clicksdigital.RegisterLogin;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.JoinGroup.JoinGroupActivity;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Profile.SetProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth             firebaseAuth;
    // DatabaseReference RootRef;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    private String            number;
    private CountryCodePicker ccp;
    private EditText          mobileNo_reg;
    UserDatabase db;
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
        i.putExtra(ConstFirebase.MO_NUMBER, number);
        i.putExtra(ConstFirebase.prevActivity, ConstFirebase.registerActivity);
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

        /*if (pref.getData(SharedPreference.userState, getApplicationContext()) != null
                && pref.getData(SharedPreference.userState, getApplicationContext()).equals(Const.profileStoredUserStored)){
            sendUserToStartActivity();
        }else if (pref.getData(SharedPreference.userState, getApplicationContext()) != null
                && pref.getData(SharedPreference.userState, getApplicationContext()).equals(Const.verifiedUserState)) {
            checkUserOnline();
        }*/

        if (pref.getData(SharedPreference.isPhoneVerified, getApplicationContext()) != null
                && pref.getData(SharedPreference.isPhoneVerified, getApplicationContext()).equals(ConstFirebase.isPhoneVerified)
        ) {
            if (pref.getData(SharedPreference.isProfileSet, getApplicationContext()) != null
                    && pref.getData(SharedPreference.isProfileSet, getApplicationContext()).equals(ConstFirebase.profileSet)) {
                //start Activity
                sendUserToStartActivity();
            }else {
                //check online
                checkUserOnline();
            }
        }

       /* if (pref.getData(SharedPreference.logging, getApplicationContext()) != null
                && pref.getData(SharedPreference.logging, getApplicationContext()).equals(Const.loggedIn)) {

            if (pref.getData(SharedPreference.isProfileSet, getApplicationContext()) != null
                    && pref.getData(SharedPreference.isProfileSet, getApplicationContext()).equals(Const.profileSet)) {

                sendUserToStartActivity();
            } else {
                checkUserOnline();
                // SendUserToSetProfileActivity();
            }
        }*/
    }

    private void checkUserOnline() {
        // rootRef.getUserRef()
        String currentUserID=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child(ConstFirebase.USER_NAME).exists())) {
                    User user = dataSnapshot.getValue(User.class);
                    addCurrentUserSqliteData(user);
                    //save data of profile updated because we won't go to profile setting but profil is already set stored online
                    pref.saveData(SharedPreference.isProfileSet, ConstFirebase.profileSet, getApplicationContext());
                    pref.saveData(SharedPreference.user_type, dataSnapshot.child(ConstFirebase.USER_TYPE).getValue().toString(), getApplicationContext());

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
    private HashMap<String, String> putDataIntoHashMap(User user){
        final HashMap<String, String> userItems=new HashMap<>();

        userItems.put(ConstFirebase.USER_ID, user.getUser_id());
        userItems.put(ConstFirebase.USER_NAME, user.getUser_name());
        userItems.put(ConstFirebase.USER_BIO, user.getUser_id());
        userItems.put(ConstFirebase.IMAGE_URL, user.getImage_url());
        userItems.put(ConstFirebase.USER_TYPE, user.getUser_type());
        userItems.put(ConstFirebase.CITY, user.getCity());
        userItems.put("expectations_from_us", user.getExpectations_from_us());
        userItems.put("experiences", user.getExperiences());
        userItems.put("gender", user.getGender());
        userItems.put("number", user.getNumber());
        userItems.put("offer_to_community", user.getOffer_to_community());
        userItems.put("speaker_experience", user.getSpeaker_experience());
        userItems.put("email", user.getUser_email());
        userItems.put("weblink", user.getWeblink());
        userItems.put("working", user.getWeblink());
        userItems.put("last_name", user.getLast_name());
        userItems.put("company", user.getCompany());
        return userItems;
    }

    private void addCurrentUserSqliteData(User user) {
        db=new UserDatabase(this);
        HashMap<String, String> userItems = putDataIntoHashMap(user);
        SQLiteDatabase sqlDb=db.getWritableDatabase();
        db.onUpgrade(sqlDb, 0, 1);
        db.insertData(userItems);
        //  db.close();
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
        intent.putExtra(ConstFirebase.PreviousActivity, "RegisterActivity");
        startActivity(intent);
        finish();
    }

}