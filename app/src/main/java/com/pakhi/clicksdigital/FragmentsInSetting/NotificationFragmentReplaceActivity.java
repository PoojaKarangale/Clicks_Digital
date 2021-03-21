package com.pakhi.clicksdigital.FragmentsInSetting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.HomePageTopicAdapter;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.Notification;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.ArrayList;

public class NotificationFragmentReplaceActivity extends AppCompatActivity {
    private View view;
    private com.google.android.material.switchmaterial.SwitchMaterial simpleSwitch;
    private TextView subText;
    RecyclerView recView;
    NotificationAdapter notificationAdapter;
    ArrayList<String> notificationList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_fragment_replace);

        //getSupportActionBar().setTitle("Notifications");
        initializingFields();
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Notification.isNotificationOn=true;
                    subText.setText("If you uncheck you won't receive any notifications \nfrom this application");
                    Log.d("NOTIFICATIONTEST", subText.getText().toString() + Notification.isNotificationOn);
                } else {
                    Notification.isNotificationOn=false;
                    subText.setText("To get notification from the app check this");
                    Log.d("NOTIFICATIONTEST", subText.getText().toString() + Notification.isNotificationOn);
                }
            }
        });

        SharedPreference pref = SharedPreference.getInstance();
        final String currentUserId = pref.getData(SharedPreference.currentUserId, NotificationFragmentReplaceActivity.this);

        recView  = (RecyclerView) findViewById(R.id.rec_view_notif);
        recView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationFragmentReplaceActivity.this);
        recView.setLayoutManager(linearLayoutManager);

        FirebaseDatabaseInstance rootRef = FirebaseDatabaseInstance.getInstance();
        notificationAdapter = new NotificationAdapter(NotificationFragmentReplaceActivity.this, notificationList);
        recView.setAdapter(notificationAdapter);

        rootRef.getNotificationRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                notificationAdapter.notifyDataSetChanged();
                for(DataSnapshot snap : snapshot.getChildren()){
                    if(snap.child(ConstFirebase.notificationRecieverID).getValue().toString().equals(currentUserId)){
                        Log.i("key----", snap.getKey());
                        notificationList.add(0, snap.getKey());

                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    public void initializingFields() {
        simpleSwitch=findViewById(R.id.switch_notify);
        subText=findViewById(R.id.subtext);
    }
}
