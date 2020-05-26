package com.pakhi.clicksdigital.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Activities.Constants;
import com.pakhi.clicksdigital.Model.User_request;
import com.pakhi.clicksdigital.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.ViewHolder> {
    String in_current_user_groups;
    DatabaseReference databaseReference;
    private Context mcontext;
    private List<User_request> userRequests;
    private FirebaseUser firebaseUser;
    private boolean sentRequestFlag = false;

    public UserRequestAdapter(Context mcontext, List<User_request> userRequests) {
        this.mcontext = mcontext;
        this.userRequests = userRequests;
    }

    @NonNull
    @Override
    public UserRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group_chat, parent, false);
        return new UserRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserRequestAdapter.ViewHolder holder, final int position) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User_request userRequest = userRequests.get(position);

        final String userId;
        final String groupId;
        final String group_name;
        final String[] user_name = new String[1];
        userId = userRequest.getRequesting_user();
        databaseReference.child("Users").child(userId).child(Constants.USER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_name[0] = dataSnapshot.getValue(String.class);
                holder.displayName.setText(user_name[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        groupId = userRequest.getGroup_id();
        group_name = userRequest.getGroup_name();
        holder.group_name.setText(group_name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileActivity = new Intent();
                profileActivity.putExtra("user_id", userId);
                profileActivity.putExtra("group_id", groupId);
                mcontext.startActivity(profileActivity);

            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserRequest(userRequest.getRequest_id(), position);
            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserToGroup(groupId, userId);
                deleteUserRequest(userRequest.getRequest_id(), position);
            }
        });
    }

    private void deleteUserRequest(String request_id, int position) {
        userRequests.remove(position);
        databaseReference.child("User_requests").child(request_id).removeValue();

    }

    private void addUserToGroup(String groupId, String userId) {
        databaseReference.child("Groups").child(groupId).child("Users").child(userId).setValue(" ");
        databaseReference.child("Users").child(userId).child("groups").child(groupId).setValue(" ");
        //databaseReference.child("User_requests").child("request_status").setValue("accepted");

    }


    @Override
    public int getItemCount() {
        return userRequests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView displayName, group_name;
        public CircleImageView image_profile;
        Button accept, cancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            image_profile = itemView.findViewById(R.id.image_profile);
            group_name = itemView.findViewById(R.id.group_name);
            accept = itemView.findViewById(R.id.request_accept_btn);
            cancel = itemView.findViewById(R.id.request_cancel_btn);

            group_name.setVisibility(View.VISIBLE);
            accept.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
        }
    }
}
