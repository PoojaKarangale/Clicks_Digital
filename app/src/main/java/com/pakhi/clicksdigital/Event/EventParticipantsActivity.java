package com.pakhi.clicksdigital.Event;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventParticipantsActivity extends AppCompatActivity {
    Event             event;
    DatabaseReference eventRef, currentEventRef, usersRef;
    List<String>             participantsList=new ArrayList<>();
    RecyclerView             user_recycler_list;
    FirebaseDatabaseInstance rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_participants);

        rootRef=FirebaseDatabaseInstance.getInstance();

        event=(Event) getIntent().getSerializableExtra("Event");
        eventRef=rootRef.getEventRef();
        usersRef=rootRef.getUserRef();

        currentEventRef=eventRef.child(event.getEventType()).child(event.getEventId());
        initializeFields();
        currentEventRef.child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int participants=(int) snapshot.getChildrenCount();
                    // no_of_participants.setText(participants);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        participantsList.add(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeFields() {
        user_recycler_list=findViewById(R.id.user_recycler_list);
        user_recycler_list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //updateUserStatus("online");
        FirebaseRecyclerOptions<User> options=
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(currentEventRef, User.class)
                        .build();
        FirebaseRecyclerAdapter<User, EventParticipantsActivity.EventParticipantsViewHolder> adapter=
                new FirebaseRecyclerAdapter<User, EventParticipantsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final EventParticipantsViewHolder holder, final int position, @NonNull final User model) {
                        final String visit_user_id=getRef(position).getKey();
                        usersRef.child(visit_user_id).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                holder.userName.setText(dataSnapshot.child(Const.USER_NAME).getValue().toString());
                                holder.userStatus.setText(dataSnapshot.child(Const.USER_BIO).getValue().toString());
                                final String image_url=dataSnapshot.child(Const.IMAGE_URL).getValue().toString();
                                Picasso.get()
                                        .load(image_url).placeholder(R.drawable.profile_image)
                                        .resize(120, 120)
                                        .into(holder.profile_image);

                                holder.profile_image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent fullScreenIntent=new Intent(v.getContext(), EnlargedImage.class);
                                        fullScreenIntent.putExtra(Const.IMAGE_URL, image_url);
                                        v.getContext().startActivity(fullScreenIntent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        holder.chat_with_friend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatActivity=new Intent(EventParticipantsActivity.this, ChatActivity.class);
                                chatActivity.putExtra("visit_user_id", getRef(position).getKey());
                                startActivity(chatActivity);
                            }
                        });
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // String visit_user_id = getRef(position).getKey();
                                Intent profileIntent=new Intent(EventParticipantsActivity.this, VisitProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public EventParticipantsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user, viewGroup, false);
                        EventParticipantsViewHolder viewHolder=new EventParticipantsViewHolder(view);
                        return viewHolder;
                    }
                };

        user_recycler_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class EventParticipantsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profile_image;
        ImageView       chat_with_friend;

        public EventParticipantsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.display_name);
            userStatus=itemView.findViewById(R.id.user_status);
            chat_with_friend=itemView.findViewById(R.id.chat_with_friend);

            chat_with_friend.setVisibility(View.VISIBLE);
            userName.setTextColor(Color.BLACK);
            profile_image=itemView.findViewById(R.id.image_profile);
            userStatus.setVisibility(View.VISIBLE);
        }
    }
}
