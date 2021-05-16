package com.pakhi.clicksdigital.Utils;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class UserStateStatus {

    public static void setUserStatus(String userId, String state){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateUserStatus(userId,state);
        }
    }
    private static void updateUserStatus(String userId,String state) {
        FirebaseDatabaseInstance rootRef=FirebaseDatabaseInstance.getInstance();

        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put(Const.time, saveCurrentTime);
        onlineStateMap.put(Const.date, saveCurrentDate);
        onlineStateMap.put(Const.state, state);

        rootRef.getUserRef().child(userId).child(ConstFirebase.userState)
                .updateChildren(onlineStateMap);
    }
}