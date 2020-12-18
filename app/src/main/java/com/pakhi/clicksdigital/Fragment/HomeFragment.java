package com.pakhi.clicksdigital.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.GroupChat.TopicRepliesActivity;
import com.pakhi.clicksdigital.Model.GroupTopic;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.ScreenSlidePageFragment;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private static final int NUM_PAGES=5;
    Context           context;
    DatabaseReference topicReference, userRequestRef;
    ArrayList<String> arayOfTopicID=new ArrayList<>();
    String            publisherKey, currentUserID;
    RecyclerView     display;
    SharedPreference pref;
    String           messageType, messagekEY, currentTime, currentDate, messagePass, currentGroupId, publisher;
    FirebaseDatabaseInstance rootRef;
    DatabaseReference        userRef, grpChatRef, grpNameRef, topicReplyRef;
    Button requestBtn;
    private ViewPager    mPager;
    private PagerAdapter pagerAdapter;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView=inflater.inflate(R.layout.fragment_home, container, false);
        pref=SharedPreference.getInstance();

        rootRef=FirebaseDatabaseInstance.getInstance();
        userRef=rootRef.getUserRef();
        grpChatRef=rootRef.getGroupChatRef();
        grpNameRef=rootRef.getGroupRef();
        topicReplyRef=rootRef.getReplyRef();
        userRequestRef=rootRef.getUserRequestsRef();

        topicReference=rootRef.getTopicRef();
        Log.i("topicReference", String.valueOf(topicReference));
        display=(RecyclerView) homeView.findViewById(R.id.display);
        requestBtn=homeView.findViewById(R.id.request_button);
        display.setLayoutManager(new LinearLayoutManager(getContext()));
        currentUserID=pref.getData(SharedPreference.currentUserId, getContext());

        // Inflate the layout for this fragment
        /*mPager = (ViewPager) homeView.findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(pagerAdapter);*/

        rootRef.getUserRef().child(currentUserID).child(Const.USER_DETAILS).child("approved").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    requestBtn.setVisibility(View.VISIBLE);
                } else {
                    requestBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRequestRef.child(currentUserID).setValue("");
            }
        });

        return homeView;
    }

    @Override
    public void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<GroupTopic> options=
                new FirebaseRecyclerOptions.Builder<GroupTopic>()
                        .setQuery(topicReference, GroupTopic.class)
                        .build();

        FirebaseRecyclerAdapter<GroupTopic, HomeFragment.TopicDisplayHome> adapter
                =new FirebaseRecyclerAdapter<GroupTopic, TopicDisplayHome>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final TopicDisplayHome holder, final int position, @NonNull GroupTopic model) {

                Log.i("group id ------------", String.valueOf(getRef(position).getKey()));

                final String grpID=getRef(position).getKey();
                //GroupTopic groupTopic = (GroupTopic) getRef(position);
                Log.i("group id ------------", String.valueOf(grpID));
                topicReference.child(grpID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (final DataSnapshot mysnap : snapshot.getChildren()) {

                            Log.i("topic id -----------", String.valueOf(mysnap.getKey()));

                            topicReplyRef.child(mysnap.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        holder.NoOfReplies.setText(String.valueOf(snapshot.getChildrenCount()));
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            grpNameRef.child(grpID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    holder.groupName.setText(snapshot.child("group_name").getValue().toString());
                                    //   final String image_url=snapshot.child("image_url").getValue().toString();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            grpChatRef.child(grpID).child(mysnap.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    final Message m=snapshot.getValue(Message.class);

                                    holder.replyButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent replyIntent=new Intent(getContext(), TopicRepliesActivity.class);
                                            replyIntent.putExtra("message", m);
                                            startActivity(replyIntent);

                                        }
                                    });
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent replyIntent=new Intent(getContext(), TopicRepliesActivity.class);
                                            replyIntent.putExtra("message", m);
                                            startActivity(replyIntent);
                                        }
                                    });

                                    publisherKey=m.getFrom();
                                    userRef.child(publisherKey).child("DETAILS").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            //holder.publisherName.setText(snapshot.child(rootRef.getUserDetails().toString()).child(rootRef.getUserName().toString()).getValue().toString() + " " + snapshot.child(rootRef.getUserDetails().toString()).child(rootRef.getLastName().toString()).getValue().toString());
                                            holder.publisherName.setText(snapshot.child(Const.USER_NAME).getValue() + " " + snapshot.child("last_name").getValue());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            arayOfTopicID.add(mysnap.getKey());

                        }

                        Log.i("Length of arrayOfTopic", String.valueOf(arayOfTopicID.size()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public TopicDisplayHome onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending_topic, parent, false);
                // HomeFragment.TopicDisplayHome viewHolder=new HomeFragment.TopicDisplayHome(view);
                return new TopicDisplayHome(view);

            }
        };

        display.setAdapter(adapter);
        adapter.startListening();
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

    public class TopicDisplayHome extends RecyclerView.ViewHolder {

        TextView groupName, topicText, dateAndTime, NoOfReplies, publisherName, replyButton;

        public TopicDisplayHome(@NonNull View itemView) {
            super(itemView);

            groupName=itemView.findViewById(R.id.group_name);
            topicText=itemView.findViewById(R.id.topic);
            dateAndTime=itemView.findViewById(R.id.date_time);
            NoOfReplies=itemView.findViewById(R.id.no_of_replies);
            publisherName=itemView.findViewById(R.id.publisher_name);
            replyButton=itemView.findViewById(R.id.reply);

        }
    }
}
