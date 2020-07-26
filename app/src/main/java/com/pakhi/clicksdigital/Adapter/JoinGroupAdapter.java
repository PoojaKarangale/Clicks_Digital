package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
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
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.ActivitiesGroupChat.GroupChatActivity;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinGroupAdapter extends RecyclerView.Adapter<JoinGroupAdapter.ViewHolder> {
    private String in_current_user_groups = "";
    private Context mcontext;
    private List<Group> groups;
    private FirebaseUser firebaseUser;
    private boolean sentRequestFlag = false;

    public JoinGroupAdapter(Context mcontext, List<Group> groups, String in_current_user_groups) {
        this.mcontext = mcontext;
        this.groups = groups;
        this.in_current_user_groups = in_current_user_groups;
    }

    public JoinGroupAdapter(Context mcontext, List<Group> groups) {
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
    public void onBindViewHolder(@NonNull final JoinGroupAdapter.ViewHolder holder, final int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Group group = groups.get(position);
        holder.displayName.setText(group.getGroup_name());
        holder.status_of_request.setText(group.getStatus());
        holder.displayName.setTextColor(Color.BLACK);
        Picasso.get()
                .load(group.getImage_url()).placeholder(R.drawable.profile_image)
                .resize(120, 120)
                .into(holder.image_profile);

        Log.d("joinGroupAdapter", "----------" + group.getGroup_name());

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra("image_url_string", group.getImage_url());
                v.getContext().startActivity(fullScreenIntent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (in_current_user_groups.equals("users_group")) {
                    Intent groupChatActivity = new Intent(mcontext.getApplicationContext(), GroupChatActivity.class);
                    groupChatActivity.putExtra("groupName", group.getGroup_name());
                    groupChatActivity.putExtra("groupId", group.getGroupid());
                    mcontext.startActivity(groupChatActivity);

                } else {
                    holder.itemView.setEnabled(false);
                    sentRequestToJoinGroup(group.getGroupid(), group.getGroup_name(), firebaseUser.getUid());
                    if (sentRequestFlag) {
                        holder.status_of_request.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void sentRequestToJoinGroup(String group_id, final String displayName, final String uid) {
        //final String userid = firebaseUser.getUid();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User_requests");

        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        String group_request_id = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("request_id", group_request_id);
        hashMap.put("group_name", displayName);
        hashMap.put("group_id", group_id);
        hashMap.put("date", saveCurrentDate);
        hashMap.put("requesting_user", uid);
        hashMap.put("request_status", "pending");

        assert group_request_id != null;
        reference.child(group_request_id).setValue(hashMap);
        sentRequestFlag = true;

    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView displayName, status_of_request;
        CircleImageView image_profile;
        ImageView img_info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            image_profile = itemView.findViewById(R.id.image_profile);
            img_info = itemView.findViewById(R.id.img_info);
            status_of_request = itemView.findViewById(R.id.status_of_request);

            img_info.setVisibility(View.VISIBLE);
            status_of_request.setVisibility(View.VISIBLE);

            if (!in_current_user_groups.equals("users_group")) {
                img_info.setVisibility(View.INVISIBLE);
                status_of_request.setVisibility(View.INVISIBLE);
            }
        }
    }
}
