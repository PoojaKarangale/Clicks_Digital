package com.pakhi.clicksdigital.Event;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.Image;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventGalleryActivity extends AppCompatActivity {
    static final int REQUESTCODE =12;
    static       int REQUEST_CODE=1;
  //  GridView            gridView;
   /* List<String> imageUrls =new ArrayList<>();
    List<String> imageNames=new ArrayList<>();*/
    List<Image>  images=new ArrayList<>();

    ImageView           add_photo;
    PermissionsHandling permissions;
    Uri                 imageUri;
    Event               event;
    DatabaseReference   eventRef,sliderImageRef;
    ImageAdapter mAdapter;
    String eventName;
    int number;
    ArrayList<String> imageName = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_gallery);

        event=(Event) getIntent().getSerializableExtra("event");
        FirebaseDatabaseInstance rootRef=FirebaseDatabaseInstance.getInstance();
        eventRef=rootRef.getEventRef().child(event.getEventType()).child(event.getEventId()).child("Photos");
        sliderImageRef = rootRef.getsliderRef();
        rootRef.getEventRef().child(event.getEventType()).child(event.getEventId()).child("EventDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventName= snapshot.child("eventName").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        initializeFields();

        getAllImages();
       /* gridView.setAdapter(new ImageAdapter(imageUrls, imageNames, this));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item_pos=imageUrls.get(position);
                ShowDialogBox(item_pos);
            }
        });
        */
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


        RecyclerView mRecyclerView=(RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true); // Helps improve performance


        mAdapter=new ImageAdapter(this, images);
        mRecyclerView.setAdapter(mAdapter);
    }

    void requestForPremission() {
        //checking for permissions
        permissions=new PermissionsHandling(this);
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
        Intent i=new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
    }

    private void initializeFields() {
       // gridView=findViewById(R.id.myGrid);
        add_photo=findViewById(R.id.add_photo);
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
                        images.add(new Image(dataSnapshot.getKey(),dataSnapshot.getValue().toString()));
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

/*
    public void ShowDialogBox(final String item_pos) {
        final Dialog dialog=new Dialog(this);

        dialog.setContentView(R.layout.custom_dialog_for_gallery);

        //Getting custom dialog views
        // TextView image_name = dialog.findViewById(R.id.txt_Image_name);
        ImageView image=dialog.findViewById(R.id.img);
        Button btn_Full=dialog.findViewById(R.id.btn_full);
        Button btn_Close=dialog.findViewById(R.id.btn_close);

        // String title = imageNames(item_pos);

        //extracting name
        */
/*int index = title.indexOf("/");
        String name = title.substring(index + 1, title.length());
        image_name.setText(name);*//*


        // image.setImageResource(item_pos);

        Picasso.get().load(item_pos).placeholder(R.drawable.profile_image).into(image);
        btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_Full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EnlargedImage.enlargeImage(item_pos,getApplicationContext());

            }
        });
        dialog.show();

    }
*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null) {
            imageUri=data.getData();
            uploadImage(imageUri);

        } else {
            //showToast("Nothing is selected");
        }
    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(final Uri imageUri) {
        StorageReference sReference=FirebaseStorageInstance.getInstance().getRootRef().child("Event_photos").child(event.getEventId());
        final String image_name=System.currentTimeMillis() + ""; //+ "." + getFileExtention(imageUri
        final StorageReference imgPath=sReference.child(image_name);
        final StorageReference sliderRef = FirebaseStorageInstance.getInstance().getRootRef().child("Slider_Images").child(image_name);
        sliderRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                sliderRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        addToRTDB(uri.toString(), image_name);
                    }
                });
            }
        });
        imgPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        SaveUrlToDatabase(uri.toString(), image_name);

                    }
                });

            }
        });
    }
    public void addToRTDB(final String imageUrl, final String image_name){
        final FirebaseDatabaseInstance rootRefAdd=FirebaseDatabaseInstance.getInstance();

        rootRefAdd.getsliderRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                number= (int) snapshot.getChildrenCount();
                    imageName.add(image_name);
                    rootRefAdd.getsliderRef().child(image_name).child("NameOfEvent").setValue(eventName);
                    rootRefAdd.getsliderRef().child(image_name).child("URL").setValue(imageUrl);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void SaveUrlToDatabase(String imageUrl, String image_name) {

        eventRef.child(image_name).setValue(imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(EventGalleryActivity.this, "image added", Toast.LENGTH_SHORT);

                //do somthing on data change

            }
        });
    }

}
