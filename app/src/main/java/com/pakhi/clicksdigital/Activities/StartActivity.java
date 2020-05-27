package com.pakhi.clicksdigital.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Fragment.ChatsFragment;
import com.pakhi.clicksdigital.Fragment.EventsFragment;
import com.pakhi.clicksdigital.Fragment.GroupsFragment;
import com.pakhi.clicksdigital.Fragment.HomeFragment;
import com.pakhi.clicksdigital.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private EventsFragment eventsFragment;
    private GroupsFragment groupsFragment;
    private ChatsFragment chatsFragment;
    private HomeFragment homeFragment;

    private ImageView profile;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    //private ProfileFragment profileFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();
        profile = findViewById(R.id.profile_activity);
        currentUser = mAuth.getCurrentUser();

        RootRef = FirebaseDatabase.getInstance().getReference();


        if (!(currentUser == null))
        {
            currentUserID = mAuth.getCurrentUser().getUid();
        }


        eventsFragment = new EventsFragment();
        groupsFragment = new GroupsFragment();
        homeFragment = new HomeFragment();
        chatsFragment = new ChatsFragment();
        //profileFragment = new ProfileFragment();

        tabLayout=findViewById(R.id.tab_layout);

        toolbar=findViewById(R.id.toolbar_start);
        setSupportActionBar(toolbar);
        //assert getSupportActionBar() != null;
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager=findViewById(R.id.viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),0);
        viewPagerAdapter.addFragment(homeFragment,"Home");
        viewPagerAdapter.addFragment(groupsFragment,"Groups");
        viewPagerAdapter.addFragment(chatsFragment,"Chat");
        viewPagerAdapter.addFragment(eventsFragment,"Events");
        //viewPagerAdapter.addFragment(profileFragment,"Profile");


        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.home);
        tabLayout.getTabAt(1).setIcon(R.drawable.chat);
        tabLayout.getTabAt(2).setIcon(R.drawable.nav_profile);
        tabLayout.getTabAt(3).setIcon(R.drawable.event);

        BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(10);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, ProfileActivity.class));
            }
        });


    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        List<Fragment> fragments=new ArrayList<>();
        List<String> fragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment,String title){
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
/*
    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(StartActivity.this,RegisterActivity.class));
    }

 */

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        new AlertDialog.Builder(this)

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.logout)
        {
            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToRegisterActivity();
        }
        if (item.getItemId() == R.id.user_request)
        {
            SendUserToUserRequestActivity();
        }
        if (item.getItemId() == R.id.group_request)
        {
            //SendUserToGroupRequestActivity();
        }
        if (item.getItemId() == R.id.join_new_groups)
        {
            //SendUserToGroupRequestActivity();
            startActivity(new Intent(this, JoinGroupActivity.class));
        }
        if (item.getItemId() == R.id.contact_users)
        {
            //SendUserToSettingsActivity();
            startActivity(new Intent(this, ContactUserActivity.class));
        }

        if (item.getItemId() == R.id.settings)
        {
            //SendUserToSettingsActivity();
        }

        if (item.getItemId() == R.id.find_friends)
        {
           // SendUserToFindFriendsActivity();
        }

        return true;
    }

    private void SendUserToUserRequestActivity() {

        startActivity(new Intent(StartActivity.this,UserRequestActivity.class));
    }

    private void SendUserToRegisterActivity() {

        startActivity(new Intent(StartActivity.this,RegisterActivity.class));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (currentUser == null)
        {
            SendUserToRegisterActivity();
        }
        else
        {
            updateUserStatus("online");
            VerifyUserExistance();
        }
    }


    @Override
    protected void onStop()
    {
        super.onStop();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }



    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance()
    {
        String currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.child(Constants.USER_NAME).exists()))

                {
                    //Toast.makeText(StartActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SendUserToSetProfileActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetProfileActivity() {
        startActivity(new Intent(StartActivity.this,SetProfileActivity.class));
    }


    private void updateUserStatus(String state)
    {
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

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }

}
