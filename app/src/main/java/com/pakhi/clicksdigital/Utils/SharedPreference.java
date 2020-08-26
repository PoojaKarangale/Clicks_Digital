package com.pakhi.clicksdigital.Utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreference {

    public static final String           phone          ="PHONE";
    public static final String           sharedPrefName ="SHARED_PREFERENCES";
    public static final String           logging        ="LOGGING";
    public static final String           user_type      ="user_type";
    public static       String           isProfileSet   ="isProfileSet";
    public static       String           currentUserId  ="currentUserId";
    public static       String           userState      ="userState";
    // static variable single_instance of type Singleton
    private static      SharedPreference single_instance=null;

    private SharedPreference() {
    }

    // static method to create instance of Singleton class
    public static SharedPreference getInstance() {
        if (single_instance == null)
            single_instance=new SharedPreference();
        return single_instance;
    }

    public boolean saveData(String key, String data, Context context) {
        SharedPreferences prefs=context.getSharedPreferences(sharedPrefName, 0);
        SharedPreferences.Editor prefsEditor=prefs.edit();
        prefsEditor.putString(key, data);
        prefsEditor.apply();
        return true;
    }

    public String getData(String key, Context context) {
        SharedPreferences prefs=context.getSharedPreferences(sharedPrefName, 0);
        return prefs.getString(key, null);
    }

}
