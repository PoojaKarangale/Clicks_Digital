package com.pakhi.clicksdigital.Utils;

import android.content.Context;
import android.widget.Toast;

public class ToastClass {
    public static void makeText(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
