package com.pakhi.clicksdigital.Notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.List;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static final String CHANNEL_ID  ="WorldDigitalConclave";
    private static final String CHANNEL_NAME="WorldDigitalConclave";

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground=true;
        ActivityManager am=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses=am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground=false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo=am.getRunningTasks(1);
            ComponentName componentInfo=taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground=false;
            }
        }

        return isInBackground;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sent=remoteMessage.getData().get("sent");
        String user=remoteMessage.getData().get("user");

        SharedPreference pref=SharedPreference.getInstance();
        String currentUser=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) {
            if (!currentUser.equals(user)) {
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Build.VERSION_CODES.O
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // Build.VERSION_CODES.O
                    sendOreoNotification(remoteMessage);
                } else {
                    sendNotification(remoteMessage);
                }
            }
        }
    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
     /*   String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
         intent.putExtra("visit_user_id",user);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                defaultSound, icon);

        int i = 0;
        if (j > 0){
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());*/

        if (!isAppIsInBackground(getApplicationContext())) {
            //foreground app
            Log.e("remoteMessage", remoteMessage.getData().toString());
            String user=remoteMessage.getData().get("user");
            String icon=remoteMessage.getData().get("icon");
            String title=remoteMessage.getData().get("title");
            String body=remoteMessage.getData().get("body");
            // Intent resultIntent = remoteMessage.getNotification().get
            Intent resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
            resultIntent.putExtra("visit_user_id", user);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultsound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            OreoNotification oreoNotification=new OreoNotification(this);
            Notification.Builder builder=oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, icon); //String.valueOf(R.drawable.logo)

            int i=0;
            oreoNotification.getManager().notify(i, builder.build());
        } else {
            Log.e("remoteMessage", remoteMessage.getData().toString());
            String title=remoteMessage.getData().get("title");
            String body=remoteMessage.getData().get("body");
            String user=remoteMessage.getData().get("user");
            String icon=remoteMessage.getData().get("icon");
            Intent resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
            resultIntent.putExtra("visit_user_id", user);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Uri defaultsound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            OreoNotification oreoNotification=new OreoNotification(this);
            Notification.Builder builder=oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, icon); //String.valueOf(R.drawable.logo)

            NotificationManager notificationManager=
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int i=0;
            oreoNotification.getManager().notify(i, builder.build());
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {

       /* String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        noti.notify(i, builder.build());*/

        if (!isAppIsInBackground(getApplicationContext())) {
            //foreground app
            Log.e("remoteMessageforeground", remoteMessage.getData().toString());
//            String title = remoteMessage.getNotification().getTitle();
//            String body = remoteMessage.getNotification().getBody();
            String user=remoteMessage.getData().get("user");
            String icon=remoteMessage.getData().get("icon");
            String title=remoteMessage.getData().get("title");
            String body=remoteMessage.getData().get("body");
            Intent resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
            resultIntent.putExtra("visit_user_id", user);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(Integer.parseInt(icon))
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(10)
                    .setTicker("World Digital Conclave")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentInfo("Info")
                    .setContentIntent(pendingIntent);
            ;
            notificationManager.notify(1, notificationBuilder.build());
        } else {
            Log.e("remoteMessagebackground", remoteMessage.getData().toString());
            //  Map data = remoteMessage.getData();
//            String title = data.get("title");
//            String body = data.get("body");
            String user=remoteMessage.getData().get("user");
            String icon=remoteMessage.getData().get("icon");
            String title=remoteMessage.getData().get("title");
            String body=remoteMessage.getData().get("body");
            Intent resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
            resultIntent.putExtra("visit_user_id", user);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(Integer.parseInt(icon))
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setNumber(10)
                    .setTicker("World Digital Conclave")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(pendingIntent)
                    .setContentInfo("Info");
            notificationManager.notify(1, notificationBuilder.build());
        }
    }
}
