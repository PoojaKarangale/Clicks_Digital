package com.pakhi.clicksdigital.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class SetProfileActivity extends AppCompatActivity {

    final static int PICK_PDF_CODE = 2342, REQUEST_CODE_FOR_CERTIFICATE = 3;
    private static final String TAG = "ProfileActivity";
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    String userid;
    Uri picImageUri = null;
    FirebaseAuth firebaseAuth;
    String number;
    User user;
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference, RootRef;
    ArrayList<Certificates> certificates;

    String previousActivity;
    private ImageView profile_img, done_btn;
    private EditText full_name, email, weblink, bio;
    private ProgressDialog progressDialog;
    private String gender, user_type;
    //    private Button choose_certificate;
//    private EditText cerifications;
//    private TextView set_cetificate_name;
    private EditText get_working, get_experiences, get_speaker_experience, get_offer_to_community, get_expectations_from_us, get_facebook_link, get_insta_link, get_twiter_link;
    private ImageButton add_more_certificate;
private boolean isNewProfilePicSelected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);


        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        //  number = getIntent().getStringExtra("PhoneNumber");
        number = pref.getString("PhoneNumber", "");

        if (number.equals("+918007997748")) {
            user_type = "admin";
        } else {
            user_type = "user";
        }

        editor.putString("user_type", user_type);
        editor.commit();

        Intent intent = getIntent();
        previousActivity = intent.getStringExtra("PreviousActivity");

        get_working = findViewById(R.id.working);
        get_experiences = findViewById(R.id.experiences);
        get_speaker_experience = findViewById(R.id.speaker_experience);
        get_offer_to_community = findViewById(R.id.offer_to_community);
        get_expectations_from_us = findViewById(R.id.expectations_from_us);
        get_facebook_link = findViewById(R.id.facebook_link);
        get_insta_link = findViewById(R.id.insta_link);
        get_twiter_link = findViewById(R.id.twiter_link);
        // add_more_certificate = findViewById(R.id.add_more_certificate);

        certificates = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        userid = firebaseAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        profile_img = findViewById(R.id.profile_img);
        full_name = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        weblink = findViewById(R.id.weblink);
        bio = findViewById(R.id.bio);
        done_btn = findViewById(R.id.done_btn);
        progressDialog = new ProgressDialog(SetProfileActivity.this);
        progressDialog.setMessage("Loading...");

        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermissions();
                } else {
                    openGallery();
                }
            }
        });

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String full_name_str, email_str, bio_str, weblink_str;
                full_name_str = full_name.getText().toString().trim();
                email_str = email.getText().toString().trim();
                bio_str = bio.getText().toString().trim();
                weblink_str = weblink.getText().toString().trim();

                if (TextUtils.isEmpty(full_name_str)) {
                    showToast("full name cannot be empty");
                } else if (TextUtils.isEmpty(email_str)) {
                    showToast("email cannot be empty");
                } else if (picImageUri == null) {
                    showToast("Select you profile picture");
                } else {
                    progressDialog.show();
                    if(isNewProfilePicSelected)
                    createUserProfile(full_name_str, email_str, bio_str, weblink_str);
                    else
                     uploadData(full_name_str, email_str, bio_str, weblink_str);
                }
            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference();

        add_more_certificate = findViewById(R.id.add_more_certificate);
        add_more_certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_more_certificate_function();
            }
        });
    }

    private void add_more_certificate_function() {
        Intent addMoreCertiIntent = new Intent(SetProfileActivity.this, AddNewCertificateActivity.class);
        startActivityForResult(addMoreCertiIntent, REQUEST_CODE_FOR_CERTIFICATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        VerifyUserExistance();
        updateUserStatus("online");
    }

    private void VerifyUserExistance() {
        String currentUserID = firebaseAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child(Constants.USER_DETAILS).child(Constants.USER_NAME).exists())) {
                    if (previousActivity.equals("RegisterActivity") || previousActivity.equals("RegisterActivity")) {
                        if (dataSnapshot.child("groups").exists()) {
                            sendUserToStartActivity();
                        } else {
                            sendUserToJoinGroupActivity();
                        }
                    }
                    user = dataSnapshot.child(Constants.USER_DETAILS).getValue(User.class);
                    Log.d("setProfileTESTING", user.getUser_name());
                    loadData();
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadData() {
        Picasso.get()
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .resize(120, 120)
                .into(profile_img);
        picImageUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        full_name.setText(user.getUser_name());
        email.setText(user.getUser_email());
        weblink.setText(user.getWeblink());
        bio.setText(user.getUser_bio());
        get_expectations_from_us.setText(user.getExpectations_from_us());
        get_experiences.setText(user.getExperiences());
        get_facebook_link.setText(user.getFacebook_link());
        get_working.setText(user.getWork_profession());
        get_speaker_experience.setText(user.getSpeaker_experience());
        get_offer_to_community.setText(user.getOffer_to_community());
        get_insta_link.setText(user.getInsta_link());
        get_twiter_link.setText(user.getTwiter_link());

    }

    private void sendUserToStartActivity() {
        startActivity(new Intent(SetProfileActivity.this, StartActivity.class));
    }

    private void uploadData(final String full_name_str, final String email_str, final String bio_str, final String weblink_str) {

        String working = "";
        working = get_working.getText().toString();

        String experiences = "";
        experiences = get_experiences.getText().toString();

        String speaker_experience = "";
        speaker_experience = get_speaker_experience.getText().toString();

        String offer_to_community = "";
        offer_to_community = get_offer_to_community.getText().toString();

        String expectations_from_us = "";
        expectations_from_us = get_expectations_from_us.getText().toString();

        String facebook_link = "";
        facebook_link = get_facebook_link.getText().toString();

        String insta_link = "";
        insta_link = get_insta_link.getText().toString();

        String twiter_link = "";

        twiter_link = get_twiter_link.getText().toString();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        User user = new User(expectations_from_us, experiences, facebook_link, gender, insta_link, number, offer_to_community,
                speaker_experience, twiter_link, bio_str, email_str, full_name_str, user_type, weblink_str, working, picImageUri.toString());

        final int[] numberOfCertificate = {-1};
        reference.child(userid).child("certificates").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //showToast(dataSnapshot.getValue().toString());
                Log.d("setProfileTESTING","------------"+dataSnapshot.getValue().toString());
                if (dataSnapshot.exists())
                    numberOfCertificate[0] = (int) dataSnapshot.getChildrenCount();
               // showToast(String.valueOf(numberOfCertificate[0])+"-----------"+dataSnapshot.getChildrenCount());
                Log.d("setProfileTESTING","------------"+numberOfCertificate[0]+"-----------"+dataSnapshot.getChildrenCount());
                Log.d("setProfileTESTING","------------"+numberOfCertificate[0]);
                //numberOfCertificate[0]++;
                for (Certificates c : certificates) {
                    reference.child(userid).child("certificates").child(String.valueOf(numberOfCertificate[0])).setValue(c);
                    numberOfCertificate[0]++;
                }
                certificates.clear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d("setProfileTESTING","------------"+numberOfCertificate[0]);
        //numberOfCertificate[0]++;
       /* for (Certificates c : certificates) {
            reference.child(userid).child("certificates").child(String.valueOf(numberOfCertificate[0])).setValue(c);
            numberOfCertificate[0]++;
        }*/

        reference.child(userid).child(Constants.USER_DETAILS).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                updateUI(userid);
            }
        });
    }

    private void createUserProfile(final String full_name_str, final String email_str, final String bio_str, final String weblink_str) {
        //  String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child(Constants.USER_MEDIA_PATH).child(userid).child(Constants.PHOTOS).child(Constants.PROFILE_IMAGE);
        final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        picImageUri = uri;
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updateProfile(profileUpdate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        showToast("Profile updated");
                                        uploadData(full_name_str, email_str, bio_str, weblink_str);
                                    }
                                });
                    }
                });

            }
        });
    }

    private void updateUI(final String userid) {

        if (previousActivity.equals("RegisterActivity") || previousActivity.equals("RegisterActivity")) {
            RootRef.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.child("groups").exists())) {
                        sendUserToStartActivity();
                    } else {
                        sendUserToJoinGroupActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (previousActivity.equals("ProfileActivity")) {
            //sendUserToProfileActivity(userid);
            finish();
        }
    }

    private void sendUserToProfileActivity(String userid) {
        Intent profileIntent = new Intent(SetProfileActivity.this, ProfileActivity.class);
        profileIntent.putExtra("visit_user_id", userid);
        startActivity(profileIntent);
    }

    private void sendUserToJoinGroupActivity() {
        Intent joinGroupActivityIntent = new Intent(getApplicationContext(), JoinGroupActivity.class);
        startActivity(joinGroupActivityIntent);
        finish();
    }

    private void showToast(String s) {
        Toast.makeText(SetProfileActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {

      /*  Intent galIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galIntent.setType("image/*");
       */
     /*   Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);

      */
        CropImage.activity().setAspectRatio(1, 1)
                .start(SetProfileActivity.this);

    }

    private void checkAndRequestForPermissions() {
        if (ContextCompat.checkSelfPermission(SetProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SetProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showToast("Please accept for required permission");
            } else {
                ActivityCompat.requestPermissions(SetProfileActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode
                );
                openGallery();
            }
        } else {
            openGallery();
        }
    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    picImageUri = result.getUri();
                    isNewProfilePicSelected = true;
                    profile_img.setImageURI(picImageUri);
                    break;
                case REQUEST_CODE_FOR_CERTIFICATE:
                    Certificates certificate;
                    certificate = (Certificates) data.getSerializableExtra("certificate");
                    certificates.add(certificate);
                    showAddedCertificates(certificate, certificates.size());
                    break;
                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddedCertificates(Certificates certificate, int size) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.add_cerificate_layout);
        // Add textview 1
        TextView show_certificate = new TextView(this);
        show_certificate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        show_certificate.setText(size + " " + certificate.getName());
        show_certificate.setTextColor(Color.BLUE);
        // show_certificate.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
        // textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
        linearLayout.addView(show_certificate);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.male:
                if (checked)
                    gender = "Male";
                break;
            case R.id.female:
                if (checked)
                    gender = "Female";
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
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

        RootRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("userState")
                .updateChildren(onlineStateMap);

    }

}
