package com.pakhi.clicksdigital.Profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.ValidateInput;

import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    final static int REQUEST_CODE_FOR_CERTIFICATE=3, GET_CITY_CODE=100;
    private static final String TAG         ="ProfileActivity";
    static               int    REQUEST_CODE=1;
    String userid;
    Uri    picImageUri;
    ArrayList<Certificates> cer = new ArrayList<>();
    String                   number;
    ArrayList<Certificates>  certificates;
    boolean                  isCertificatesAdded=false;
    StorageReference         mStorageReference;
    User                     users;
    //UserDatabase             db;
    SharedPreference         pref;
    EditText                 weblink;
    PermissionsHandling      permissions;
    Button                   done_btn;
    ImageView                close;
    FirebaseDatabaseInstance rootRef;
    private boolean          isNewProfilePicSelected=false;
    private ImageView        profile_img;
    private EditText full_name, email, bio, last_name;
    private ProgressDialog progressDialog;
    private String         gender, user_type;
    private EditText get_working, get_experiences, get_speaker_experience, get_offer_to_community, get_expectations_from_us, company, get_city;
    private Button add_more_certificate;
    RadioButton male, female;

    EditText country, referredBy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        Intent intent=getIntent();
        users=(User) intent.getSerializableExtra(Const.userdata);
        //gender = intent.getStringExtra("gender");
        //user_type=user.getUser_type();
        pref=SharedPreference.getInstance();
        userid=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef=FirebaseDatabaseInstance.getInstance();
        mStorageReference=FirebaseStorageInstance.getInstance().getRootRef();

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

        male = findViewById(R.id.male);
        female = findViewById(R.id.female);

        referredBy = findViewById(R.id.refer_by);
        country = findViewById(R.id.country);
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
    }

    private void add_more_certificate_function() {
        Intent addMoreCertiIntent=new Intent(this, AddNewCertificateActivity.class);
        startActivityForResult(addMoreCertiIntent, REQUEST_CODE_FOR_CERTIFICATE);
    }

    private void readUserData() {
        UserDatabase db=new UserDatabase(this);
        users = db.getSqliteUser();
    }

    private void loadData() {

        rootRef.getUserRef().child(userid).child(ConstFirebase.certificate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    cer.clear();
                    for(DataSnapshot c : snapshot.getChildren()){
                        cer.add(c.getValue(Certificates.class));
                        showAddedCertificates(c.getValue(Certificates.class), cer.size());
                    }

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

      if(users.getGender().equals("Male")){
          male.setChecked(true);
          gender="Male";
      }
      else{
          female.setChecked(true);
          gender="Female";
      }
        //Log.i("gender", user.getGender());
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        picImageUri=firebaseAuth.getCurrentUser().getPhotoUrl();

        Glide.with(getApplicationContext()).load(users.getImage_url())
                .apply(RequestOptions.circleCropTransform())
                .into(profile_img);

        full_name.setText(users.getUser_name());
        email.setText(users.getUser_email());
        weblink.setText(users.getWeblink());
        bio.setText(users.getUser_bio());
        get_expectations_from_us.setText(users.getExpectations_from_us());
        get_experiences.setText(users.getExperiences());
        get_working.setText(users.getWork_profession());
        get_speaker_experience.setText(users.getSpeaker_experience());
        get_offer_to_community.setText(users.getOffer_to_community());
        get_city.setText(users.getCity());
        last_name.setText(users.getLast_name());
        company.setText(users.getCompany());
        country.setText(users.getCountry());
        referredBy.setText(users.getReferal());

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
        user_type=users.getUser_type();

        String countryName = country.getText().toString().trim();
        String referred = referredBy.getText().toString().trim();

        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");

        final User user=new User(userid, full_name_str, bio_str, picImageUri.toString(), user_type, city, expectations_from_us, experiences, gender, number, offer_to_community,
                speaker_experience, email_str, weblink_str, working, last_name_str, company_str, countryName, referred, users.getBlueTick());

        final HashMap<String, String> userItems=new HashMap<>();
        userItems.put(ConstFirebase.USER_ID, user.getUser_id());
        userItems.put(ConstFirebase.USER_NAME, user.getUser_name());
        userItems.put(ConstFirebase.USER_BIO, user.getUser_bio());
        userItems.put(ConstFirebase.IMAGE_URL, user.getImage_url());
        userItems.put(ConstFirebase.USER_TYPE, user.getUser_type());
        userItems.put(ConstFirebase.CITY, user.getCity());
        userItems.put(ConstFirebase.expeactations, user.getExpectations_from_us());
        userItems.put(ConstFirebase.expireince, user.getExperiences());
        userItems.put(ConstFirebase.GENDER, user.getGender());
        userItems.put(ConstFirebase.MO_NUMBER, user.getNumber());
        userItems.put(ConstFirebase.offerToComm, user.getOffer_to_community());
        userItems.put(ConstFirebase.speakerExp, user.getSpeaker_experience());
        userItems.put(ConstFirebase.email, user.getUser_email());
        userItems.put(ConstFirebase.webLink, user.getWeblink());
        userItems.put(ConstFirebase.working, user.getWork_profession());
        userItems.put(ConstFirebase.last_name, user.getLast_name());
        userItems.put(ConstFirebase.company, user.getCompany());
        userItems.put(ConstFirebase.country, user.getCountry());
        userItems.put(ConstFirebase.getReferral, user.getReferal());
        userItems.put(ConstFirebase.getBlueTick, user.getBlueTick());

        if (isCertificatesAdded) {

            final int[] numberOfCertificate={0};
            reference.child(userid).child(ConstFirebase.certificate).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists())
                        numberOfCertificate[0]=(int) dataSnapshot.getChildrenCount();

                    for (Certificates c : certificates) {
                        reference.child(userid).child(ConstFirebase.certificate).child(String.valueOf(numberOfCertificate[0])).setValue(c);
                        numberOfCertificate[0]++;
                    }
                    certificates.clear();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        reference.child(userid).child(ConstFirebase.USER_DETAILS).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addCurrentUserToDatabase(userItems);
                progressDialog.dismiss();
                Intent intent = new Intent(EditProfile.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addCurrentUserToDatabase(HashMap<String, String> userItems) {
        UserDatabase db = new UserDatabase(this);
        SQLiteDatabase sqlDb=db.getWritableDatabase();
        db.onUpgrade(sqlDb, 0, 1);
        db.insertData(userItems);
    }

    private void createUserProfile() {
        StorageReference sReference=FirebaseStorageInstance.getInstance().getRootRef().child(ConstFirebase.USER_MEDIA_PATH).child(userid).child(ConstFirebase.PHOTOS).child(ConstFirebase.PROFILE_IMAGE);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
        finish();
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
                    certificate=(Certificates) data.getSerializableExtra(ConstFirebase.certificate);
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

