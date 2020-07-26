package com.pakhi.clicksdigital.ActivitiesEvent;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateEventActivity extends AppCompatActivity {
    private final static int GET_CITY_CODE = 100, REQUESTCODE = 1;
    static int PReqCode = 1;
    TextView choose_date_text, choose_time_text;
    String city;
    private ImageView event_image;
    private Uri picImageUri;
    private boolean isProfileSelected = false;
    private Spinner spinner;
    private Chip onlineChip, offlineChip, paidChip, unpaidChip;
    private String category = "Category", event_type = "Offline";
    private DatabaseReference rootRef, userRef, eventRef, eventCategory;
    private FirebaseAuth firebaseAuth;
    private MaterialEditText get_cost, get_location, event_name, get_duration, description;
    private boolean payable = false;
    private ImageButton choose_date;
    private ImageButton choose_time;
    private ProgressDialog progressDialog;
    private Button submit_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users");
        eventRef = FirebaseDatabase.getInstance().getReference("Events");
        eventCategory = FirebaseDatabase.getInstance().getReference("EventCategory");
        firebaseAuth = FirebaseAuth.getInstance();

        initializeFields();

        progressDialog = new ProgressDialog(CreateEventActivity.this);
        progressDialog.setMessage("Loading...");

        getCitySelected();
        settingDateAndTime();
        spinnerImplementationForTopic();
        chipActionHandled();
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

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent();
            }
        });
    }

    private void initializeFields() {
        get_duration = findViewById(R.id.get_duration);
        choose_time = findViewById(R.id.choose_time);
        choose_date = findViewById(R.id.choose_date);
        choose_time_text = findViewById(R.id.choose_time_text);
        choose_date_text = findViewById(R.id.choose_date_text);
        get_cost = findViewById(R.id.get_cost);
        // get_city = view.findViewById(R.id.get_city);
        get_location = findViewById(R.id.get_location);
        submit_btn = findViewById(R.id.submit_btn);
        event_name = findViewById(R.id.event_name);
        unpaidChip = findViewById(R.id.unpaidChip);
        paidChip = findViewById(R.id.paidChip);
        onlineChip = findViewById(R.id.onlineChip);
        offlineChip = findViewById(R.id.offlineChip);
        event_image = findViewById(R.id.event_image);
        description = findViewById(R.id.description);
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
    }

    private void checkAndRequestForPermissions() {
                /*if (ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            Log.e("DB", "PERMISSION GRANTED");
        }*/

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
        ContentResolver contentResolver = CreateEventActivity.this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createEventStorage(String eventId) {
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child("Events").child(eventId);

        final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        picImageUri = uri;
                        // Toast.makeText(getContext(),"new event created", Toast.LENGTH_SHORT).show();
                    }

                });
            }
        });
    }

    private void createEvent() {
        String eventName = event_name.getText().toString();
        String event_description = description.getText().toString();
        String location = get_location.getText().toString();
        //String city = get_city.getText().toString();
        String event_date = choose_date_text.getText().toString();
        String event_time = choose_time_text.getText().toString();
        String time_duration = get_duration.getText().toString();
        String eventPayable = "Unpaid";
        String cost = "0";
        if (payable) {
            eventPayable = "Paid";
            get_cost.setEnabled(true);
            cost = String.valueOf(get_cost.getText());
            if (TextUtils.isEmpty(cost)) {
                get_cost.requestFocus();
                get_cost.setError("required");
            }
        }
        String userId = firebaseAuth.getUid();

        if (TextUtils.isEmpty(eventName)) {
            event_name.requestFocus();
            event_name.setError("required");
        }
        if (TextUtils.isEmpty(event_description)) {
            description.requestFocus();
            description.setError("required");
        }
        if (TextUtils.isEmpty(location)) {
            get_location.requestFocus();
            get_location.setError("required");
        }
        if (TextUtils.isEmpty(time_duration)) {
            get_duration.requestFocus();
            get_duration.setError("required");
        }

        progressDialog.show();

      /*  if (event_type.equals("Online")) {
            String eventKey = eventRef.child("Online").push().getKey();
            createEventStorage(eventKey);
            Event event = new Event(eventKey, eventName, event_description, picImageUri.toString(), city, location, event_time, event_date, time_duration, event_type, eventPayable, cost, category, userId);
            eventRef.child("Online").child(eventKey).child("EventDetails").setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(CreateEventActivity.this, "new event created", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                    //getFragmentManager().popBackStack();
                    //getFragmentManager().beginTransaction().remove(CreateEventActivity.this).commit();
                }
            });
        } else {
            String eventKey = eventRef.child("Offline").push().getKey();
            createEventStorage(eventKey);
            Event event = new Event(eventKey, eventName, event_description, picImageUri.toString(), city, location, event_time, event_date, time_duration, event_type, eventPayable, cost, category, userId);
            eventRef.child("Offline").child(eventKey).child("EventDetails").setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(CreateEventActivity.this, "new event created", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                    //getFragmentManager().popBackStack();
                    // getFragmentManager().beginTransaction().remove(CreateEventFragment.this).commit();
                }
            });
        }*/

        String eventKey = eventRef.child(event_type).push().getKey();
        createEventStorage(eventKey);
        Event event = new Event(eventKey, eventName, event_description, picImageUri.toString(), city, location, event_time, event_date, time_duration, event_type, eventPayable, cost, category, userId);
        eventRef.child(event_type).child(eventKey).child("EventDetails").setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreateEventActivity.this, "new event created", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
                //getFragmentManager().popBackStack();
                //getFragmentManager().beginTransaction().remove(CreateEventActivity.this).commit();
            }
        });
        //  all_info.setText(eventName + " " + eventCategory + " \n" + location + " \n" + city + " \n" + event_date + " " + event_time + " \n" + time_duration + " " + eventType + " \n" + eventPayable + " " + cost + " ");
    }

    private void getCitySelected() {
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
    }

    private void spinnerImplementationForTopic() {
        spinner = findViewById(R.id.event_cat_spinner);
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(CreateEventActivity.this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //selectedCategory_text.setText((CharSequence) parent.getItemAtPosition(position));
                String cat = (String) parent.getItemAtPosition(position);
                if (cat.equals("_Other")) {
                    getOtherNewCategory(spinnerAdapter, position);
                    Log.d("CreateEventActivity", cat + " -------------------other");
                }
                // spinnerAdapter.add(category);
                //eventCategory.child(category).setValue("");
                // category = cat;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
       /*eventCategory.child("Artificial Inteligence").setValue("");
        eventCategory.child("Cyber Security").setValue("");
        eventCategory.child("Virtual Reality").setValue("");
        eventCategory.child("Block Chain").setValue("");
        eventCategory.child("_Other").setValue("");*/

        eventCategory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                spinnerAdapter.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String cat = dataSnapshot.getKey();
                    spinnerAdapter.add(cat);
                }
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getOtherNewCategory(final ArrayAdapter<String> spinnerAdapter, final int position) {
        // final String[] newCategory = new String[1];
        // newCategory[0]="";
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
        builder.setTitle("Enter new Category");

        // Set up the input
        final EditText input = new EditText(CreateEventActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                category = input.getText().toString();
                spinnerAdapter.add(category);
                eventCategory.child(category).setValue("");
                spinnerAdapter.insert(category, position);
                Log.d("CreateEventActivity", category + " -------------------1 newCat");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                category = "";
                Toast.makeText(CreateEventActivity.this, "You have to specify the new Category", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        builder.show();
        Log.d("CreateEventActivity", category + " -------------------2 newCat");
        //return newCategory[0];
    }

    private void chipActionHandled() {
        paidChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // payable = "Paid";
                payable = true;
                get_cost.setEnabled(true);
                unpaidChip.setChipBackgroundColor(getResources().getColorStateList(R.color.chipColor));
                paidChip.setChipBackgroundColor(getResources().getColorStateList(R.color.cyan));
            }
        });
        unpaidChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // payable = "Unpaid";
                payable = false;
                get_cost.setEnabled(false);
                paidChip.setChipBackgroundColor(getResources().getColorStateList(R.color.chipColor));
                unpaidChip.setChipBackgroundColor(getResources().getColorStateList(R.color.cyan));
            }
        });
        offlineChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_type = "Offline";
                //offlineChip.setChipBackgroundColor(ColorStateList.valueOf(Color.CYAN));
                onlineChip.setChipBackgroundColor(getResources().getColorStateList(R.color.chipColor));
                offlineChip.setChipBackgroundColor(getResources().getColorStateList(R.color.cyan));
            }
        });
        onlineChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_type = "Online";
                // onlineChip.setChipBackgroundColor(ColorStateList.valueOf(Color.CYAN));
                offlineChip.setChipBackgroundColor(getResources().getColorStateList(R.color.chipColor));
                onlineChip.setChipBackgroundColor(getResources().getColorStateList(R.color.cyan));
            }
        });
    }

    private void settingDateAndTime() {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        choose_date_text.setText(mDay + "-" + (mMonth + 1) + "-" + mYear);

        final int mHour = c.get(Calendar.HOUR_OF_DAY);
        final int mMinute = c.get(Calendar.MINUTE);
        choose_time_text.setText(mHour + ":" + mMinute);

        choose_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePickerDialog(mYear, mMonth, mDay);
            }
        });

        choose_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePickerDialog(mHour, mMinute);
            }
        });
    }

    private void openDatePickerDialog(int mYear, int mMonth, int mDay) {

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        choose_date_text.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void openTimePickerDialog(int mHour, int mMinute) {
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        choose_time_text.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GET_CITY_CODE:
                    Log.d("TESTING", "------------------get city code");
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.d("SETPROFILETESTING", "------------------get city code" + place.getName());
                    get_location.setText(place.getAddress());
                    city = place.getName();
                    // get_city.setText(place.getName());
                    break;
                case REQUESTCODE:
                    picImageUri = data.getData();
                    event_image.setImageURI(picImageUri);
                    isProfileSelected = true;
                    break;
                default:
                    Toast.makeText(CreateEventActivity.this, "nothing is selected", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(CreateEventActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
