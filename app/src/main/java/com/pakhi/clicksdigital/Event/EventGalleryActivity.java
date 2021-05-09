package com.pakhi.clicksdigital.Event;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.Image;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.Notification;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventGalleryActivity extends AppCompatActivity {
    static final int REQUESTCODE = 12;
    static int REQUEST_CODE = 1;
    List<Image> images = new ArrayList<>();
    String name;
    ImageView add_photo;
    PermissionsHandling permissions;
    Uri imageUri;
    Event event;
    DatabaseReference eventRef, sliderImageRef;
    ImageAdapter mAdapter;
    String eventName, messageSenderID;
    int number;
    ArrayList<String> imageName = new ArrayList<>();
    SharedPreference pref;
    private byte[] mUploadBytes;
    private Uri mlectedUri;
    private double mProgress = 0;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_gallery);



        event = (Event) getIntent().getSerializableExtra(Const.event);

        pref = SharedPreference.getInstance();
        FirebaseDatabaseInstance rootRef = FirebaseDatabaseInstance.getInstance();

        messageSenderID = pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef.getUserRef().child(messageSenderID).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child(ConstFirebase.userName).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        eventRef = rootRef.getEventRef().child(event.getEventId()).child(ConstFirebase.PHOTOS);
        sliderImageRef = rootRef.getsliderRef();

        rootRef.getEventRef().child(event.getEventId()).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventName = snapshot.child(ConstFirebase.eventName1).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        initializeFields();

        getAllImages();

        add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 22) {
                    requestForPremission();
                } else {
                    openGallery();
                }
            }
        });
    }

    private void initializeFields() {
        // gridView=findViewById(R.id.myGrid);
        add_photo = findViewById(R.id.add_photo);

        loadingBar = new ProgressDialog(this);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true); // Helps improve performance

        mAdapter = new ImageAdapter(this, images, event);
        mRecyclerView.setAdapter(mAdapter);
    }

    void requestForPremission() {
        //checking for permissions
        permissions = new PermissionsHandling(this);
        if (!permissions.isPermissionGranted()) {
            //when permissions not granted
            if (permissions.isRequestPermissionable()) {
                //creating alertDialog
                permissions.showAlertDialog(REQUEST_CODE);

            } else {
                permissions.requestPermission(REQUEST_CODE);
            }
        } else {
            openGallery();
            //when those permissions are already granted
            //popupMenuSettigns();
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
                //permission granted

            } else {

                //permission not granted
            }
        }
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
    }

    private void getAllImages() {
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        imageUrls.add(dataSnapshot.getValue().toString());
//                        imageNames.add(dataSnapshot.getKey());
                        images.add(new Image(dataSnapshot.getKey(), dataSnapshot.getValue().toString()));
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null) {
            imageUri = data.getData();
            uploadImage(imageUri);

        } else {
            //showToast("Nothing is selected");
        }
    }

    private void uploadImage(final Uri imageUri) {
        Bitmap mbitmap = null;
        try {
            mbitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        } catch (IOException e) {
            // Log.i("The exception --- ", e.getMessage());
        }

        BackGroundImageResize resize = new BackGroundImageResize(mbitmap);
        resize.execute(imageUri);

    }

    private void SaveUrlToDatabase(String imageUrl, String image_name) {

        eventRef.child(image_name).setValue(imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //do somthing on data change
                FirebaseDatabaseInstance root= FirebaseDatabaseInstance.getInstance();
                root.getEventRef().child(event.getEventId()).child(ConstFirebase.participants).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap:snapshot.getChildren()){
                            if(!messageSenderID.equals(snap.getKey())){
                                Notification.sendPersonalNotifiaction(event.getEventId(), snap.getKey(), name+" has added photo to the event", eventName, "eventPhoto","" );

                                FirebaseDatabaseInstance rootRef= FirebaseDatabaseInstance.getInstance();


                                String notificationKey = rootRef.getNotificationRef().push().getKey();

                                rootRef.getNotificationRef().child(notificationKey).child("to").setValue(snap.getKey());
                                rootRef.getNotificationRef().child(notificationKey).child("from").setValue(messageSenderID);
                                rootRef.getNotificationRef().child(notificationKey).child("go").setValue(event.getEventId());
                                rootRef.getNotificationRef().child(notificationKey).child("type").setValue("eventPhoto");

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
        return stream.toByteArray();
    }

    public void executeUploadtask(byte[] mUploadBytes) {

        StorageReference sReference = FirebaseStorageInstance.getInstance().getRootRef().child(ConstFirebase.EventPhotos).child(event.getEventId());
        final String image_name = System.currentTimeMillis() + ""; //+ "." + getFileExtention(imageUri
        final StorageReference imgPath = sReference.child(image_name);

        imgPath.putBytes(mUploadBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        SaveUrlToDatabase(uri.toString(), image_name);
                        addToRTDB(uri.toString(), image_name);
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                mProgress = 100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                loadingBar.setMessage("Uploading ... " + (int) mProgress + "%");

            }
        });

    }

    public void addToRTDB(final String imageUrl, final String image_name) {

        final FirebaseDatabaseInstance rootRefAdd = FirebaseDatabaseInstance.getInstance();

        rootRefAdd.getsliderRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                number = (int) snapshot.getChildrenCount();
                imageName.add(image_name);
                rootRefAdd.getsliderRef().child(image_name).child(ConstFirebase.nameOfEvent).setValue(eventName);
                rootRefAdd.getsliderRef().child(image_name).child(ConstFirebase.url).setValue(imageUrl);
                rootRefAdd.getsliderRef().child(image_name).child(ConstFirebase.sender).setValue(messageSenderID);

                loadingBar.setMessage("The Image is successfully uploaded");
                loadingBar.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }//gs://clicksdigital-ad067.appspot.com/Events/-MEmdYeqcVMFRzeUTTgH/1597503847356.jpg
        });
    }

    public class BackGroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mbitmap;

        public BackGroundImageResize(Bitmap bitmap) {
            this.mbitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingBar.setTitle("Compressing and Uploading your image");
            loadingBar.setMessage("Compressing ...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            executeUploadtask(bytes);
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            if (mbitmap == null) {
                try {
                    mbitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), params[0]);
                } catch (IOException e) {
                    Log.i("The exception --- ", e.getMessage());
                }
            }
            byte[] bytes = null;
            bytes = getBytesFromBitmap(mbitmap, 65);
            return bytes;
        }
    }
}