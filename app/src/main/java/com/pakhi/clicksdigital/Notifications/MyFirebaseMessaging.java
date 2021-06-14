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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.Event.EventDetailsActivity;
import com.pakhi.clicksdigital.Event.EventParticipantsActivity;
import com.pakhi.clicksdigital.GroupChat.GroupChatActivity;
import com.pakhi.clicksdigital.GroupChat.MyGroupsAdapter;
import com.pakhi.clicksdigital.HelperClasses.NotificationCountDatabase;
import com.pakhi.clicksdigital.JoinGroup.JoinGroupActivity;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.Profile.ProfileUserRequest;
import com.pakhi.clicksdigital.Topic.TopicRepliesActivity;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static final String CHANNEL_ID  ="WorldDigitalConclave";
    private static final String CHANNEL_NAME="WorldDigitalConclave";
    private static final String WDC = "com.pakhi.clicksdigital";

    Intent resultIntent;

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

        String WDC = "com.pakhi.clicksdigital";
        String sent=remoteMessage.getData().get("sent");
        String user=remoteMessage.getData().get("user");


        SharedPreference pref=SharedPreference.getInstance();
        String currentUser=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null && sent.equals(firebaseUser.getUid())) { // checking sent is equal to current
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
            final String icon=remoteMessage.getData().get("icon");
            final String title=remoteMessage.getData().get("title");
            final String body=remoteMessage.getData().get("body");
            String type=remoteMessage.getData().get("type");
            String sent=remoteMessage.getData().get("sent");
            String topicId = remoteMessage.getData().get("topicId");



            if(type.equals("chat")){

                localFriendsDatabase(user);

                resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
                resultIntent.putExtra(Const.visitUser, user);
                Log.i("userPersonal -----", user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("grpChat")){

                localDatabaseNotification(user);

                resultIntent=new Intent(getApplicationContext(), GroupChatActivity.class);
                resultIntent.putExtra(Const.group_id, user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("topic")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getGroupChatRef().child(user).child(topicId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Message m = snapshot.getValue(Message.class);
                        //Log.i("message---", )
                        resultIntent=new Intent(getApplicationContext(), TopicRepliesActivity.class);
                        resultIntent.putExtra(Const.message, m);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        makeNotiOreoFore(resultIntent,title, body, icon);

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }

            else if (type.equals("like")){

            }
            else if(type.equals("request")){
                resultIntent=new Intent(getApplicationContext(), ProfileUserRequest.class);
                resultIntent.putExtra(Const.visitUser, user);
                resultIntent.putExtra(Const.fromRequest, "req");
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("accepted")){
                resultIntent=new Intent(getApplicationContext(), JoinGroupActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("rejected")){
                resultIntent=new Intent(getApplicationContext(), StartActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("event")){
                final FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();

                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                        resultIntent.putExtra(Const.event, event);
                        //Log.i("Event ")

                        //resultIntent.putExtra(Const.organiser, event.getCreater_id());
                        rootRef.getUserRef().child(event.getCreater_id()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                User organiser = snapshot.getValue(User.class);
                                resultIntent.putExtra(Const.organiser, organiser);
                                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                makeNotiOreoFore(resultIntent, body, icon, title);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            else if(type.equals("participant")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeNotiOreoFore(resultIntent,title, body, icon);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //Event event;


            }
            else if(type.equals("eventPhoto")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeNotiOreoFore(resultIntent,title, body, icon);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }




            // Intent resultIntent = remoteMessage.getNotification().get
            if(!type.equals("topic")&& !type.equals("event")&& !type.equals("participant") && !type.equals("eventPhoto")){
                PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                        0 /* Request code */, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Uri defaultsound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                OreoNotification oreoNotification=new OreoNotification(this);
                Notification.Builder builder=oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, icon); //String.valueOf(R.drawable.logo)

                Random rand = new Random();
                int i = rand.nextInt(1000);

                oreoNotification.getManager().notify(i, builder.build());
            }

        } else {

            Log.e("remoteMessage", remoteMessage.getData().toString());
            final String title=remoteMessage.getData().get("title");
            final String body=remoteMessage.getData().get("body");
            String user=remoteMessage.getData().get("user");
            final String icon=remoteMessage.getData().get("icon");
            String type = remoteMessage.getData().get("type");
            String topicId = remoteMessage.getData().get("topicId");



            if(type.equals("chat")){
                localFriendsDatabase(user);
                resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
                resultIntent.putExtra(Const.visitUser, user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("grpChat")){
                localDatabaseNotification(user);
                resultIntent=new Intent(getApplicationContext(), GroupChatActivity.class);
                resultIntent.putExtra(Const.group_id, user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("topic")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getGroupChatRef().child(user).child(topicId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Message m = snapshot.getValue(Message.class);
                            resultIntent=new Intent(getApplicationContext(), TopicRepliesActivity.class);
                            resultIntent.putExtra(Const.message, m);
                            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            makeNotiBackOreo(resultIntent, body, icon, title);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            else if(type.equals("request")){
                resultIntent=new Intent(getApplicationContext(), ProfileUserRequest.class);
                resultIntent.putExtra(Const.visitUser, user);
                resultIntent.putExtra(Const.fromRequest, "req");
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("accepted")){
                resultIntent=new Intent(getApplicationContext(), JoinGroupActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("rejected")){
                resultIntent=new Intent(getApplicationContext(), StartActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }


            else if(type.equals("event")){
                final FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();

                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                        resultIntent.putExtra(Const.event, event);
                        //Log.i("Event ")

                        //resultIntent.putExtra(Const.organiser, event.getCreater_id());
                        rootRef.getUserRef().child(event.getCreater_id()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                User organiser = snapshot.getValue(User.class);
                                resultIntent.putExtra(Const.organiser, organiser);
                                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                makeNotiBackOreo(resultIntent, body, icon, title);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            else if(type.equals("participant")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeNotiBackOreo(resultIntent, body, icon, title);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //Event event;


            }
            else if(type.equals("eventPhoto")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeNotiBackOreo(resultIntent,title, body, icon);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }


            if(!type.equals("topic") && !type.equals("event") && !type.equals("participant") && !type.equals("eventPhoto")){

                PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                        0 /* Request code */, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Uri defaultsound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                OreoNotification oreoNotification=new OreoNotification(this);
                Notification.Builder builder=oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, icon); //String.valueOf(R.drawable.logo)

                NotificationManager notificationManager=
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Random rand = new Random();
                int i = rand.nextInt(1000);

                oreoNotification.getManager().notify(i, builder.build());
            }
        }
    }

    private void localFriendsDatabase(final String user) {
        FirebaseDatabaseInstance rootRef = FirebaseDatabaseInstance.getInstance();
        SharedPreference pref=SharedPreference.getInstance();
        String currentUser=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef.getUserRef().child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if(!snapshot.child("UserIsIn").getValue().toString().equals(user)){
                    NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(MyFirebaseMessaging.this);

                    if((notificationCountDatabase.getSqliteUser_data(Const.grpOrUserID, user) == null)){
                        Log.i("New Contact",user);
                        HashMap<String, String> hmap = new HashMap<>();
                        hmap.put(Const.grpOrUserID, user);
                        Log.i("before adding msg --", notificationCountDatabase.getSqliteUser_data(Const.number,user));
                        hmap.put(Const.number, "1");
                        hmap.put(Const.mute, "false");
                        notificationCountDatabase.insertData(hmap);


                    }else {
                        Log.i("Old Contact", user);
                        HashMap<String, String> hmap = new HashMap<>();
                        hmap.put(Const.grpOrUserID, user);
                        Log.i("before adding msg --", notificationCountDatabase.getSqliteUser_data(Const.number,user));
                        hmap.put(Const.number, String.valueOf(Integer.parseInt(notificationCountDatabase.getSqliteUser_data(Const.number, user))+1));
                        hmap.put(Const.mute, notificationCountDatabase.getSqliteUser_data(Const.mute, user));
                        notificationCountDatabase.updateDataNotification(hmap);

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });


    }

    private void localDatabaseNotification(final String user) {
        FirebaseDatabaseInstance rootRef = FirebaseDatabaseInstance.getInstance();
        SharedPreference pref=SharedPreference.getInstance();
        String currentUser=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        rootRef.getUserRef().child(currentUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if(!snapshot.child("UserIsIn").getValue().toString().equals(user)){
                    NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(MyFirebaseMessaging.this);
                    HashMap<String, String> hmap = new HashMap<>();
                    hmap.put(Const.grpOrUserID, user);
                    Log.i("before adding msg --", notificationCountDatabase.getSqliteUser_data(Const.number,user));
                    hmap.put(Const.number, String.valueOf(Integer.parseInt(notificationCountDatabase.getSqliteUser_data(Const.number, user))+1));
                    hmap.put(Const.mute, notificationCountDatabase.getSqliteUser_data(Const.mute, user));
                    notificationCountDatabase.updateDataNotification(hmap);
                    MyGroupsAdapter myGroupsAdapter = new MyGroupsAdapter();
                    myGroupsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });

    }

    private void makeNotiBackOreo(Intent resultIntent, String body, String icon, String title) {
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultsound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification=new OreoNotification(this);
        Notification.Builder builder=oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, icon); //String.valueOf(R.drawable.logo)

        NotificationManager notificationManager=
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Random rand = new Random();
        int i = rand.nextInt(1000);

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void makeNotiOreoFore(Intent resultIntent, String title, String body, String icon) {
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultsound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification=new OreoNotification(this);
        Notification.Builder builder=oreoNotification.getOreoNotification(title, body, pendingIntent, defaultsound, icon); //String.valueOf(R.drawable.logo)

        Random rand = new Random();
        int i = rand.nextInt(1000);

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void sendNotification(final RemoteMessage remoteMessage) {

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
            final String user=remoteMessage.getData().get("user");
            final String icon=remoteMessage.getData().get("icon");
            final String title=remoteMessage.getData().get("title");
            final String body=remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type");
            String topicId = remoteMessage.getData().get("topicId");

            if(type.equals("chat")){
                localFriendsDatabase(user);
                resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
                resultIntent.putExtra(Const.visitUser, user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("grpChat")){
                localDatabaseNotification(user);
                resultIntent=new Intent(getApplicationContext(), GroupChatActivity.class);
                resultIntent.putExtra(Const.group_id, user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("topic")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getGroupChatRef().child(user).child(topicId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Message m = snapshot.getValue(Message.class);
                        resultIntent=new Intent(getApplicationContext(), TopicRepliesActivity.class);
                        resultIntent.putExtra(Const.message, m);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeOtherFore(resultIntent, body, title, icon);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
            else if(type.equals("request")){
                resultIntent=new Intent(getApplicationContext(), ProfileUserRequest.class);
                resultIntent.putExtra(Const.visitUser, user);
                resultIntent.putExtra(Const.fromRequest, "req");
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("accepted")){
                resultIntent=new Intent(getApplicationContext(), JoinGroupActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("rejected")){
                resultIntent=new Intent(getApplicationContext(), StartActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            else if(type.equals("event")){
                final FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();

                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                        resultIntent.putExtra(Const.event, event);
                        //Log.i("Event ")

                        //resultIntent.putExtra(Const.organiser, event.getCreater_id());
                        rootRef.getUserRef().child(event.getCreater_id()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                User organiser = snapshot.getValue(User.class);
                                resultIntent.putExtra(Const.organiser, organiser);
                                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                makeOtherFore(resultIntent, body, icon, title);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            else if(type.equals("participant")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeOtherFore(resultIntent, body, title, icon);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //Event event;


            }
            else if (type.equals("eventPhoto")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeOtherFore(resultIntent, body, title, icon);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            if(!type.equals("topic") && !type.equals("event") && !type.equals("participant") && !type.equals("eventPhoto")){

                PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                        0 /* Request code */, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis()) // check params
                        .setSmallIcon(Integer.parseInt(icon))
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setNumber(10)
                        .setTicker("World Digital Conclave")
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentInfo("Info")
                        .setGroup(WDC)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                ;
                Random rand = new Random();
                int i = rand.nextInt(1000);
                notificationManager.notify(i, notificationBuilder.build()); // generating
            }

        } else {
            Log.e("remoteMessagebackground", remoteMessage.getData().toString());
            //  Map data = remoteMessage.getData();
//            String title = data.get("title");
//            String body = data.get("body");
            final String user=remoteMessage.getData().get("user");
            final String icon=remoteMessage.getData().get("icon");
            final String title=remoteMessage.getData().get("title");
            final String body=remoteMessage.getData().get("body");
            String type = remoteMessage.getData().get("type");
            String topicId = remoteMessage.getData().get("topicId");


            if(type.equals("chat")){
                localFriendsDatabase(user);
                resultIntent=new Intent(getApplicationContext(), ChatActivity.class);
                resultIntent.putExtra(Const.visitUser, user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("grpChat")){
                localDatabaseNotification(user);
                resultIntent=new Intent(getApplicationContext(), GroupChatActivity.class);
                resultIntent.putExtra(Const.group_id, user);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("topic")){
                Log.i("user val----", user);
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getGroupChatRef().child(user).child(topicId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Message m = snapshot.getValue(Message.class);
                        resultIntent=new Intent(getApplicationContext(), TopicRepliesActivity.class);
                        resultIntent.putExtra(Const.message, m);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeOtherBack(resultIntent, body, icon, title);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            else if(type.equals("request")){
                resultIntent=new Intent(getApplicationContext(), ProfileUserRequest.class);
                resultIntent.putExtra(Const.visitUser, user);
                resultIntent.putExtra(Const.fromRequest, "req");
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("accepted")){
                resultIntent=new Intent(getApplicationContext(), JoinGroupActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("rejected")){
                resultIntent=new Intent(getApplicationContext(), StartActivity.class);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            else if(type.equals("reply")){

            }
            else if(type.equals("event")){
                final FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();

                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                        resultIntent.putExtra(Const.event, event);
                        //Log.i("Event ")

                        //resultIntent.putExtra(Const.organiser, event.getCreater_id());
                        rootRef.getUserRef().child(event.getCreater_id()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                User organiser = snapshot.getValue(User.class);
                                resultIntent.putExtra(Const.organiser, organiser);
                                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                makeOtherBack(resultIntent, body, icon, title);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            else if(type.equals("participant")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeOtherBack(resultIntent, body, icon, title);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //Event event;


            }
            else if(type.equals("eventPhoto")){
                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();
                rootRef.getEventRef().child(user).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Event event = snapshot.getValue(Event.class);
                        resultIntent = new Intent(getApplicationContext(), EventParticipantsActivity.class);
                        resultIntent.putExtra(Const.Event, event);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        makeOtherBack(resultIntent, body, icon, title);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }



            if(!type.equals("topic") && !type.equals("event") && !type.equals("participant")&& !type.equals("eventPhoto")){

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
                        .setGroup(WDC)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentInfo("Info");
                Random rand = new Random();
                int i = rand.nextInt(1000);
                notificationManager.notify(i, notificationBuilder.build());
            }
        }
    }

    private void makeOtherBack(Intent resultIntent, String body, String icon, String title) {
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
                .setGroup(WDC)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentInfo("Info");
        Random rand = new Random();
        int i = rand.nextInt(1000);
        notificationManager.notify(i, notificationBuilder.build());
    }

    private void makeOtherFore(Intent resultIntent, String body, String title, String icon) {
        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis()) // check params
                .setSmallIcon(Integer.parseInt(icon))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setNumber(10)
                .setTicker("World Digital Conclave")
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info")
                .setGroup(WDC)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        ;
        Random rand = new Random();
        int i = rand.nextInt(1000);
        notificationManager.notify(i, notificationBuilder.build()); // generating
    }
}