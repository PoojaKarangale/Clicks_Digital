package com.pakhi.clicksdigital.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {
    private View view;
    private OnlineEventsFragment onlineEventsFragment;
    private OfflineEventsFragment offlineEventsFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_events, container, false);

        onlineEventsFragment = new OnlineEventsFragment();
        offlineEventsFragment = new OfflineEventsFragment();
        TabLayout tabLayout = view.findViewById(R.id.tabs);
        ViewPager viewPager = view.findViewById(R.id.viewPagerEvents);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0);

        viewPagerAdapter.addFragment(onlineEventsFragment, "Online");
        viewPagerAdapter.addFragment(offlineEventsFragment, "Offline");

        viewPager.setAdapter(viewPagerAdapter);
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
