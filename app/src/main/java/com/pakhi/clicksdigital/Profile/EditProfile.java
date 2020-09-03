package com.pakhi.clicksdigital.Profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
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
import com.pakhi.clicksdigital.Activities.AddNewCertificateActivity;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.ValidateInput;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    final static int REQUEST_CODE_FOR_CERTIFICATE=3, GET_CITY_CODE=100;
    private static final String TAG         ="ProfileActivity";
    static               int    REQUEST_CODE=1;
    String userid;
    Uri    picImageUri;

    String                   number;
    ArrayList<Certificates>  certificates;
    boolean                  isCertificatesAdded=false;
    StorageReference         mStorageReference;
    User                     user;
    UserDatabase             db;
    SharedPreference         pref;
    EditText                 weblink;
    PermissionsHandling      permissions;
    Button                   done_btn;
    ImageView                close;
    FirebaseDatabaseInstance rootRef;
    private boolean          isNewProfilePicSelected=false;
    private ImageView        profile_img;
    private MaterialEditText full_name, email, bio, last_name;
    private ProgressDialog progressDialog;
    private String         gender, user_type;
    private MaterialEditText get_working, get_experiences, get_speaker_experience, get_offer_to_community, get_expectations_from_us, company, get_city;
    private Button add_more_certificate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        Intent intent=getIntent();
        user=(User) intent.getSerializableExtra("userdata");

        pref=SharedPreference.getInstance();
        userid=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef=FirebaseDatabaseInstance.getInstance();
        mStorageReference=FirebaseStorage.getInstance().getReference();

        // rootRef = FirebaseDatabase.getInstance().getReference();

        initializedFields();
        readUserData();
        loadData();

        profile_img.setOnClickListener(this);
        done_btn.setOnClickListener(this);
        add_more_certificate.setOnClickListener(this);
        close.setOnClickListener(this);
    }

    private void initializedFields() {

        get_working=findViewById(R.id.working);
        get_experiences=findViewById(R.id.experiences);
        get_speaker_experience=findViewById(R.id.speaker_experience);
        get_offer_to_community=findViewById(R.id.offer_to_community);
        get_expectations_from_us=findViewById(R.id.expectations_from_us);
        company=findViewById(R.id.company);
        profile_img=findViewById(R.id.profile_img);
        full_name=findViewById(R.id.full_name);
        last_name=findViewById(R.id.last_name);
        email=findViewById(R.id.email);
        weblink=findViewById(R.id.weblink);
        bio=findViewById(R.id.bio);
        done_btn=findViewById(R.id.done_btn);

        progressDialog=new ProgressDialog(EditProfile.this);
        progressDialog.setMessage("Loading...");

        certificates=new ArrayList<>();

        add_more_certificate=findViewById(R.id.add_more_certificate);
        get_city=findViewById(R.id.get_city);
        close=findViewById(R.id.close);
    }


    private void validateInputAndUpdate() {

        if (ValidateInput.field(full_name)
                && ValidateInput.field(last_name)
                && ValidateInput.field(email)
                && ValidateInput.field(bio)
                && ValidateInput.field(get_working)
                && ValidateInput.field(get_expectations_from_us)
                && ValidateInput.field(company)

        ) {
            if (picImageUri == null) {
                showToast("Select you profile picture");
            } else {
                progressDialog.show();
                if (isNewProfilePicSelected)
                    createUserProfile();
                else
                    uploadData();
            }
        }

  /*      if (TextUtils.isEmpty(full_name_str)) {
            showToast("full name cannot be empty");
            full_name.setError("required");
            full_name.requestFocus();
        } else if (TextUtils.isEmpty(last_name_str)) {
            showToast("full name cannot be empty");
            last_name.setError("required");
            last_name.requestFocus();
        } else if (TextUtils.isEmpty(email_str)) {
            showToast("email cannot be empty");
            email.setError("required");
            email.requestFocus();
        } else if (picImageUri == null) {
            showToast("Select you profile picture");
        } else {
            progressDialog.show();
            if (isNewProfilePicSelected)
                createUserProfile(full_name_str, last_name_str, email_str);
            else
                uploadData();
        }*/
    }

    private void add_more_certificate_function() {
        Intent addMoreCertiIntent=new Intent(this, AddNewCertificateActivity.class);
        startActivityForResult(addMoreCertiIntent, REQUEST_CODE_FOR_CERTIFICATE);
    }

    private void readUserData() {
        db=new UserDatabase(this);
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
                    res.getString(14), res.getString(15), res.getString(16));

        }
        // db.close();
    }

    private void loadData() {

        rootRef.getUserRef().child(userid).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(Const.IMAGE_URL).exists()) {
                    // picImageUri=(Uri)snapshot.child(Const.IMAGE_URL).getValue().toString();
                    Picasso.get()
                            .load(snapshot.child(Const.IMAGE_URL).getValue().toString())
                            .resize(120, 120)
                            .into(profile_img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

      /*  Picasso.get()
                .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                .resize(120, 120).placeholder(R.drawable.persone_profile)
                .into(profile_img);*/

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        picImageUri=firebaseAuth.getCurrentUser().getPhotoUrl();

        full_name.setText(user.getUser_name());
        email.setText(user.getUser_email());
        weblink.setText(user.getWeblink());
        bio.setText(user.getUser_bio());
        get_expectations_from_us.setText(user.getExpectations_from_us());
        get_experiences.setText(user.getExperiences());
        get_working.setText(user.getWork_profession());
        get_speaker_experience.setText(user.getSpeaker_experience());
        get_offer_to_community.setText(user.getOffer_to_community());
        get_city.setText(user.getCity());
        last_name.setText(user.getLast_name());
        company.setText(user.getCompany());
    }

    private void uploadData() {

        String full_name_str="", email_str="", last_name_str="";
        full_name_str=full_name.getText().toString().trim();
        last_name_str=last_name.getText().toString().trim();
        email_str=email.getText().toString().trim();
        String working="";
        working=get_working.getText().toString();

        String experiences="";
        experiences=get_experiences.getText().toString();

        String speaker_experience="";
        speaker_experience=get_speaker_experience.getText().toString();

        String offer_to_community="";
        offer_to_community=get_offer_to_community.getText().toString();

        String expectations_from_us="";
        expectations_from_us=get_expectations_from_us.getText().toString();

        String city="";
        city=get_city.getText().toString();

        String company_str="";
        company_str=company.getText().toString();

        String bio_str=bio.getText().toString().trim();
        String weblink_str=weblink.getText().toString().trim();

        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");

        final User user=new User(userid, full_name_str, bio_str, picImageUri.toString(), user_type, city, expectations_from_us, experiences, gender, number, offer_to_community,
                speaker_experience, email_str, weblink_str, working, last_name_str, company_str);

        final HashMap<String, String> userItems=new HashMap<>();
        userItems.put(Const.USER_ID, userid);
        userItems.put(Const.USER_NAME, full_name_str);
        userItems.put(Const.USER_BIO, bio_str);
        userItems.put(Const.IMAGE_URL, picImageUri.toString());
        userItems.put(Const.USER_TYPE, user_type);
        userItems.put(Const.CITY, city);
        userItems.put("expectations_from_us", expectations_from_us);
        userItems.put("experiences", experiences);
        userItems.put("gender", gender);
        userItems.put("number", number);
        userItems.put("offer_to_community", offer_to_community);
        userItems.put("speaker_experience", speaker_experience);
        userItems.put("email", email_str);
        userItems.put("weblink", weblink_str);
        userItems.put("working", working);
        userItems.put("last_name", last_name_str);
        userItems.put("company", company_str);

        if (isCertificatesAdded) {

            final int[] numberOfCertificate={0};
            reference.child(userid).child("certificates").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists())
                        numberOfCertificate[0]=(int) dataSnapshot.getChildrenCount();

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
        }

        reference.child(userid).child(Const.USER_DETAILS).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addCurrentUserToDatabase(userItems);
                progressDialog.dismiss();
                finish();

            }
        });
    }

    private void addCurrentUserToDatabase(HashMap<String, String> userItems) {
        SQLiteDatabase sqlDb=db.getWritableDatabase();
        db.onUpgrade(sqlDb, 0, 1);
        db.insertData(userItems);
    }

    private void createUserProfile() {
        StorageReference sReference=FirebaseStorage.getInstance().getReference().child(Const.USER_MEDIA_PATH).child(userid).child(Const.PHOTOS).child(Const.PROFILE_IMAGE);
        // final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));
        final StorageReference imgPath=sReference.child("profile_image");
        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        picImageUri=uri;
                        UserProfileChangeRequest profileUpdate=new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();

                        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                        user.updateProfile(profileUpdate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        showToast("Profile updated");
                                        uploadData();
                                    }
                                });
                    }
                });
            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {

        CropImage.activity().setAspectRatio(1, 1)
                .start(this);
    }

    private void checkAndRequestForPermissions() {
       /* if (ContextCompat.checkSelfPermission(SetProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
        }*/

        permissions=new PermissionsHandling(this);
        if (!permissions.isPermissionGranted()) {
            //when permissions not granted
            if (permissions.isRequestPermissionable()) {
                //creating alertDialog
                permissions.showAlertDialog(REQUEST_CODE);
            } else {
                permissions.requestPermission(REQUEST_CODE);
                openGallery();
            }
        } else {
            openGallery();
            //when those permissions are already granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //  super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.length > 0) &&
                    (grantResults[0] + grantResults[1] + grantResults[2] + grantResults[3] + grantResults[4]
                            == PackageManager.PERMISSION_GRANTED
                    )
            ) {
                openGallery();
                //popupMenuSettigns();
                //permission granted
                // logMessage(" permission granted-----------");

            } else {

                //permission not granted
                //requestForPremission();
                // logMessage(" permission  not granted-------------");

            }
        }
    }

    private void showAddedCertificates(Certificates certificate, int size) {
        LinearLayout linearLayout=(LinearLayout) findViewById(R.id.add_cerificate_layout);
        // Add textview 1
        TextView show_certificate=new TextView(this);
        show_certificate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        show_certificate.setText(size + " " + certificate.getName());
        show_certificate.setTextColor(Color.BLUE);
        // show_certificate.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
        // textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
        linearLayout.addView(show_certificate);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked=((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.male:
                if (checked)
                    gender="Male";
                break;
            case R.id.female:
                if (checked)
                    gender="Female";
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result=CropImage.getActivityResult(data);
                    picImageUri=result.getUri();
                    isNewProfilePicSelected=true;
                    profile_img.setImageURI(picImageUri);
                    break;
                case REQUEST_CODE_FOR_CERTIFICATE:
                    Certificates certificate;
                    isCertificatesAdded=true;
                    certificate=(Certificates) data.getSerializableExtra("certificate");
                    certificates.add(certificate);
                    showAddedCertificates(certificate, certificates.size());
                    break;
              /*  case GET_CITY_CODE:
                    Log.d("TESTING", "------------------get city code");
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.d("SETPROFIETESTING", "------------------get city code" + place.getName());
                    get_city.setText(place.getName());
                    break;*/
                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status=Autocomplete.getStatusFromIntent(data);
            showToast(status.getStatusMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_img:
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermissions();
                } else {
                    openGallery();
                }
                break;
            case R.id.done_btn:
                validateInputAndUpdate();
                break;
            case R.id.add_more_certificate:
                add_more_certificate_function();
                break;
            case R.id.close:
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("Offline");
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
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

        rootRef.getUserRef().child(userid).child("userState")
                .updateChildren(onlineStateMap);

    }

     /*   private void add_more_certificate_function() {
        Intent addMoreCertiIntent = new Intent(this, AddNewCertificateActivity.class);
        startActivityForResult(addMoreCertiIntent, REQUEST_CODE_FOR_CERTIFICATE);
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
        get_working.setText(user.getWork_profession());
        get_speaker_experience.setText(user.getSpeaker_experience());
        get_offer_to_community.setText(user.getOffer_to_community());


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

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid).child(Const.USER_MEDIA_PATH).child(Const.FILES_PATH);
        Toast.makeText(this, "Wait for file to be uploaded", Toast.LENGTH_SHORT).show();
        progressDialog.show();
        StorageReference sRef = mStorageReference.child(Const.USER_MEDIA_PATH).child(userid).child("Files/" + str_name_of_file + " " + System.currentTimeMillis() + ".pdf");
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


        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

        final User user = new User(userid, full_name_str, bio_str, picImageUri.toString(), user_type, city, expectations_from_us, experiences, gender, number, offer_to_community,
                speaker_experience, email_str, weblink_str, working);

        final HashMap<String, String> userItems = new HashMap<>();
        userItems.put(Const.USER_ID, userid);
        userItems.put(Const.USER_NAME, full_name_str);
        userItems.put(Const.USER_BIO, bio_str);
        userItems.put(Const.IMAGE_URL, picImageUri.toString());
        userItems.put(Const.USER_TYPE, user_type);
        userItems.put(Const.CITY, city);
        userItems.put("expectations_from_us", expectations_from_us);
        userItems.put("experiences", experiences);
        userItems.put("gender", gender);
        userItems.put("number", number);
        userItems.put("offer_to_community", offer_to_community);
        userItems.put("speaker_experience", speaker_experience);
        userItems.put("email", email_str);
        userItems.put("weblink", weblink_str);
        userItems.put("working", working);

        if (isCertificatesAdded) {

            final int[] numberOfCertificate = {0};
            reference.child(userid).child("certificates").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists())
                        numberOfCertificate[0] = (int) dataSnapshot.getChildrenCount();

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
        }

        // Log.d("setProfileTESTING", "------------" + numberOfCertificate[0]);

        reference.child(userid).child(Const.USER_DETAILS).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addCurrentUserToDatabase(userItems);
                saveDataToSharedPref();
                sendUserToStartActivity();
            }
        });
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
    private void createUserProfile() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child(Const.USER_MEDIA_PATH).child(uid).child(Const.PHOTOS).child(Const.PROFILE_IMAGE);

        if (picImageUri != null) {
            final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid).child(Const.USER_MEDIA_PATH).child(Const.PHOTOS).child(Const.PROFILE_IMAGE);
            imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mDatabaseReference.child("dp").setValue(String.valueOf(uri));
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
                                    .build();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.updateProfile(profileUpdate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            showToast("Details Updated");
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
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(), ProfileActivity.class);
        progressDialog.dismiss();
        startActivity(homeActivity);
        finish();
    }

    private void showToast(String s) {
        Toast.makeText(EditProfile.this, s, Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {

        Intent galIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galIntent.setType("image/*");

        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
    }

    private void checkAndRequestForPermissions() {
        if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showToast("Please accept for required permission");
            } else {
                ActivityCompat.requestPermissions(EditProfile.this,
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
                    gender = "Male";
                break;
            case R.id.female:
                if (checked)
                    gender = "Female";
                break;
        }
    }

    @Override
    public void onClick(View v) {

    }*/
}

