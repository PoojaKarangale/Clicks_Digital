package com.pakhi.clicksdigital.GroupChat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AddMembersToGroupAdapter extends RecyclerView.Adapter<AddMembersToGroupAdapter.ViewHolder> {

    DatabaseReference databaseReference;
    private Context    mcontext;
    private List<User> allUsers;

    public AddMembersToGroupAdapter(Context mcontext, List<User> allUsers) {
        this.mcontext=mcontext;
        this.allUsers=allUsers;
    }

    @NonNull
    @Override
    public AddMembersToGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_user, parent, false);
        return new AddMembersToGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AddMembersToGroupAdapter.ViewHolder holder, int position) {
        final User user=allUsers.get(position);
        Picasso.get().load(user.getImage_url()).placeholder(R.drawable.profile_image).into(holder.image_profile);
        holder.display_name.setText(user.getUser_name());
        holder.user_status.setText(user.getUser_bio());

        holder.checkbox_add.setTag(position);
        holder.checkbox_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer pos=(Integer) holder.checkbox_add.getTag();
                //Toast.makeText(ctx, imageModelArrayList.get(pos).getAnimal() + " clicked!", Toast.LENGTH_SHORT).show();

                if (allUsers.get(pos).getSelected()) {
                    allUsers.get(pos).setSelected(false);
                } else {
                    allUsers.get(pos).setSelected(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return allUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image_profile;
        TextView  display_name, user_status;
        CheckBox checkbox_add;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image_profile=itemView.findViewById(R.id.image_profile);
            display_name=itemView.findViewById(R.id.display_name);
            checkbox_add=itemView.findViewById(R.id.checkbox_add);
            user_status=itemView.findViewById(R.id.user_status);

            checkbox_add.setVisibility(View.VISIBLE);
            user_status.setVisibility(View.VISIBLE);
        }
    }
}
