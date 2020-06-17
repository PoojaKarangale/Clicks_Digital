package com.pakhi.clicksdigital.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Adapter.ProfileListAdapter;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    static int PReqCode = 1;
    Uri picImageUri = null;
    boolean isVisterIsAdmin = false;
    boolean isProfileUserIsAdmin = false;
    int[] imagesForListView = {R.drawable.find_friends, R.drawable.my_friends, R.drawable.chat_requests};
    ProfileListAdapter profileListAdapter;
    boolean isThisIsMyProfile = false;
    private String user_id;
    private ListView listView;
    private ImageView profile_image;
    private TextView user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private FirebaseAuth mAuth;
    private User user;
    private String receiverUserID, senderUserID, Current_State;
    private Button SendMessageRequestButton, DeclineMessageRequestButton, make_admin, removeAdmin;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private String[] titleForListView = {"Find Friends", "My Friends", " Chat Requests"};

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
        Button edit_profile = findViewById(R.id.edit_profile);
        make_admin = findViewById(R.id.make_admin);
        removeAdmin = findViewById(R.id.remove_admin);
        listView = findViewById(R.id.list_view);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Toast.makeText(this, "Wait for data to load.", Toast.LENGTH_SHORT).show();

        initializeMsgRequestFields();

        //Loading the data

        //databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user_id);
        UserRef.child(user_id).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Picasso.get()
                        .load(user.getImage_url())
                        .resize(120, 120)
                        .into(profile_image);
                if (user.getUser_type().equals("admin")) {
                    isProfileUserIsAdmin = true;
                }
                loadData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        UserRef.child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(Const.USER_DETAILS).child("user_type").exists()) {
                            String user_type = dataSnapshot.child(Const.USER_DETAILS).child("user_type").getValue().toString();

                            if (user_type.equals("admin")) {
                                isVisterIsAdmin = true;
                            }
                            if (isVisterIsAdmin && (!isProfileUserIsAdmin)) {
                                // make him admin btn set visible
                                make_admin.setVisibility(View.VISIBLE);
                            }
                            if (isVisterIsAdmin && isProfileUserIsAdmin) {
                                removeAdmin.setVisibility(View.VISIBLE);
                            }
                            if (user_id.equals(mAuth.getCurrentUser().getUid())) {
                                make_admin.setVisibility(View.GONE);
                                removeAdmin.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                            } else {
                                listView.setVisibility(View.GONE);
                            }
                            Log.d("ProfileActMAKEADMIN", user_type + " ---------- " + isVisterIsAdmin);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        SendMessageRequestButton = findViewById(R.id.accept_msg_request);
        DeclineMessageRequestButton = findViewById(R.id.decline_msg_request);
        //loading done

        Log.d("ProfileActMAKEADMIN", isProfileUserIsAdmin + " ---------- " + isVisterIsAdmin);
        if (user_id.equals(mAuth.getCurrentUser().getUid())) {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
            edit_profile.setVisibility(View.VISIBLE);
            make_admin.setVisibility(View.GONE);
            removeAdmin.setVisibility(View.GONE);
            isThisIsMyProfile = true;
        } else {
            SendMessageRequestButton.setVisibility(View.VISIBLE);
            edit_profile.setVisibility(View.INVISIBLE);
        }

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isThisIsMyProfile) {


                    CharSequence options[] = {"View Photo", "Change Photo"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    // builder.setTitle("");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    viewProfile(user.getImage_url());
                                    break;
                                case 1:
                                    changeProfile();
                                    break;
                            }
                        }
                    });
                    builder.show();
                } else {
                    viewProfile(user.getImage_url());
                }
            }
        });
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, SetProfileActivity.class);
                intent.putExtra("PreviousActivity", "ProfileActivity");
                startActivity(intent);
            }
        });

        ManageChatRequests();

        profileListAdapter = new ProfileListAdapter(this, titleForListView, imagesForListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sendUserToFindFriendsActivity();
                        break;
                    case 1:
                        sendUserToContactActivity();
                        break;
                    case 2:
                        sendUserToConnectionRequestsActivity();
                        break;
                }
            }
        });
        listView.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        listView.setAdapter(profileListAdapter);
    }

    private void changeProfile() {
        if (Build.VERSION.SDK_INT >= 22) {
            checkAndRequestForPermissions();
        } else {
            openGallery();
        }
    }

    private void openGallery() {

        CropImage.activity().setAspectRatio(1, 1)
                .start(ProfileActivity.this);

    }

    private void checkAndRequestForPermissions() {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //showToast("Please accept for required permission");
            } else {
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode
                );
                openGallery();
            }
        } else {
            openGallery();
        }
    }

    private void viewProfile(String image_url) {
        Intent fullScreenIntent = new Intent(ProfileActivity.this, EnlargedImage.class);
        fullScreenIntent.putExtra("image_url_string", image_url);
        startActivity(fullScreenIntent);
    }

    private void sendUserToConnectionRequestsActivity() {
        startActivity(new Intent(this, ConnectionRequests.class));
    }

    private void sendUserToContactActivity() {
        startActivity(new Intent(this, ContactUserActivity.class));
    }

    private void sendUserToFindFriendsActivity() {

        startActivity(new Intent(this, FindFriendsActivity.class));
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
        Toast.makeText(ProfileActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
    }

    private void socialMediaHandles() {
        ImageView linkedin = findViewById(R.id.iv_user_linkedin);

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

    }

    private void loadCertification() {
        final List<String> certificates = new ArrayList<String>();

        //Loading the data
        DatabaseReference databaseReference = UserRef.child(user_id).child(Const.USER_MEDIA_PATH).child(Const.FILES_PATH);
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

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + user.getUser_email()));
                startActivity(intent);
            }
        });

        /*
        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + user.getNumber()));
                startActivity(callIntent);
            }
        });
        */
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
        DatabaseReference databaseReference = UserRef.child(user_id).child(Const.USER_DETAILS).child("user_type");
        databaseReference.setValue("admin").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                make_admin.setVisibility(View.GONE);
                removeAdmin.setVisibility(View.VISIBLE);
                Toast.makeText(ProfileActivity.this, user.getUser_name() + " is admin now", Toast.LENGTH_SHORT);
            }
        });
    }

    public void removeAdmin(View view) {
        DatabaseReference databaseReference = UserRef.child(user_id).child(Const.USER_DETAILS).child("user_type");
        databaseReference.setValue("user").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                make_admin.setVisibility(View.VISIBLE);
                removeAdmin.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, user.getUser_name() + " is no longer admin now", Toast.LENGTH_SHORT);
            }
        });
    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createUserProfile() {
        final String uid = mAuth.getCurrentUser().getUid();
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child(Const.USER_MEDIA_PATH).child(uid).child(Const.PHOTOS).child(Const.PROFILE_IMAGE);
        final StorageReference imgPath = sReference.child("profile_image");

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        picImageUri = uri;
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updateProfile(profileUpdate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // showToast("Profile updated");
                                        UserRef.child(uid).child(Const.USER_DETAILS).child(Const.IMAGE_URL).setValue(picImageUri).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(ProfileActivity.this,"Profile updated",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                    }
                });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    picImageUri = result.getUri();
                    profile_image.setImageURI(picImageUri);
                    createUserProfile();
                    break;
            }
        }
    }
}
