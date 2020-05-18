package com.pakhi.clicksdigital;

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
import android.os.Bundle;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.pakhi.clicksdigital.Fragment.EventsFragment;
import com.pakhi.clicksdigital.Fragment.GroupsFragment;
import com.pakhi.clicksdigital.Fragment.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private EventsFragment eventsFragment;
    private GroupsFragment groupsFragment;
    private HomeFragment homeFragment;
    //private ProfileFragment profileFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        eventsFragment = new EventsFragment();
        groupsFragment = new GroupsFragment();
        homeFragment = new HomeFragment();
        //profileFragment = new ProfileFragment();

        tabLayout=findViewById(R.id.tab_layout);

        toolbar=findViewById(R.id.toolbar_start);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager=findViewById(R.id.viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),0);
        viewPagerAdapter.addFragment(homeFragment,"Home");
        viewPagerAdapter.addFragment(groupsFragment,"Chat");
        viewPagerAdapter.addFragment(eventsFragment,"Events");
        //viewPagerAdapter.addFragment(profileFragment,"Profile");


        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.home);
        tabLayout.getTabAt(1).setIcon(R.drawable.chat);
        tabLayout.getTabAt(2).setIcon(R.drawable.event);
        //tabLayout.getTabAt(3).setIcon(R.drawable.nav_profile);

        BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
        badgeDrawable.setVisible(true);
        badgeDrawable.setNumber(10);


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


}
