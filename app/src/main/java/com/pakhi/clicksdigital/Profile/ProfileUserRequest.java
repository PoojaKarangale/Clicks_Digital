package com.pakhi.clicksdigital.Profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Notifications.Notification;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileUserRequest extends AppCompatActivity {

    TextView acceptMessage;
    private static boolean ACCEPTED = false;
    boolean isVisterIsAdmin = false;
    boolean isProfileUserIsAdmin = false;

    FirebaseDatabaseInstance rootRef;
    private String user_id;
    private ImageView profile_image;
    private TextView user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private User user, currentUser;
    private String receiverUserID, senderUserID, Current_State;
    private Button make_admin, removeAdmin, message_btn;
    private DatabaseReference userRef, ChatRequestRef, ContactsRef, userRefAppCan;
    Button cancel, accept;
    String req;
    LinearLayout acceptLayout;
    RecyclerView certificateList;
    TextView certfiText;
    LinearLayout certificateLayout;
    MyAdapter myAdapter;

    TextView country, referredBy, company, city, expFromComm, expFromMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_profile);

        rootRef = FirebaseDatabaseInstance.getInstance();
        userRef = rootRef.getUserRef();
        user_id = getIntent().getStringExtra(Const.visitUser);
        req = getIntent().getStringExtra(Const.fromRequest);

        userRefAppCan = rootRef.getApprovedUserRef();

        acceptMessage = findViewById(R.id.accept_message);
        acceptLayout = findViewById(R.id.accept_layout);

        certificateLayout = findViewById(R.id.certification_layout);
        certfiText = findViewById(R.id.certifications);
        certificateList = findViewById(R.id.certificates_list);

        userRefAppCan.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    if(snap.getKey().equals(user_id)){
                        acceptMessage.setVisibility(View.VISIBLE);
                        acceptLayout.setVisibility(View.GONE);
                        ACCEPTED=true;
                        break;
                    }
                }
                if(!ACCEPTED){
                    acceptLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        getCurrentUserFromDb();

        initializeMsgRequestFields();

        userRef.child(user_id).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                    Picasso.get()
                            .load(user.getImage_url())
                            .resize(120, 120)
                            .into(profile_image);

                    if (user.getUser_type().equals(ConstFirebase.admin)) {
                        isProfileUserIsAdmin = true;
                        if (currentUser.getUser_type().equals(ConstFirebase.admin)) {
                            isVisterIsAdmin = true;
                        }

                        if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
                            // make him admin btn set visible
                            make_admin.setVisibility(View.VISIBLE);

                        } else {

                            make_admin.setVisibility(View.GONE);
                        }

                        if (isVisterIsAdmin && isProfileUserIsAdmin) {
                            removeAdmin.setVisibility(View.VISIBLE);
                        }
                    }
                    if(dataSnapshot.child("country").exists()){
                        country.setText(dataSnapshot.child("country").getValue().toString());
                    }
                    if(dataSnapshot.child("referred_by").exists()){
                        referredBy.setText(dataSnapshot.child("referred_by").getValue().toString());
                    }
                    loadData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
                Notification.sendPersonalNotifiaction(senderUserID, user_id,"Your Dialog Profile has been approved. Feel free to join groups, attend or events and share knowledge while you learn from the community. Stay Hungry, Stay Foolish!", "Profile Request Status", "accepted","");
                acceptLayout.setVisibility(View.GONE);
                rootRef.getApprovedUserRef().child(user.getUser_id()).setValue(true);
                rootRef.getUserRequestsRef().child(user.getUser_id()).removeValue();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean cancelledOrNot = sendCancelReason();
                if(cancelledOrNot){
                    acceptLayout.setVisibility(View.GONE);
                    //acceptMessage.setVisibility(View.VISIBLE);
                }


            }
        });
        // ManageChatRequests();
    }

    private boolean sendCancelReason() {
        final boolean[] cancelledOrNot = {false};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        /*builder.setTitle("Enter new Topic");*/

        LayoutInflater inflater = ((Activity) this).getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_get_cancel, null);
        final EditText cancelMessage = v.findViewById(R.id.cancelText);
        cancelMessage.setText("Your Dialog Profile has not been approved because ");
        builder.setView(v);


        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                String maaa=cancelMessage.getText().toString();
                Notification.sendPersonalNotifiaction(senderUserID, user_id,maaa+". You can apply again after 6 months.  You can still access the Events section and keep learning through the community.", "Profile Request Status", "rejected","");
                rootRef.getUserRequestsRef().child(user.getUser_id()).removeValue();
                //cancelledOrNot[0] =true;
                acceptLayout.setVisibility(View.GONE);
            }
        });

        builder.setNegativeButton("Do it later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        builder.show();
        return cancelledOrNot[0];
    }

    private void getCurrentUserFromDb() {
        UserDatabase db=new UserDatabase(this);
        user = db.getSqliteUser();
    }

    private void initializeMsgRequestFields() {

        ChatRequestRef = rootRef.getChatRequestsRef();
        ContactsRef = rootRef.getContactRef();
        //   NotificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");
        senderUserID = currentUser.getUser_id();
        receiverUserID = user_id;

        profile_image = findViewById(R.id.profile_img);
        user_name_heading = findViewById(R.id.tv_user_name_heading);
        user_name = findViewById(R.id.tv_user_name);
        gender = findViewById(R.id.tv_gender);
        profession = findViewById(R.id.tv_profession);
        bio = findViewById(R.id.tv_user_bio);
        speaker_experience = findViewById(R.id.tv_speaker_experience);
        experience = findViewById(R.id.tv_experiences);

        make_admin = findViewById(R.id.make_admin);
        removeAdmin = findViewById(R.id.remove_admin);
        message_btn = findViewById(R.id.message_btn);
        message_btn.setVisibility(View.GONE);

        Current_State = "new";

        cancel = findViewById(R.id.request_cancel_btn);
        accept = findViewById(R.id.request_accept_btn);

        country = findViewById(R.id.country_user);
        referredBy = findViewById(R.id.tv_referred_by);
        company = findViewById(R.id.current_company);
        city = findViewById(R.id.city_user);
        expFromComm =findViewById(R.id.exp_from_comm);
        expFromMe=findViewById(R.id.exp_from_us);


    }

    private void loadData() {


        String fullName = user.getUser_name()+user.getLast_name();
        user_name_heading.setText(fullName);
        user_name.setText(fullName);
        gender.setText(user.getGender());
        company.setText(user.getCompany());
        city.setText(user.getCity());
        expFromMe.setText(user.getOffer_to_community());
        expFromComm.setText(user.getExpectations_from_us());
        try {
            country.setText(user.getCountry());
            referredBy.setText(user.getReferal());
        }catch (Exception e){

        }
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
        ImageView certi_1 = findViewById(R.id.certi_1);
        certi_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (certificates == null) {
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

        DatabaseReference databaseReference = userRef.child(user_id).child("cerificate");
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
        Intent intent = new Intent(ProfileUserRequest.this, LoadImage.class);
        intent.putExtra(Const.IMAGE_URL, image_url);
        startActivity(intent);
    }

    public void makeAdmin(final View view) {
        DatabaseReference databaseReference = userRef.child(user_id).child(ConstFirebase.USER_DETAILS).child(ConstFirebase.userType);
        databaseReference.setValue(ConstFirebase.admin).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        databaseReference.setValue(ConstFirebase.user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                make_admin.setVisibility(View.VISIBLE);
                removeAdmin.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), user.getUser_name() + " is no longer admin now", Toast.LENGTH_SHORT);
            }
        });
    }
}
