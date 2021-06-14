package com.pakhi.clicksdigital.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Fragment.ChatsFragment;
import com.pakhi.clicksdigital.Fragment.EventsFragment;
import com.pakhi.clicksdigital.Fragment.GroupsFragment;
import com.pakhi.clicksdigital.Fragment.HomeFragment;
import com.pakhi.clicksdigital.Fragment.InternetCheckFragment;
import com.pakhi.clicksdigital.HelperClasses.NotificationCountDatabase;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.JoinGroup.JoinGroupActivity;
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Profile.ProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Settings.SettingActivity;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.ShareApp;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.UserStateStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    static int REQUEST_CODE = 1;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    EventsFragment eventsFragment;
    GroupsFragment groupsFragment;
    HomeFragment homeFragment;
    ChatsFragment chatsFragment;
    Toolbar toolbar;
    TabLayout tabLayout;
    PermissionsHandling permissions;
    String user_type;
    String userID;
    FirebaseDatabaseInstance rootRef;
    private ImageView profile;
    ArrayList<String> groupsOfUsers = new ArrayList<>();
    ArrayList<String> friendsOfUser = new ArrayList<>();
    // to check if we are connected to Network
    boolean isConnected = true;
    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        checkConnectivity();

        rootRef = FirebaseDatabaseInstance.getInstance();

        permissions = new PermissionsHandling(StartActivity.this);
        requestForPremission();

        SharedPreference pref = SharedPreference.getInstance();
        user_type = pref.getData(SharedPreference.user_type, getApplicationContext());

        userID = pref.getData(SharedPreference.currentUserId, getApplicationContext());
        setupTabLayout();
        putDataIntoHashMap();
        setupBadgeCount();

        try{
            NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(StartActivity.this);

                SQLiteDatabase db = notificationCountDatabase.getWritableDatabase();
                notificationCountDatabase.onUpgrade(db,0,1);
                if(!notificationCountDatabase.checkTableIfExists()){
                    insertDataIntoNotificationTable();
                    insertDataIntoNotificationTableForFriends();
                }




        }catch (Exception e){}
        /*NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(StartActivity.this);
        Log.i("came here", "here");
        //SQLiteDatabase db = notificationCountDatabase.getReadableDatabase();
        String grpName = notificationCountDatabase.getSqliteUser_data(Const.grpOrUserID,"-MTRZMoFEivrWqxUFJp0");
        String grpMsgs = notificationCountDatabase.getSqliteUser_data(Const.grpOrUserID,"-MTRZMoFEivrWqxUFJp0");
        String grpMute = notificationCountDatabase.getSqliteUser_data(Const.grpOrUserID,"-MTRZMoFEivrWqxUFJp0");


        Log.i("grpName---", grpName);
        Log.i("grpMsg---",grpMsgs);
        Log.i("grpMute", grpMute);*/
        //NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(StartActivity.this);


            //notificationCountDatabase.onUpgrade(db,0,1);
            //notificationCountDatabase.onCreate(db);


        UserDatabase userDatabase = new UserDatabase(StartActivity.this);

        String imgUrl = userDatabase.getSqliteUser_data(ConstFirebase.IMAGE_URL);

        profile = findViewById(R.id.profile_activity);

        Glide.with(getApplicationContext()).load(imgUrl)
                .transform(new CenterCrop(), new RoundedCorners(50))
                .placeholder(R.drawable.nav_profile).into(profile);

