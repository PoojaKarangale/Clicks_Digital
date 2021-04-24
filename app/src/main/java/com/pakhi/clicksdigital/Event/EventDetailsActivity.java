package com.pakhi.clicksdigital.Event;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PaymentGatewayFiles.PaymentActivity;
import com.pakhi.clicksdigital.Profile.SetProfileActivity;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.Notification;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.ToastClass;


import java.util.Calendar;
import java.util.Locale;

public class EventDetailsActivity extends AppCompatActivity {
    private String currentUserId;
    TextView organiserBio;

    private ImageView event_image, organiser_image, close_btn;
    private ImageButton gallery;
    private TextView    cost, time_date_text, event_name, category,
            no_of_participants, description, location_city, location, txtLoc,
            organiser_name;
    private Button join_event_btn;

    private Event event;
    private User  organiser;

    private DatabaseReference eventRef, currentEventRef;
    private SharedPreference         pref;
    private FirebaseDatabaseInstance rootRef;

    private ProgressDialog progressDialog;
    String name;
    boolean registered  = false;
    TextView time_date_text2;


    public static String timestampToDateString(long timestamp) {
       /* SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date(timestamp);
        return dateFormat.format(date);*/

        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp * 1000);
        String date=DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        event=(Event) getIntent().getSerializableExtra(Const.event);
        organiser=(User) getIntent().getSerializableExtra(Const.organiser);

        pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef=FirebaseDatabaseInstance.getInstance();
        eventRef=rootRef.getEventRef();
        //currentUserId = FirebaseAuth.getInstance().getUid();

        currentEventRef=eventRef.child(event.getEventId());

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        initialiseFields();

        checkUserIsAlreadyRegisterd();

        loadData();

        if(currentUserId.equals(event.getCreater_id())){
            join_event_btn.setVisibility(View.GONE);
        }


        organiser_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventDetailsActivity.this, VisitProfileActivity.class);
                intent.putExtra(Const.visitUser, organiser.getUser_id());
                startActivity(intent);
            }
        });
        no_of_participants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToEventsParticipants();
            }
        });
        join_event_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event.isPayable()) {
                    startPaymentGateWay();

                } else {
                    addUserToEventDataBase();
                }
            }
        });

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventDetailsIntent = new Intent(EventDetailsActivity.this, EventGalleryActivity.class);
                eventDetailsIntent.putExtra(Const.event, event);
                eventDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(eventDetailsIntent);
            }
        });
    }

    private boolean checkUserIsAlreadyRegisterd() {
        currentEventRef.child(ConstFirebase.participants).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child(currentUserId).exists()) {
                        join_event_btn.setEnabled(false);
                        join_event_btn.setTextColor(Color.GRAY);
                        join_event_btn.setText("you are already registered");
                        registered=true;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return registered;
    }

    private void sendUserToEventsParticipants() {
        Intent intent=new Intent(this, EventParticipantsActivity.class);
        intent.putExtra(Const.Event, event);
        startActivity(intent);
    }

    private void startPaymentGateWay() {
        Intent intent=new Intent(this, PaymentActivity.class);
        intent.putExtra(Const.Event, event);
        startActivity(intent);
    }

    private void addUserToEventDataBase() {
        join_event_btn.setEnabled(false);
        Toast.makeText(this, "you have registerd successully", Toast.LENGTH_SHORT).show();
        currentEventRef.child(ConstFirebase.participants).child(currentUserId).setValue("");
        rootRef.getUserRef().child(currentUserId).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child(ConstFirebase.USER_NAME).getValue().toString();
                Log.i("name---", name);
                if(!currentUserId.equals(event.getCreater_id())){
                    Log.i("entered here --- ", event.getCreater_id());
                    Notification.sendPersonalNotifiaction(event.getEventId(), event.getCreater_id(), name+ "has registered for your event", event.getEventName(), "participant","");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openGoogleMap() {
        String map="http://maps.google.co.in/maps?q=" + event.getAddress();
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(intent);
    }

    private void initialiseFields() {
        gallery=findViewById(R.id.gallery);
        organiserBio=findViewById(R.id.organiser_bio);
        join_event_btn=findViewById(R.id.register);
        organiser_name=findViewById(R.id.organiser_name);
        event_image=findViewById(R.id.event_image);
        organiser_image=findViewById(R.id.organiser_image);
        cost=findViewById(R.id.cost);
        time_date_text=findViewById(R.id.time_date_text);
        time_date_text2=findViewById(R.id.time_date_text_two);
        category=findViewById(R.id.category);
        event_name=findViewById(R.id.event_name);
        no_of_participants=findViewById(R.id.no_of_participants);
        description=findViewById(R.id.description);
        location_city=findViewById(R.id.location_city);
        location=findViewById(R.id.location);
        //txtLoc=findViewById(R.id.txtLoc);
        close_btn=findViewById(R.id.close_btn);
    }

    private void loadData() {
        progressDialog.show();

        Glide.with(getApplicationContext())
                .load(event.getEvent_image())
                .transform(new CenterCrop(), new RoundedCorners(15))
                .into(event_image);


        Glide.with(getApplicationContext()).load(organiser.getImage_url()).
                transform(new CenterCrop(), new RoundedCorners(10))
                .into(organiser_image);
        time_date_text.setText(event.getStartDate()+" - "+event.getStartTime());
        time_date_text2.setText(event.getEndDate()+" - "+event.getEndTime());

        //time_date_text.setText(event.getStartDate() + " to " + event.getEndDate() + ", " + event.getStartTime() + " to " + event.getEndTime());
        event_name.setText(event.getEventName().toUpperCase());
        category.setText(event.getCategory());
        description.setText(event.getDescription());
        organiser_name.setText(organiser.getUser_name());
        location.setText(event.getAddress());
        organiserBio.setText(organiser.getUser_bio());

        if (event.isPayable()) {
            cost.setText(String.valueOf("Event Fee " + event.getTotalFee()) + "/-");
        } else {
            cost.setText(String.valueOf("Event Fee :  " + Html.fromHtml("<font  color = '#092859'><i>Free</i></font>")));
        }
        if (event.getEventType().equals(Const.Online)) {
            location_city.setText(event.getVenu());
            //txtLoc.setVisibility(View.GONE);
            //location.setVisibility(View.GONE);
        } else {
            location_city.setText(event.getVenu() + ", " + event.getCity());
            //txtLoc.setVisibility(View.VISIBLE);
            location.setVisibility(View.VISIBLE);
        }

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!event.getAddress().equals("")) {

                    openMap();
                } else {
                    ToastClass.makeText(getApplicationContext(), "no link provided");
                }
            }
        });
        if(progressDialog!=null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void openMap() {
        Intent browserIntent=new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(event.getAddress()));
        startActivity(browserIntent);
    }


}