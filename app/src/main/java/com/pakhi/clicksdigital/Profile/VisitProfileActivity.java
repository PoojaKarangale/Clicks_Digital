package com.pakhi.clicksdigital.Profile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VisitProfileActivity extends AppCompatActivity {

    boolean      isVisterIsAdmin     =false;
    boolean      isProfileUserIsAdmin=false;
    UserDatabase db;
    FirebaseDatabaseInstance rootRef;
    private String    user_id;
    private ImageView profile_image;
    private TextView  user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private User user, currentUser;
    private String receiverUserID, senderUserID, Current_State;
    private Button make_admin, removeAdmin, message_btn;
    private DatabaseReference userRef, ChatRequestRef, ContactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_profile);

        rootRef=FirebaseDatabaseInstance.getInstance();
        userRef=rootRef.getUserRef();
        user_id=getIntent().getStringExtra(Const.visitUser);
        db=new UserDatabase(this);
        getCurrentUserFromDb();

        initializeMsgRequestFields();

        userRef.child(user_id).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user=dataSnapshot.getValue(User.class);
                    Picasso.get()
                            .load(user.getImage_url())
                            .resize(120, 120)
                            .into(profile_image);
                    if (user.getUser_type().equals("admin")) {
                        isProfileUserIsAdmin=true;
                    }
                    loadData();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (currentUser.getUser_type().equals("admin")) {
            isVisterIsAdmin=true;
        }
        if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
            // make him admin btn set visible
            make_admin.setVisibility(View.VISIBLE);
        } else {
            make_admin.setVisibility(View.INVISIBLE);
        }
        if (isVisterIsAdmin && isProfileUserIsAdmin) {
            removeAdmin.setVisibility(View.VISIBLE);
        }

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewProfile(user.getImage_url());

            }
        });

        message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent=new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra(Const.visitUser, user.getUser_id());
                chatIntent.putExtra(Const.visit_user_name, user.getUser_name());
                startActivity(chatIntent);
            }
        });
        // ManageChatRequests();
    }

    private void getCurrentUserFromDb() {
        db.getReadableDatabase();
        Cursor res=db.getAllData();
        if (res.getCount() == 0) {

        } else {
            res.moveToFirst();
            currentUser=new User(res.getString(0), res.getString(1),
                    res.getString(2), res.getString(3), res.getString(4),
                    res.getString(5), res.getString(6), res.getString(7),
                    res.getString(8), res.getString(9), res.getString(10),
                    res.getString(11), res.getString(12), res.getString(13),
                    res.getString(14));
        }
    }

    private void initializeMsgRequestFields() {

        ChatRequestRef=rootRef.getChatRequestsRef();
        ContactsRef=rootRef.getContactRef();
        //   NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");
        senderUserID=currentUser.getUser_id();
        receiverUserID=user_id;

        profile_image=findViewById(R.id.profile_img);
        user_name_heading=findViewById(R.id.tv_user_name_heading);
        user_name=findViewById(R.id.tv_user_name);
        gender=findViewById(R.id.tv_gender);
        profession=findViewById(R.id.tv_profession);
        bio=findViewById(R.id.tv_user_bio);
        speaker_experience=findViewById(R.id.tv_speaker_experience);
        experience=findViewById(R.id.tv_experiences);

        make_admin=findViewById(R.id.make_admin);
        removeAdmin=findViewById(R.id.remove_admin);
        message_btn=findViewById(R.id.message_btn);

        Current_State="new";

    }

    private void loadData() {

        user_name_heading.setText(user.getUser_name());
        user_name.setText(user.getUser_name());
        gender.setText(user.getGender());

        if (user.getWork_profession().equals("")) {
            profession.setText("No Profession Provided");
        } else {
            profession.setText(user.getWork_profession());
        }

        if (user.getUser_bio().equals("")) {
            bio.setText("No Bio Provided");
        } else {
            bio.setText(user.getUser_bio());
        }

        if (user.getSpeaker_experience().equals("")) {
            speaker_experience.setText("No Speaker Experience");
        } else {
            speaker_experience.setText(user.getSpeaker_experience());
        }

        if (user.getSpeaker_experience().equals("")) {
            experience.setText("No Professional Experience added");
        } else {
            experience.setText(user.getExperiences());
        }

        loadCertification();
        socialMediaHandles();
        contactInfo();
        Toast.makeText(this, "Data Loaded", Toast.LENGTH_SHORT).show();
    }

    private void addCertificationData(final List<Certificates> certificates) {
        ImageView certi_1=findViewById(R.id.certi_1);
        certi_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (certificates == null) {
                    Toast.makeText(VisitProfileActivity.this, "No Certificates Provided", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("certificates", (Serializable) certificates);
                    ShowCertificatesFragment gmapFragment=new ShowCertificatesFragment();
                    gmapFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, gmapFragment).commit();
                   /* Uri uri = Uri.parse(certificates.get(0)); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);*/
                }
            }
        });
    }

    private void loadCertification() {
        final List<Certificates> certificates=new ArrayList<Certificates>();
        //Loading the data

        DatabaseReference databaseReference=userRef.child(user_id).child("cerificates");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        certificates.add(childSnapshot.getValue(Certificates.class));
//                        certificates.add(String.valueOf(childSnapshot.getValue()));
//                        Log.e("this", childSnapshot.getKey() + "    " + childSnapshot.getValue());
                    }
                    addCertificationData(certificates);
                } else {
                    addCertificationData(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void socialMediaHandles() {
        ImageView linkedin=findViewById(R.id.iv_user_linkedin);

        //opening the linkedin link
        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getWeblink().equals("")) {
                    Toast.makeText(view.getContext(), "No Linked-in Profile given", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getWeblink())));
                }
            }
        });
    }

    private void contactInfo() {
        ImageView email=findViewById(R.id.iv_user_email);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + user.getUser_email()));
                startActivity(intent);
            }
        });
    }

    private void viewProfile(String image_url) {
        EnlargedImage.enlargeImage(image_url, getApplicationContext());
    }

    public void makeAdmin(final View view) {
        DatabaseReference databaseReference=userRef.child(user_id).child(ConstFirebase.USER_DETAILS).child(ConstFirebase.userType);
        databaseReference.setValue("admin").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                make_admin.setVisibility(View.GONE);
                removeAdmin.setVisibility(View.VISIBLE);
                Toast.makeText(view.getContext(), user.getUser_name() + " is admin now", Toast.LENGTH_SHORT);
            }
        });
    }

    public void removeAdmin(final View view) {
        DatabaseReference databaseReference=userRef.child(user_id).child(ConstFirebase.USER_DETAILS).child(ConstFirebase.userType);
        databaseReference.setValue("user").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                make_admin.setVisibility(View.VISIBLE);
                removeAdmin.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), user.getUser_name() + " is no longer admin now", Toast.LENGTH_SHORT);
            }
        });
    }
}
