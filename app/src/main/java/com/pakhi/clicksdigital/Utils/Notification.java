package com.pakhi.clicksdigital.Utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.APIService;
import com.pakhi.clicksdigital.Notifications.Client;
import com.pakhi.clicksdigital.Notifications.Data;
import com.pakhi.clicksdigital.Notifications.MyResponse;
import com.pakhi.clicksdigital.Notifications.Sender;
import com.pakhi.clicksdigital.Notifications.Token;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notification {
    public static boolean isNotificationOn = false;

    public static void sendPersonalNotifiaction(final Context applicationContext, final String messageSenderID, final String receiverID, final String body, final String title) {
        DatabaseReference tokens = FirebaseDatabaseInstance.getInstance().getTokensRef();
        Query query = tokens.orderByKey().equalTo(receiverID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);

                    Intent resultIntent = new Intent(applicationContext, ChatActivity.class);
                    resultIntent.putExtra("visit_user_id", receiverID);
                    resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                    Data data = new Data(messageSenderID, R.drawable.logo, body, title,
                            receiverID);
                  /*  Data data = new Data(messageSenderID, R.drawable.logo, body, title,
                            receiverID,resultIntent);*/
















                    Sender sender = new Sender(data, token.getToken());
                    APIService apiService;
                    apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            //Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


  /*  public static void autoCancel(Context context, String title, String message, Intent intent, int notificationID) {

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

*/
    /*Intent intent = new Intent(this, AlertDetails.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

   // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
      builder.setContentIntent(pendingIntent)
       NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // notificationId is a unique int for each notification that you must define
    notificationManager.notify(notificationId,builder.build());*/


}
