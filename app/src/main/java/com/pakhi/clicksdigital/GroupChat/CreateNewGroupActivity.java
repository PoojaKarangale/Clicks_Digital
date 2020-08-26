package com.pakhi.clicksdigital.GroupChat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CreateNewGroupActivity extends AppCompatActivity {
    static int PReqCode   =1;
    static int REQUESTCODE=1;
    Uri    picImageUri;
    String user_type, currentUserId;
    String saveCurrentTime, saveCurrentDate;
    SimpleDateFormat currentTime, currentDate;
    Calendar                 calendar;
    boolean                  isProfileSelected=false;
    FirebaseDatabaseInstance rootRef;
    private ImageView profile_img;
    private EditText  display_name, description;
    private FloatingActionButton done_btn;
    private ProgressDialog       progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        rootRef=FirebaseDatabaseInstance.getInstance();
        SharedPreference pref=SharedPreference.getInstance();
        user_type=pref.getData(SharedPreference.user_type, getApplicationContext());
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        currentDate=new SimpleDateFormat("MMM dd, yyyy");
        currentTime=new SimpleDateFormat("hh:mm a");

        profile_img=findViewById(R.id.profile_img);
        done_btn=findViewById(R.id.done_btn);
        display_name=findViewById(R.id.display_name);
        description=findViewById(R.id.description);
        done_btn.setVisibility(View.VISIBLE);

        progressDialog=new ProgressDialog(CreateNewGroupActivity.this);
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
                String groupName=display_name.getText().toString().trim();
                String description_str=description.getText().toString().trim();
                if (TextUtils.isEmpty(groupName)) {
                    showToast("Group name cannot be empty");
                } else {
                    if (progressDialog != null)
                        progressDialog.show();
                    createGroup();
                    updateGroupInfo(groupName, description_str);

                }
            }
        });

    }

 /*   private void sendRequestToCreateGroup(final String groupName, final String description_str) {
        final String userid = currentUserId;
        final DatabaseReference reference = rootRef.getGroupRequests();
        // final SimpleDateFormat sdf_date = new SimpleDateFormat("dd/mm/yyyy");

        calendar = Calendar.getInstance();
        saveCurrentDate = currentDate.format(calendar.getTime());
        saveCurrentTime = currentTime.format(calendar.getTime());

        String group_request_id = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("group_name", groupName);
        hashMap.put("description", description_str);
        hashMap.put("group_request_id", group_request_id);
        hashMap.put("date", saveCurrentDate);
        hashMap.put("requesting_user", userid);
        hashMap.put("request_status", "pending");

        reference.child(group_request_id).setValue(hashMap);
        goToActivity();

    }
*/


    private void updateGroupInfo(final String groupName, final String description_str) {
        // final String userid = currentUserId;

        final DatabaseReference reference=rootRef.getGroupRef();
        final String groupid=reference.push().getKey();

        calendar=Calendar.getInstance();
        saveCurrentDate=currentDate.format(calendar.getTime());
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> hashMap=new HashMap<>();

        hashMap.put("group_name", groupName);
        hashMap.put("description", description_str);
        hashMap.put("groupid", groupid);
        hashMap.put("uid_creater", currentUserId);
        hashMap.put("date", saveCurrentDate);
        hashMap.put("time", saveCurrentTime);

        if (isProfileSelected)
            hashMap.put("image_url", picImageUri.toString());
        else
            hashMap.put("image_url", "default_profile");

        reference.child(groupid).setValue(hashMap);

        addAdminToTheGroup(currentUserId, groupid);
    }

    private void addAdminToTheGroup(String userid, String groupid) {
        DatabaseReference groupRef, userRef;
        groupRef=rootRef.getGroupRef().child(groupid).child("Users").child(userid);
        groupRef.setValue("");
        userRef=rootRef.getUserRef().child(userid).child("groups").child(groupid);
        userRef.setValue("");
//        setting group admin
        rootRef.getGroupRef().child(groupid).child("admins").child(userid).setValue("");
    }

    private void openGallery() {
        CropImage.activity().setAspectRatio(16, 9)
                .start(this);
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
        progressDialog.dismiss();
        Intent homeActivity=new Intent(getApplicationContext(), StartActivity.class);
        startActivity(homeActivity);
        finish();
    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createGroup() {
        StorageReference sReference=FirebaseStorage.getInstance().getReference().child("Group_photos").child("Group_profile");

        final StorageReference imgPath=sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        //  Log.d("createNewGroupActivity","-----before----pic img uri-----"+picImageUri.toString());
                        picImageUri=uri;
                        // Log.d("createNewGroupActivity","----after-----pic img uri-----"+picImageUri.toString());
                        showToast("new group created");
                        updateUI();

                    }

                });

            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(CreateNewGroupActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result=CropImage.getActivityResult(data);
                    picImageUri=result.getUri();
                    profile_img.setImageURI(picImageUri);
                    isProfileSelected=true;
                    break;

                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //updateUserStatus("online");
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        rootRef.getUserRef().child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);

    }
}
