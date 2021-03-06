package com.pakhi.clicksdigital.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.FirebaseUser;
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.squareup.picasso.Picasso;

import java.util.List;

//import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.FindFriendViewHolder> {

    private Context    mcontext;
    private List<User> userList;
    private FirebaseUser firebaseUser;

    public FindFriendsAdapter(Context mcontext, List<User> userList) {
        this.mcontext=mcontext;
        this.userList=userList;
    }

    @NonNull
    @Override
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_user, parent, false);
        return new FindFriendsAdapter.FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {

        final User user=userList.get(position);
        if(user.getBlueTick().equals("yes")){
            holder.verified.setVisibility(View.VISIBLE);
        }

        holder.userName.setText(user.getUser_name() + " " + user.getLast_name());
        holder.userStatus.setText(user.getUser_bio());
        Log.d("findFriend", "--------------" + user.getUser_name() + " " + user.getUser_bio());
        final String image_url=user.getImage_url();
       Glide.with(mcontext)
                .load(image_url).placeholder(R.drawable.profile_image)
                .transform(new CenterCrop(), new RoundedCorners(10))
                .into(holder.profile_image);

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //EnlargedImage.enlargeImage(image_url,v.getContext());
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
                        load(image_url).
                        transform(new CenterCrop(), new RoundedCorners(15)).into(imageView);
                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                        800,
                        800));
                builder.show();

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mcontext, LoadImage.class);
                        intent.putExtra(Const.IMAGE_URL, image_url );
                        mcontext.startActivity(intent);
                    }
                });
            }
        });

        holder.chat_with_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatActivity=new Intent(mcontext, ChatActivity.class);
                chatActivity.putExtra(Const.visitUser, user.getUser_id());
                chatActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mcontext.startActivity(chatActivity);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // String visit_user_id = getRef(position).getKey();
                Intent profileIntent=new Intent(mcontext, VisitProfileActivity.class);
                profileIntent.putExtra(Const.visitUser, user.getUser_id());
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mcontext.startActivity(profileIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        ImageView profile_image, verified;
        ImageView       chat_with_friend;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.display_name);
            userStatus=itemView.findViewById(R.id.user_status);
            chat_with_friend=itemView.findViewById(R.id.chat_with_friend);
            verified = itemView.findViewById(R.id.display_verify);

            chat_with_friend.setVisibility(View.VISIBLE);
            userName.setTextColor(Color.BLACK);
            profile_image=itemView.findViewById(R.id.image_profile);
            userStatus.setVisibility(View.VISIBLE);
        }
    }

}

