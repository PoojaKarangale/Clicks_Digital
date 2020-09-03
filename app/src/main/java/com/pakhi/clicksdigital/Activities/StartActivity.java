package com.pakhi.clicksdigital.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pakhi.clicksdigital.Fragment.ChatsFragment;
import com.pakhi.clicksdigital.Fragment.EventsFragment;
import com.pakhi.clicksdigital.Fragment.GroupsFragment;
import com.pakhi.clicksdigital.Fragment.HomeFragment;
import com.pakhi.clicksdigital.JoinGroup.JoinGroupActivity;
import com.pakhi.clicksdigital.Profile.ProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    static int REQUEST_CODE=1;
    ViewPager                viewPager;
    ViewPagerAdapter         viewPagerAdapter;
    EventsFragment           eventsFragment;
    GroupsFragment           groupsFragment;
    HomeFragment             homeFragment;
    ChatsFragment            chatsFragment;
    Toolbar                  toolbar;
    TabLayout                tabLayout;
    PermissionsHandling      permissions;
    String                   user_type;
    String                   userID;
    FirebaseDatabaseInstance rootRef;
    private ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        rootRef=FirebaseDatabaseInstance.getInstance();

        permissions=new PermissionsHandling(StartActivity.this);
        requestForPremission();

        SharedPreference pref=SharedPreference.getInstance();
        user_type=pref.getData(SharedPreference.user_type, getApplicationContext());

        userID=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        setupTabLayout();

        profile=findViewById(R.id.profile_activity);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent=new Intent(StartActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
    }

    private void setupTabLayout() {
        homeFragment=new HomeFragment();
        eventsFragment=new EventsFragment();
        groupsFragment=new GroupsFragment();
        chatsFragment=new ChatsFragment();

        tabLayout=findViewById(R.id.tab_layout);
        toolbar=findViewById(R.id.toolbar_start);

        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        viewPager=findViewById(R.id.viewPager);

        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(homeFragment, "");//Home
        viewPagerAdapter.addFragment(groupsFragment, "");//Groups
        viewPagerAdapter.addFragment(chatsFragment, "");//Chat
        viewPagerAdapter.addFragment(eventsFragment, "");//Events

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.home).select();;
        tabLayout.getTabAt(1).setIcon(R.drawable.people);
        tabLayout.getTabAt(2).setIcon(R.drawable.chat);
        tabLayout.getTabAt(3).setIcon(R.drawable.event);
        tabLayout.setTabTextColors(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.white));
        // tabLayout.setSelectedTabIndicator(0);
        //tabLayout.getTabAt(0).select();
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor=ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor=ContextCompat.getColor(getApplicationContext(), R.color.black);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
        /*BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(10);*/
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (viewPager.getCurrentItem() == 0) {

            if (viewPagerAdapter.getItem(0) instanceof HomeFragment) {
                new AlertDialog.Builder(this)

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Closing Activity")
                        .setMessage("Are you sure you want to close this activity?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager fm=getSupportFragmentManager();
            fm.popBackStack();
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.join_new_groups) {
            startActivity(new Intent(this, JoinGroupActivity.class));
        }

        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingActivity.class));
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

      /*  if (currentUser != null) {
            // updateUserStatus("offline");
        }*/
    }

    private void VerifyUserExistance() {
    /*    String currentUserID = mAuth.getCurrentUser().getUid();

         rootRef.getUserRef().child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child(Const.USER_NAME).exists())) {
                    //Toast.makeText(StartActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    SendUserToSetProfileActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        rootRef.getUserRef().child(userID).child("userState")
                .updateChildren(onlineStateMap);

    }

    void requestForPremission() {
        //checking for permissions
        if (!permissions.isPermissionGranted()) {
            //when permissions not granted
            if (permissions.isRequestPermissionable()) {
                //creating alertDialog
                permissions.showAlertDialog(REQUEST_CODE);
            } else {
                permissions.requestPermission(REQUEST_CODE);
            }
        } else {
            //when those permissions are already granted
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //  super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.length > 0) &&
                    (grantResults[0] + grantResults[1] + grantResults[2] + grantResults[3] + grantResults[4]
                            == PackageManager.PERMISSION_GRANTED
                    )
            ) {
                //permission granted

            } else {
                //permission not granted
            }
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        List<Fragment> fragments    =new ArrayList<>();
        List<String>   fragmentTitle=new ArrayList<>();

        ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }

}
