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
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CreateNewGroupActivity extends AppCompatActivity {
    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    Uri picImageUri;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference;
    String user_type;
    private ImageView profile_img;
    private EditText display_name, description;
    private FloatingActionButton done_btn;
    private ProgressDialog progressDialog;
    private Button send_request_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        //user_type = getIntent().getStringExtra("user_type");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        user_type=pref.getString("user_type","user");
        //user_type = "admin";
        profile_img = findViewById(R.id.profile_img);
        done_btn = findViewById(R.id.done_btn);
        display_name = findViewById(R.id.display_name);
        description = findViewById(R.id.description);
        send_request_btn = findViewById(R.id.send_request_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(CreateNewGroupActivity.this);
        progressDialog.setMessage("Loading...");

        if (user_type.equals("user")) {
            done_btn.setVisibility(View.INVISIBLE);
            send_request_btn.setVisibility(View.VISIBLE);

        }
        if (user_type.equals("admin")) {
            send_request_btn.setVisibility(View.INVISIBLE);
            done_btn.setVisibility(View.VISIBLE);
        }

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
                String groupName = display_name.getText().toString().trim();
                String description_str = description.getText().toString().trim();
                if (TextUtils.isEmpty(groupName)) {
                    showToast("Group name cannot be empty");
                } else {
                    progressDialog.show();
                    updateGroupInfo(groupName, description_str);
                    createGroup();
                }
            }
        });

        send_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = display_name.getText().toString().trim();
                String description_str = description.getText().toString().trim();
                if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(description_str)) {
                    showToast("Group name and description cannot be empty");
                } else {
                    progressDialog.show();
                    sendRequestToCreateGroup(groupName, description_str);
                }
            }
        });

    }

    private void sendRequestToCreateGroup(final String groupName, final String description_str) {
        final String userid = firebaseAuth.getCurrentUser().getUid();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group_requests");
        final SimpleDateFormat sdf_date = new SimpleDateFormat("dd/mm/yyyy");

                String group_request_id = reference.push().getKey();
                HashMap<String, Object> hashMap = new HashMap<>();

                hashMap.put("group_name", groupName);
                hashMap.put("description", description_str);
                hashMap.put("group_request_id", group_request_id);
                hashMap.put("date", sdf_date.format(new Date()));
                hashMap.put("requesting_user", userid);
                hashMap.put("request_status", "pending");

                reference.child(group_request_id).setValue(hashMap);
                goToActivity();

    }

    private void goToActivity() {
        Intent homeActivity = new Intent(getApplicationContext(), StartActivity.class);
        progressDialog.dismiss();
        startActivity(homeActivity);
        finish();
    }

    private void updateGroupInfo(final String groupName, final String description_str) {
        final String userid = firebaseAuth.getCurrentUser().getUid();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        final String groupid = reference.push().getKey();

        final SimpleDateFormat sdf_date = new SimpleDateFormat("dd/mm/yyyy");
        final SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm:ss");

                //Log.d("CreateNewGroupActivity",groupid+"---groupid-------------------------");

                HashMap<String, Object> hashMap = new HashMap<>();

                hashMap.put("group_name", groupName);
                hashMap.put("description", description_str);
                hashMap.put("groupid", groupid);
                hashMap.put("uid_creater", userid);
                hashMap.put("date", sdf_date.format(new Date()));
                hashMap.put("time", sdf_time.format(new Date()));
                hashMap.put("image_url", picImageUri.toString());

                reference.child(groupid).setValue(hashMap);

    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
    }

    private void checkAndRequestForPermissions() {
        if (ContextCompat.checkSelfPermission(CreateNewGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(CreateNewGroupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showToast("Please accept for required permission");
            } else {
                ActivityCompat.requestPermissions(CreateNewGroupActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode
                );
            }
        } else {
            openGallery();
        }
    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(), StartActivity.class);
        progressDialog.dismiss();
        startActivity(homeActivity);
        finish();
    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createGroup() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child("Group_photos").child("Group_profile");

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
                                        showToast("new group created");
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

    private void showToast(String s) {
        Toast.makeText(CreateNewGroupActivity.this, s, Toast.LENGTH_SHORT).show();
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

}
