package com.pakhi.clicksdigital.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.ActivitiesGroupChat.JoinGroupActivity;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Fragment.ChatsFragment;
import com.pakhi.clicksdigital.Fragment.EventsFragment;
import com.pakhi.clicksdigital.Fragment.GroupsFragment;
import com.pakhi.clicksdigital.Fragment.HomeFragment;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.ActivitiesProfile.ProfileActivity;
import com.pakhi.clicksdigital.ActivitiesProfile.SetProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.ActivitiesRegisterLogin.RegisterActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    static int REQUEST_CODE = 1;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    UserDatabase db;
    EventsFragment eventsFragment;
    GroupsFragment groupsFragment;
    HomeFragment homeFragment;
    ChatsFragment chatsFragment;
    Toolbar toolbar;
    TabLayout tabLayout;
    PermissionsHandling permissions;
    private ImageView profile, user_requests_to_join_group;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        profile = findViewById(R.id.profile_activity);
        user_requests_to_join_group = findViewById(R.id.user_requests_to_join_group);
        db = new UserDatabase(this);
        getUserFromDb();
        permissions = new PermissionsHandling(StartActivity.this);
        requestForPremission();

        if (user.getUser_type().equals("admin")) {
            user_requests_to_join_group.setVisibility(View.VISIBLE);
        } else {
            user_requests_to_join_group.setVisibility(View.GONE);
        }

        setupTabLayout();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(StartActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        user_requests_to_join_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToUserRequestActivity();
            }
        });
    }

    private void setupTabLayout() {

        eventsFragment = new EventsFragment();
        groupsFragment = new GroupsFragment();
        homeFragment = new HomeFragment();
        chatsFragment = new ChatsFragment();

        tabLayout = findViewById(R.id.tab_layout);
        toolbar = findViewById(R.id.toolbar_start);

        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewPager);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPagerAdapter.addFragment(homeFragment, "Home");
        viewPagerAdapter.addFragment(groupsFragment, "Groups");
        viewPagerAdapter.addFragment(chatsFragment, "Chat");
        viewPagerAdapter.addFragment(eventsFragment, "Events");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
/*
        tabLayout.getTabAt(0).setIcon(R.drawable.home);
        tabLayout.getTabAt(1).setIcon(R.drawable.chat);
        tabLayout.getTabAt(2).setIcon(R.drawable.nav_profile);
        tabLayout.getTabAt(3).setIcon(R.drawable.event);
 */
        BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(10);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
       /* Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.createEventContainer);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }*/        /* new AlertDialog.Builder(this)

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
                .show();*/        /* int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();

            //additional code
        } else {
            getSupportFragmentManager().popBackStack();
            //finish();
        }*/
    /*    Log.d("TESTINGSTART", "---------------on back pressed--");
        Log.d("TESTINGSTART", "-------------get cur itrm----" + viewPager.getCurrentItem());
        Log.d("TESTINGSTART", "-----------frag count------" + getSupportFragmentManager().getBackStackEntryCount());
        Log.d("TESTINGSTART", "-----------frag count------" + getSupportFragmentManager());*/
        if (viewPager.getCurrentItem() == 0) {
//            Log.d("TESTINGSTART", "-------------get cur itrm----" + viewPager.getCurrentItem());
            if (viewPagerAdapter.getItem(0) instanceof HomeFragment) {
//                Log.d("TESTINGSTART", "---------home--------");
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
//            Log.d("TESTINGSTART", "-----------------" + getSupportFragmentManager().getBackStackEntryCount());
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack();
        } else {
//            Log.d("TESTINGSTART", "---------------else part--");
               /* int count = getSupportFragmentManager().getBackStackEntryCount();

                if (count == 0) {
                    super.onBackPressed();

                    //additional code
                } else {
                    getSupportFragmentManager().popBackStack();
                    //finish();
                }*/
               /* String TAG_FRAGMENT = "TAG_FRAGMENT";

                Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
                if (fragment != null) // could be null if not instantiated yet
                {
                    if (fragment.getView() != null) {
                        // Pop the backstack on the ChildManager if there is any. If not, close this activity as normal.
                        if (!fragment.getChildFragmentManager().popBackStackImmediate()) {
                            finish();
                        }
                    }
                }*/
                /*final Myfragment fragment = (Myfragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);

                if (fragment.allowBackPressed()) { // and then you define a method allowBackPressed with the logic to allow back pressed or not
                    super.onBackPressed();
                }*/
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

        if (item.getItemId() == R.id.logout) {
            // updateUserStatus("offline");
            mAuth.signOut();
            SendUserToRegisterActivity();
        }
        if (item.getItemId() == R.id.join_new_groups) {
            startActivity(new Intent(this, JoinGroupActivity.class));
        }

        if (item.getItemId() == R.id.settings) {
        }

        return true;
    }

    private void getUserFromDb() {
        db.getReadableDatabase();
        Cursor res = db.getAllData();
        if (res.getCount() == 0) {

        } else {
            res.moveToFirst();
            user = new User(res.getString(0), res.getString(1),
                    res.getString(2), res.getString(3), res.getString(4),
                    res.getString(5), res.getString(6), res.getString(7),
                    res.getString(8), res.getString(9), res.getString(10),
                    res.getString(11), res.getString(12), res.getString(13),
                    res.getString(14));
        }
    }

    private void SendUserToUserRequestActivity() {

        startActivity(new Intent(StartActivity.this, UserRequestActivity.class));
    }

    private void SendUserToRegisterActivity() {

        startActivity(new Intent(StartActivity.this, RegisterActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            // SendUserToRegisterActivity();
        } else {
            //  updateUserStatus("online");
            //VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (currentUser != null) {
            //updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentUser != null) {
            // updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
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
        });
    }

    private void SendUserToSetProfileActivity() {
        Intent intent = new Intent(StartActivity.this, SetProfileActivity.class);
        intent.putExtra("PreviousActivity", "StartActivity");
        startActivity(intent);
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        RootRef.child("Users").child(user.getUser_id()).child("userState")
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
            //popupMenuSettigns();
        }

      /*  if (ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(StartActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(StartActivity.this,
                        Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(StartActivity.this,
                        Manifest.permission.WRITE_CONTACTS) +
                ContextCompat.checkSelfPermission(StartActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //when permissions not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this, Manifest.permission.WRITE_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this, Manifest.permission.CAMERA)) {
                //creating alertDialog
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(StartActivity.this);
                builder.setTitle("Grant permissioms");
                builder.setMessage("Camera, read & write Contacts, read & write Storage");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        ActivityCompat.requestPermissions(
                                StartActivity.this,
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS,
                                        Manifest.permission.CAMERA
                                },
                                REQUEST_CODE
                        );
                    }
                });

                //builder.setNegativeButton("Cancel",null);
                androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else {
                ActivityCompat.requestPermissions(
                        StartActivity.this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.CAMERA
                        },
                        REQUEST_CODE
                );

            }
        } else {
            //when those permissions are already granted
            //popupMenuSettigns();
            //logMessage("when those permissions are already granted=----------");
        }*/
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
                //popupMenuSettigns();
                //permission granted
                // logMessage(" permission granted-----------");

            } else {

                //permission not granted
                //requestForPremission();
                // logMessage(" permission  not granted-------------");

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
