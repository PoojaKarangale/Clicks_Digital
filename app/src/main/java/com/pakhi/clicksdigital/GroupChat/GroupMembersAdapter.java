package com.pakhi.clicksdigital.GroupChat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.ViewHolder> {
    DatabaseReference userRef, groupRef;
    boolean isClickedMemberIsAdmin=false, isCurrentUserIsAdmin=false;
    SharedPreference         pref;
    String                   currentUserId;
    FirebaseDatabaseInstance rootRef;
    private Context    mcontext;
    private List<User> groupMembers;
    private String     groupid;

    public GroupMembersAdapter(Context mcontext, List<User> groupMembers, String groupid) {
        this.mcontext=mcontext;
        this.groupMembers=groupMembers;
        this.groupid=groupid;
        Log.d("GroupMembersTESTING", String.valueOf(groupMembers.size()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_user, parent, false);
        pref=SharedPreference.getInstance();
        rootRef=FirebaseDatabaseInstance.getInstance();

        groupRef=rootRef.getGroupRef();
        userRef=rootRef.getUserRef();

        currentUserId=pref.getData(SharedPreference.currentUserId, view.getContext());
        return new GroupMembersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final User groupMember=groupMembers.get(position);

        Picasso.get().load(groupMember.getImage_url()).into(holder.profileImage);
        holder.userName.setText(groupMember.getUser_name());
        holder.userStatus.setText(groupMember.getUser_bio());

        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String visit_user_id=groupMembers.get(position).getUser_id();
                viewPhoto(visit_user_id);
            }
        });
        // check is visiting user is admin or not
        groupRef.child(groupid).child("admins").child(groupMembers.get(position).getUser_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    holder.is_admin.setVisibility(View.VISIBLE);
                    isClickedMemberIsAdmin=true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //check if current user is admin or not
        groupRef.child(groupid).child("admins").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String[] options;
                if (dataSnapshot.exists()) {
                    // current user is admin of current group
                    isCurrentUserIsAdmin=true;
                    if (isClickedMemberIsAdmin) {
                        options=new String[]{"view profile", "remove member", "remove group admin"};
                    } else {
                        options=new String[]{"view profile", "remove member", "make group admin"};
                    }
                } else {
                    options=new String[]{"view profile"};
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id=groupMembers.get(position).getUser_id();

                        showOptionsBuilder(options, visit_user_id);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showOptionsBuilder(String[] options, final String visit_user_id) {
        CharSequence optionsShown[]=options;
        AlertDialog.Builder builder=new AlertDialog.Builder(mcontext);
        // builder.setTitle("");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        viewProfile(visit_user_id);
                        break;
                    case 1:
                        removeMemberFromGroup(visit_user_id);
                        break;
                    case 2:
                        if (isClickedMemberIsAdmin) {
                            removeGroupAdmin(visit_user_id);
                        } else {
                            makeGroupAdmin(visit_user_id);
                        }

                        break;
                }
            }
        });
        builder.show();
    }

    private void removeMemberFromGroup(final String visit_user_id) {
        groupRef.child(groupid).child("Users").child(visit_user_id).removeValue();
        userRef.child(visit_user_id).child("groups").child(groupid).removeValue();
        groupRef.child(groupid).child("admins").child(visit_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    groupRef.child(groupid).child("admins").child(visit_user_id).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void viewProfile(String visit_user_id) {
        Intent profileIntent=new Intent(mcontext, VisitProfileActivity.class);
        profileIntent.putExtra("visit_user_id", visit_user_id);
        mcontext.startActivity(profileIntent);
    }

    private void viewPhoto(String image_url) {
        Intent fullScreenIntent=new Intent(mcontext, EnlargedImage.class);
        fullScreenIntent.putExtra(Const.IMAGE_URL, image_url);
        mcontext.startActivity(fullScreenIntent);
    }

    private void makeGroupAdmin(String visit_user_id) {
        groupRef.child(groupid).child("admins").child(visit_user_id).setValue("").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // he is now group admin
            }
        });
    }

    private void removeGroupAdmin(String visit_user_id) {
        groupRef.child(groupid).child("admins").child(visit_user_id).removeValue();
    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView        userStatus, userName, is_admin;
        // ImageView online_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.image_profile);
            userStatus=itemView.findViewById(R.id.user_status);
            //online_status = itemView.findViewById(R.id.user_online_status);
            is_admin=itemView.findViewById(R.id.is_admin);
            userName=itemView.findViewById(R.id.display_name);

            userStatus.setVisibility(View.VISIBLE);

        }
    }
}
