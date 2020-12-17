package com.pakhi.clicksdigital.FragmentsInSetting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Notification;

public class NotificationFragmentReplaceActivity extends AppCompatActivity {
    private View view;
    private com.google.android.material.switchmaterial.SwitchMaterial simpleSwitch;
    private TextView subText;
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
        // simpleSwitch.setChecked(true);

    }

    public void initializingFields() {
        simpleSwitch=findViewById(R.id.switch_notify);
        subText=findViewById(R.id.subtext);
    }
}
