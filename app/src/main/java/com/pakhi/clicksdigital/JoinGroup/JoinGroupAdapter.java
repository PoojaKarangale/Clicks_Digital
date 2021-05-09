package com.pakhi.clicksdigital.JoinGroup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinGroupAdapter extends RecyclerView.Adapter<JoinGroupAdapter.ViewHolder> {

    private String                   current_user_id;
    private SharedPreference         pref;
    private FirebaseDatabaseInstance rootRef;
    //AsyncOperation task = new AsyncOperation();
    private Context                  mcontext;
    private List<Group>              groups;

    public JoinGroupAdapter(Context mcontext, List<Group> groups) {
        this.mcontext=mcontext;
        this.groups=groups;
    }

    @NonNull
    @Override
    public JoinGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group, parent, false);
        pref=SharedPreference.getInstance();
        rootRef=FirebaseDatabaseInstance.getInstance();
        current_user_id=pref.getData(SharedPreference.currentUserId, mcontext);
        return new JoinGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final JoinGroupAdapter.ViewHolder holder, final int position) {

        final Group group=groups.get(position);

        holder.displayName.setText(group.getGroup_name());
        holder.displayName.setTextColor(Color.BLACK);
        holder.description.setVisibility(View.VISIBLE);
        holder.description.setText(group.getDescription());

        /*Picasso.get()
                .load(group.getImage_url()).placeholder(R.drawable.profile_image)
                .resize(120, 120)
                .into(holder.image_profile);
        */

        //  final String[] image_url=new String[1];
        FirebaseStorageInstance storageRootRef=FirebaseStorageInstance.getInstance();
        final StorageReference imgPath=storageRootRef.getGroupProfileRef().child(group.getGroupid()); //+ "." + getFileExtention(picImageUri)

        imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //   image_url[0]=uri.toString();
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
        rootRef.getUserRef().child(current_user_id).child("groups").child(group.getGroupid()).addValueEventListener(new ValueEventListener() {
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
        rootRef.getGroupRef().child(group.getGroupid()).child("Users").addValueEventListener(new ValueEventListener() {
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
                //task.execute("saveDataToDatabase", group.getGroupid(), group.getGroup_name());
                //sentRequestToJoinGroup(group.getGroupid(),group.getGroup_name());
                addUserToGroup(group.getGroupid(), current_user_id);
                holder.join_btn.setVisibility(View.GONE);
                //sendRequest(group.getGroupid());
            }
        });
    }

    private void addUserToGroup(String groupId, String userId) {
        rootRef.getGroupRef().child(groupId).child("Users").child(userId).setValue("");
        rootRef.getUserRef().child(userId).child("groups").child(groupId).child("getNotification").setValue(true);
        rootRef.getUserRef().child(userId).child("groups").child(groupId).child("noOfMessages").setValue(0);
        //databaseReference.child("User_requests").child("request_status").setValue("accepted");
    }

    private void sendRequest(String groupid) {
        final DatabaseReference reference=rootRef.getUserRequestsRef();
        reference.child(groupid).child(current_user_id).setValue("");

        showDialog();
     /*  final DatabaseReference groupRef = rootRef.getGroupRef();
        groupRef.child(groupid).child(ConstFirebase.userRequests).child(current_user_id).child("");
        final DatabaseReference userRef = rootRef.getUserRef();
        userRef.child(current_user_id).child(ConstFirebase.groupRequests).child(groupid).setValue("");*/
    }

    private void showDialog() {
        new AlertDialog.Builder(mcontext)

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Request sent")
                .setMessage("your request to join the group is sent successfully, wait for admin to accept it")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                //.setNegativeButton("No", null)
                .show();
    }

    /*    private void sentRequestToJoinGroup(String group_id, final String displayName) {
        final DatabaseReference reference=rootRef.getUserRequestsRef();

        String saveCurrentTime, saveCurrentDate;
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        String group_request_id=reference.push().getKey();

        User_request request=new User_request(displayName, group_id, saveCurrentDate, current_user_id, "pending", group_request_id, saveCurrentTime);

        reference.child(group_request_id).setValue(request);

        rootRef.getUserRef().child(current_user_id).child(ConstFirebase.groupRequests).child(group_id).setValue(group_request_id);

    }*/

    @Override
    public int getItemCount() {
        return groups.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView displayName, description, status_of_request, number_of_participants;
        ImageView image_profile;
        Button          join_btn;
        ImageView       people;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName=itemView.findViewById(R.id.display_name);
            description=itemView.findViewById(R.id.description);
            image_profile=itemView.findViewById(R.id.image_profile);
            status_of_request=itemView.findViewById(R.id.status_of_request);
            join_btn=itemView.findViewById(R.id.join_btn);
            number_of_participants=itemView.findViewById(R.id.number_of_participants);
            people=itemView.findViewById(R.id.people);

            join_btn.setVisibility(View.VISIBLE);
            description.setVisibility(View.VISIBLE);
            people.setVisibility(View.VISIBLE);
            number_of_participants.setVisibility(View.VISIBLE);
        }
    }

    private final class AsyncOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String param=params[0];
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}
