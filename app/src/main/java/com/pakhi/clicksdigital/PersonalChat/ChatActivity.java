package com.pakhi.clicksdigital.PersonalChat;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.APIService;
import com.pakhi.clicksdigital.Adapter.MessageAdapter;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Notifications.Client;
import com.pakhi.clicksdigital.Notifications.Data;
import com.pakhi.clicksdigital.Notifications.MyResponse;
import com.pakhi.clicksdigital.Notifications.Sender;
import com.pakhi.clicksdigital.Notifications.Token;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    static final  int           REQUEST_IMAGE_CAPTURE=1;
    final static  int           PICK_PDF_CODE        =2342;
    static final  int           REQUESTCODE          =12;
    static        int           REQUEST_CODE         =1;
    private final List<Message> messagesList         =new ArrayList<>();
    Uri imageUriGalary, imageUriCamera;
    User      user;
    ImageView attach_file_btn, back_btn;
    PermissionsHandling      permissions;
    ValueEventListener       seenListener;
    DatabaseReference        reference;
    APIService               apiService;
    boolean                  notify=false;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID, messageSenderName;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private DatabaseReference   databaseReference;
    private ImageButton         SendMessageButton;
    private EditText            MessageInputText;
    private LinearLayoutManager linearLayoutManager;
    ;
    private MessageAdapter messageAdapter;
    private RecyclerView   userMessagesList;
    private String         saveCurrentTime, saveCurrentDate;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef=FirebaseDatabaseInstance.getInstance();
        pref=SharedPreference.getInstance();
        messageSenderID=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        messageReceiverID=getIntent().getExtras().get("visit_user_id").toString();

        apiService=Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        databaseReference=rootRef.getUserRef().child(messageReceiverID);
        databaseReference.child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user=dataSnapshot.getValue(User.class);
                messageReceiverName=user.getUser_name();
                userName.setText(messageReceiverName);
                Picasso.get()
                        .load(user.getImage_url())
                        .resize(120, 120)
                        .into(userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference=rootRef.getUserRef().child(messageSenderID);
        databaseReference.child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user=dataSnapshot.getValue(User.class);
                messageSenderName=dataSnapshot.child(Const.USER_NAME).getValue(String.class);
                //  userName.setText(messageReceiverName);
            /*    Picasso.get()
                        .load(user.getImage_url())
                        .resize(120, 120)
                        .into(userImage);*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        IntializeControllers();
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify=true;
                String messageText=MessageInputText.getText().toString();

                if (TextUtils.isEmpty(messageText)) {
                    showToast("first write your message...");
                } else {
                    MessageInputText.setText("");
                    SendMessage("text", messageText);
                }
            }
        });

        DisplayLastSeen();
        permissions=new PermissionsHandling(this);
        attach_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForPremission();
                popupMenuSettigns();


               // sendNotifiaction(messageSenderID,"Pooja","this is notification");
            }
        });

        seenMessage();
    }

    private void IntializeControllers() {
      /*  ChatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);*/

        back_btn=findViewById(R.id.back_btn);
        userName=(TextView) findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView) findViewById(R.id.custom_user_last_seen);
        userImage=(CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton=(ImageButton) findViewById(R.id.send_message_btn);
        MessageInputText=(EditText) findViewById(R.id.input_message);

        messageAdapter=new MessageAdapter(messagesList, Const.personalChat);
        userMessagesList=(RecyclerView) findViewById(R.id.private_messages_list_of_users);

        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        //userMessagesList.setItemAnimator(null);

        userMessagesList.setAdapter(messageAdapter);
        attach_file_btn=findViewById(R.id.attach_file_btn);
        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());
    }

    private void seenMessage() {
        reference=FirebaseDatabase.getInstance().getReference("Messages");
        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat=snapshot.getValue(Message.class);
                    if (chat.getTo().equals(messageSenderID) && chat.getFrom().equals(messageReceiverID)) {
                        HashMap<String, Object> hashMap=new HashMap<>();
                        hashMap.put("seen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayLastSeen() {
        rootRef.getUserRef().child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state=dataSnapshot.child("userState").child("state").getValue().toString();
                            String date=dataSnapshot.child("userState").child("date").getValue().toString();
                            String time=dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        } else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
        messagesList.clear();
        messageAdapter.notifyDataSetChanged();
        rootRef.getMessagesListRef().child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String messageId=dataSnapshot.getValue().toString();
                        rootRef.getMessagesRef().child(messageId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Message messages=snapshot.getValue(Message.class);
                                //messagesList.clear();
                                messagesList.add(messages);
                                messageAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //Message messages = dataSnapshot.getValue(Message.class);
                        //messagesList.add(messages);

                        //  messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    private void SendMessage(String messageType, String message) {

        String messageSenderRef="MessagesList/" + messageSenderID + "/" + messageReceiverID;
        String messageReceiverRef="MessagesList/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef=rootRef.getMessagesRef().push();
        // .child(messageSenderID).child(messageReceiverID).push();

        String messagePushID=userMessageKeyRef.getKey();

        /* Map messageTextBody = new HashMap();*/

        Message message1=new Message(messageSenderID, message,
                messageType, messageReceiverID, messagePushID, saveCurrentTime, saveCurrentDate, false);

        /*  messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);

        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("message", message);
        messageTextBody.put("type", messageType);*/

        Map messageBodyDetails=new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messagePushID);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messagePushID);

        rootRef.getMessagesRef().child(messagePushID).setValue(message1);


        rootRef.getRootRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    // Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
                MessageInputText.setText("");
            }
        });

        if (notify) {
            sendNotifiaction(messageReceiverID, messageSenderName, message);
        }
        notify=false;
    }

    private void sendNotifiaction(String receiver, final String username, final String message) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token=snapshot.getValue(Token.class);

                 /*   Intent resultIntent = new Intent(getApplicationContext(), ChatActivity.class);
                    resultIntent.putExtra("visit_user_id", user);
                    resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/

                    Data data=new Data(messageSenderID, R.drawable.logo, username + ": " + message, "New Message",
                            messageReceiverID);

                    Sender sender=new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

        rootRef.getUserRef().child(messageSenderID).child("userState")
                .updateChildren(onlineStateMap);
    }

    private void popupMenuSettigns() {
        PopupMenu popup=new PopupMenu(ChatActivity.this, attach_file_btn);
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
        /*if (item.getItemId() == R.id.audio_menu) {

        }
        if (item.getItemId() == R.id.contact_menu) {

        }
        if (item.getItemId() == R.id.location_menu) {

        }*/

        return true;
    }

    private void openFileGetDoc() {

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
                //permission granted
                logMessage("permission granted-----------");

            } else {

                //permission not granted
                logMessage(" permission  not granted-------------");

            }
        }
    }

    void openGalary() {
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
            switch (requestCode) {
               /* case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    imageUriGalary = result.getUri();
                    uploadImage(imageUriGalary);
                    break;

                */
                case REQUESTCODE:
                    imageUriGalary=data.getData();
                    progressDialog.show();
                    Log.d("ChatActivity", "-----------on rersult---gallery img---------" + data.getData().toString());
                    uploadImage(imageUriGalary);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras=data.getExtras();
                    Bitmap imageBitmap=(Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path=MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera=Uri.parse(path);
                    progressDialog.show();
                    uploadImage(imageUriCamera);
                    break;
                case PICK_PDF_CODE:
                    // docUri = data.getData();
                    Log.d("ChatActivity", "-----------on rersult-----------------" + data.getData().toString());
                    progressDialog.show();
                    uploadFile(data.getData());
                    break;
                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();

            }
        } else {
            showToast("something gone wrong");
        }
    }

    private void uploadFile(Uri data) {
        Toast.makeText(this, "Wait for file to be uploaded", Toast.LENGTH_SHORT).show();

        // progressDialog.show();
        StorageReference storageRootReference=FirebaseStorage.getInstance().getReference();
        StorageReference sRef=storageRootReference.child(Const.USER_MEDIA_PATH).child(messageSenderID).child(Const.FILES_PATH).child("Sent_Pdf").child(messageReceiverID).child(System.currentTimeMillis() + "");
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
                                        Log.d("ChatActivity", "----------------------------------------------hhhhhhhhhhhhhhhhhhhhh-----------------" + uri.toString());
                                        SendMessage("pdf", String.valueOf(uri));
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
        Log.d("ChatActivity", "-----------uploading image----------------------");
        StorageReference sReference=FirebaseStorage.getInstance().getReference().child("User_Media").child(messageSenderID).child(Const.PHOTOS).child("Sent_Photos").child(messageReceiverID);
        final StorageReference imgPath=sReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));

        imgPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        Log.d("ChatActivity", "-----------uploading image----------------------" + uri.toString());
                        SendMessage("image", uri.toString());

                    }
                });

            }
        });
    }

    private void logMessage(String s) {
        Log.d("Testing Developer mode ", s);
    }

    private void showToast(String s) {
        Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        reference.removeEventListener(seenListener);

    }

}
