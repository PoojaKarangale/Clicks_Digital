package com.pakhi.clicksdigital.GroupChat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TopicRepliesActivity extends AppCompatActivity {

    Message   topic;
    ImageView profile_img, like;
    TextView name, profession, topic_detail, date_time, reply, no_of_replies, no_of_likes;
    RecyclerView     replies_list;
    List<Message>    replies;
    ReplyAdapter     replyAdapter;
    String           currentUserId;
    SharedPreference pref;
    private DatabaseReference UsersRef, replyRef;
    private FirebaseDatabaseInstance rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_replies);
        topic=(Message) getIntent().getSerializableExtra("message");

        rootRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=rootRef.getUserRef();
        replyRef=rootRef.getReplyRef();

        pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        initialiseFields();
        loadData();
        readReplies();

        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle=new Bundle();
                bundle.putSerializable("message", (Serializable) topic);

                ReplyFragment fragment=new ReplyFragment();
                fragment.setArguments(bundle);

                AppCompatActivity activity=(AppCompatActivity) v.getContext();
                FragmentTransaction transaction=activity.getSupportFragmentManager()
                        .beginTransaction();
                // transaction.addToBackStack(null);
                transaction.add(R.id.fragmentContainer, fragment, "TAG_FRAGMENT");
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
            }
        });

        final boolean[] isLiked={false};
        final DatabaseReference topicLikesRef=rootRef.getTopicLikesRef();
        topicLikesRef.child(topic.getMessageID()).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //topic is liked by user
                    isLiked[0]=true;
                    like.setImageResource(R.drawable.liked);
                } else {
                    //topik is not liked by user
                    isLiked[0]=false;
                    like.setImageResource(R.drawable.like_border);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        topicLikesRef.child(topic.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    no_of_likes.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLiked[0]) {
                    isLiked[0]=false;
                    // dislike the topic make hart black
                    topicLikesRef.child(topic.getMessageID()).child(currentUserId).removeValue();
                    like.setImageResource(R.drawable.like_border);
                } else {
                    isLiked[0]=true;
                    // like the topic reden the heart
                    topicLikesRef.child(topic.getMessageID()).child(currentUserId).setValue("");
                    like.setImageResource(R.drawable.liked);
                }
            }
        });
    }

    private void initialiseFields() {
        profile_img=findViewById(R.id.profile_img);
        name=findViewById(R.id.name);
        profession=findViewById(R.id.profession);
        topic_detail=findViewById(R.id.topic);
        date_time=findViewById(R.id.date_time);
        reply=findViewById(R.id.reply);
        no_of_replies=findViewById(R.id.no_of_replies);
        like=findViewById(R.id.like);
        no_of_likes=findViewById(R.id.no_of_likes);


        replies_list=findViewById(R.id.replies_list);
        replies_list.setHasFixedSize(true);
        replies_list.setLayoutManager(new LinearLayoutManager(this));

        replies=new ArrayList<>();

        replyAdapter=new ReplyAdapter(this, replies);
        replies_list.setAdapter(replyAdapter);

    }

    private void loadData() {

        topic_detail.setText(topic.getMessage());
        UsersRef.child(topic.getFrom()).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Picasso.get()
                            .load(snapshot.child(Const.IMAGE_URL).getValue(String.class))
                            .resize(120, 120)
                            .into(profile_img);
                    name.setText(snapshot.child(Const.USER_NAME).getValue().toString());
                    profession.setText(snapshot.child("work_profession").getValue().toString());
                    name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TopicRepliesActivity.this, VisitProfileActivity.class);
                            intent.putExtra("visit_user_id",topic.getFrom());
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        date_time.setText(topic.getTime() + " " + topic.getDate());
        topic_detail.setText(topic.getMessage());


    }

    private void readReplies() {

      /*  FirebaseRecyclerOptions<Message> options
                = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(replyRef.child(topic.getMessageID()), Message.class)
                .build();*/

        replyRef.child(topic.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    no_of_replies.setText(String.valueOf(snapshot.getChildrenCount()));
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Message reply1=dataSnapshot.getValue(Message.class);
                        replies.add(reply1);
                    }
                }
                replyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
