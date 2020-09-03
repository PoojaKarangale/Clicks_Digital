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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.ValidateInput;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.Calendar;

public class EditEventActivity extends AppCompatActivity {

    private static int     PReqCode         =1;
    private Event  event;
    private int    totalAmount;
    private Long   selectedStartDate;
    private String category="Artificial Intelligence", event_type="Offline", currentUserId, picImageUrlString;
    private boolean payable          =false;
    private boolean isProfileSelected=false;

    private TextView choose_start_date, choose_end_date, choose_start_time, choose_end_time;
    private TextView total_fee, cancel_btn;
    private TextView convenience_fee_amount, payumoney_amount;
    private ImageView   event_image;
    private ImageButton gallery;
    private Button      submit_btn, calculateTotal;
    private RelativeLayout   fee_layout;
    private MaterialEditText event_name, description, venu, city, address;
    private MaterialEditText fee_amount;

    private Uri  picImageUri=null;
    private Chip onlineChip, offlineChip, bothChip, paidChip, unpaidChip;
    private Spinner        spinner;
    private ProgressDialog progressDialog;

    private DatabaseReference        eventRef;
    private FirebaseDatabaseInstance rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        event=(Event) getIntent().getSerializableExtra("event");

        rootRef=FirebaseDatabaseInstance.getInstance();
        eventRef=rootRef.getEventRef();
        initializeFields();
        loadData();
        chipActionHandled();
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

                createEvent();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeFields() {
        currentUserId=event.getCreater_id();

        spinner=findViewById(R.id.event_cat_spinner);
        event_name=findViewById(R.id.event_name);
        event_image=findViewById(R.id.event_image);
        description=findViewById(R.id.description);

        fee_layout=findViewById(R.id.fee_layout);
        unpaidChip=findViewById(R.id.unpaidChip);
        paidChip=findViewById(R.id.paidChip);

        total_fee=findViewById(R.id.total_fee);
        fee_amount=findViewById(R.id.fee_amount);
        convenience_fee_amount=findViewById(R.id.convenience_fee_amount);
        payumoney_amount=findViewById(R.id.payumoney_amount);

        onlineChip=findViewById(R.id.onlineChip);
        offlineChip=findViewById(R.id.offlineChip);
        bothChip=findViewById(R.id.bothChip);

        venu=findViewById(R.id.venu);
        city=findViewById(R.id.city);
        address=findViewById(R.id.address);

        choose_end_date=findViewById(R.id.choose_end_date);
        choose_start_date=findViewById(R.id.choose_start_date);
        choose_start_time=findViewById(R.id.choose_start_time);
        choose_end_time=findViewById(R.id.choose_end_time);

        calculateTotal=findViewById(R.id.calculateTotal);
        submit_btn=findViewById(R.id.submit_btn);
        cancel_btn=findViewById(R.id.cancel_btn);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
    }

    private void loadData() {
        picImageUrlString=event.getEvent_image();
        Picasso.get().load(picImageUrlString).fit().into(event_image);
        event_name.setText(event.getEventName());
        description.setText(event.getDescription());
        venu.setText(event.getVenu());
        city.setText(event.getCity());
        address.setText(event.getAddress());

        choose_start_date.setText(event.getStartDate());
        choose_end_date.setText(event.getEndDate());
        choose_start_time.setText(event.getStartTime());
        choose_end_time.setText(event.getEndTime());

        selectedStartDate=event.getTimeStamp();

        if (event.isPayable()) {
            totalAmount=event.getTotalFee();
            total_fee.setText(event.getTotalFee());
            // calculateEventFee();
            paidChip.setSelected(true);

        } else {

            unpaidChip.setSelected(true);
        }
        if (event.getEventType().equals("Offline")) {

            offlineChip.setSelected(true);
            venu.setVisibility(View.VISIBLE);
            city.setVisibility(View.VISIBLE);
            address.setVisibility(View.VISIBLE);

        } else if (event.getEventType().equals("Online")) {

            onlineChip.setSelected(true);
            venu.setVisibility(View.GONE);
            city.setVisibility(View.GONE);
            address.setVisibility(View.VISIBLE);
        } else {
            /* offlineChip.setChipBackgroundColor(getResources().getColorStateList(R.color.chipColor));
             */
            bothChip.setSelected(true);
            venu.setVisibility(View.VISIBLE);
            city.setVisibility(View.VISIBLE);
            address.setVisibility(View.VISIBLE);
        }
    }

    private void openGallery() {
        CropImage.activity().setAspectRatio(1, 1)
                .start(this);
    }

