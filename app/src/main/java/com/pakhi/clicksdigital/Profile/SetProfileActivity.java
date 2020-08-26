package com.pakhi.clicksdigital.Profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.pakhi.clicksdigital.Activities.AddNewCertificateActivity;
import com.pakhi.clicksdigital.Activities.StartActivity;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/*import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;*/

public class SetProfileActivity extends AppCompatActivity implements View.OnClickListener {

    final static int REQUEST_CODE_FOR_CERTIFICATE=3, GET_CITY_CODE=100;
    private static final String TAG         ="ProfileActivity";
    static               int    REQUEST_CODE=1;
    String userid;
    Uri    picImageUri;
    String number, imageUrl_string;
    ArrayList<Certificates>  certificates       =new ArrayList<>();
    boolean                  isCertificatesAdded=false;
    User                     user;
    UserDatabase             db;
    SharedPreference         pref;
    EditText                 weblink;
    PermissionsHandling      permissions;
    Button                   done_btn;
    ImageView                close;
    HashMap<String, String>  userItems;
    String                   previousActivity;
    FirebaseDatabaseInstance rootRef;
    // AsyncTask<?, ?, ?> runningTask;
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
        setContentView(R.layout.activity_set_profile);

        Intent intent=getIntent();
        previousActivity=intent.getStringExtra("PreviousActivity");

        pref=SharedPreference.getInstance();

        rootRef=FirebaseDatabaseInstance.getInstance();
        userid=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        db=new UserDatabase(this);

        number=pref.getData(SharedPreference.phone, getApplicationContext());

        if (number.equals("+918007997748")) {
            user_type="admin";
        } else {
            user_type="user";
        }

        pref.saveData(SharedPreference.user_type, user_type, getApplicationContext());

