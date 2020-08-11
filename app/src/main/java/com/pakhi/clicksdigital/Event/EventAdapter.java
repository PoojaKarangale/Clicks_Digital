package com.pakhi.clicksdigital.Event;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private Context context;
    private List<Event> events;
    private DatabaseReference userRef;

    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_event, parent, false);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return new EventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventAdapter.ViewHolder holder, int position) {
        final Event event = events.get(position);
        String createrId = event.getCreater_id();
        final User[] organiser = new User[1];
        userRef.child(createrId).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                organiser[0] = snapshot.getValue(User.class);
                // createrName = snapshot.child(Const.USER_NAME).toString();
                // createrBio = snapshot.child(Const.USER_BIO).toString();
                // createrImage = snapshot.child(Const.IMAGE_URL).toString();
                holder.organiser_name.setText(organiser[0].getUser_name());
               /*if (createrBio.equals(""))
                    createrBio = "Organiser";*/
                holder.organiser_bio.setText(organiser[0].getUser_bio());
                Picasso.get()
                        .load(organiser[0].getImage_url())
                        .into(holder.organiser_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        if (event.getEvent_type().equals("Online")) {
            holder.location.setVisibility(View.GONE);
            holder.city.setVisibility(View.GONE);
        }
        Picasso.get()
                .load(event.getEvent_image())
                .resize(120, 120)
                .into(holder.event_image);
        holder.payable_text.setText(event.getPayable());
        if (event.getPayable().equals("Paid")) {
            holder.cost.setVisibility(View.VISIBLE);
            holder.cost.setText(event.getCost());
        }
        holder.category.setText(event.getCategory());
        holder.event_name.setText(event.getName());
        holder.city.setText(event.getCity());
        holder.location.setText(event.getLocation());
        String timeDateString = event.getDate() + " " + event.getTime() + " Duration : " + event.getDuration();
        holder.time_date_text.setText(timeDateString);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventDetailsIntent = new Intent(context, EventDetailsActivity.class);
                eventDetailsIntent.putExtra("event", event);
                eventDetailsIntent.putExtra("organiser", organiser[0]);
                context.startActivity(eventDetailsIntent);
            }
        });
        holder.event_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra(Const.IMAGE_URL, event.getEvent_image());
                v.getContext().startActivity(fullScreenIntent);
            }
        });
        holder.organiser_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra(Const.IMAGE_URL, organiser[0].getImage_url());
                v.getContext().startActivity(fullScreenIntent);
            }
        });
        holder.organiser_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(context, VisitProfileActivity.class);
                profileIntent.putExtra("visit_user_id", organiser[0].getUser_id());
                context.startActivity(profileIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView event_image, organiser_image;
        TextView payable_text, time_date_text, event_name, category, city,
                location, organiser_name, organiser_bio, no_of_participants, cost;
        //  Button btn_register;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            event_image = itemView.findViewById(R.id.event_image);
            organiser_image = itemView.findViewById(R.id.organiser_image);
            payable_text = itemView.findViewById(R.id.payable_text);
            time_date_text = itemView.findViewById(R.id.time_date_text);
            event_name = itemView.findViewById(R.id.event_name);
            category = itemView.findViewById(R.id.category);
            city = itemView.findViewById(R.id.city);
            location = itemView.findViewById(R.id.location);
            organiser_name = itemView.findViewById(R.id.organiser_name);
            organiser_bio = itemView.findViewById(R.id.organiser_bio);
            no_of_participants = itemView.findViewById(R.id.no_of_participants);
            //btn_register = itemView.findViewById(R.id.reg_btn);
            cost = itemView.findViewById(R.id.cost);
        }
    }
}
