package com.pakhi.clicksdigital.GroupChat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pakhi.clicksdigital.HelperClasses.NotificationCountDatabase;
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;


import java.util.HashMap;
import java.util.List;

//import de.hdodenhof.circleimageview.CircleImageView;

public class MyGroupsAdapter extends RecyclerView.Adapter<MyGroupsAdapter.ViewHolder> {
    FirebaseDatabaseInstance rootRef;
    SharedPreference pref;
    private String      user_type,in_current_user_groups="", userId;
    private Context     mcontext;
    private List<Group> groups;

    private boolean sentRequestFlag=false;

    public MyGroupsAdapter(Context mcontext, List<Group> groups) {
        this.mcontext=mcontext;
        this.groups=groups;
    }
    public  MyGroupsAdapter(){}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group, parent, false);
        rootRef=FirebaseDatabaseInstance.getInstance();
        pref=SharedPreference.getInstance();
        user_type = pref.getData(SharedPreference.user_type,mcontext);
        //userId = pref.getData(SharedPreference.currentUserId, mcontext);
        return new MyGroupsAdapter.ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Group group=groups.get(position);
        holder.displayName.setText(group.getGroup_name());
        holder.displayName.setTextColor(Color.BLACK);

        final NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(mcontext);
        String numberOfUnreadMessages = notificationCountDatabase.getSqliteUser_data(Const.number, group.getGroupid());
        String muteOrUnmute = notificationCountDatabase.getSqliteUser_data(Const.mute, group.getGroupid());

        if(muteOrUnmute.equals("true")){
            holder.muteImage.setVisibility(View.VISIBLE);
            holder.crossImage.setVisibility(View.VISIBLE);
        }
        if(!numberOfUnreadMessages.equals("")){
            if(!(Integer.parseInt(numberOfUnreadMessages)==0)){
                holder.cardView.setVisibility(View.VISIBLE);
                holder.counterText.setText(numberOfUnreadMessages);
            }else{
                holder.cardView.setVisibility(View.GONE);
            }
        }



        //  Log.d("joinGroupAdapter", "---image url-----------------" + group.getImage_url());
        /*     Picasso.get()
                .load(group.getImage_url()).placeholder(R.drawable.profile_image)
                .resize(120, 120)
                .into(holder.image_profile);
        */

        final String[] image_url=new String[1];
        StorageReference sReference=FirebaseStorageInstance.getInstance().getRootRef().child(ConstFirebase.groupPhotos).child(ConstFirebase.groupProfile);
        final StorageReference imgPath=sReference.child(group.getGroupid() ); //+ "." + getFileExtention(picImageUri)
        imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                image_url[0]=uri.toString();
                //Picasso.get().load(uri).placeholder(R.drawable.profile_image).into(holder.image_profile);

                Glide.with(mcontext).load(image_url[0]).transform(new CenterCrop(), new RoundedCorners(10)).into(holder.image_profile);

            }

        });

         Log.d("joinGroupAdapter", "----group na------------------------" + group.getGroup_name());

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //EnlargedImage.enlargeImage(group.getImage_url(), mcontext);
                Dialog builder = new Dialog(mcontext);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //nothing;
                    }
                });

                ImageView imageView = new ImageView(mcontext);
                Glide.with(mcontext).
                        load(image_url[0]).
                        transform(new CenterCrop(), new RoundedCorners(15)).into(imageView);
                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                        800,
                        800));
                builder.show();

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mcontext, LoadImage.class);
                        intent.putExtra(Const.IMAGE_URL, image_url[0] );
                        mcontext.startActivity(intent);
                    }
                });
            }
        });
        final String groupId=group.getGroupid();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NotificationCountDatabase notificationCountDatabase1 = new NotificationCountDatabase(mcontext);
                HashMap<String,String> hmap = new HashMap<>();
                hmap.put(Const.grpOrUserID, groupId);
                hmap.put(Const.number,"0");
                String valOfMute = notificationCountDatabase.getSqliteUser_data(Const.mute, groupId);
                if(valOfMute.equals("false")){
                    hmap.put(Const.mute, "false");
                }else {
                    hmap.put(Const.mute, "true");
                }
                notificationCountDatabase.updateDataNotification(hmap);
                notifyDataSetChanged();
                Intent groupChatActivity=new Intent(mcontext, GroupChatActivity.class);
                groupChatActivity.putExtra(Const.groupName, group.getGroup_name());
                Log.d("joinGroupAdapter", "----group na------------------------" + group.getGroup_name());
                groupChatActivity.putExtra(Const.group_id, group.getGroupid());
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
        ImageView image_profile;
        LinearLayout counterLayout;
        TextView counterText;
        CardView cardView;
        ImageView muteImage, crossImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName=itemView.findViewById(R.id.display_name);
            image_profile=itemView.findViewById(R.id.image_profile);
            counterLayout=itemView.findViewById(R.id.counter_layout);
            counterText=itemView.findViewById(R.id.unread_counter);
            cardView=itemView.findViewById(R.id.notifCountCard);
            muteImage=itemView.findViewById(R.id.muteImage);
            crossImage=itemView.findViewById(R.id.cross_image);
        }
    }
}
