package com.pakhi.clicksdigital.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;
import com.pakhi.clicksdigital.Adapter.HomePageTopicAdapter;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.Notification;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private static final int NUM_PAGES = 5;
    private static int currentPage = 0;
    ArrayList<Message> trendingTopics = new ArrayList<>();
    String currentUserID;
    RecyclerView topicRecyclerView;
    SharedPreference pref;
    FirebaseDatabaseInstance rootRef;

    Button requestBtn;
    HomePageTopicAdapter topicAdapter;
    ArrayList<String> images = new ArrayList<>();

    ArrayList<String> uploader = new ArrayList<>();
    ArrayList<String> eventName = new ArrayList<>();
    DatabaseReference sliderRef;
    Context context;
    String userName;
    ArrayList<String> listAdmins = new ArrayList<>();
    boolean notify = false;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        pref = SharedPreference.getInstance();

        rootRef = FirebaseDatabaseInstance.getInstance();

        final String user_type = pref.getData(SharedPreference.user_type, getContext());
        currentUserID = pref.getData(SharedPreference.currentUserId, getContext());
        requestBtn = homeView.findViewById(R.id.request_button);
        setupRecyclerView(homeView);

        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = snapshot.child(ConstFirebase.USER_NAME).getValue().toString()+snapshot.child(ConstFirebase.LAST_NAME).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rootRef.getApprovedUserRef().child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    requestBtn.setVisibility(View.VISIBLE);
                    /* if (user_type.equals("admin")) {
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
        rootRef.getUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){

                    if(snap.child(ConstFirebase.USER_DETAILS).exists()){
                        String type = snap.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.userType).getValue().toString();

                        if(type.equals("admin")){
                            listAdmins.add(snap.getKey());
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                rootRef.getUserRequestsRef().child(currentUserID).setValue("");
                Toast.makeText(getContext(), "Request is sent to admin wait for approval ", Toast.LENGTH_LONG).show();
                for(int j=0;j<listAdmins.size();j++){
                    if(notify) Notification.sendPersonalNotifiaction(currentUserID, listAdmins.get(j), "Profile verification request by "+userName,"User Request", "request","");

                    String notificationKey = rootRef.getNotificationRef().push().getKey();

                    rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationRecieverID).setValue(listAdmins.get(j));
                    rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationFrom).setValue(currentUserID);
                    rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.goToNotificationId).setValue(currentUserID);
                    rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.typeOfNotification).setValue("profileRequest");


                }
                notify=false;

            }
        });

        removeTopicOlderThanTwoMonths();
        readTopics();
        init(homeView);

        return homeView;
    }

    private void removeTopicOlderThanTwoMonths() {
        final Calendar calendar = Calendar.getInstance();
        rootRef.getTopicRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (final DataSnapshot topicSnap : snapshot.getChildren()) {

                    Log.d("REMOVE TOPIC", "---in for -----------" + topicSnap.getValue());

                    rootRef.getGroupChatRef().child(topicSnap.getValue().toString()).child(topicSnap.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                //Log.d("REMOVE TOPIC", "---in group chat -----------" + snapshot.getValue());

                                Date topicDate = null;
                                SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
                                try {
                                    topicDate = formatter.parse(snapshot.child("date").getValue().toString());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                calendar.add(Calendar.MONTH, -2);
                                Date twoMonthAgo = new Date(formatter.format(calendar.getTime()));

                                if (topicDate.before(twoMonthAgo)) {
//                                    Log.d("REMOVE TOPIC", "---in group chat -----------" + snapshot.getValue());
//                                    Log.d("REMOVE TOPIC", "---in group chat -----------" + rootRef.getTopicRef().child(topicSnap.getKey()));
                                    rootRef.getTopicRef().child(topicSnap.getKey()).removeValue();
                                    //remove the topicwith replies and likes.....
                                }
                            }
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

    private void setupRecyclerView(View v) {
        topicRecyclerView = (RecyclerView) v.findViewById(R.id.display);
        topicRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //  topicRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        topicRecyclerView.setLayoutManager(linearLayoutManager);
        //  linearLayoutManager.setReverseLayout(true);

        topicAdapter = new HomePageTopicAdapter(getContext(), trendingTopics);
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
                topicAdapter.notifyDataSetChanged();
                for (final DataSnapshot topicSnap : snapshot.getChildren()) {
                    final String groupId = (String) topicSnap.getValue();
                    rootRef.getUserRef().child(currentUserID).child(ConstFirebase.groups1).child(groupId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                rootRef.getGroupChatRef().child(groupId).child(topicSnap.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            trendingTopics.add(0, snapshot.getValue(Message.class));
                                            // topicAdapter.notifyDataSetChanged();
                                        }
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
                // topicAdapter.notifyDataSetChanged();
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

    private void init(View homeView) {

        final ViewPager mViewPager;
        mViewPager = homeView.findViewById(R.id.viewPagerMain);

        final ImageViewPagerAdapter mViewPagerAdapter = new ImageViewPagerAdapter(getContext(), images, eventName, uploader);
        //        mViewPager.setAdapter(mViewPagerAdapter);

        images.clear();
        eventName.clear();
        uploader.clear();
        rootRef.getsliderRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (snap.child(ConstFirebase.url).exists() && snap.child(ConstFirebase.nameOfEvent).exists() && snap.child(ConstFirebase.sender).exists()) {
                            images.add(0, snap.child(ConstFirebase.url).getValue().toString());
                            eventName.add(0, snap.child(ConstFirebase.nameOfEvent).getValue().toString());
                            uploader.add(0, snap.child(ConstFirebase.sender).getValue().toString());
                            mViewPagerAdapter.notifyDataSetChanged();
                        }
                    }
                }
                mViewPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mViewPager.setAdapter(mViewPagerAdapter);

        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if (currentPage == images.size()) {
                    currentPage = 0;
                }
                mViewPagerAdapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(currentPage++, true);
                mViewPagerAdapter.notifyDataSetChanged();
            }
        };
        //Auto start
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 5000, 2500);
    }
}