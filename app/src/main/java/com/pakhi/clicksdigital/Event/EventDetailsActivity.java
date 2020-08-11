package com.pakhi.clicksdigital.Event;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PaymentGatewayFiles.PaymentActivity;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

public class EventDetailsActivity extends AppCompatActivity {
    DatabaseReference eventRef, currentEventRef;
    String currentUserId;
    //private CardView cardViewMap;
    ImageView map_img;
    RelativeLayout loclayout;

    private Event event;
    private User organiser;
    private ImageView event_image, organiser_image;
    private TextView payable_text, cost, time_date_text, event_name, category,
            no_of_participants, description, location_city, location,
            organiser_name, organiser_bio;
    private Button join_event_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventRef = FirebaseDatabase.getInstance().getReference().child("Events");
        currentUserId = FirebaseAuth.getInstance().getUid();

        event = (Event) getIntent().getSerializableExtra("event");
        organiser = (User) getIntent().getSerializableExtra("organiser");

        currentEventRef = eventRef.child(event.getEvent_type()).child(event.getEventId());

        initialiseFields();

        if (event.getEvent_type().equals("Online")) {
            loclayout.setVisibility(View.GONE);
        } else {

        }

        loadData();
        currentEventRef.child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int participants = (int) snapshot.getChildrenCount();
                    no_of_participants.setText(participants);
                   /* for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        participantsList.add(dataSnapshot.getKey());
                    }*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        no_of_participants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToEventsParticipants();
            }
        });
        map_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGoogleMap();
            }
        });
        join_event_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event.getPayable().equals("Paid")) {
                    startPaymentGateWay();

                } else {
                    addUserToEventDataBase();
                }
            }
        });
    }

    private void sendUserToEventsParticipants() {
        Intent intent = new Intent(this, EventParticipantsActivity.class);
        intent.putExtra("Event", event);
        startActivity(intent);
    }

    private void startPaymentGateWay() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("Event", event);
        startActivity(intent);
    }

    private void addUserToEventDataBase() {
        currentEventRef.child("Participants").child(currentUserId).setValue("");
    }

    private void openGoogleMap() {
        String map = "http://maps.google.co.in/maps?q=" + event.getLocation();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(intent);
    }

    private void initialiseFields() {
        loclayout = findViewById(R.id.locLayout);
        join_event_btn = findViewById(R.id.register);
        organiser_name = findViewById(R.id.organiser_name);
        event_image = findViewById(R.id.event_image);
        organiser_image = findViewById(R.id.organiser_image);
        payable_text = findViewById(R.id.payable_text);
        cost = findViewById(R.id.cost);
        time_date_text = findViewById(R.id.time_date_text);
        category = findViewById(R.id.category);
        event_name = findViewById(R.id.event_name);
        no_of_participants = findViewById(R.id.no_of_participants);
        description = findViewById(R.id.description);
        location_city = findViewById(R.id.location_city);
        location = findViewById(R.id.location);
        organiser_bio = findViewById(R.id.organiser_bio);
        //cardViewMap = findViewById(R.id.cardViewMap);
        map_img = findViewById(R.id.map_img);
    }

    private void loadData() {
        Picasso.get()
                .load(event.getEvent_image())
                .resize(120, 120)
                .into(event_image);
        Picasso.get()
                .load(organiser.getImage_url())
                .resize(120, 120)
                .into(organiser_image);
        payable_text.setText(event.getPayable());
        cost.setText(event.getCost());
        event_name.setText(event.getName());
        category.setText(event.getCategory());
        description.setText(event.getDescription());
        cost.setText(event.getCost());
        location_city.setText(event.getCity());
        location.setText(event.getLocation());
        organiser_name.setText(organiser.getUser_name());
        organiser_bio.setText(organiser.getUser_bio());

    }
}