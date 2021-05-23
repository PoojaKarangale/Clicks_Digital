package com.pakhi.clicksdigital.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;

public class SettingActivity extends AppCompatActivity {
    SettingListAdapter listAdapter;
    private ListView listView;
    private int[]    imagesForListView={
            R.drawable.notifications,
            R.drawable.change_number,
            R.drawable.contact_us,
            R.drawable.info
    };
    private String[] titleForListView ={
            Const.Notifications,
            Const.Change_number,
            Const.Contact_us,
            Const.About};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initializingFields();
        settingUpList();
    }

    private void settingUpList() {
        listAdapter=new SettingListAdapter(this, titleForListView, imagesForListView);
        setUpListItems();
        setUpListOnClick();
        listView.setAdapter(listAdapter);
    }

    private void setUpListOnClick() {
        listView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action=event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });


    }

    private void setUpListItems() {


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Fragment fragment=null;
                switch (position) {
                    case 0:
                        //fragment=new NotificationsFragment();
                        Intent intent = new Intent(SettingActivity.this, NotificationFragmentReplaceActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        //fragment=new ChangeMyNumberFragment();
                        Intent intent2 = new Intent(SettingActivity.this, ChangeMyNumberFragmentReplacementActivity.class);
                        startActivity(intent2);

                        //startActivity(new Intent(SettingActivity.this, ChangeMyNumberFragmentReplacementActivity.class));
                        break;
                    case 2:
                        //fragment=new ContactUsFragment();
                        startActivity(new Intent(SettingActivity.this, ContactusFragmentReplacementActivity.class));
                        break;
                    case 3:
                        //fragment=new AppInfoFragment();
                        startActivity(new Intent(SettingActivity.this, AppInfoFragmentReplacementActivity.class));
                        break;
                }
                //listView.setOnItemClickListener(null);

            }
        });

    }


    private void initializingFields() {
        listView=findViewById(R.id.list_view);
    }
}