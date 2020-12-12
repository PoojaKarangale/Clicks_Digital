package com.pakhi.clicksdigital.GroupChat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyGroupsAdapter extends RecyclerView.Adapter<MyGroupsAdapter.ViewHolder> {
    FirebaseDatabaseInstance rootRef;
    SharedPreference pref;
    private String      user_type,in_current_user_groups="";
    private Context     mcontext;
    private List<Group> groups;
    private boolean sentRequestFlag=false;


    public MyGroupsAdapter(Context mcontext, List<Group> groups) {
        this.mcontext=mcontext;
        this.groups=groups;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group, parent, false);
        rootRef=FirebaseDatabaseInstance.getInstance();
        pref=SharedPreference.getInstance();
        user_type = pref.getData(SharedPreference.user_type,mcontext);
        return new MyGroupsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Group group=groups.get(position);
        holder.displayName.setText(group.getGroup_name());
        holder.displayName.setTextColor(Color.BLACK);
        //  Log.d("joinGroupAdapter", "---image url-----------------" + group.getImage_url());
        Picasso.get()
                .load(group.getImage_url()).placeholder(R.drawable.profile_image)
                .resize(120, 120)
                .into(holder.image_profile);

        // Log.d("joinGroupAdapter", "----group na------------------------" + group.getGroup_name());

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EnlargedImage.enlargeImage(group.getImage_url(), mcontext);
            }
        });
        final String groupId=group.getGroupid();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent groupChatActivity=new Intent(mcontext.getApplicationContext(), GroupChatActivity.class);
                groupChatActivity.putExtra("groupName", group.getGroup_name());
                groupChatActivity.putExtra("groupId", groupId);
                mcontext.startActivity(groupChatActivity);


            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i("user type ",user_type);
                if(user_type.equals("admin")) {

                    // delete group
                    // groups occurences in db 1.Groups 2.GroupChat 3.User-id-groups
                    // for deleting groups under users pic uid from Groups-users

                    DatabaseReference groupRef=rootRef.getGroupRef();
                    final DatabaseReference userRef=rootRef.getUserRef();
                    DatabaseReference groupChatRef=rootRef.getGroupChatRef();

                    groupChatRef.child(groupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                    groupRef.child(groupId).child("Users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String uid=dataSnapshot.getKey();
                                    userRef.child(uid).child("groups").child(groupId).removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    groupRef.child(groupId).removeValue();
                }

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView        displayName;
        CircleImageView image_profile;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName=itemView.findViewById(R.id.display_name);
            image_profile=itemView.findViewById(R.id.image_profile);

        }
    }
}