/*rootRef.getUserRef().addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
            if (dataSnapshot.child(ConstFirebase.USER_DETAILS).exists()){
                rootRef.getUserRef().child(dataSnapshot.getKey()).child(ConstFirebase.USER_DETAILS).child(ConstFirebase.webLink).setValue("https://www.linkedin.com/in/aditchouhan/");
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});*/
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(StartActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
    }

    public void setupBadgeCount() {
        final int[] grpNotifications = {0};
        final NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(getApplicationContext());
        rootRef.getUserRef().child(userID).child(ConstFirebase.groups).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    String num = notificationCountDatabase.getSqliteUser_data(Const.number, snap.getKey());
                     if((num.equals("0") )){

                    }
                    else {
                        ++grpNotifications[0];
                    }
                }
                BadgeDrawable badgeDrawable = tabLayout.getTabAt(1).getOrCreateBadge();
                if(!(grpNotifications[0]==0)){
                    badgeDrawable.setVisible(true);
                    badgeDrawable.setNumber(grpNotifications[0]);
                }else {
                    badgeDrawable.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
        final int[] personamNotifications = {0};
        rootRef.getMessagesListRef().child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    String num = notificationCountDatabase.getSqliteUser_data(Const.number, snap.getKey());
                     if((num.equals("0") )){

                    }
                    else {
                        ++personamNotifications[0];
                    }
                }
                BadgeDrawable badgeDrawable = tabLayout.getTabAt(2).getOrCreateBadge();
                if(!(personamNotifications[0] ==0)){    badgeDrawable.setVisible(true);
                    badgeDrawable.setNumber(personamNotifications[0]);
                }
                else {
                    badgeDrawable.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void insertDataIntoNotificationTableForFriends() {
        friendsOfUser.clear();
        rootRef.getMessagesListRef().child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if(snapshot.exists()){
                    NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(getApplicationContext());
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.clear();
                    for(DataSnapshot snap : snapshot.getChildren()){
                        hashMap.put(Const.grpOrUserID, snap.getKey());
                        hashMap.put(Const.number,"0");
                        hashMap.put(Const.mute,"false");
                        notificationCountDatabase.insertData(hashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void insertDataIntoNotificationTable() {
        groupsOfUsers.clear();
        rootRef.getUserRef().child(userID).child(ConstFirebase.groups).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                //groupsOfUsers.add(snapshot.);
                if(snapshot.exists()){
                    NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(StartActivity.this);
                    HashMap<String, String> hmap = new HashMap<>();
                    hmap.clear();
                    for(DataSnapshot key : snapshot.getChildren()){
                        hmap.put(Const.grpOrUserID, key.getKey());
                        hmap.put(Const.number,"0");
                        hmap.put(Const.mute,"false");
                        notificationCountDatabase.insertData(hmap);

                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    String TAG_InternetChecks = "InternetIssueFrag";

    public void removeFragment(String TAG) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragment != null) {
            // fragment exist
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    public void addFragment(String TAG) {

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.internetCheckFrame, new InternetCheckFragment(), TAG)
                .commit();

    }

    private void checkConnectivity() {
        //removeFragment(TAG_InternetChecks);
        // here we are getting the connectivity service from connectivity manager
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);

        // Getting network Info
        // give Network Access Permission in Manifest
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        monitoringConnectivity = true;
        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(), connectivityCallback);

        // isConnected is a boolean variable
        // here we check if network is connected or is getting connected
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            // SHOW ANY ACTION YOU WANT TO SHOW
            // WHEN WE ARE NOT CONNECTED TO INTERNET/NETWORK
            Toast.makeText(getApplicationContext(), "internet not available", Toast.LENGTH_SHORT).show();
            addFragment(TAG_InternetChecks);
            // if Network is not connected we will register a network callback to  monitor network
            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(), connectivityCallback);
            monitoringConnectivity = true;
        }
    }

    private ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
            //Toast.makeText(getApplicationContext(), "internet available", Toast.LENGTH_SHORT).show();
            removeFragment(TAG_InternetChecks);
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Toast.makeText(getApplicationContext(), "internet not available", Toast.LENGTH_SHORT).show();
            addFragment(TAG_InternetChecks);

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        checkConnectivity();
    }

    @Override
    protected void onPause() {
        // if network is being moniterd then we will unregister the network callback
        //
        //
        // Toast.makeText(getApplicationContext(), "on pause", Toast.LENGTH_SHORT).show();
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
        super.onPause();
    }

    private void putDataIntoHashMap() {
        final User[] user = {new User()};
        final HashMap<String, String> userItems1;
        rootRef.getUserRef().child(userID).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user[0] = snapshot.getValue(User.class);
                    final HashMap<String, String> userItems = new HashMap<>();

                    userItems.put(ConstFirebase.USER_ID, user[0].getUser_id());
                    userItems.put(ConstFirebase.USER_NAME, user[0].getUser_name());
                    userItems.put(ConstFirebase.USER_BIO, user[0].getUser_bio());
                    userItems.put(ConstFirebase.IMAGE_URL, user[0].getImage_url());
                    userItems.put(ConstFirebase.USER_TYPE, user[0].getUser_type());
                    userItems.put(ConstFirebase.CITY, user[0].getCity());
                    userItems.put(ConstFirebase.expeactations, user[0].getExpectations_from_us());
                    userItems.put(ConstFirebase.expireince, user[0].getExperiences());
                    userItems.put(ConstFirebase.GENDER, user[0].getGender());
                    userItems.put(ConstFirebase.number, user[0].getNumber());
                    userItems.put(ConstFirebase.offerToComm, user[0].getOffer_to_community());
                    userItems.put(ConstFirebase.speakerExp, user[0].getSpeaker_experience());
                    userItems.put(ConstFirebase.email, user[0].getUser_email());
                    userItems.put(ConstFirebase.webLink, user[0].getWeblink());
                    userItems.put(ConstFirebase.working, user[0].getWork_profession());
                    userItems.put(ConstFirebase.last_name, user[0].getLast_name());
                    userItems.put(ConstFirebase.company, user[0].getCompany());
                    userItems.put(ConstFirebase.country, user[0].getCountry());
                    userItems.put(ConstFirebase.getReferral, user[0].getReferal());

                    //HashMap<String, String> userItems = putDataIntoHashMap();
                    UserDatabase db = new UserDatabase(getApplicationContext());
                    SQLiteDatabase sqlDb = db.getWritableDatabase();
                    db.onUpgrade(sqlDb, 1, 2);
                    db.insertData(userItems);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setupTabLayout() {
        homeFragment = new HomeFragment();
        eventsFragment = new EventsFragment();
        groupsFragment = new GroupsFragment();
        chatsFragment = new ChatsFragment();

        tabLayout = findViewById(R.id.tab_layout);
        toolbar = findViewById(R.id.toolbar_start);

        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        viewPager = findViewById(R.id.viewPager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(homeFragment, "");//Home
        viewPagerAdapter.addFragment(groupsFragment, "");//Groups
        viewPagerAdapter.addFragment(chatsFragment, "");//Chat
        viewPagerAdapter.addFragment(eventsFragment, "");//Events

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.home).select();

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
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.white);
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
        //finish();

        if (tabLayout.getSelectedTabPosition() != 0) {
            tabLayout.getTabAt(0).select();
        } else {
            finish();
        }

        /*Log.d("ACTIVITYSTATE","BACK PRESSED "+getClass().getName());

        moveTaskToBack(true);
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        finish();*/

        //System.exit(0);
        /*moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);*/

       /* if (viewPager.getCurrentItem() == 0) {
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
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (!user_type.equals("admin")) {

            getMenuInflater().inflate(R.menu.options_menu, menu);
            menu.getItem(1).setVisible(false);
        } else {
            getMenuInflater().inflate(R.menu.options_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.join_new_groups) {
            Intent joingroupIntent = new Intent(this, JoinGroupActivity.class);
            //joingroupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(joingroupIntent);
        }

        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingActivity.class));
        }

        if (item.getItemId() == R.id.share_app) {

            ShareApp.shareApp(getApplicationContext());
        }

        if (item.getItemId() == R.id.user_request) {

            //  Log.d("User_type","................"+user_type);
            if (user_type.equals("admin")) {
                startActivity(new Intent(this, UserRequestActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "Since you are not admin, you don't have access to this part of the app", Toast.LENGTH_LONG).show();
            }
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ACTIVITYSTATE", "START" + getClass().getName());
        UserStateStatus.setUserStatus(userID, ConstFirebase.onlineStatus);

        rootRef.getUserRef().addValueEventListener(new ValueEventListener() {
            ArrayList<String> myList = new ArrayList<>();

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    myList.add(snap.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setupBadgeCount();
    }

    private void setFile(ArrayList<String> myList) {
        for (String str : myList) {
            try {
                rootRef.getUserRef().child(str).child("groups").removeValue();
            } catch (Exception e) {
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("ACTIVITYSTATE", "DESTROY" + getClass().getName());
        UserStateStatus.setUserStatus(userID, ConstFirebase.onlineStatus);
        System.exit(0);
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

        List<Fragment> fragments = new ArrayList<>();
        List<String> fragmentTitle = new ArrayList<>();

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