        initializeFields();
        profile_img.setOnClickListener(this);
        done_btn.setOnClickListener(this);
        add_more_certificate.setOnClickListener(this);

    }

    private void initializeFields() {
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
        get_city=findViewById(R.id.get_city);

        progressDialog=new ProgressDialog(SetProfileActivity.this);
        progressDialog.setMessage("Loading...");

        add_more_certificate=findViewById(R.id.add_more_certificate);
        close=findViewById(R.id.close);
        done_btn=findViewById(R.id.done_btn);

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
                showToast("Select your profile picture");
            } else {
                progressDialog.show();
                createUserProfile();
                // new AsyncOperation().execute("createProfile");
            }
        }
    }

    /*  private void getCitySelected() {
        // Places.initialize(getApplicationContext(), "AIzaSyCh4NMlBgcTL_HhUI_zvOeYliPMAhTvKTo");
        Places.initialize(getApplicationContext(), "AIzaSyDFLqgnYmeNcmyllMsoxTe9Co_KrcN7cQs");
        //   PlacesClient placesClient = Places.createClient(this);
        get_city.setFocusable(false);
        get_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initialize field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.NAME);
                //create intent
                Intent intent = new Autocomplete
                        .IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                        .build(SetProfileActivity.this);
                startActivityForResult(intent, GET_CITY_CODE);
            }
        });
    }*/

    private void add_more_certificate_function() {
        Intent addMoreCertiIntent=new Intent(SetProfileActivity.this, AddNewCertificateActivity.class);
        startActivityForResult(addMoreCertiIntent, REQUEST_CODE_FOR_CERTIFICATE);
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

        user=new User(userid, full_name_str, bio_str, imageUrl_string, user_type, city, expectations_from_us, experiences, gender, number, offer_to_community,
                speaker_experience, email_str, weblink_str, working, last_name_str, company_str);

        ;//= new HashMap<>();
        userItems=putDataIntoHashMap(userid, full_name_str, bio_str, imageUrl_string, user_type, city, expectations_from_us, experiences, gender, number, offer_to_community,
                speaker_experience, email_str, weblink_str, working, last_name_str, company_str);
        /*  if (runningTask != null) {
            runningTask.cancel(true);
        }*/

        if (isCertificatesAdded) {
            //  new AsyncOperation().execute("cerificate");
            addCertificatesToDatabase();
        } else {
            // new AsyncOperation().execute("uploadData");
        }
        addUserDetailsToDatabase();
    }

    private void addUserDetailsToDatabase() {
        final DatabaseReference reference=rootRef.getUserRef();
        reference.child(userid).child(Const.USER_DETAILS).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addCurrentUserToDatabase(userItems);
                saveDataToSharedPref();
                progressDialog.dismiss();
                // finish();

                sendUserToStartActivity();
            }
        });
    }

    private void addCertificatesToDatabase() {
        final DatabaseReference reference=rootRef.getUserRef();
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

        //addUserDetailsToDatabase();
    }

    private HashMap<String, String> putDataIntoHashMap(String userid, String full_name_str, String bio_str, String imageUrl_string, String user_type, String city, String expectations_from_us, String experiences, String gender, String number, String offer_to_community, String speaker_experience, String email_str, String weblink_str, String working, String last_name_str, String company_str) {
        final HashMap<String, String> userItems=new HashMap<>();

        userItems.put(Const.USER_ID, userid);
        userItems.put(Const.USER_NAME, full_name_str);
        userItems.put(Const.USER_BIO, bio_str);
        userItems.put(Const.IMAGE_URL, imageUrl_string);
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
        return userItems;
    }

    private void saveDataToSharedPref() {
        pref.saveData(SharedPreference.userState, Const.profileStoredUserStored, getApplicationContext());
        pref.saveData(SharedPreference.isProfileSet, Const.profileSet, getApplicationContext());
    }

    private void addCurrentUserToDatabase(HashMap<String, String> userItems) {
        SQLiteDatabase sqlDb=db.getWritableDatabase();
        db.onUpgrade(sqlDb, 0, 1);
        db.insertData(userItems);
        //  db.close();
    }

    private void createUserProfile() {
//).child(
        StorageReference sReference=FirebaseStorage.getInstance().getReference(Const.USER_MEDIA_PATH).child(userid).child(Const.PHOTOS).child(Const.PROFILE_IMAGE);
        // final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));
        final StorageReference imgPath=sReference.child("profile_image");
        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // picImageUri = uri;
                        imageUrl_string=uri.toString();
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
        Toast.makeText(SetProfileActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {

        CropImage.activity().setAspectRatio(1, 1)
                .start(SetProfileActivity.this);
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
                //openGallery();
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
               /* case GET_CITY_CODE:
                    Log.d("TESTING", "------------------get city code");
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.d("SETPROFIETESTING", "------------------get city code" + place.getName());
                    get_city.setText(place.getName());
                    break;*/
                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();
            }
        } /*else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            showToast(status.getStatusMessage());
        }*/
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
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        VerifyUserExistance();
        //  updateUserStatus("online");
        // new AsyncOperation().execute("online");
    }

    private void VerifyUserExistance() {

        if (pref.getData(SharedPreference.isProfileSet, getApplicationContext()) != null
                && pref.getData(SharedPreference.isProfileSet, getApplicationContext()).equals(Const.profileSet)) {
            // loadData(pref.getData(SharedPreference.currentUserId, getApplicationContext()));
            sendUserToStartActivity();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //  updateUserStatus("offline");
        new AsyncOperation().execute("offline");
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


    private void sendUserToStartActivity() {
        progressDialog.dismiss();
        startActivity(new Intent(SetProfileActivity.this, StartActivity.class));
    }

    private final class AsyncOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String param=params[0];
            String s="";
            switch (param) {
                case "createProfile":
                    createUserProfile();
                    break;
                case "cerificate":
                    addCertificatesToDatabase();
                    //s = "Executed";
                    //break;
                case "uploadData":
                    addUserDetailsToDatabase();
                    s="Executed";
                    break;
                case "online":
                    updateUserStatus("online");
                    break;
                case "offline":
                    updateUserStatus("offline");
                    break;
            }
            return s;
        }

        @Override
        protected void onPostExecute(String result) {
//            if (result.equals("Executed"))
//                sendUserToStartActivity();

        }
    }
}
