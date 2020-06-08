package com.pakhi.clicksdigital.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String user_id;
    //private FirebaseAuth mAuth;
    boolean isVisterIsAdmin = false;
    boolean isProfileUserIsAdmin = false;
    private ImageView profile_image;
    private Button edit_profile, visit_profile;
    private TextView user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private User user, getUser;
    private String receiverUserID, senderUserID, Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton, make_admin;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user_id = getIntent().getStringExtra("visit_user_id");

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
        make_admin = findViewById(R.id.make_admin);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Toast.makeText(this, "Wait for data to load.", Toast.LENGTH_SHORT).show();


        //Loading the data

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user_id);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Picasso.get()
                        .load(user.getImage_url())
                        .resize(120, 120)
                        .into(profile_image);
                if (user.getUser_type().equals("admin"))
                    isProfileUserIsAdmin = true;
                Toast.makeText(ProfileActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
                loadData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra("image_url_string", user.getImage_url());
                v.getContext().startActivity(fullScreenIntent);
            }
        });

        UserRef.child(mAuth.getCurrentUser().getUid())
                .child("user_type")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String user_type = dataSnapshot.getValue().toString();

                            if (user_type.equals("admin")) {
                                isVisterIsAdmin = true;
                            }
                            if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
                                // make him admin btn set visible
                                make_admin.setVisibility(View.VISIBLE);
                            }
                            Log.d("ProfileActMAKEADMIN",user_type+" ---------- "+isVisterIsAdmin);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        SendMessageRequestButton = findViewById(R.id.accept_msg_request);
        DeclineMessageRequestButton = findViewById(R.id.decline_msg_request);
        //loading done
        Log.d("ProfileActMAKEADMIN",isProfileUserIsAdmin+" ---------- "+isVisterIsAdmin);
        if (user_id.equals(mAuth.getCurrentUser().getUid())) {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
            edit_profile.setVisibility(View.VISIBLE);
        } else {
            //if visiter (current user) is admin
           /* if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
                // make him admin btn set visible
                make_admin.setVisibility(View.VISIBLE);
            }

            */
            SendMessageRequestButton.setVisibility(View.VISIBLE);
            edit_profile.setVisibility(View.INVISIBLE);
        }
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, SetProfileActivity.class);
                intent.putExtra("userdata", user);
                startActivity(intent);
            }
        });


        initializeMsgRequestFields();

        ManageChatRequests();

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
                if (user.getWeblink().equals("")) {
                    Toast.makeText(ProfileActivity.this, "No Linked-in Profile given", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getWeblink())));
                }
            }
        });

        //opening the insta account
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getInsta_link().equals("")) {
                    Toast.makeText(ProfileActivity.this, "No Instagram Profile given", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getInsta_link())));
                }
            }
        });

        //opening the facebook link
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getFacebook_link().equals("")) {
                    Toast.makeText(ProfileActivity.this, "No Facebook Profile given", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(user.getFacebook_link())));
                }

            }
        });

        //opening the twitter link
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.getTwiter_link().equals("")) {
                    Toast.makeText(ProfileActivity.this, "No Twitter Profile given", Toast.LENGTH_SHORT).show();
                } else {
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
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        certificates.add(String.valueOf(childSnapshot.getValue()));
                        Log.e("this", childSnapshot.getKey() + "    " + childSnapshot.getValue());
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

    private void addCertificationData(final List<String> certificates) {
        ImageView certi_1 = findViewById(R.id.certi_1);
        certi_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (certificates == null) {
                    Toast.makeText(ProfileActivity.this, "No Certificates Provided", Toast.LENGTH_SHORT).show();
                } else {
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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + user.getUser_email()));
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

    private void initializeMsgRequestFields() {

        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        Current_State = "new";

        receiverUserID = user_id;
        senderUserID = mAuth.getCurrentUser().getUid();
    }

    private void ManageChatRequests() {
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)) {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")) {
                                Current_State = "request_sent";
                                SendMessageRequestButton.setText("Cancel Chat Request");
                            } else if (request_type.equals("received")) {
                                Current_State = "request_received";
                                SendMessageRequestButton.setText("Accept Chat Request");

                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)) {
                                                Current_State = "friends";
                                                SendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        if (!senderUserID.equals(receiverUserID)) {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessageRequestButton.setEnabled(false);

                    if (Current_State.equals("new")) {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent")) {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received")) {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends")) {
                        RemoveSpecificContact();
                    }
                }
            });
        } else {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    SendMessageRequestButton.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    SendMessageRequestButton.setText("Remove this Contact");

                                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                SendMessageRequestButton.setText("Send Message");

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    Current_State = "request_sent";
                                                                    SendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        UserRef.child(mAuth.getCurrentUser().getUid()).child("userState")
                .updateChildren(onlineStateMap);

    }

    public void makeAdmin(View view) {
        databaseReference.child("user_type").setValue("admin").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this,user.getUser_name()+" is admin now",Toast.LENGTH_SHORT);
            }
        });
    }
}
