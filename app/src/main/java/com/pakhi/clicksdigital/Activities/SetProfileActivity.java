package com.pakhi.clicksdigital.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class SetProfileActivity extends AppCompatActivity {

    final static int PICK_PDF_CODE = 2342;
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
    private ImageView profile_img, done_btn;
    private EditText full_name, email, weblink, bio;
    private ProgressDialog progressDialog;
    private String gender, user_type;
    private Button choose_certificate;
    private EditText cerifications;
    private TextView set_cetificate_name;
    private EditText get_working, get_experiences, get_speaker_experience, get_offer_to_community, get_expectations_from_us, get_facebook_link, get_insta_link, get_twiter_link;

    //String str_image_url="";
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

        get_working = findViewById(R.id.working);
        get_experiences = findViewById(R.id.experiences);
        get_speaker_experience = findViewById(R.id.speaker_experience);
        get_offer_to_community = findViewById(R.id.offer_to_community);
        get_expectations_from_us = findViewById(R.id.expectations_from_us);
        get_facebook_link = findViewById(R.id.facebook_link);
        get_insta_link = findViewById(R.id.insta_link);
        get_twiter_link = findViewById(R.id.twiter_link);


        firebaseAuth = FirebaseAuth.getInstance();
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
                    createUserProfile(full_name_str, email_str, bio_str, weblink_str);
                    // uploadData(full_name_str, email_str, bio_str, weblink_str);
                }
            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference();
        choose_certificate = findViewById(R.id.btn_cerification_choose);
        cerifications = findViewById(R.id.cerifications);
        set_cetificate_name = findViewById(R.id.cerifications);

        choose_certificate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str_name_of_file = set_cetificate_name.getText().toString().trim();
                        if (str_name_of_file.equals(""))
                            showToast("Please enter name for your file first");
                        else
                            getPDF();
                    }
                }
        );
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
                if ((dataSnapshot.child(Constants.USER_NAME).exists())) {
                    //Toast.makeText(StartActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                    //sendUserToStartActivity();
                    user = dataSnapshot.getValue(User.class);
                    loadData();

                } else {
                    //SendUserToSetProfileActivity();
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

    private void getPDF() {
        //for greater than lolipop versions we need the permissions asked on runtime
        //so if the permission is not available user will go to the screen to allow storage permission
        Toast.makeText(this, "Allow all the required permissions for this app", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        //creating an intent for file chooser
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF_CODE);
    }

    private void uploadFile(Uri data) {
        userid = firebaseAuth.getCurrentUser().getUid();
        final String str_name_of_file = cerifications.getText().toString();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid).child(Constants.USER_MEDIA_PATH).child(Constants.FILES_PATH);
        Toast.makeText(this, "Wait for file to be uploaded", Toast.LENGTH_SHORT).show();
        progressDialog.show();
        StorageReference sRef = mStorageReference.child(Constants.USER_MEDIA_PATH).child(userid).child("Files/" + str_name_of_file + " " + System.currentTimeMillis() + ".pdf");
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    String url = "";

                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        url = String.valueOf(uri);
                                        mDatabaseReference.child(mDatabaseReference.push().getKey()).setValue(url);
                                        set_cetificate_name.setVisibility(View.VISIBLE);
                                        set_cetificate_name.setText(str_name_of_file + " uploaded successfully");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void uploadData(final String full_name_str, final String email_str, final String bio_str, final String weblink_str) {
        userid = firebaseAuth.getCurrentUser().getUid();
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

        reference.child(userid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                updateUI(userid);
            }
        });
    }

    private void createUserProfile(final String full_name_str, final String email_str, final String bio_str, final String weblink_str) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child(Constants.USER_MEDIA_PATH).child(uid).child(Constants.PHOTOS).child(Constants.PROFILE_IMAGE);
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

        RootRef.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("groups").exists())) {
                    SendUserToSetProfileActivity(userid);
                } else {
                    sendUserToJoinGroupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetProfileActivity(String userid) {
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
                    profile_img.setImageURI(picImageUri);
                    break;
                case PICK_PDF_CODE:
                    if (data.getData() != null) {
                        uploadFile(data.getData());
                    }
                    break;
                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();

            }
        }
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
