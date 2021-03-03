package com.pakhi.clicksdigital.Fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.ArrayList;


public class FullScreenTopicImageView extends AppCompatActivity {
    int position;
    ArrayList<String> images = new ArrayList<>();
    ArrayList<String> eventName = new ArrayList<>();
    ArrayList<String> uploader = new ArrayList<>();
    FirebaseDatabaseInstance rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_topic_image_view);
        position = getIntent().getIntExtra(Const.position,0);
        rootRef = FirebaseDatabaseInstance.getInstance();
        final ViewPager viewPager;
        viewPager = findViewById(R.id.viewPagerMain_);
        ImageView crossButton = findViewById(R.id.close_butt);
        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        images.clear();
        eventName.clear();
        uploader.clear();

        final FullScreenTopicAdapter adapter = new FullScreenTopicAdapter(images, eventName, uploader, getApplicationContext());

        rootRef.getsliderRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (snap.child(ConstFirebase.url).exists() && snap.child(ConstFirebase.nameOfEvent).exists() && snap.child(ConstFirebase.sender).exists()) {
                            images.add(0, snap.child(ConstFirebase.url).getValue().toString());
                            eventName.add(0, snap.child(ConstFirebase.nameOfEvent).getValue().toString());
                            uploader.add(0, snap.child(ConstFirebase.sender).getValue().toString());
                            adapter.notifyDataSetChanged();
                        }
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
        Log.i("position ----", String.valueOf(position));


    }
}
