package com.pakhi.clicksdigital;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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

import java.util.HashMap;

public class SetProfileActivity extends AppCompatActivity {


    private static final String TAG = "ProfileActivity";
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    Uri picImageUri;
    FirebaseAuth firebaseAuth;
    String number;
    StorageReference storageReference;
    private ImageView profile_img, done_btn;
    private EditText full_name, email, weblink, bio;
    private ProgressDialog progressDialog;
    private String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Activity", "ProfileAcitivity");
        editor.commit();

        number = getIntent().getStringExtra("number");
        // number = "+918007997748";
        firebaseAuth = FirebaseAuth.getInstance();
        profile_img = findViewById(R.id.profile_img);
        full_name = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        weblink = findViewById(R.id.weblink);
        bio = findViewById(R.id.bio);
        done_btn = findViewById(R.id.done_btn);
        progressDialog=new ProgressDialog(SetProfileActivity.this);
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
    }

    private void uploadData(final String full_name_str, final String email_str, final String bio_str, final String weblink_str) {

        final String userid = firebaseAuth.getCurrentUser().getUid();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> hashMap = new HashMap<>();

                hashMap.put("name", full_name_str);
                hashMap.put("email", email_str);
                hashMap.put("bio", bio_str);
                hashMap.put("number", number);
                hashMap.put("weblink", weblink_str);
                hashMap.put("gender",gender);
                if(number.equals("+9180079 97748")){
                    hashMap.put("user_type","admin");
                }
                else hashMap.put("user_type","user");
                reference.child(userid).setValue(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //showToast("check your internet connection ");
                showToast(databaseError.getMessage().toString());
            }
        });
    }

    private void createUserProfile() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child("Users_photos").child(uid).child("ProfileImage");

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
