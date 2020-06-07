package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pakhi.clicksdigital.Activities.ProfileActivity;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {
    private Context mcontext;
    private List<User> groupMembers;

    public GroupMembersAdapter(Context mcontext, List<User> groupMembers) {
        this.mcontext = mcontext;
        this.groupMembers = groupMembers;
        Log.d("GroupMembersTESTING", String.valueOf(groupMembers.size()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group_chat, parent, false);

        return new GroupMembersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final User groupMember = groupMembers.get(position);
        Picasso.get().load(groupMember.getImage_url()).into(holder.profileImage);
        holder.userName.setText(groupMember.getUser_name());
        Log.d("GroupMembersTESTING", groupMember.getUser_name());
        holder.userStatus.setText(groupMember.getUser_bio());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // sendUserToProfileActivity();
            }
        });
    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(mcontext, ProfileActivity.class);
        // profileIntent.putExtra("visit_user_id", userId);
        mcontext.startActivity(profileIntent);
    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName;
        ImageView online_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.image_profile);
            userStatus = itemView.findViewById(R.id.user_status);
            online_status = itemView.findViewById(R.id.user_online_status);

            userName = itemView.findViewById(R.id.display_name);
            userStatus.setVisibility(View.VISIBLE);
        }
    }
}
