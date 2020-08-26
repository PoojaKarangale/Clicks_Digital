package com.pakhi.clicksdigital.GroupChat;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Activities.UserRequestActivity;
import com.pakhi.clicksdigital.Adapter.MessageAdapter;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    static final  int           REQUEST_IMAGE_CAPTURE=1;
    final static  int           PICK_PDF_CODE        =2342;
    static final  int           REQUESTCODE          =12;
    static        int           REQUEST_CODE         =1;
    private final List<Message> messagesList         =new ArrayList<>();
    ImageView attach_file_btn, image_profile, requesting_users, back_btn;
    Uri imageUriGalary, imageUriCamera;
    UserDatabase             db;
    User                     user;
    PermissionsHandling      permissions;
    FirebaseDatabaseInstance roothRef;
    SharedPreference         pref;
    private Toolbar           mToolbar;
    private ImageButton       SendMessageButton;
    private EditText          userMessageInput;
    private DatabaseReference UsersRef, GroupMessageKeyRef, GroupIdRef, groupChatRefForCurrentGroup;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime, currentGroupId;
    private MessageAdapter      messageAdapter;
    private RecyclerView        userMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private ProgressDialog      progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat2);

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        currentGroupId=getIntent().getExtras().get("groupId").toString();

        pref=SharedPreference.getInstance();
        currentUserID=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        roothRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=roothRef.getUserRef();
        GroupIdRef=roothRef.getGroupRef().child(currentGroupId);
        groupChatRefForCurrentGroup=roothRef.getGroupChatRef().child(currentGroupId);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        db=new UserDatabase(this);
        getUserFromDb();

        InitializeFields();

        GroupIdRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(currentUserID).exists()) {
                    userMessageInput.setText("You can't sent message to this group");
                    userMessageInput.setEnabled(false);
                    SendMessageButton.setEnabled(false);
                    attach_file_btn.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String[] image_url=new String[1];
        GroupIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("image_url")) {
                        image_url[0]=dataSnapshot.child("image_url").getValue().toString();
                        Picasso.get().load(image_url[0]).into(image_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message=userMessageInput.getText().toString();

                if (TextUtils.isEmpty(message)) {
                    //Toast.makeText(GroupChatActivity.this, "Please write message first...", Toast.LENGTH_SHORT).show();
                    showToast("Please write message first...");
                } else {
                    userMessageInput.setText("");
                    SaveMessageInfoToDatabase("text", message);
                }
            }
        });
        permissions=new PermissionsHandling(this);
        attach_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForPremission();
                popupMenuSettigns();
            }
        });

        /*   banner_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToGroupDetails(image_url[0]);
            }
        });*/
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToGroupDetails(image_url[0]);
            }
        });

        String user_type=pref.getData(SharedPreference.user_type, getApplicationContext());
        if (user_type.equals("admin")) {
            requesting_users.setVisibility(View.VISIBLE);
        }
        requesting_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestsActivity();
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void goToRequestsActivity() {
        Intent intent=new Intent(this, UserRequestActivity.class);
        intent.putExtra(Const.groupId, currentGroupId);
        intent.putExtra(Const.groupName, currentGroupName);
        startActivity(intent);
    }

    private void sendUserToGroupDetails(String s) {
        Intent groupMembersIntent=new Intent(GroupChatActivity.this, GroupDetailsActivity.class);
        groupMembersIntent.putExtra("group_id", currentGroupId);
        groupMembersIntent.putExtra("image_url", s);
        groupMembersIntent.putExtra("group_name", currentGroupName);

        startActivity(groupMembersIntent);
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
                    res.getString(14)); //,res.getString(15),res.getString(16)
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //updateUserStatus("online");
        messagesList.clear();
        messageAdapter.notifyDataSetChanged();

        groupChatRefForCurrentGroup.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {

                    Message messages=dataSnapshot.getValue(Message.class);

                    messagesList.add(messages);

                    messageAdapter.notifyDataSetChanged();

                    userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    // DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields() {
        mToolbar=findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton=findViewById(R.id.send_message_button);
        userMessageInput=findViewById(R.id.input_group_message);

        attach_file_btn=findViewById(R.id.attach_file_btn);
        image_profile=findViewById(R.id.image_profile);
        requesting_users=findViewById(R.id.requesting_users);
        back_btn=findViewById(R.id.back_btn);
        // banner_white = findViewById(R.id.banner);
        //  group_members = findViewById(R.id.group_members);

        messageAdapter=new MessageAdapter(messagesList, "GroupChat", currentGroupId);
        userMessagesList=(RecyclerView) findViewById(R.id.private_messages_list_of_users);

        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }

    private void GetUserInfo() {
        currentUserName=user.getUser_name();
       /* UsersRef.child(currentUserID).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child(Const.USER_NAME).getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    */
    }

    private void SaveMessageInfoToDatabase(String messageType, String message) {

        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
        currentDate=currentDateFormat.format(calForDate.getTime());

        SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
        currentTime=currentTimeFormat.format(calForDate.getTime());

        HashMap<String, Object> groupMessageKey=new HashMap<>();

        groupChatRefForCurrentGroup.updateChildren(groupMessageKey);
        String messagekEY=groupChatRefForCurrentGroup.push().getKey();


        Message message1=new Message(currentUserID, message,
                messageType, currentGroupId, messagekEY, currentTime, currentDate);

       /* HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("name", currentUserName);
        messageInfoMap.put("to", currentGroupId);
        messageInfoMap.put("from", currentUserID);

        messageInfoMap.put("message", message);
        messageInfoMap.put("messageID", messagekEY);
        messageInfoMap.put("date", currentDate);
        messageInfoMap.put("time", currentTime);
        messageInfoMap.put("type", messageType);*/

        //  messageInfoMap.put("to", messageReceiverID);
        //  messageInfoMap.put("messageID", messagePushID);
        groupChatRefForCurrentGroup.child(messagekEY).setValue(message1);
        progressDialog.dismiss();
        // GroupMessageKeyRef.updateChildren(m);
    }

    private void popupMenuSettigns() {
        PopupMenu popup=new PopupMenu(GroupChatActivity.this, attach_file_btn);
        popup.getMenuInflater().inflate(R.menu.attach_file_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return menuItemClicked(item);
            }
        });
        popup.show();
    }

    private boolean menuItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.galary_pic_menu) {
            openGalary();
        }
        if (item.getItemId() == R.id.camera_menu) {
            openCamera();
        }
        if (item.getItemId() == R.id.doc_file_menu) {
            openFileGetDoc();
        }
      /*  if (item.getItemId() == R.id.audio_menu) {

        }
        if (item.getItemId() == R.id.contact_menu) {

        }
        if (item.getItemId() == R.id.location_menu) {

        }*/

        return true;
    }

    private void openFileGetDoc() {
     /*   Toast.makeText(this, "Allow all the required permissions for this app", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

      */
        //creating an intent for file chooser
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF_CODE);
    }

    void requestForPremission() {
        //checking for permissions

        if (!permissions.isPermissionGranted()) {
            //when permissions not granted
            if (permissions.isRequestPermissionable()) {
                //creating alertDialog
                permissions.showAlertDialog(REQUEST_CODE);
            } else {
                permissions.requestPermission(REQUEST_CODE);
            }
        } else {
            //when those permissions are already granted
            //popupMenuSettigns();
        }

  /*      if (ContextCompat.checkSelfPermission(GroupChatActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.WRITE_CONTACTS) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //when permissions not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.WRITE_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.CAMERA)) {
                //creating alertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Grant permissioms");
                builder.setMessage("Camera, read & write Contacts, read & write Storage");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        ActivityCompat.requestPermissions(
                                GroupChatActivity.this,
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS,
                                        Manifest.permission.CAMERA
                                },
                                REQUEST_CODE
                        );
                    }
                });

                //builder.setNegativeButton("Cancel",null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else {
                ActivityCompat.requestPermissions(
                        GroupChatActivity.this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.CAMERA
                        },
                        REQUEST_CODE
                );

            }
        } else {
            //when those permissions are already granted
            //popupMenuSettigns();
            logMessage("when those permissions are already granted=----------");
        }*/

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
                //popupMenuSettigns();
                //permission granted
                logMessage(" permission granted-----------");

            } else {

                //permission not granted
                //requestForPremission();
                logMessage(" permission  not granted-------------");

            }
        }
    }

    void openGalary() {
     /*   if (ContextCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(GroupChatActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCodeForGalary
                );
            }
        } else {}
      */

      /*  CropImage.activity().setAspectRatio(1, 1)
                .start(GroupChatActivity.this);

       */
        Intent i=new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
    }

    void openCamera() {
        Intent takePictureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            progressDialog.show();
            switch (requestCode) {
             /*   case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    imageUriGalary = result.getUri();
                    uploadImage(imageUriGalary);
                    break;

              */
                case REQUESTCODE:
                    imageUriGalary=data.getData();
                    uploadImage(imageUriGalary);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras=data.getExtras();
                    Bitmap imageBitmap=(Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path=MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera=Uri.parse(path);
                    uploadImage(imageUriCamera);
                    break;
                case PICK_PDF_CODE:
                    // docUri = data.getData();
                    uploadFile(data.getData());
                    break;
            }
        } else {
            showToast("something gone wrong");
        }
    }

    private void uploadFile(Uri data) {
        Toast.makeText(this, "Wait for file to be uploaded", Toast.LENGTH_SHORT).show();

        // progressDialog.show();
        StorageReference storageRootReference=FirebaseStorage.getInstance().getReference();
        StorageReference sRef=storageRootReference.child(Const.USER_MEDIA_PATH).child(currentUserID).child(Const.FILES_PATH).child("Sent_Pdf").child(currentGroupId).child(System.currentTimeMillis() + "");
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // progressDialog.dismiss();
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //SendMessage("pdf", String.valueOf(uri));
                                        SaveMessageInfoToDatabase("pdf", String.valueOf(uri));
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

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(final Uri imageUri) {
        StorageReference sReference=FirebaseStorage.getInstance().getReference().child("Group_photos").child(currentGroupId).child("photos");
        final StorageReference imgPath=sReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));

        imgPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        SaveMessageInfoToDatabase("image", uri.toString());

                    }
                });

            }
        });
    }

    private void logMessage(String s) {
        Log.d("Testing Developer mode ", s);
    }

    private void showToast(String s) {
        Toast.makeText(GroupChatActivity.this, s, Toast.LENGTH_SHORT).show();
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

        UsersRef.child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }
}
