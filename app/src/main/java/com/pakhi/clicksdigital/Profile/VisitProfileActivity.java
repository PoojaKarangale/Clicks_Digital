package com.pakhi.clicksdigital.Profile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VisitProfileActivity extends AppCompatActivity {

    boolean isVisterIsAdmin = false;
    boolean isProfileUserIsAdmin = false;
    UserDatabase db;
    FirebaseDatabaseInstance rootRef;
    private String user_id;
    private ImageView profile_image;
    private TextView user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private User user, currentUser;
    private String receiverUserID, senderUserID, Current_State;
    private Button make_admin, removeAdmin, message_btn;
    private DatabaseReference userRef, ChatRequestRef, ContactsRef;
    Button cancel, accept;
    String req="normal";
    LinearLayout acceptLayout;
    private TextView designation, currentCompany, city, expectationsFromComm;
    private TextView expectationsFromUs;

    String currentUserID;
    SharedPreference pref;

    Button block, unblock;
    NestedScrollView nestedScrollView;
    RecyclerView certificateList;
    TextView certfiText;
    LinearLayout certificateLayout;
    MyAdapter myAdapter;
    TextView country, referredBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_profile);


        rootRef = FirebaseDatabaseInstance.getInstance();
        userRef = rootRef.getUserRef();
        user_id = getIntent().getStringExtra(Const.visitUser);
        //req = getIntent().getStringExtra(Const.fromRequest);

        pref=SharedPreference.getInstance();
        currentUserID = pref.getData(SharedPreference.currentUserId, getApplicationContext());

        nestedScrollView = findViewById(R.id.nestedScrollView2);
        final LinearLayout layout = findViewById(R.id.lay);

        certificateLayout = findViewById(R.id.certification_layout);
        certfiText = findViewById(R.id.certifications);
        certificateList = findViewById(R.id.certificates_list);



        rootRef.getBlockRef().child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(currentUserID).exists()){
                        nestedScrollView.setVisibility(View.GONE);
                        layout.setVisibility(View.VISIBLE);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        db = new UserDatabase(this);
        getCurrentUserFromDb();

        initializeMsgRequestFields();


        userRef.child(user_id).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                    Glide.with(getApplicationContext())
                            .load(user.getImage_url())
                            .into(profile_image);

                    if (user.getUser_type().equals("admin")) {

                        isProfileUserIsAdmin = true;

                        if (currentUser.getUser_type().equals("admin")) {

                            isVisterIsAdmin = true;

                        }

                        if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
                            // make him admin btn set visible
                            Log.i("userProfile", String.valueOf(isProfileUserIsAdmin));
                            make_admin.setVisibility(View.VISIBLE);

                        } else {

                            make_admin.setVisibility(View.GONE);
                        }

                        if (isVisterIsAdmin && isProfileUserIsAdmin) {
                            removeAdmin.setVisibility(View.VISIBLE);
                        }
                    }else {
                        if (currentUser.getUser_type().equals("admin")) {

                            isVisterIsAdmin = true;

                        }

                        if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
                            // make him admin btn set visible
                            Log.i("userProfile", String.valueOf(isProfileUserIsAdmin));
                            make_admin.setVisibility(View.VISIBLE);

                        } else {

                            make_admin.setVisibility(View.GONE);
                        }

                        if (isVisterIsAdmin && isProfileUserIsAdmin) {
                            removeAdmin.setVisibility(View.VISIBLE);
                        }
                    }

                    currentCompany.setText(dataSnapshot.child("company").getValue().toString());
                    city.setText(dataSnapshot.child("city").getValue().toString());
                    expectationsFromComm.setText(dataSnapshot.child("expectations_from_us").getValue().toString());
                    expectationsFromUs.setText(dataSnapshot.child("offer_to_community").getValue().toString());
                    bio.setText(dataSnapshot.child("user_bio").getValue().toString());
                    profession.setText(dataSnapshot.child("work_profession").getValue().toString());

                    if(dataSnapshot.child("country").exists()){
                        country.setText(dataSnapshot.child("country").getValue().toString());
                    }
                    if(dataSnapshot.child("referred_by").exists()){
                        referredBy.setText(dataSnapshot.child("referred_by").getValue().toString());
                    }

                    if (user.getUser_type().equals("admin")) {
                        isProfileUserIsAdmin = true;
                        if (currentUser.getUser_type().equals("admin")) {
                            isVisterIsAdmin = true;
                        }

                        if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
                            // make him admin btn set visible
                            Log.i("userProfile", String.valueOf(isProfileUserIsAdmin));
                            make_admin.setVisibility(View.VISIBLE);

                        } else {
                            Log.i("userProfile", String.valueOf(isProfileUserIsAdmin));
                            Log.i("visitProfile", String.valueOf(isVisterIsAdmin));
                            make_admin.setVisibility(View.GONE);
                        }

                        if (isVisterIsAdmin && isProfileUserIsAdmin) {
                            removeAdmin.setVisibility(View.VISIBLE);
                        }
                    }
                    loadData();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

          /*  if (currentUser.getUser_type().equals("admin")) {
            isVisterIsAdmin=true;
        }

        Log.d("USERTYPE","(out) isProfileUserIsAdmin : "+isProfileUserIsAdmin+" isVisterIsAdmin : "+isVisterIsAdmin);

        if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
            // make him admin btn set visible
            make_admin.setVisibility(View.VISIBLE);
            Log.d("USERTYPE","(out of datachange)(in if) isProfileUserIsAdmin : "+isProfileUserIsAdmin+" isVisterIsAdmin : "+isVisterIsAdmin);

        } else {
            Log.d("USERTYPE","(out of datachange)(in else) isProfileUserIsAdmin : "+isProfileUserIsAdmin+" isVisterIsAdmin : "+isVisterIsAdmin);

            make_admin.setVisibility(View.GONE);
        }

        if (isVisterIsAdmin && isProfileUserIsAdmin) {
            removeAdmin.setVisibility(View.VISIBLE);
        }*/

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewProfile(user.getImage_url());

            }
        });

        message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra(Const.visitUser, user.getUser_id());
                chatIntent.putExtra(Const.visit_user_name, user.getUser_name());
                startActivity(chatIntent);
            }
        });
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        // ManageChatRequests();
    }

    private void getCurrentUserFromDb() {
        db.getReadableDatabase();
        Cursor res = db.getAllData();
        if (res.getCount() == 0) {

        } else {
            res.moveToFirst();
            currentUser = new User(res.getString(0), res.getString(1),
                    res.getString(2), res.getString(3), res.getString(4),
                    res.getString(5), res.getString(6), res.getString(7),
                    res.getString(8), res.getString(9), res.getString(10),
                    res.getString(11), res.getString(12), res.getString(13),
                    res.getString(14), res.getString(15), res.getString(16),
                    res.getString(17), res.getString(18));
        }
    }

    private void initializeMsgRequestFields() {

        ChatRequestRef = rootRef.getChatRequestsRef();
        ContactsRef = rootRef.getContactRef();
        //   NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");
        senderUserID = currentUser.getUser_id();
        Log.i("currentUserType", currentUser.getUser_type());
        receiverUserID = user_id;

        profile_image = findViewById(R.id.profile_img);
        user_name_heading = findViewById(R.id.tv_user_name_heading);
        user_name = findViewById(R.id.tv_user_name);
        gender = findViewById(R.id.tv_gender);
        profession = findViewById(R.id.tv_profession);
        bio = findViewById(R.id.tv_user_bio);
        speaker_experience = findViewById(R.id.tv_speaker_experience);
        experience = findViewById(R.id.tv_experiences);


        currentCompany = findViewById(R.id.current_company);
        city = findViewById(R.id.city_user);
        expectationsFromComm = findViewById(R.id.exp_from_comm);
        expectationsFromUs = findViewById(R.id.exp_from_us);

        make_admin = findViewById(R.id.make_admin);
        removeAdmin = findViewById(R.id.remove_admin);
        message_btn = findViewById(R.id.message_btn);

        Current_State = "new";

        acceptLayout = findViewById(R.id.accept_layout);
        cancel = findViewById(R.id.request_cancel_btn);
        accept = findViewById(R.id.request_accept_btn);

        block = findViewById(R.id.block_contact);
        unblock = findViewById(R.id.unblock_contact);

        country = findViewById(R.id.country_user);
        referredBy = findViewById(R.id.tv_referred_by);



    }

    private void loadData() {

        user_name_heading.setText(user.getUser_name()+user.getLast_name());
        user_name.setText(user.getUser_name()+user.getLast_name());
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

        rootRef.getBlockRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(currentUserID).exists()){
                    if(snapshot.child(currentUserID).child(user_id).exists()){
                        unblock.setVisibility(View.VISIBLE);
                    }
                    else {
                        block.setVisibility(View.VISIBLE);
                    }

                }
                else {
                    block.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addCertificationData(final List<Certificates> certificates) {
        ImageView certi_1 = findViewById(R.id.certi_1);
        certi_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (certificates == null) {
                    //Toast.makeText(VisitProfileActivity.this, "No Certificates Provided", Toast.LENGTH_SHORT).show();
                    certificateList.setVisibility(View.GONE);
                    certfiText.setText("No certificate provided");
                } else {
                    LinearLayout certificationLayout = findViewById(R.id.certification_layout);
                    certificationLayout.setVisibility(View.GONE);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    certificateList.setLayoutManager(linearLayoutManager);


                    myAdapter = new MyAdapter(certificates);
                    certificateList.setAdapter(myAdapter);
                }
            }
        });
    }

    private void loadCertification() {
        final List<Certificates> certificates = new ArrayList<Certificates>();
        //Loading the data

        DatabaseReference databaseReference = userRef.child(user_id).child("certificate");
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
        ImageView linkedin = findViewById(R.id.iv_user_linkedin);

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
        ImageView email = findViewById(R.id.iv_user_email);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + user.getUser_email()));
                startActivity(intent);
            }
        });
    }

    private void viewProfile(String image_url) {
        Intent intent = new Intent(VisitProfileActivity.this, LoadImage.class);
        intent.putExtra("image_url", image_url);
        startActivity(intent);

    }

    public void makeAdmin(final View view) {
        DatabaseReference databaseReference = userRef.child(user_id).child(ConstFirebase.USER_DETAILS).child(ConstFirebase.userType);
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
        DatabaseReference databaseReference = userRef.child(user_id).child(ConstFirebase.USER_DETAILS).child(ConstFirebase.userType);
        databaseReference.setValue("user").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                make_admin.setVisibility(View.VISIBLE);
                removeAdmin.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), user.getUser_name() + " is no longer admin now", Toast.LENGTH_SHORT);
            }
        });
    }

    public void blockContact(View view) {
        rootRef.getBlockRef().child(currentUserID).child(user_id).setValue(user_id);
        unblock.setVisibility(View.VISIBLE);
        block.setVisibility(View.GONE);

    }

    public void unBlockContact(View view) {
        rootRef.getBlockRef().child(currentUserID).child(user_id).removeValue();
        unblock.setVisibility(View.GONE);
        block.setVisibility(View.VISIBLE);
    }
}
