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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;

public class SetProfileActivity extends AppCompatActivity {

    final static int PICK_PDF_CODE = 2342;
    private static final String TAG = "ProfileActivity";
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    String userid;
    Uri picImageUri;
    FirebaseAuth firebaseAuth;
    String number;

    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;
    private ImageView profile_img, done_btn;
    private EditText full_name, email, weblink, bio;
    private ProgressDialog progressDialog;
    private String gender, user_type;
    private Button choose_certificate;
    private EditText cerifications;
    private TextView set_cetificate_name;

    private EditText get_working, get_experiences, get_speaker_experience, get_offer_to_community, get_expectations_from_us, get_facebook_link, get_insta_link, get_twiter_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        number = getIntent().getStringExtra("PhoneNumber");
        // number="+9180079 97748";
        get_working = findViewById(R.id.working);
        get_experiences = findViewById(R.id.experiences);
        get_speaker_experience = findViewById(R.id.speaker_experience);
        get_offer_to_community = findViewById(R.id.offer_to_community);
        get_expectations_from_us = findViewById(R.id.expectations_from_us);
        get_facebook_link = findViewById(R.id.facebook_link);
        get_insta_link = findViewById(R.id.insta_link);
        get_twiter_link = findViewById(R.id.twiter_link);

        number = getIntent().getStringExtra("PhoneNumber");
        number = "+9180079 97748";
        get_working = findViewById(R.id.working);
        get_experiences = findViewById(R.id.experiences);
        get_speaker_experience = findViewById(R.id.speaker_experience);
        get_offer_to_community = findViewById(R.id.offer_to_community);
        get_expectations_from_us = findViewById(R.id.expectations_from_us);
        get_facebook_link = findViewById(R.id.facebook_link);
        get_insta_link = findViewById(R.id.insta_link);
        get_twiter_link = findViewById(R.id.twiter_link);


        if (number.equals("+9180079 97748")) {
            user_type = "admin";
        } else {
            user_type = "user";
        }


        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("user_type", user_type);
        editor.commit();


        firebaseAuth = FirebaseAuth.getInstance();
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
                } else {
                    progressDialog.show();
                    uploadData(full_name_str, email_str, bio_str, weblink_str);
                    createUserProfile();
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

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_MEDIA_PATH).child(userid).child(Constants.FILES_PATH);
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

        String number_without_special_char = number.replace(" ", "");
        User user = new User(expectations_from_us, experiences, facebook_link, gender, insta_link, number_without_special_char, offer_to_community,
                speaker_experience, twiter_link, bio_str, email_str, full_name_str, user_type, weblink_str, working);


        reference.child(userid).setValue(user);
    }

    private void createUserProfile() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child(Constants.USER_MEDIA_PATH).child(uid).child(Constants.PHOTOS).child(Constants.PROFILE_IMAGE);
        final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updateProfile(profileUpdate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        showToast("Welcome to Digital Clicks");
                                        updateUI();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(), JoinGroupActivity.class);
        progressDialog.dismiss();
        startActivity(homeActivity);
        finish();
    }

    private void showToast(String s) {
        Toast.makeText(SetProfileActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {

      /*  Intent galIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galIntent.setType("image/*");
       */
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
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
        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null) {
            picImageUri = data.getData();
            profile_img.setImageURI(picImageUri);
        } else {
            showToast("Nothing is selected");
        }

        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //if a file is selected
            if (data.getData() != null) {
                uploadFile(data.getData());
            } else {
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.male:
                if (checked)
                    gender = "male";
                break;
            case R.id.female:
                if (checked)
                    gender = "female";
                break;
        }
    }
}
