package com.pakhi.clicksdigital.FragmentsInSetting;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Notification;

public class NotificationsFragment extends Fragment {


    private View                                                      view;
    private com.google.android.material.switchmaterial.SwitchMaterial simpleSwitch;
    private TextView                                                  subText;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_notifications, container, false);
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
        return view;
    }

    private void initializingFields() {
        simpleSwitch=view.findViewById(R.id.switch_notify);
        subText=view.findViewById(R.id.subtext);
    }
}
