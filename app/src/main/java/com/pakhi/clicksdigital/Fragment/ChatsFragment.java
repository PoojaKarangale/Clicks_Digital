package com.pakhi.clicksdigital.Fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.ceylonlabs.imageviewpopup.ImagePopup;
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
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Notifications.Token;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

//import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private View         PrivateChatsView;
    private RecyclerView chatsList;
    Button findNewProf;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String       currentUserID="";
    FirebaseDatabaseInstance rootRef;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView=inflater.inflate(R.layout.fragment_chat, container, false);

        rootRef=FirebaseDatabaseInstance.getInstance();

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

        final ImagePopup imagePopup = new ImagePopup(getContext());
        imagePopup.setWindowHeight(800); // Optional
        imagePopup.setWindowWidth(800); // Optional
        imagePopup.setBackgroundColor(Color.BLACK);  // Optional
        imagePopup.setFullScreen(true); // Optional
        imagePopup.setHideCloseIcon(true);  // Optional
        imagePopup.setImageOnClickClose(true);
        // Optional

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
                                        //Picasso.get().load(retImage[0]).into(holder.profileImage);
                                        Glide.with(getContext()).load(retImage[0]).transform(new CenterCrop(), new RoundedCorners(10))
                                                .into(holder.profileImage);
                                        // after adding image uri to users database
                                    }

                                    final String retName=dataSnapshot.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.USER_NAME).getValue().toString()
                                            +" "+dataSnapshot.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.last_name).getValue().toString();
                                    final String retStatus=dataSnapshot.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.working).getValue().toString()
                                            +", "+dataSnapshot.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.company).getValue().toString();

                                    holder.userName.setText(retName);
                                    holder.userStatus.setText(retStatus);

                                    if (dataSnapshot.child(ConstFirebase.userState).hasChild(ConstFirebase.state)) {
                                        String state=dataSnapshot.child(ConstFirebase.userState).child(ConstFirebase.state).getValue().toString();
                                        String date=dataSnapshot.child(ConstFirebase.userState).child(ConstFirebase.date).getValue().toString();
                                        String time=dataSnapshot.child(ConstFirebase.userState).child(ConstFirebase.time).getValue().toString();

                                        if (state.equals(ConstFirebase.onlineStatus)) {
                                            // holder.userStatus.setText("online");
                                            holder.online_status.setVisibility(View.VISIBLE);
                                        } else if (state.equals(ConstFirebase.onlineStatus)) {
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
                                //EnlargedImage.enlargeImage(retImage[0],v.getContext());
                                Dialog builder = new Dialog(getContext());
                                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                builder.getWindow().setBackgroundDrawable(
                                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        //nothing;
                                    }
                                });

                                ImageView imageView = new ImageView(getContext());
                                Glide.with(getContext()).
                                        load(retImage[0]).
                                        transform(new CenterCrop(), new RoundedCorners(15)).into(imageView);
                                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                                        800,
                                        800));
                                builder.show();

                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), LoadImage.class);
                                        intent.putExtra(Const.IMAGE_URL, retImage[0]);
                                        startActivity(intent);
                                    }
                                });


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
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference(ConstFirebase.tokens);
        Token token1=new Token(token);
        reference.child(mAuth.getUid()).setValue(token1);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView        userStatus, userName;
        ImageView online_status;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.image_profile);
            userStatus=itemView.findViewById(R.id.user_status);
            online_status=itemView.findViewById(R.id.user_online_status);
            online_status.setVisibility(View.GONE);

            userName=itemView.findViewById(R.id.display_name);
            userStatus.setVisibility(View.VISIBLE);
        }
    }
}
