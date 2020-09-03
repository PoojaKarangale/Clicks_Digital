package com.pakhi.clicksdigital.Event;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private String                   currentUid;
    private Context                  context;
    private List<Event>              events;
    private SharedPreference         pref;
    private DatabaseReference        userRef;
    private FirebaseDatabaseInstance rootRef;

    public EventAdapter(Context context, List<Event> events) {
        this.context=context;
        this.events=events;
    }

    public static String timestampToDateString(long timestamp) {
       /* SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date(timestamp);
        return dateFormat.format(date);*/

        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp * 1000);
        String date=DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context)
                .inflate(R.layout.item_event, parent, false);
        rootRef=FirebaseDatabaseInstance.getInstance();
        userRef=rootRef.getUserRef();

        pref=SharedPreference.getInstance();
        currentUid=pref.getData(SharedPreference.currentUserId, view.getContext());
        return new EventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventAdapter.ViewHolder holder, int position) {
        final Event event=events.get(position);

        String createrId=event.getCreater_id();
        final User[] organiser=new User[1];
        userRef.child(createrId).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                organiser[0]=snapshot.getValue(User.class);
                holder.organiser_name.setText(organiser[0].getUser_name() + " " + organiser[0].getLast_name());
                holder.organiser_name.setPaintFlags(holder.organiser_name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Picasso.get()
                .load(event.getEvent_image())
                .fit()
                .into(holder.event_image);

        holder.event_name.setText(event.getEventName().toUpperCase());

        holder.time_date_text.setText(event.getStartDate() + " to " + event.getEndDate() + ", " + event.getStartTime() + " to " + event.getEndTime());

        String text=event.getDescription();
        if (text.length() > 82) {
            text=text.substring(0, 82) + "...";
            holder.event_description.setText(Html.fromHtml(text + "<font  color = '#092859'>Read More</font>"));
        } else {
            holder.event_description.setText(text);
        }
        // holder.event_description.setText(event.getDescription());
        holder.event_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventDetailsActivity(event, organiser);
            }
        });
        if (event.getEventType().equals("Online")) {
            holder.cardLayout.setBackgroundResource(R.drawable.card_back_4);
            holder.venue.setText(event.getAddress());
        } else {
            holder.cardLayout.setBackgroundResource(R.drawable.card_background);
            holder.venue.setText(event.getVenu() + ", " + event.getCity());
        }

        if (event.isPayable()) {
            holder.event_fee.setText(String.valueOf("INR " + event.getTotalFee()) + "/-");
        } else {
            holder.event_fee.setText("Free");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventDetailsActivity(event, organiser);
            }
        });

        holder.event_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventDetailsActivity(event, organiser);
            }
        });

        holder.organiser_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent=new Intent(context, VisitProfileActivity.class);
                profileIntent.putExtra("visit_user_id", organiser[0].getUser_id());
                context.startActivity(profileIntent);
            }
        });

        if (event.getCreater_id().equals(currentUid)) {
            //give option to edit and delete ;
            holder.bookBtn.setVisibility(View.GONE);
            holder.editBtn.setVisibility(View.VISIBLE);

        } else {
            holder.bookBtn.setVisibility(View.VISIBLE);
            holder.editBtn.setVisibility(View.GONE);
        }
        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*eventRef.child(event.getEventType()).child(event.getEventId()).removeValue();
                Toast.makeText(v.getContext(), "event deleted", Toast.LENGTH_SHORT).show();
                */
                Intent intent=new Intent(context, EditEventActivity.class);
                intent.putExtra("event", event);
                context.startActivity(intent);
            }
        });

        holder.bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEventDetailsActivity(event, organiser);
            }
        });
    }

    private void openEventDetailsActivity(Event event, User[] organiser) {
        Intent eventDetailsIntent=new Intent(context, EventDetailsActivity.class);
        eventDetailsIntent.putExtra("event", event);
        eventDetailsIntent.putExtra("organiser", organiser[0]);
        context.startActivity(eventDetailsIntent);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView event_image;
        TextView  event_name, time_date_text, event_description, venue, event_fee, organiser_name;
        Button bookBtn, editBtn;
        LinearLayout cardLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            event_image=itemView.findViewById(R.id.event_image);

            event_name=itemView.findViewById(R.id.event_name);
            time_date_text=itemView.findViewById(R.id.time_date_text);
            event_description=itemView.findViewById(R.id.eventDetails);
            venue=itemView.findViewById(R.id.venue);
            event_fee=itemView.findViewById(R.id.event_fee);
            organiser_name=itemView.findViewById(R.id.organiser_name);

            bookBtn=itemView.findViewById(R.id.bookBtn);
            editBtn=itemView.findViewById(R.id.editBtn);

            cardLayout=itemView.findViewById(R.id.card_layout);

        }
    }
}
