package com.pakhi.clicksdigital;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

public  class Internet {
    Context context;
    ConnectivityManager cm;
    NetworkInfo activeNetwork;
    boolean isConnected;
    public Internet(){

    }

    public  boolean checkConnection(){

         cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

         activeNetwork = cm.getActiveNetworkInfo();
         if(activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting()){
             return true;
         }else {
             return isConnected;}
    }
    public String connectionString(){
        return "Please Check your Internet Connection to continue";

    }
}
