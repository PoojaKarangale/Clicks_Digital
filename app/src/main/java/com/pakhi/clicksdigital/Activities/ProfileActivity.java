package com.pakhi.clicksdigital.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profile_image;
    private Button edit_profile;
    private TextView user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_image = findViewById(R.id.profile_img);
        user_name_heading = findViewById(R.id.tv_user_name_heading);
        user_name = findViewById(R.id.tv_user_name);
        gender = findViewById(R.id.tv_gender);
        profession = findViewById(R.id.tv_profession);
        bio = findViewById(R.id.tv_user_bio);
        speaker_experience = findViewById(R.id.tv_speaker_experience);
        experience = findViewById(R.id.tv_experiences);
        mAuth = FirebaseAuth.getInstance();
        edit_profile = findViewById(R.id.edit_profile);

        Toast.makeText(this, "Wait for data to load.", Toast.LENGTH_SHORT).show();

        Picasso.get()
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .resize(120, 120)
                .into(profile_image);

        //Loading the data
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("details");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Toast.makeText(ProfileActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
                loadData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //loading done

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, EditProfile.class);
                intent.putExtra("userdata", user);
                startActivity(intent);
            }
        });

    }

    private void loadData() {
        user_name_heading.setText(user.getUser_name());
        user_name.setText(user.getUser_name());
        gender.setText(user.getGender());

        if(user.getWork_profession().equals("")){
            profession.setText("No Profession Provided");
        }else{
            profession.setText(user.getWork_profession());
        }

        if(user.getUser_bio().equals("")){
            bio.setText("No Bio Provided");
        }else{
            bio.setText(user.getUser_bio());
        }

        if(user.getSpeaker_experience().equals("")){
            speaker_experience.setText("No Speaker Experience");
        }else{
            speaker_experience.setText(user.getSpeaker_experience());
        }

        if(user.getSpeaker_experience().equals("")){
            experience.setText("No Professional Experience added");
        }else{
            experience.setText(user.getExperiences());
        }

        loadCertification();
        socialMediaHandles();
        contactInfo();

    }

    private void socialMediaHandles() {
        ImageView linkedin = findViewById(R.id.iv_user_linkedin);
        ImageView instagram = findViewById(R.id.iv_user_instagram);
        ImageView facebook = findViewById(R.id.iv_user_facebook);
        ImageView twitter = findViewById(R.id.iv_user_twitter);

        //opening the linkedin link
        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user.getWeblink().equals("")){
                    Toast.makeText(ProfileActivity.this, "No Linked-in Profile given", Toast.LENGTH_SHORT).show();
                }else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getWeblink())));
                }
            }
        });

        //opening the insta account
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user.getInsta_link().equals("")){
                    Toast.makeText(ProfileActivity.this, "No Instagram Profile given", Toast.LENGTH_SHORT).show();
                }else{
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getInsta_link())));
                }
            }
        });

        //opening the facebook link
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user.getFacebook_link().equals("")){
                    Toast.makeText(ProfileActivity.this, "No Facebook Profile given", Toast.LENGTH_SHORT).show();
                }else{
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getFacebook_link())));
                }

            }
        });

        //opening the twitter link
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user.getTwiter_link().equals("")){
                    Toast.makeText(ProfileActivity.this, "No Twitter Profile given", Toast.LENGTH_SHORT).show();
                }else{
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getTwiter_link())));
                }
            }
        });

    }

    private void loadCertification() {
        final List<String> certificates = new ArrayList<String>();

        //Loading the data
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child(Constants.USER_MEDIA_PATH).child(Constants.FILES_PATH);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        certificates.add(String.valueOf(childSnapshot.getValue()));
                        Log.e("this", childSnapshot.getKey() + "    " + childSnapshot.getValue());
                    }

                    addCertificationData(certificates);
                }else{
                    addCertificationData(null);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addCertificationData(final List<String> certificates) {
        ImageView certi_1 = findViewById(R.id.certi_1);
        certi_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(certificates == null){
                    Toast.makeText(ProfileActivity.this, "No Certificates Provided", Toast.LENGTH_SHORT).show();
                }else{
                    Uri uri = Uri.parse(certificates.get(0)); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
    }

    private void contactInfo() {
        ImageView email = findViewById(R.id.iv_user_email);
        ImageView number = findViewById(R.id.iv_user_number);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + user.getUser_email()));
                startActivity(intent);
            }
        });

        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + user.getNumber()));
                startActivity(callIntent);
            }
        });
    }

}
