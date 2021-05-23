package com.pakhi.clicksdigital.JoinGroup;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.List;


public class JoinGroupAdapter extends RecyclerView.Adapter<JoinGroupAdapter.ViewHolder> {

    private String current_user_id;
    private SharedPreference pref;
    private FirebaseDatabaseInstance rootRef;
    //AsyncOperation task = new AsyncOperation();
    private Context mcontext;
    private List<Group> groups;

    public JoinGroupAdapter(Context mcontext, List<Group> groups) {
        this.mcontext = mcontext;
        this.groups = groups;
    }

    @NonNull
    @Override
    public JoinGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group, parent, false);
        pref = SharedPreference.getInstance();
        rootRef = FirebaseDatabaseInstance.getInstance();
        current_user_id = pref.getData(SharedPreference.currentUserId, mcontext);
        return new JoinGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final JoinGroupAdapter.ViewHolder holder, final int position) {

        final Group group = groups.get(position);

        holder.displayName.setText(group.getGroup_name());
        holder.displayName.setTextColor(Color.BLACK);
        holder.description.setVisibility(View.VISIBLE);
        holder.description.setText(group.getDescription());
        FirebaseStorageInstance storageRootRef = FirebaseStorageInstance.getInstance();
        final StorageReference imgPath = storageRootRef.getGroupProfileRef().child(group.getGroupid()); //+ "." + getFileExtention(picImageUri)

        imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(mcontext).load(uri).placeholder(R.drawable.profile_image).
                        transform(new CenterCrop(), new RoundedCorners(10)).into(holder.image_profile);
            }

        });

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnlargedImage.enlargeImage(group.getImage_url(), v.getContext());
            }
        });

        rootRef.getApprovedUserRef().child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    holder.join_btn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // first we'll check if the group is present in users group list then visible or hide join button
        rootRef.getUserRef().child(current_user_id).child(ConstFirebase.groups).child(group.getGroupid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.join_btn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rootRef.getGroupRef().child(group.getGroupid()).child(ConstFirebase.users).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    holder.number_of_participants.setText(String.valueOf(snapshot.getChildrenCount()));
                else
                    holder.number_of_participants.setText(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserToGroup(group.getGroupid(), current_user_id);
                holder.join_btn.setVisibility(View.GONE);
            }
        });
    }

    private void addUserToGroup(String groupId, String userId) {
        rootRef.getGroupRef().child(groupId).child(ConstFirebase.users).child(userId).setValue("");
        rootRef.getUserRef().child(userId).child(ConstFirebase.groups).child(groupId).child(ConstFirebase.getNotification).setValue(true);
        rootRef.getUserRef().child(userId).child(ConstFirebase.groups).child(groupId).child(ConstFirebase.noOfMessages).setValue(0);

    }


    @Override
    public int getItemCount() {
        return groups.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView displayName, description, status_of_request, number_of_participants;
        ImageView image_profile;
        Button join_btn;
        ImageView people;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            description = itemView.findViewById(R.id.description);
            image_profile = itemView.findViewById(R.id.image_profile);
            status_of_request = itemView.findViewById(R.id.status_of_request);
            join_btn = itemView.findViewById(R.id.join_btn);
            number_of_participants = itemView.findViewById(R.id.number_of_participants);
            people = itemView.findViewById(R.id.people);

            join_btn.setVisibility(View.VISIBLE);
            description.setVisibility(View.VISIBLE);
            people.setVisibility(View.VISIBLE);
            number_of_participants.setVisibility(View.VISIBLE);
        }
    }
}
