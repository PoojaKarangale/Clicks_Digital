package com.pakhi.clicksdigital.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.HomePageTopicAdapter;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.ScreenSlidePageFragment;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private static final int NUM_PAGES=5;
    ArrayList<Message>       trendingTopics=new ArrayList<>();
    String                   currentUserID;
    RecyclerView             topicRecyclerView;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    Button                   requestBtn;
    HomePageTopicAdapter     topicAdapter;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView=inflater.inflate(R.layout.fragment_home, container, false);
        pref=SharedPreference.getInstance();

        rootRef=FirebaseDatabaseInstance.getInstance();
        final String user_type=pref.getData(SharedPreference.user_type, getContext());
        currentUserID=pref.getData(SharedPreference.currentUserId, getContext());
        requestBtn=homeView.findViewById(R.id.request_button);
        setupRecyclerView(homeView);

        rootRef.getApprovedUserRef().child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {

                    requestBtn.setVisibility(View.VISIBLE);
                  /*  if (user_type.equals("admin")) {
                        requestBtn.setVisibility(View.GONE);
                    }*/
                } else {

                    requestBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rootRef.getUserRequestsRef().child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    requestBtn.setText("requested");
                    requestBtn.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootRef.getUserRequestsRef().child(currentUserID).setValue("");
                Toast.makeText(getContext(), "Request is sent to admin wait for approval ", Toast.LENGTH_LONG).show();
            }
        });
        readTopics();
        return homeView;
    }

    private void setupRecyclerView(View v) {
        topicRecyclerView=(RecyclerView) v.findViewById(R.id.display);
        topicRecyclerView.setHasFixedSize(true);
        topicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        topicAdapter=new HomePageTopicAdapter(getContext(), trendingTopics);
        topicRecyclerView.setAdapter(topicAdapter);
    }

    public void readTopics() {
        //..... we can save groups of user in sqlite db to fetch in less time ......
        //here fetch the groups of user from User->user_id->groups
        //if group id id present in groups then only topic will be visible

        rootRef.getTopicRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trendingTopics.clear();
                for (final DataSnapshot topicSnap : snapshot.getChildren()) {
                    final String groupId=(String) topicSnap.getValue();
                    rootRef.getUserRef().child(currentUserID).child("groups").child(groupId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                rootRef.getGroupChatRef().child(groupId).child(topicSnap.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        trendingTopics.add(0, snapshot.getValue(Message.class));
                                        topicAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                topicAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //  readTopics();
    }

    public void backPressed() {
        new AlertDialog.Builder(getContext())

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    //Adapter class for slider
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
