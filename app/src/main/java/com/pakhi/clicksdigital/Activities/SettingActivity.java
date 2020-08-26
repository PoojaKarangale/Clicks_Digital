package com.pakhi.clicksdigital.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.pakhi.clicksdigital.Adapter.SettingListAdapter;
import com.pakhi.clicksdigital.FragmentsInSetting.AppInfoFragment;
import com.pakhi.clicksdigital.FragmentsInSetting.ChangeMyNumberFragment;
import com.pakhi.clicksdigital.FragmentsInSetting.ContactUsFragment;
import com.pakhi.clicksdigital.FragmentsInSetting.NotificationsFragment;
import com.pakhi.clicksdigital.R;

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
            "Notifications",
            "Change number",
            "Contact us",
            "About"};

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
                Fragment fragment=null;
                switch (position) {
                    case 0:
                        fragment=new NotificationsFragment();
                        break;
                    case 1:
                        fragment=new ChangeMyNumberFragment();
                        break;
                    case 2:
                        fragment=new ContactUsFragment();
                        break;
                    case 3:
                        fragment=new AppInfoFragment();
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                }
                //listView.setOnItemClickListener(null);
                updatePage(fragment);
            }
        });

    }

    private void updatePage(Fragment fragment) {
        FragmentTransaction transaction=getSupportFragmentManager()
                .beginTransaction();
        transaction.addToBackStack(null);
        transaction.add(R.id.fragmentContainer, fragment, "TAG_FRAGMENT");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        Log.d("TESTINGSTART", "-----------frag count------" + getSupportFragmentManager().getBackStackEntryCount());

    }

    private void initializingFields() {
        listView=findViewById(R.id.list_view);
    }
}
