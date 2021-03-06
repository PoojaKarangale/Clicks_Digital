package com.pakhi.clicksdigital.Event;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Notifications.Notification;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.TypeAndSeparateURL;
import com.pakhi.clicksdigital.Utils.ValidateInput;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateEventActivity extends AppCompatActivity {
    static  int    PReqCode=1;
    private Long   selectedStartDate;
    private String category="Artificial Intelligence", event_type="Offline", currentUserId, picImageUrlString;
    private boolean payable          =false;
    private boolean isProfileSelected=false;
    int startMonth, endMonth;

    private TextView choose_start_date, choose_end_date, choose_start_time, choose_end_time;
    private TextView total_fee, cancel_btn;
    private TextView convenience_fee_amount, payumoney_amount;
    private ImageView   event_image;
    private ImageButton gallery;
    private Button      submit_btn, calculateTotal;
    private LinearLayout fee_layout;
    private EditText event_name, description, venue, city, address, noOfSeats;
    private EditText fee_amount;

    private Uri  picImageUri=null;

    private Spinner        spinner;
    private ProgressDialog progressDialog;
    TextView textOverImage;

    private DatabaseReference userRef, eventRef /*,eventCategory*/ ;
    private FirebaseDatabaseInstance rootRef;

    Date startDate, endDate;
    ArrayList<String> listOfCat = new ArrayList<>();
    ArrayList<String> listOfCat1 = new ArrayList<>();
    String name;
    RadioButton on, off, both;
    LinearLayout citylayout, linklayout, seatLayout, countryLayout;
    CardView linkCard, seatCard;

    EditText country;

    String separetURLForAddress="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        rootRef=FirebaseDatabaseInstance.getInstance();
        userRef=rootRef.getUserRef();
        eventRef=rootRef.getEventRef();

        //eventCategory=rootRef.getEventCatRef();

        SharedPreference pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        Log.i("setCatergories-------","----------");
        setCategories();
        initializeFields();

        progressDialog=new ProgressDialog(CreateEventActivity.this);
        progressDialog.setMessage("Loading...");

        settingDateAndTime();
        spinnerImplementationForTopic();

        event_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermissions();
                } else {
                    openGallery();
                }
            }
        });
        calculateTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateEventFee();
            }
        });
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProfileSelected) {
                    // profileFlag=true;
                    if (validateEvent()) {
                        if(validateDate()){
                            progressDialog.show();
                            createEventStorage();
                        }
                    }
                } else {
                    Toast.makeText(CreateEventActivity.this, "Select event picture", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validateDate() {

        if(startDate.compareTo(endDate)>0){
            Toast.makeText(CreateEventActivity.this, "Start date can't be greater than end date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setCategories() {
        userRef.child(currentUserId).child(ConstFirebase.groups).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap:snapshot.getChildren()){
                    Log.i("listOfCat-------", snap.getKey());
                    listOfCat.add(snap.getKey());
                    Log.i("Length of listCat-----",String.valueOf(listOfCat.size()));
                    setFinal(snap.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.i("Length of listCat-----",String.valueOf(listOfCat.size()));
    }

    private void setFinal(String val) {

            rootRef.getGroupRef().child(val).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("finalList ----- ",snapshot.child("group_name").getValue().toString());
                    listOfCat1.add(snapshot.child("group_name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    private void initializeFields() {
        textOverImage = findViewById(R.id.text_over_image);
        submit_btn=findViewById(R.id.submit_btn);
        cancel_btn=findViewById(R.id.cancel_btn);
        calculateTotal=findViewById(R.id.calculateTotal);
        event_name=findViewById(R.id.event_name);

        citylayout = findViewById(R.id.city_layout);

        fee_layout=findViewById(R.id.fee_layout);
        //unpaidChip=findViewById(R.id.unpaidChip);
        //paidChip=findViewById(R.id.paidChip);

        total_fee=findViewById(R.id.total_fee);
        fee_amount=findViewById(R.id.fee_amount);
        convenience_fee_amount=findViewById(R.id.convenience_fee_amount);
        payumoney_amount=findViewById(R.id.payumoney_amount);


        venue=findViewById(R.id.venu);
        city=findViewById(R.id.city);
        country=findViewById(R.id.country_loc);
        address=findViewById(R.id.address);
        noOfSeats=findViewById(R.id.no_of_seats);

        event_image=findViewById(R.id.event_image);
        description=findViewById(R.id.description);

        choose_end_date=findViewById(R.id.choose_end_date);
        choose_start_date=findViewById(R.id.choose_start_date);
        choose_start_time=findViewById(R.id.choose_start_time);
        choose_end_time=findViewById(R.id.choose_end_time);

        on = findViewById(R.id.online);
        off= findViewById(R.id.off);
        both = findViewById(R.id.offline_and_online);

        linklayout = findViewById(R.id.link_layout);
        seatLayout = findViewById(R.id.seat_layout);
        countryLayout = findViewById(R.id.country_layout);

        linkCard = findViewById(R.id.link_card);
        seatCard = findViewById(R.id.seat_card);

    }

    private void calculateEventFee() {

        int feeAmount=Integer.parseInt(fee_amount.getText().toString());
        int convenienceFee=(int) Math.ceil(feeAmount * 0.08f);
        convenience_fee_amount.setText(String.valueOf(convenienceFee));
        int payumoneyFee=(int) Math.round((feeAmount + convenienceFee) * 0.02);
        payumoney_amount.setText(String.valueOf(payumoneyFee));
        int total=feeAmount + convenienceFee + payumoneyFee;
        total_fee.setText(String.valueOf(total));

    }

    private void openGallery() {
        /*   Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);*/
        CropImage.activity().setAspectRatio(1, 1)
                .start(this);
    }

    private void checkAndRequestForPermissions() {

        if (ActivityCompat.checkSelfPermission(CreateEventActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(CreateEventActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CreateEventActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(CreateEventActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        } else {
            openGallery();
        }
    }

    String getFileExtention(Uri uri) {
        /* ContentResolver contentResolver=CreateEventActivity.this.getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
        */
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createEventStorage() {

        StorageReference sReference=FirebaseStorageInstance.getInstance().getRootRef().child("Events");
        String extention=getFileExtention(picImageUri);

        final StorageReference imgPath=sReference.child("" + System.currentTimeMillis());//+ "." + extention);
        Log.d("TESTINGEXTENTION", "--------pic image url----before-------------------" + picImageUrlString);

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("TESTINGEXTENTION", "--------pic image url--uploaded--before-download------------------" + picImageUrlString);
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        picImageUrlString=uri.toString();
                        createEvent();
                        Log.d("TESTINGEXTENTION", "--------pic image on success------------------" + picImageUrlString);
                    }
                });
               /* imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        picImageUrlString=uri.toString();

                        Log.d("TESTINGEXTENTION", "--------pic image url-----------------------" + picImageUrlString);
                    }
                });*/
            }
        });
    }

    boolean validateEvent() {
        boolean addressLinkFlag=true;
        String addressLink = address.getText().toString();

        if(!addressLink.equals("")){

            TypeAndSeparateURL typeAndSeparateURL = checkIfURLIsEnteredInLink(addressLink);

            if(typeAndSeparateURL.typeURLOrNot==1){
                separetURLForAddress=typeAndSeparateURL.separateURL;
                addressLinkFlag=true;
            }else{
                addressLinkFlag=false;
            }
        }


        boolean addressFlag=false, feeFlag=false, profileFlag=false;
        if (payable) {
            //check fee_amount
            if (ValidateInput.field(fee_amount)) {
                feeFlag=true;
            } else {
                feeFlag=false;
            }
        } else {
            feeFlag=true;
        }

        if (event_type.equals(ConstFirebase.Both) || event_type.equals(ConstFirebase.eventOffline)) {
            if (ValidateInput.field(venue) || ValidateInput.field(city) || ValidateInput.field(address)) {
                addressFlag=true;
            } else {
                addressFlag=false;
            }
        } else {
            if (ValidateInput.field(venue)) {
                addressFlag=true;
            } else {
                addressFlag=false;
            }
        }

        if(!addressLinkFlag){
            Toast.makeText(getApplicationContext(), "Please provide valid map/ meeting link", Toast.LENGTH_LONG).show();
        }

        if (addressFlag && feeFlag && addressLinkFlag)  {
            if (ValidateInput.field(event_name) && ValidateInput.field(description)) {
                return true;

            }
        }
        return false;
    }

    private TypeAndSeparateURL checkIfURLIsEnteredInLink(String addressLink) {

        String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        String separateURL="";
        Pattern p;
        Matcher m = null;
        String[] words = addressLink.split(" ");
        int checkIfTypeIsURL = 0;
        for (String word : words) {
            p = Pattern.compile(URL_REGEX);
            m = p.matcher(word);

            if (m.find()) {
                separateURL=word;
                Toast.makeText(getApplicationContext(), "The String contains URL", Toast.LENGTH_LONG).show();
                checkIfTypeIsURL = 1;
                break;
            }

        }
        TypeAndSeparateURL typeAndSeparateURL = new TypeAndSeparateURL(separateURL, checkIfTypeIsURL);
        return typeAndSeparateURL;

    }

    private void createEvent() {

        // progressDialog.show();
        final String eventKey=eventRef.push().getKey();
        Log.d("TESTINGEXTENTION", "--------pic image url----after-------------------" + picImageUrlString);

        final String eventName=event_name.getText().toString();
        String eventDescription=description.getText().toString();

        String venueStr="";
        venueStr=venue.getText().toString();
        String cityStr="";
        cityStr=city.getText().toString();
        String addressStr=separetURLForAddress;

        String startDate=choose_start_date.getText().toString();
        String endDate=choose_end_date.getText().toString();
        String startTime=choose_start_time.getText().toString();
        String endTime=choose_end_time.getText().toString();
        Long timeStamp=selectedStartDate;
        int totalFee=Integer.parseInt(total_fee.getText().toString());
        int totalSeats = Integer.parseInt(noOfSeats.getText().toString());
        String countryName = country.getText().toString();
        Log.i("countryName", countryName);
        Event event;
        event=new Event(eventKey, eventName, eventDescription, category,
                picImageUrlString, event_type, venueStr, cityStr, addressStr, timeStamp,
                startDate, endDate, startTime, endTime, payable, totalFee, currentUserId, totalSeats, countryName);

        eventRef.child(eventKey).child(ConstFirebase.EventDetails).setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreateEventActivity.this, "new event created", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }
        });
        //eventRef.child(eventKey).child(ConstFirebase.EventDetails).child(ConstFirebase.startMonth).setValue(startMonth);
        //eventRef.child(eventKey).child(ConstFirebase.EventDetails).child(ConstFirebase.endMonth).setValue(endMonth);

        rootRef.getUserRef().child(currentUserId).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child(ConstFirebase.USER_NAME).getValue().toString();
                send(name, eventKey, eventName);
                            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void send(final String name, final String eventKey, final String eventName) {
        rootRef.getUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    if(snap.child(ConstFirebase.USER_DETAILS).exists()){
                        if(snap.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.userType).getValue().toString().equals("admin")&& !currentUserId.equals(snap.child(ConstFirebase.USER_DETAILS).child(ConstFirebase.USER_ID).getValue().toString())){
                            //Notification.sendPersonalNotifiaction(eventKey, snap.getKey(), name+" has created new event "+eventName, "New Event", "event", "");
                            String notificationKey = rootRef.getNotificationRef().push().getKey();

                            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationRecieverID).setValue(snap.getKey());
                            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationFrom).setValue(currentUserId);
                            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.goToNotificationId).setValue(eventKey);
                            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.typeOfNotification).setValue("createEvent");

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

  /*  private void getCitySelected() {
        // Places.initialize(getApplicationContext(), "AIzaSyCh4NMlBgcTL_HhUI_zvOeYliPMAhTvKTo");
        Places.initialize(CreateEventActivity.this, "AIzaSyDFLqgnYmeNcmyllMsoxTe9Co_KrcN7cQs");
        //   PlacesClient placesClient = Places.createClient(this);
        get_location.setFocusable(false);
        get_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initialize field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.NAME);
                //create intent
                Intent intent = new Autocomplete
                        .IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                        .build(CreateEventActivity.this);
                startActivityForResult(intent, GET_CITY_CODE);
            }
        });
    }*/

    private void spinnerImplementationForTopic() {
        spinner=findViewById(R.id.event_cat_spinner);
        //final ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(CreateEventActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1);
        final String[] countries=getResources().getStringArray(R.array.array_categories);

        //final String[] countries=listOfCat1;



        final ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,android.R.id.text1, countries);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category=(String) parent.getItemAtPosition(position);

                if (category.equals("Other")) {
                    getOtherNewCategory(spinnerAdapter, position);
                    Log.d("CreateEventActivity", category + " -------------------other");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getOtherNewCategory(final ArrayAdapter<String> spinnerAdapter, int position) {
        AlertDialog.Builder builder=new AlertDialog.Builder(CreateEventActivity.this);
        builder.setTitle("Enter new Category");

        // Set up the input
        final EditText input=new EditText(CreateEventActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                category=input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                category="";
                Toast.makeText(CreateEventActivity.this, "You have to specify the new Category", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        builder.show();
    }

    /*private void chipActionHandled() {
        paidChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // payable =
                //calculateEventFee();
                payable=true;
                fee_layout.setVisibility(View.VISIBLE);
            }
        });
        unpaidChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // payable = "Unpaid";
                payable=false;
                fee_layout.setVisibility(View.GONE);
            }
        });
        offlineChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_type=Const.Offline;

                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);

            }
        });
        onlineChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_type=Const.Online;

                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.GONE);
                citylayout.setVisibility(View.GONE);
                //address.setVisibility(View.GONE);

                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);

                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);

            }
        });
        bothChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                event_type="Both";
                citylayout.setVisibility(View.VISIBLE);
                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);
            }
        });
    }*/


    private void settingDateAndTime() {
        final Calendar c=Calendar.getInstance();
        final int mYear=c.get(Calendar.YEAR);
        final int mMonth=c.get(Calendar.MONTH);
        final int mDay=c.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat currentDateFormat=new SimpleDateFormat("dd MMM yyyy");
        String currentDate=currentDateFormat.format(c.getTime());
        startDate = c.getTime();
        endDate = c.getTime();

        choose_start_date.setText(currentDate);
        choose_end_date.setText(currentDate);

        final int mHour=c.get(Calendar.HOUR_OF_DAY);
        final int mMinute=c.get(Calendar.MINUTE);

        choose_start_time.setText(mHour + ":" + mMinute);
        choose_end_time.setText(mHour + ":" + mMinute);

        selectedStartDate=fieldToTimestamp(mYear, mMonth, mDay);

        choose_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMonth = mMonth;
                openDatePickerDialog(mYear, mMonth, mDay, choose_start_date, true);
            }
        });
        choose_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endMonth = mMonth;
                openDatePickerDialog(mYear, mMonth, mDay, choose_end_date, false);
            }
        });

        choose_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePickerDialog(mHour, mMinute, choose_start_time);
            }
        });
        choose_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePickerDialog(mHour, mMinute, choose_end_time);
            }
        });
    }

    private void openDatePickerDialog(int mYear, int mMonth, int mDay, final TextView choose_date, final boolean start) {

        DatePickerDialog datePickerDialog=new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        final Calendar cal=Calendar.getInstance();
                        cal.set(year,monthOfYear,dayOfMonth);
                        SimpleDateFormat currentDateFormat=new SimpleDateFormat("dd MMM yyyy");
                        String currentDate=currentDateFormat.format(cal.getTime());

                        choose_date.setText(currentDate);

                        if(start){
                            startDate=cal.getTime();
                        }
                        else{
                            endDate=cal.getTime();
                        }

                        if (start)
                            selectedStartDate=fieldToTimestamp(year, (monthOfYear), dayOfMonth);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private long fieldToTimestamp(int year, int month, int day) {
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return (long) (calendar.getTimeInMillis() / 1000L);
    }

    private void openTimePickerDialog(int mHour, int mMinute, final TextView choose_time) {
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog=new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                      /*  String format = "%1$02d"; // two digits
                        //textView.setText(String.format(format, minute));
                        choose_time.setText(hourOfDay + ":" + String.format(format, minute));
*/
                        NumberFormat f=new DecimalFormat("00");
                        long time=minute;
                        choose_time.setText(hourOfDay + ":" + f.format(time));
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result=CropImage.getActivityResult(data);
                    picImageUri=result.getUri();
                    Glide.with(getApplicationContext())
                            .load(picImageUri)
                            .transform(new CenterCrop(), new RoundedCorners(15))
                            .into(event_image);
                    //event_image.setImageURI(picImageUri);
                    isProfileSelected=true;
                    textOverImage.setVisibility(View.GONE);
                    break;
                default:
                    Toast.makeText(CreateEventActivity.this, "nothing is selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onRadioClickedEvent(View view) {
        boolean checked=((RadioButton) view).isChecked();

        switch (view.getId()){
            case R.id.online:
                if(checked){
                    event_type = ConstFirebase.eventOnline;
                    venue.setVisibility(View.VISIBLE);
                    city.setVisibility(View.GONE);
                    citylayout.setVisibility(View.GONE);
                    //address.setVisibility(View.GONE);
                    countryLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.offline:
                if(checked){
                    event_type=ConstFirebase.eventOffline;
                    venue.setVisibility(View.VISIBLE);
                    city.setVisibility(View.VISIBLE);
                    address.setVisibility(View.VISIBLE);
                    citylayout.setVisibility(View.VISIBLE);
                    countryLayout.setVisibility(View.VISIBLE);


                }

                break;
            case R.id.offline_and_online:
                if(checked){
                    event_type =ConstFirebase.eventBoth;
                    venue.setVisibility(View.VISIBLE);
                    city.setVisibility(View.VISIBLE);
                    address.setVisibility(View.VISIBLE);
                    citylayout.setVisibility(View.VISIBLE);
                    countryLayout.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.paidChip:
                payable=true;
                fee_layout.setVisibility(View.VISIBLE);
                break;
            case R.id.unpaidChip:
                payable=false;
                fee_layout.setVisibility(View.GONE);


        }
    }
}
