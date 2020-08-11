package com.pakhi.clicksdigital.JoinGroup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.Model.User_request;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinGroupAdapter extends RecyclerView.Adapter<JoinGroupAdapter.ViewHolder> implements View.OnClickListener {

    String current_user_id;
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
        return new JoinGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final JoinGroupAdapter.ViewHolder holder, final int position) {

        current_user_id = FirebaseAuth.getInstance().getUid();
        final Group group = groups.get(position);

        holder.displayName.setText(group.getGroup_name());
        holder.displayName.setTextColor(Color.BLACK);
        holder.description.setVisibility(View.VISIBLE);
        holder.description.setText(group.getDescription());

        Picasso.get()
                .load(group.getImage_url()).placeholder(R.drawable.profile_image)
                .resize(120, 120)
                .into(holder.image_profile);

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra(Const.IMAGE_URL, group.getImage_url());
                v.getContext().startActivity(fullScreenIntent);
            }
        });

        holder.join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //task.execute("saveDataToDatabase", group.getGroupid(), group.getGroup_name());
                // sentRequestToJoinGroup(group.getGroupid(),group.getGroup_name());
                sendRequest(group.getGroupid());
            }
        });
    }

    private void sendRequest(String groupid) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(ConstFirebase.groups);
        reference.child(groupid).child(ConstFirebase.userRequests).child(current_user_id).child("");
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(ConstFirebase.users);
        userRef.child(current_user_id).child(ConstFirebase.groupRequests).child(groupid).setValue("");
    }

    private void sentRequestToJoinGroup(String group_id, final String displayName) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference(ConstFirebase.userRequests);

        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        String group_request_id = reference.push().getKey();

        User_request request = new User_request(displayName, group_id, saveCurrentDate, current_user_id, "pending", group_request_id, saveCurrentTime);

        reference.child(group_request_id).setValue(request);

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(ConstFirebase.users);
        userRef.child(current_user_id).child(ConstFirebase.groupRequests).child(group_id).setValue(group_request_id);

    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.join_btn:
                break;
            case R.id.image_profile:
                break;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView displayName, description, status_of_request;
        CircleImageView image_profile;
        Button join_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            description = itemView.findViewById(R.id.description);
            image_profile = itemView.findViewById(R.id.image_profile);
            status_of_request = itemView.findViewById(R.id.status_of_request);
            join_btn = itemView.findViewById(R.id.join_btn);

            join_btn.setVisibility(View.VISIBLE);
            description.setVisibility(View.VISIBLE);
        }
    }

    private final class AsyncOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String param = params[0];
            String s = "";
            switch (param) {
                case "getUserId":
                    current_user_id = FirebaseAuth.getInstance().getUid();
                    break;
                case "saveDataToDatabase":
                    String groupId = params[1];
                    String groupName = params[2];
                    // sentRequestToJoinGroup(groupId, groupName);
                    break;
            }
            return s;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}
