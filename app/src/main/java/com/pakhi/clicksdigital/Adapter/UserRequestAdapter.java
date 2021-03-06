package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Profile.ProfileUserRequest;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

import java.util.List;

//import de.hdodenhof.circleimageview.CircleImageView;

public class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.ViewHolder> {

    FirebaseDatabaseInstance rootRef;
    private Context mcontext;
    private List<String> requestingUsers;

    public UserRequestAdapter(Context mcontext, List<String> requestingUsers) {
        this.mcontext = mcontext;
        this.requestingUsers = requestingUsers;
    }

    @NonNull
    @Override
    public UserRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext)
                .inflate(R.layout.item_user, parent, false);
        rootRef = FirebaseDatabaseInstance.getInstance();
        return new UserRequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserRequestAdapter.ViewHolder holder, final int position) {
        final String userId = requestingUsers.get(position);

        /*  final User_request userRequest = userRequests.get(position);
        final String userId;
        final String groupId;
        final String group_name;
        final String[] user_name = new String[1];
        userId = userRequest.getRequesting_user();
        final String image[] = new String[1];
       */

        rootRef.getUserRef().child(userId).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //user_name[0] = dataSnapshot.child(Constfirebase.USER_NAME).getValue().toString();
                    holder.displayName.setText(dataSnapshot.child(ConstFirebase.USER_NAME).getValue().toString());
                    //if (dataSnapshot.hasChild("image_url")) {}
                    final String image_url = dataSnapshot.child(ConstFirebase.IMAGE_URL).getValue().toString();
                    Glide.with(mcontext).load(image_url).placeholder(R.drawable.profile_image).transform(new CenterCrop(), new RoundedCorners(10)).into(holder.image_profile);

                    holder.image_profile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enlargeImage(image_url);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitUsersProfile(userId);
            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserRequest(userId, position);
                // sendUserTheRejectionMessage(groupName);
                // createDialog("");
            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // addUserToGroup(groupId, userId);
                approveUser(userId);
                deleteUserRequest(userId, position);
                //sendUserTheWelcomeMessage(groupName, groupId);
            }
        });
    }

    private void deleteUserRequest(String userId, int position) {
        requestingUsers.remove(position);
        rootRef.getUserRequestsRef().child(userId).removeValue();
    }

    private void approveUser(String userId) {
        // one field in user details showing request is approved
        //  rootRef.getUserRef().child(userId).child(Constfirebase.USER_DETAILS).child("approved").setValue(true);
        rootRef.getApprovedUserRef().child(userId).setValue(true);

    }

    /*  private void createDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
        builder.setTitle("Enter new Category");
        final String[] message = {s};
        // Set up the input
        final EditText input = new EditText(mcontext);
        input.setText(message[0]);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                message[0] = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }*/

    /* private void sendUserTheWelcomeMessage(String groupName, String groupId) {
         String s=mcontext.getString(R.string.requestAcceptMessage) + " " + groupName;
         String title="Request Accepted";
         Intent resultIntent=new Intent(mcontext, StartActivity.class);
         resultIntent.putExtra(Const.groupName, groupName);
         resultIntent.putExtra(Const.groupId, groupId);
         resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         Notification.autoCancel(mcontext, title, s, resultIntent, 0);
     }


     private void sendUserTheRejectionMessage(String groupName) {
         String s=mcontext.getString(R.string.requestRejectionMessage) + " " + groupName;
         String title="Request rejected";
         Intent resultIntent=new Intent(mcontext, StartActivity.class);
         resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         Notification.autoCancel(mcontext, title, s, resultIntent, 0);
     }
 */
    private void visitUsersProfile(String userId) {
        Intent profileActivity = new Intent(mcontext, ProfileUserRequest.class);
        profileActivity.putExtra(Const.visitUser, userId);
        profileActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mcontext.startActivity(profileActivity);
    }

    private void enlargeImage(String image_url) {

        EnlargedImage.enlargeImage(image_url, mcontext);

    }

   /* private void deleteUserRequest(String userId, int position) {
        requestingUsers.remove(position);
        rootRef.getUserRequestsRef().child(userId).removeValue();
    }*/

    private void addUserToGroup(String groupId, String userId) {
        rootRef.getGroupRef().child(groupId).child(ConstFirebase.users).child(userId).setValue("");
        rootRef.getUserRef().child(userId).child(ConstFirebase.groups).child(groupId).setValue("");
        //databaseReference.child("User_requests").child("request_status").setValue("accepted");
    }

    @Override
    public int getItemCount() {
        return requestingUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView displayName, group_name;

        public ImageView image_profile;
        Button accept, cancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            image_profile = itemView.findViewById(R.id.image_profile);
            group_name = itemView.findViewById(R.id.group_name);
            accept = itemView.findViewById(R.id.request_accept_btn);
            cancel = itemView.findViewById(R.id.request_cancel_btn);

            group_name.setVisibility(View.VISIBLE);
            //accept.setVisibility(View.VISIBLE);
            //cancel.setVisibility(View.VISIBLE);
        }
    }
}