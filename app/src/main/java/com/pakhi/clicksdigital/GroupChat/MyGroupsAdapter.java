package com.pakhi.clicksdigital.GroupChat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
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
        /*     Picasso.get()
                .load(group.getImage_url()).placeholder(R.drawable.profile_image)
                .resize(120, 120)
                .into(holder.image_profile);
        */
        final String[] image_url=new String[1];
        StorageReference sReference=FirebaseStorageInstance.getInstance().getRootRef().child("Group_photos").child("Group_profile");
        final StorageReference imgPath=sReference.child(group.getGroupid() ); //+ "." + getFileExtention(picImageUri)
        imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                image_url[0]=uri.toString();
                Picasso.get().load(uri).placeholder(R.drawable.profile_image).into(holder.image_profile);

            }

        });

         Log.d("joinGroupAdapter", "----group na------------------------" + group.getGroup_name());

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
                Log.d("joinGroupAdapter", "----group na------------------------" + group.getGroup_name());

                groupChatActivity.putExtra("groupId", group.getGroupid());
                mcontext.startActivity(groupChatActivity);

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
