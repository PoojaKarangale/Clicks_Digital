package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ForwardMessageAdapter extends RecyclerView.Adapter<ForwardMessageAdapter.ViewHolder> {

    private Context     mcontext;
    private List<Group> allGroups;
    private List<User>  allContacts;
    String groupOrContact="";

    public ForwardMessageAdapter(Context mcontext, List<User> allContacts) {
        this.mcontext=mcontext;
        this.allContacts=allContacts;
    }

    public ForwardMessageAdapter(Context mcontext, List<Group> allGroups, String groupOrContact) {
        this.mcontext=mcontext;
        this.allGroups=allGroups;
        this.groupOrContact=groupOrContact;
    }

    @NonNull
    @Override
    public ForwardMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_user, parent, false);
        return new ForwardMessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ForwardMessageAdapter.ViewHolder holder, int position) {
        if (groupOrContact.equals("Groups")) {

            Group group=allGroups.get(position);
            Picasso.get().load(group.getImage_url()).placeholder(R.drawable.profile_image).into(holder.image_profile);
            holder.display_name.setText(group.getGroup_name());

            holder.checkbox_add.setTag(position);
            holder.checkbox_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Integer pos=(Integer) holder.checkbox_add.getTag();
                    //Toast.makeText(ctx, imageModelArrayList.get(pos).getAnimal() + " clicked!", Toast.LENGTH_SHORT).show();

                    if (allGroups.get(pos).getSelected()) {
                        allGroups.get(pos).setSelected(false);
                    } else {
                        allGroups.get(pos).setSelected(true);
                    }
                }
            });
        } else {

            User user=allContacts.get(position);
            Picasso.get().load(user.getImage_url()).placeholder(R.drawable.profile_image).into(holder.image_profile);
            holder.display_name.setText(user.getUser_name());

            holder.checkbox_add.setTag(position);
            holder.checkbox_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Integer pos=(Integer) holder.checkbox_add.getTag();
                    //Toast.makeText(ctx, imageModelArrayList.get(pos).getAnimal() + " clicked!", Toast.LENGTH_SHORT).show();

                    if (allGroups.get(pos).getSelected()) {
                        allGroups.get(pos).setSelected(false);
                    } else {
                        allGroups.get(pos).setSelected(true);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (groupOrContact.equals("Groups"))
            return allGroups.size();
        else return allContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image_profile;
        TextView  display_name;
        CheckBox  checkbox_add;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_profile=itemView.findViewById(R.id.image_profile);
            display_name=itemView.findViewById(R.id.display_name);
            checkbox_add=itemView.findViewById(R.id.checkbox_add);

            checkbox_add.setVisibility(View.VISIBLE);
        }
    }
}
