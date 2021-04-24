package com.pakhi.clicksdigital.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pakhi.clicksdigital.Activities.FindFriendsActivity;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Notifications.Token;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private View         PrivateChatsView;
    private RecyclerView chatsList;
    Button findNewProf;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String       currentUserID="";

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView=inflater.inflate(R.layout.fragment_chat, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        ChatsRef=FirebaseDatabase.getInstance().getReference().child(ConstFirebase.messagesList).child(currentUserID);
        UsersRef=FirebaseDatabase.getInstance().getReference().child(ConstFirebase.users);

        chatsList=(RecyclerView) PrivateChatsView.findViewById(R.id.recycler_chats);
        findNewProf = PrivateChatsView.findViewById(R.id.find_new_prof);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        updateToken(FirebaseInstanceId.getInstance().getToken());

        FloatingActionButton fab_create_event=PrivateChatsView.findViewById(R.id.fab_create_event);
        findNewProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToFindFriendsActivity();

            }
        });
        return PrivateChatsView;
    }

    private void sendUserToFindFriendsActivity() {

        startActivity(new Intent(getContext(), FindFriendsActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<User> options=
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(ChatsRef, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, ChatsViewHolder> adapter=
                new FirebaseRecyclerAdapter<User, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull User model) {
                        final String usersIDs=getRef(position).getKey();
                        final String[] retImage={"default_image"};

                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.child(ConstFirebase.USER_DETAILS).hasChild(ConstFirebase.IMAGE_URL)) {
                                        retImage[0]=dataSnapshot.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.IMAGE_URL).getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                        // after adding image uri to users database
                                    }

                                    final String retName=dataSnapshot.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.USER_NAME).getValue().toString();
                                    final String retStatus=dataSnapshot.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.USER_BIO).getValue().toString();

                                    holder.userName.setText(retName);
                                    holder.userStatus.setText(retStatus);

                                    if (dataSnapshot.child(ConstFirebase.userState).hasChild(Const.state)) {
                                        String state=dataSnapshot.child(ConstFirebase.userState).child(Const.state).getValue().toString();
                                        String date=dataSnapshot.child(ConstFirebase.userState).child(Const.date).getValue().toString();
                                        String time=dataSnapshot.child(ConstFirebase.userState).child(Const.time).getValue().toString();

                                        if (state.equals(Const.online)) {
                                            // holder.userStatus.setText("online");
                                            holder.online_status.setVisibility(View.VISIBLE);
                                        } else if (state.equals(Const.offline)) {
                                            holder.userStatus.setText("Last Seen: " + date + " " + time);
                                        }
                                    } else {
                                        holder.userStatus.setText("offline");
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent=new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra(Const.visitUser, usersIDs);
                                            chatIntent.putExtra(Const.visit_user_name, retName);
                                            //  chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EnlargedImage.enlargeImage(retImage[0],v.getContext());

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();


    }

    private void updateToken(String token) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1=new Token(token);
        reference.child(mAuth.getUid()).setValue(token1);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView        userStatus, userName;
        ImageView online_status;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.image_profile);
            userStatus=itemView.findViewById(R.id.user_status);
            online_status=itemView.findViewById(R.id.user_online_status);

            userName=itemView.findViewById(R.id.display_name);
            userStatus.setVisibility(View.VISIBLE);
        }
    }
}
