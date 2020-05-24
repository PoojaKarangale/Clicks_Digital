package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pakhi.clicksdigital.Activities.GroupChatActivity;
import com.pakhi.clicksdigital.Model.GroupChat;
import com.pakhi.clicksdigital.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinGroupAdapter extends RecyclerView.Adapter<JoinGroupAdapter.ViewHolder> {
    private Context mcontext;
    private List<GroupChat> groups;
    private FirebaseUser firebaseUser;
    private boolean sentRequestFlag = false;

    String in_current_user_groups="";

    public JoinGroupAdapter(Context mcontext, List<GroupChat> groups, String in_current_user_groups) {
        this.mcontext = mcontext;
        this.groups = groups;
        this.in_current_user_groups = in_current_user_groups;
    }


    public JoinGroupAdapter(Context mcontext, List<GroupChat> groups) {
        this.mcontext = mcontext;
        this.groups = groups;
    }

    @NonNull
    @Override
    public JoinGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group_chat, parent, false);
        return new JoinGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final JoinGroupAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final GroupChat group = groups.get(position);
        holder.displayName.setText(group.getGroup_name());
        holder.status_of_request.setText(group.getStatus());
        //Glide.with(mcontext).load(group.getImageUrl()).into(holder.image_profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(in_current_user_groups.equals("users_group")) {
                    Intent groupChatActivity=new Intent(mcontext.getApplicationContext(), GroupChatActivity.class);
                    groupChatActivity.putExtra("groupName",group.getGroup_name());
                    groupChatActivity.putExtra("groupId",group.getGroupid());
                   mcontext.startActivity(groupChatActivity);

                }else {
                //    sentRequestToJoinGroup(group.getGroupid(), group.getGroup_name(), firebaseUser.getUid());

                }
            }
        });
    }

    private void sentRequestToJoinGroup(final String displayName, final String uid) {
        //final String userid = firebaseUser.getUid();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User_requests");
        //LocalDateTime obj = LocalDateTime.now(DD/MM/YYYY);
        final SimpleDateFormat sdf_date = new SimpleDateFormat("dd/mm/yyyy");

        String group_request_id = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("group_name", displayName);
        hashMap.put("group_request_id", group_request_id);
        hashMap.put("date", sdf_date.format(new Date()));
        hashMap.put("requesting_user", uid);
        hashMap.put("request_status", "pending");

        reference.child(group_request_id).setValue(hashMap);
        sentRequestFlag = true;
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView displayName, status_of_request;
        public CircleImageView image_profile;
        public ImageView img_info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            image_profile = itemView.findViewById(R.id.image_profile);
            img_info = itemView.findViewById(R.id.img_info);
            status_of_request = itemView.findViewById(R.id.status_of_request);


            if(!in_current_user_groups.equals("users_group")){
                img_info.setVisibility(View.VISIBLE);
                status_of_request.setVisibility(View.VISIBLE);
            }


        }
    }
}
