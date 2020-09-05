package com.pakhi.clicksdigital.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    String                   currentUserId;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    private Toolbar           mToolbar;
    private RecyclerView      FindFriendsRecyclerList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=rootRef.getUserRef();

        FindFriendsRecyclerList=(RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar=findViewById(R.id.find_friends_toolbar);
        /*EditText searchView = findViewById(R.id.search_bar);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchEvents(s.toString().trim().toLowerCase());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchEvents(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/

        ImageView close=findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
        FirebaseRecyclerOptions<User> options=
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(UsersRef, User.class)
                        .build();
        /* user model is present in  Users->id->Details->user.model instead of Users->id->user.model */
        FirebaseRecyclerAdapter<User, FindFriendViewHolder> adapter=
                new FirebaseRecyclerAdapter<User, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindFriendViewHolder holder, final int position, @NonNull final User model) {

                        final String visit_user_id=getRef(position).getKey();
                        UsersRef.child(visit_user_id).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               if(dataSnapshot.exists()) {
                                holder.userName.setText(dataSnapshot.child(Const.USER_NAME).getValue().toString());
                                holder.userStatus.setText(dataSnapshot.child(Const.USER_BIO).getValue().toString());
                                Log.d("findFriend", "--------------" + model.getUser_name() + " " + model.getUser_bio());
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
                            }}

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        holder.chat_with_friend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatActivity=new Intent(FindFriendsActivity.this, ChatActivity.class);
                                chatActivity.putExtra("visit_user_id", getRef(position).getKey());
                                startActivity(chatActivity);
                            }
                        });
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // String visit_user_id = getRef(position).getKey();
                                Intent profileIntent=new Intent(FindFriendsActivity.this, VisitProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user, viewGroup, false);
                        FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };

        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    /*  private void searchEvents(final String s) {

          UsersRef.child(Const.USER_DETAILS).orderByChild(Const.USER_NAME).addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  user.clear();
                  for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                      Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                      if(event.getName().toLowerCase().contains(s)
                              || event.getDescription().toLowerCase().contains(s)
                              || event.getCategory().toLowerCase().contains(s)
                              || event.getLocation().toLowerCase().contains(s)){
                          events.add(event);
                      }
                  }
                  eventAdapter.notifyDataSetChanged();
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });
      }*/
    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        UsersRef.child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profile_image;
        ImageView       chat_with_friend;

        public FindFriendViewHolder(@NonNull View itemView) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
