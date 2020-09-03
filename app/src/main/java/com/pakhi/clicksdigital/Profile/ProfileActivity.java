package com.pakhi.clicksdigital.Profile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    static int PReqCode=1;
    Uri          picImageUri=null;
    Button       edit_profile;
    UserDatabase db;
    private String    user_id;
    private ListView  listView;
    private ImageView profile_image;
    private TextView  user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private User              user;
    private DatabaseReference UserRef;
    //    private int[] imagesForListView = {R.drawable.find_friends, R.drawable.my_friends, R.drawable.chat_requests};
    //    private String[] titleForListView = {"Find Friends", "My Friends", " Chat Requests"};
    FirebaseDatabaseInstance rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rootRef=FirebaseDatabaseInstance.getInstance();
        SharedPreference pref=SharedPreference.getInstance();

        UserRef=rootRef.getUserRef();
        user_id=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        Log.d("PROFILETESTING", "-----------------initialize---------");
        // Toast.makeText(this, "initialize controller", Toast.LENGTH_SHORT).show();
        initializeMsgRequestFields();

        Toast.makeText(this, "Wait for data to load.", Toast.LENGTH_SHORT).show();
        db=new UserDatabase(this);
        getUserFromDb();

        Log.d("PROFILETESTING", "-----------------inibefore data load tialize---------");
        loadData();

        Log.d("PROFILETESTING", "----------------data loaded --------");
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CharSequence options[]={"View Photo", "Change Photo"};

                AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
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

            }
        });
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            /*    Intent intent = new Intent(ProfileActivity.this, SetProfileActivity.class);
                intent.putExtra("PreviousActivity", "ProfileActivity");
                startActivity(intent);*/
                Intent intent=new Intent(ProfileActivity.this, EditProfile.class);
                intent.putExtra("User", user);
                startActivity(intent);


            }
        });

    /*    listAdapter = new ListAdapter(this, titleForListView, imagesForListView);
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

        listView.setAdapter(listAdapter);*/
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
        Intent fullScreenIntent=new Intent(ProfileActivity.this, EnlargedImage.class);
        fullScreenIntent.putExtra(Const.IMAGE_URL, image_url);
        startActivity(fullScreenIntent);
    }

    private void getUserFromDb() {
        db.getReadableDatabase();
        Cursor res=db.getAllData();
        if (res.getCount() == 0) {

        } else {
            res.moveToFirst();
            user=new User(res.getString(0), res.getString(1),
                    res.getString(2), res.getString(3), res.getString(4),
                    res.getString(5), res.getString(6), res.getString(7),
                    res.getString(8), res.getString(9), res.getString(10),
                    res.getString(11), res.getString(12), res.getString(13),
                    res.getString(14));
        }
    }

    private void loadData() {
        /*  Picasso.get()
                .load(user.getImage_url())
                .resize(120, 120)
                .into(profile_image);
        */
        /*   Picasso.get()
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .resize(120, 120)
                .into(profile_image);*/

        UserRef.child(user_id).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(Const.IMAGE_URL).exists()) {
                    Picasso.get()
                            .load(snapshot.child(Const.IMAGE_URL).getValue().toString())
                            .resize(120, 120)
                            .into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
        ImageView linkedin=findViewById(R.id.iv_user_linkedin);

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
        final List<Certificates> certificates=new ArrayList<Certificates>();
        //Loading the data

        DatabaseReference databaseReference=UserRef.child(user_id).child("certificates");
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

    private void addCertificationData(final List<Certificates> certificates) {
        ImageView certi_1=findViewById(R.id.certi_1);
        certi_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (certificates == null) {
                    Toast.makeText(ProfileActivity.this, "No Certificates Provided", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("certificates", (Serializable) certificates);
                    ShowCertificatesFragment gmapFragment=new ShowCertificatesFragment();

                    gmapFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, gmapFragment).commit();
                   /* Uri uri = Uri.parse(certificates.get(0)); // missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);*/
                }
            }
        });
    }

    private void contactInfo() {
        ImageView email=findViewById(R.id.iv_user_email);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + user.getUser_email()));
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

        profile_image=findViewById(R.id.profile_img);
        user_name_heading=findViewById(R.id.tv_user_name_heading);
        user_name=findViewById(R.id.tv_user_name);
        gender=findViewById(R.id.tv_gender);
        profession=findViewById(R.id.tv_profession);
        bio=findViewById(R.id.tv_user_bio);
        speaker_experience=findViewById(R.id.tv_speaker_experience);
        experience=findViewById(R.id.tv_experiences);
        edit_profile=findViewById(R.id.edit_profile);
        listView=findViewById(R.id.list_view);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // updateUserStatus("online");
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

        UserRef.child(user_id).child("userState")
                .updateChildren(onlineStateMap);

    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createUserProfile() {

        StorageReference sReference=FirebaseStorage.getInstance().getReference().child(Const.USER_MEDIA_PATH).child(user_id).child(Const.PHOTOS).child(Const.PROFILE_IMAGE);
        final StorageReference imgPath=sReference.child("profile_image");

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        picImageUri=uri;
                        UserProfileChangeRequest profileUpdate=new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();

                        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                        user.updateProfile(profileUpdate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // showToast("Profile updated");
                                        UserRef.child(user_id).child(Const.USER_DETAILS).child(Const.IMAGE_URL).setValue(picImageUri).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
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
                    CropImage.ActivityResult result=CropImage.getActivityResult(data);
                    picImageUri=result.getUri();
                    profile_image.setImageURI(picImageUri);
                    createUserProfile();
                    break;
            }
        }
    }
}
