package com.pakhi.clicksdigital.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.pakhi.clicksdigital.R;

public class Notification {
    public static boolean isNotificationOn=false;

    public static void autoCancel(Context context, String title, String message, Intent intent, int notificationID) {

        PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context, "WorldDigtal") //.CHANNEL_ID
                .setSmallIcon(R.mipmap.wdc_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager mNotificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationID, builder.build());
    }


    public static void largeTextArea(Context context, String title, String shortMessage, String largeText) {

        NotificationCompat.Builder builder=new NotificationCompat.Builder(context) //.CHANNEL_ID
                .setSmallIcon(R.mipmap.wdc_icon)
                .setContentTitle(title)
                .setContentText(shortMessage)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(largeText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

    }


    /*Intent intent = new Intent(this, AlertDetails.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

   // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
      builder.setContentIntent(pendingIntent)
       NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // notificationId is a unique int for each notification that you must define
    notificationManager.notify(notificationId,builder.build());*/


}
