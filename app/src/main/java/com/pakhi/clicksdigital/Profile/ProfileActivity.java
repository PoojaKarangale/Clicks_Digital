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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
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
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    static int PReqCode = 1;
    Uri picImageUri = null;
    Button edit_profile;

    private String user_id;
    private ListView listView;
    private ImageView profile_image;
    private TextView user_name_heading, user_name, gender, profession, bio, speaker_experience, experience;
    private User user;
    private DatabaseReference UserRef;
    FirebaseDatabaseInstance rootRef;
    private TextView designation, currentCompany, city, expectationsFromComm;
    private TextView expectationsFromUs;
    RecyclerView certificateList;
    MyAdapter myAdapter;
    TextView certifiText;
    TextView country, referredBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rootRef = FirebaseDatabaseInstance.getInstance();
        SharedPreference pref = SharedPreference.getInstance();

        UserRef = rootRef.getUserRef();
        user_id = pref.getData(SharedPreference.currentUserId, getApplicationContext());

        certificateList = findViewById(R.id.certificates_list);
        certifiText = findViewById(R.id.certifications);
        Log.d("PROFILETESTING", "-----------------initialize---------");
        // Toast.makeText(this, "initialize controller", Toast.LENGTH_SHORT).show();
        initializeMsgRequestFields();

        Toast.makeText(this, "Wait for data to load.", Toast.LENGTH_SHORT).show();

        getUserFromDb();

        Log.d("PROFILETESTING", "-----------------inibefore data load tialize---------");
        loadData();

        Log.d("PROFILETESTING", "----------------data loaded --------");
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

            }
        });
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, EditProfile.class);
                intent.putExtra(Const.User, user);
                startActivity(intent);
                finish();
            }
        });

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
        Intent intent = new Intent(ProfileActivity.this, LoadImage.class);
        intent.putExtra(Const.IMAGE_URL, image_url);
        startActivity(intent);
        //EnlargedImage.enlargeImage(image_url,getApplicationContext());
    }

    private void getUserFromDb() {
        UserDatabase db = new UserDatabase(this);
        user = db.getSqliteUser();
    }

    private void loadData() {

        Log.i("currentUserID", user_id);

        user_name_heading.setText(user.getUser_name() + " " + user.getLast_name());
        user_name.setText(user.getUser_name() + " " + user.getLast_name());
        gender.setText(user.getGender());

        Glide.with(getApplicationContext()).load(user.getImage_url()).
                apply(RequestOptions.circleCropTransform())
                .into(profile_image);

        currentCompany.setText(user.getCompany());
        city.setText(user.getCity());
        expectationsFromComm.setText(user.getExpectations_from_us());
        expectationsFromUs.setText(user.getOffer_to_community());
        bio.setText(user.getUser_bio());
        profession.setText(user.getWork_profession());

        try {
            country.setText(user.getCountry());
            referredBy.setText(user.getReferal());

        } catch (Exception e) {

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
        final List<Certificates> certificates = new ArrayList<Certificates>();
        //Loading the data

        DatabaseReference databaseReference = UserRef.child(user_id).child("certificate");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Log.i("certify - ", childSnapshot.getKey());
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
        ImageView certi_1 = findViewById(R.id.certi_1);

        if (certificates == null) {
            certificateList.setVisibility(View.GONE);
            certifiText.setText("No certificate provided");

        } else {

            LinearLayout certificationLayout = findViewById(R.id.certification_layout);
            certificationLayout.setVisibility(View.GONE);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            certificateList.setLayoutManager(linearLayoutManager);

            myAdapter = new MyAdapter(certificates);
            certificateList.setAdapter(myAdapter);
        }
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

    private void initializeMsgRequestFields() {

        profile_image = findViewById(R.id.profile_img);
        user_name_heading = findViewById(R.id.tv_user_name_heading);
        user_name = findViewById(R.id.tv_user_name);
        gender = findViewById(R.id.tv_gender);
        profession = findViewById(R.id.tv_profession);
        bio = findViewById(R.id.tv_user_bio);
        speaker_experience = findViewById(R.id.tv_speaker_experience);
        experience = findViewById(R.id.tv_experiences);
        edit_profile = findViewById(R.id.edit_profile);
        listView = findViewById(R.id.list_view);

        //designation = findViewById(R.id.designation);
        currentCompany = findViewById(R.id.current_company);
        city = findViewById(R.id.city_user);
        expectationsFromComm = findViewById(R.id.exp_from_comm);
        expectationsFromUs = findViewById(R.id.exp_from_us);
        country = findViewById(R.id.country_user);
        referredBy = findViewById(R.id.tv_referred_by);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void createUserProfile() {

        StorageReference sReference = FirebaseStorageInstance.getInstance().getRootRef().child(ConstFirebase.USER_MEDIA_PATH).child(user_id).child(ConstFirebase.PHOTOS).child(ConstFirebase.PROFILE_IMAGE);
        final StorageReference imgPath = sReference.child(ConstFirebase.PROFILE_IMAGE);

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
                                        UserRef.child(user_id).child(ConstFirebase.USER_DETAILS).child(ConstFirebase.IMAGE_URL).setValue(picImageUri).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    picImageUri = result.getUri();
                    profile_image.setImageURI(picImageUri);
                    createUserProfile();
                    break;
            }
        }
    }
}
