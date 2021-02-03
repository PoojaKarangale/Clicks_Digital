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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Activities.FindFriendsActivity;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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

        holder.userName.setText(user.getUser_name() + " " + user.getLast_name());
        holder.userStatus.setText(user.getUser_bio());
        Log.d("findFriend", "--------------" + user.getUser_name() + " " + user.getUser_bio());
        final String image_url=user.getImage_url();
        Picasso.get()
                .load(image_url).placeholder(R.drawable.profile_image)
                .resize(120, 120)
                .into(holder.profile_image);

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EnlargedImage.enlargeImage(image_url,v.getContext());
            }
        });

        holder.chat_with_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatActivity=new Intent(mcontext, ChatActivity.class);
                chatActivity.putExtra(Const.visitUser, user.getUser_id());
                mcontext.startActivity(chatActivity);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // String visit_user_id = getRef(position).getKey();
                Intent profileIntent=new Intent(mcontext, VisitProfileActivity.class);
                profileIntent.putExtra(Const.visitUser, user.getUser_id());
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
        CircleImageView profile_image;
        ImageView       chat_with_friend;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.display_name);
            userStatus=itemView.findViewById(R.id.user_status);
            chat_with_friend=itemView.findViewById(R.id.chat_with_friend);

            chat_with_friend.setVisibility(View.VISIBLE);
            userName.setTextColor(Color.BLACK);
            profile_image=itemView.findViewById(R.id.image_profile);
            userStatus.setVisibility(View.VISIBLE);
        }
    }

}

