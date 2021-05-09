package com.pakhi.clicksdigital.Event;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.Image;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.ArrayList;


public class FullGalaryView extends AppCompatActivity {

    int position;
    ArrayList<Image> images = new ArrayList<>();
    DatabaseReference eventRef;
    FirebaseDatabaseInstance rootRef;
    Event event;
    ImageView cross;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_galary_view);
        position = getIntent().getIntExtra("position", 0);
        event = (Event) getIntent().getSerializableExtra(Const.event);

        rootRef = FirebaseDatabaseInstance.getInstance();
        eventRef = rootRef.getEventRef().child(event.getEventType()).child(event.getEventId()).child(ConstFirebase.PHOTOS);


        final ViewPager viewPager = findViewById(R.id.viewPagerMain__);
        cross = findViewById(R.id.close_butt_gal);
        final FullScreenGallaryAdapter adapter = new FullScreenGallaryAdapter(FullGalaryView.this, images);

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        imageUrls.add(dataSnapshot.getValue().toString());
//                        imageNames.add(dataSnapshot.getKey());
                        images.add(new Image(dataSnapshot.getKey(), dataSnapshot.getValue().toString()));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        viewPager.setAdapter(adapter);
        viewPager.post(new Runnable() {
            @Override
            public void run() {

                viewPager.setCurrentItem(position);
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}