package com.pakhi.clicksdigital.GroupChat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyGroupsAdapter extends RecyclerView.Adapter<MyGroupsAdapter.ViewHolder> {
    private String      in_current_user_groups="";
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
                Intent fullScreenIntent=new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra(Const.IMAGE_URL, group.getImage_url());
                v.getContext().startActivity(fullScreenIntent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent groupChatActivity=new Intent(mcontext.getApplicationContext(), GroupChatActivity.class);
                groupChatActivity.putExtra("groupName", group.getGroup_name());
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