    private void checkAndRequestForPermissions() {

        if (ActivityCompat.checkSelfPermission(EditEventActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(EditEventActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(EditEventActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(EditEventActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        } else {
            openGallery();
        }
    }

    private void createEvent() {
        String eventKey=event.getEventId();
        boolean addressFlag;

        if (event_type.equals("Both") || event_type.equals("Offline")) {
            if (ValidateInput.field(venu) || ValidateInput.field(city) || ValidateInput.field(address)) {
                addressFlag=true;
            } else {
                addressFlag=false;
            }
        } else {
            if (ValidateInput.field(address)) {
                addressFlag=true;
            } else {
                addressFlag=false;
            }
        }

        if (isProfileSelected) {
            createEventStorage(eventKey);
        }
        if (addressFlag) {
            if (ValidateInput.field(event_name) && ValidateInput.field(description)) {
                progressDialog.show();

                String eventName=event_name.getText().toString();
                String eventDescription=description.getText().toString();

                String venuStr="";
                venuStr=venu.getText().toString();
                String cityStr="";
                cityStr=city.getText().toString();
                String addressStr=address.getText().toString();

                String startDate=choose_start_date.getText().toString();
                String endDate=choose_end_date.getText().toString();
                String startTime=choose_start_time.getText().toString();
                String endTime=choose_end_time.getText().toString();
                Long timeStamp=selectedStartDate;
                int totalFee=Integer.parseInt(total_fee.getText().toString());
                Event event;
                event=new Event(eventKey, eventName, eventDescription, category, picImageUrlString, event_type, venuStr, cityStr, addressStr, timeStamp, startDate, endDate, startTime, endTime, payable, totalFee, currentUserId);
                eventRef.child(event.getEventType()).child(eventKey).child("EventDetails").setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditEventActivity.this, "new event created", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        finish();
                    }
                });
            }
        }
    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver=this.getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void createEventStorage(String eventId) {
        StorageReference sReference=FirebaseStorage.getInstance().getReference().child("Events").child(eventId);

        final StorageReference imgPath=sReference.child(System.currentTimeMillis() + "." + getFileExtention(picImageUri));

        imgPath.putFile(picImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        picImageUrlString=uri.toString();

                    }

                });
            }
        });
    }

    private void spinnerImplementationForTopic() {

        final ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category=(String) parent.getItemAtPosition(position);
                if (category.equals("Other")) {
                    getOtherNewCategory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getOtherNewCategory() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Enter New Category");

        // Set up the input
        final EditText input=new EditText(this);
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
                Toast.makeText(EditEventActivity.this, "You have to specify the new Category", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void chipActionHandled() {
        paidChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payable=true;
                fee_layout.setVisibility(View.VISIBLE);
            }
        });
        unpaidChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payable=false;
                fee_layout.setVisibility(View.GONE);
            }
        });
        offlineChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_type="Offline";
                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);
            }
        });
        onlineChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event_type="Online";

                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.GONE);
                address.setVisibility(View.GONE);

            }
        });
        bothChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                event_type="Both";
                venu.setVisibility(View.VISIBLE);
                city.setVisibility(View.VISIBLE);
                address.setVisibility(View.VISIBLE);
            }
        });
    }

    private void settingDateAndTime() {
        final Calendar c=Calendar.getInstance();
        final int mYear=c.get(Calendar.YEAR);
        final int mMonth=c.get(Calendar.MONTH);
        final int mDay=c.get(Calendar.DAY_OF_MONTH);

        /*  choose_start_date.setText(mDay + "-" + (mMonth + 1) + "-" + mYear);
        choose_end_date.setText(mDay + "-" + (mMonth + 1) + "-" + mYear);
        */

        final int mHour=c.get(Calendar.HOUR_OF_DAY);
        final int mMinute=c.get(Calendar.MINUTE);

        /*  choose_start_time.setText(mHour + ":" + mMinute);
          choose_end_time.setText(mHour + ":" + mMinute);
        */

        // selectedStartDate=fieldToTimestamp(mYear, mMonth, mDay);

        choose_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePickerDialog(mYear, mMonth, mDay, choose_start_date, true);
            }
        });
        choose_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                        choose_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
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
                        choose_time.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private void calculateEventFee() {
        int feeAmount=Integer.parseInt(fee_amount.getText().toString());
        int convenienceFee=(int) Math.ceil(feeAmount * 0.08f);
        convenience_fee_amount.setText(String.valueOf(convenienceFee));
        int payumoneyFee=(int) Math.ceil((feeAmount + convenienceFee) * 0.02);
        payumoney_amount.setText(String.valueOf(payumoneyFee));
        int total=feeAmount + convenienceFee + payumoneyFee;
        total_fee.setText(String.valueOf(total));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result=CropImage.getActivityResult(data);
                    picImageUri=result.getUri();
                    event_image.setImageURI(picImageUri);
                    isProfileSelected=true;
                    break;
                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDialog() {
        new AlertDialog.Builder(getApplicationContext())

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting event")
                .setMessage("your event will be deleted permanently")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        eventRef.child(event.getEventType()).child(event.getEventId()).removeValue();
                        Toast.makeText(getApplicationContext(), "Event deleted", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void deleteEvent(View view) {
        showDialog();
        /*eventRef.child(event.getEventType()).child(event.getEventId()).removeValue();
        Toast.makeText(view.getContext(), "event deleted", Toast.LENGTH_SHORT).show();*/
    }
}
