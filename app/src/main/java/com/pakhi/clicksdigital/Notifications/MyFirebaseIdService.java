package com.pakhi.clicksdigital.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        String refreshToken=FirebaseInstanceId.getInstance().getToken();
        if (firebaseUser != null) {
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {

        SharedPreference pref=SharedPreference.getInstance();
        String uid=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        FirebaseDatabaseInstance rootRef=FirebaseDatabaseInstance.getInstance();
        Token token=new Token(refreshToken);
        rootRef.getTokensRef().child(uid).setValue(token);
    }
}
