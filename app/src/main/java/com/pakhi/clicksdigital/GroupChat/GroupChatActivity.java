package com.pakhi.clicksdigital.GroupChat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupChatActivity extends AppCompatActivity {
    int i=0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    final static int PICK_PDF_CODE = 2342;
    static final int REQUESTCODE = 12;
    static int REQUEST_CODE = 1;
    private final List<Message> messagesList = new ArrayList<>();
    int limitation = 15, num_of_messages = 15;
    ImageView attach_file_btn, image_profile /*,requesting_users*/, back_btn, raise_topic;
    Uri imageUriGalary, imageUriCamera;
    UserDatabase db;
    User user;
    PermissionsHandling permissions;
    SharedPreference pref;
    String topic_str;
    FirebaseDatabaseInstance rootRef;
    boolean limitReached = false;
    int time_fired = 3;
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private DatabaseReference UsersRef, GroupMessageKeyRef, GroupIdRef, groupChatRefForCurrentGroup;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime, currentGroupId;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private ProgressDialog progressDialog;
    private TextView group_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat2);

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        currentGroupId = getIntent().getExtras().get(Const.groupId).toString();

        pref = SharedPreference.getInstance();
        currentUserID = pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef = FirebaseDatabaseInstance.getInstance();
        UsersRef = rootRef.getUserRef();
        GroupIdRef = rootRef.getGroupRef().child(currentGroupId);
        groupChatRefForCurrentGroup = rootRef.getGroupChatRef().child(currentGroupId);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        db = new UserDatabase(this);
        getUserFromDb();
        GetUserInfo();

        InitializeFields();
        //currentGroupName = "Name Here";
        group_name.setText(currentGroupName);

        GroupIdRef.child(ConstFirebase.users).addValueEventListener(new ValueEventListener() {
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

        final String[] image_url = new String[1];
        FirebaseStorageInstance storageRootRef = FirebaseStorageInstance.getInstance();
        final StorageReference imgPath = storageRootRef.getGroupProfileRef().child(currentGroupId); //+ "." + getFileExtention(picImageUri)

        imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                image_url[0] = uri.toString();
                Picasso.get().load(uri).into(image_profile);

            }

        });
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToGroupDetails(image_url[0]);
            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = userMessageInput.getText().toString();

                if (TextUtils.isEmpty(message)) {
                    showToast("first write your message...");
                } else {

                    String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
                    Pattern p;
                    Matcher m = null;
                    String[] words = message.split(" ");
                    i=0;
                    for (String word : words) {
                        p = Pattern.compile(URL_REGEX);
                        m = p.matcher(word);

                        if(m.find()) {
                            Toast.makeText(getApplicationContext(), "The String contains URL", Toast.LENGTH_LONG).show();
                            i=1;
                            break;
                        }

                    }
                    userMessageInput.setText("");
                    if(i==1){
                        //Toast.makeText(getApplicationContext(), "URL type", Toast.LENGTH_LONG).show();
                        SaveMessageInfoToDatabase("url", message);
                    }else{
                        //Toast.makeText(getApplicationContext(), "Text type", Toast.LENGTH_LONG).show();
                        SaveMessageInfoToDatabase("text", message);
                    }

                }

                /*if (TextUtils.isEmpty(message)) {
                    //Toast.makeText(GroupChatActivity.this, "Please write message first...", Toast.LENGTH_SHORT).show();
                    showToast("Please write message first...");
                } else {

                    userMessageInput.setText("");
                    SaveMessageInfoToDatabase("text", message);
                }*/
            }
        });
        permissions = new PermissionsHandling(this);
        attach_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForPremission();
                popupMenuSettigns();
            }
        });

        String user_type = pref.getData(SharedPreference.user_type, getApplicationContext());

       /* if (user_type.equals("admin")) {
            requesting_users.setVisibility(View.VISIBLE);
        }
        requesting_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestsActivity();
            }
        });*/

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        raise_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTopicRaiseFagmentForResult();
                // Log.d("TESTINGTOPICSTART","---------------------topic 2"+topic_str);
            }
        });

        //define limitation out of method

/*
ChildEventListener mChildEventListener ;
        groupChatRefForCurrentGroup.keepSynced(true);
        mChildEventListener = groupChatRefForCurrentGroup.limitToLast((int) limitation)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Message messages = dataSnapshot.getValue(Message.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        linearLayoutManager.setStackFromEnd(true);

                      */
/*  if(time_fired > 2 && !mSentMessage){
                          //  userMessageInput.setVisibility(View.VISIBLE);
                          //  mFab.show();
                        }

                        if(!userMessagesList.canScrollVertically(1)){
                           // userMessageInput.setVisibility(View.GONE);
                          //  mFab.hide();
                        }*/

        /*
                        if(mSentMessage){
                            mSentMessage = false;
                        }

                        time_fired++;


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });


        //When user scrolls up new data gets added

        userMessagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(-1)){
                    if(!limitReached) {
                        limitation += 12;

                        */
        /*
        DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference().child("Messages");
                        tempRef.child(mMessageSenderID).child(mMessageReceiverID)
                                groupChatRefForCurrentGroup.limitToLast((int) limitation).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (limitation > snapshot.getChildrenCount()) {
                                    limitation = num_of_messages;
                                    limitReached = true;

                                    DatabaseReference temp = FirebaseDatabase.getInstance().getReference().child("Messages");
                                    temp.child(mMessageSenderID).child(mMessageReceiverID);

                                    groupChatRefForCurrentGroup.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            messagesList.clear();
                                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                Message messages = snapshot1.getValue(Message.class);
                                                messagesList.add(messages);
                                            }
                                            messageAdapter.notifyItemRangeChanged(0, messageAdapter.getItemCount());

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                                messagesList.clear();
                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                    Message messages = snapshot1.getValue(Message.class);
                                    messagesList.add(messages);
                                    //messageAdapter.notifyDataSetChanged(); BEFORE
                                }
                                messageAdapter.notifyItemRangeChanged(0, messageAdapter.getItemCount());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(dy < -20){
                  //  mFab.show();
                }
            }
        });*/
    }

    private void startTopicRaiseFagmentForResult() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        /*builder.setTitle("Enter new Topic");*/

        LayoutInflater inflater = ((Activity) this).getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_topic_raise, null);
        final EditText topic = v.findViewById(R.id.topic);
        //  v.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT));
        builder.setView(v);

        // Set up the buttons
        builder.setPositiveButton("Publish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                topic_str = topic.getText().toString();
                SaveMessageInfoToDatabase("topic", topic_str);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void sendUserToGroupDetails(String s) {
        Intent groupMembersIntent = new Intent(GroupChatActivity.this, GroupDetailsActivity.class);
        groupMembersIntent.putExtra(Const.group_id, currentGroupId);
        //groupMembersIntent.putExtra("image_url", s);
        groupMembersIntent.putExtra(Const.group_name, currentGroupName);

        startActivity(groupMembersIntent);
    }

    private void getUserFromDb() {
        db.getReadableDatabase();
        Cursor res = db.getAllData();
        if (res.getCount() == 0) {

        } else {
            res.moveToFirst();
            user = new User(res.getString(0), res.getString(1),
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

                    Message messages = dataSnapshot.getValue(Message.class);

                    messagesList.add(messages); ////-----------

                    Collections.sort(messagesList, new Comparator<Message>() {
                        public int compare(Message o1, Message o2) {
                            return o1.getTimestamp().compareTo(o2.getTimestamp());
                        }
                    });

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
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);

        attach_file_btn = findViewById(R.id.attach_file_btn);
        image_profile = findViewById(R.id.image_profile);
        group_name = findViewById(R.id.group_name);
        back_btn = findViewById(R.id.back_btn);
        raise_topic = findViewById(R.id.raise_topic);

        messageAdapter = new MessageAdapter(messagesList, "GroupChat", getApplicationContext());
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);

        linearLayoutManager = new LinearLayoutManager(this);
        // linearLayoutManager.setStackFromEnd(true);
        // linearLayoutManager.setReverseLayout(true); ////-----------
        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messageAdapter);

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setBackground(getResources().getDrawable(R.drawable.icn_actionbar_background));
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(toolbar_text);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }*/

    }

    @Override
    protected void onRestart() {
        super.onRestart();

      /*  new Handler().post(new Runnable() {

            @Override
            public void run() {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                finish();

                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });*/
        /*  if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        } else {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);

            startActivity(intent);
            overridePendingTransition(0, 0);
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*  new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                finish();

                overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });*/

        /* if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        } else {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);

            startActivity(intent);
            overridePendingTransition(0, 0);
        }*/
    }

    private void GetUserInfo() {
        currentUserName = user.getUser_name();
    }

    private void SaveMessageInfoToDatabase(String messageType, String message) {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForDate.getTime());

        HashMap<String, Object> groupMessageKey = new HashMap<>();

        groupChatRefForCurrentGroup.updateChildren(groupMessageKey);
        String messagekEY = groupChatRefForCurrentGroup.push().getKey();
        Long timestamp = calForDate.getTimeInMillis() / 1000L;

        Message message1 = new Message(currentUserID, message,
                messageType, currentGroupId, messagekEY, currentTime, currentDate, timestamp);

        groupChatRefForCurrentGroup.child(messagekEY).setValue(message1);

        if (messageType == "topic") {
            saveSeparateTopicNode(messagekEY);
        }
        progressDialog.dismiss();
    }

    private void saveSeparateTopicNode(String messagekEY) {
        DatabaseReference topicRef = rootRef.getTopicRef();
        topicRef.child(messagekEY).setValue(currentGroupId);
    }

    private void popupMenuSettigns() {
        PopupMenu popup = new PopupMenu(GroupChatActivity.this, attach_file_btn);
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
        Intent intent = new Intent();
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
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUESTCODE);
    }

    void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                    imageUriGalary = data.getData();
                    uploadImage(imageUriGalary);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera = Uri.parse(path);
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
        StorageReference storageRootReference = FirebaseStorageInstance.getInstance().getRootRef();
        StorageReference sRef = storageRootReference.child(ConstFirebase.USER_MEDIA_PATH).child(currentUserID).child(ConstFirebase.FILES_PATH).child("Sent_Pdf").child(currentGroupId).child(System.currentTimeMillis() + "");
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
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(final Uri imageUri) {
        StorageReference sReference = FirebaseStorageInstance.getInstance().getRootRef().child("Group_photos").child(currentGroupId).child("photos");
        final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));

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

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put(Const.time, saveCurrentTime);
        onlineStateMap.put(Const.date, saveCurrentDate);
        onlineStateMap.put(Const.state, state);

        UsersRef.child(currentUserID).child(ConstFirebase.userState)
                .updateChildren(onlineStateMap);

    }

   /* public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView no_of_likes, senderMessageText, receiverMessageText, senderDate, receiverDate, receiver_name, isSeen, publisher_name, topic_text, topic_date_time, reply, no_of_replies;
        public CircleImageView receiverProfileImage;

        public ImageView messageSenderPicture, messageReceiverPicture, download_image_receiver, download_pdf_receiver, like;
        LinearLayout receiverlayout, senderlayout, receiverLayoutPdf, senderLayoutPdf, group_topic_layout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            //left side box
            receiverlayout=itemView.findViewById(R.id.receiver_message_layout);
            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);
            receiver_name=itemView.findViewById(R.id.receiver_name);
            receiverMessageText=(TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverDate=itemView.findViewById(R.id.receiver_message_date_time);

            download_image_receiver=itemView.findViewById(R.id.download_image_receiver);
            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_image_view);

            receiverLayoutPdf=itemView.findViewById(R.id.layout_recevier_pdf);
            download_pdf_receiver=itemView.findViewById(R.id.download_pdf);

            //right side box
            senderlayout=itemView.findViewById(R.id.sender_message_layout);
            senderMessageText=(TextView) itemView.findViewById(R.id.sender_messsage_text);
            senderDate=itemView.findViewById(R.id.sender_messsage_date_time);

            senderLayoutPdf=itemView.findViewById(R.id.layout_sender_pdf);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_image_view);

            //right side
            isSeen=itemView.findViewById(R.id.isSeen);
            isSeen.setText("deliverd");

            //Discussion Topic
            group_topic_layout=itemView.findViewById(R.id.group_topic_layout);
            publisher_name=itemView.findViewById(R.id.publisher_name);
            topic_text=itemView.findViewById(R.id.topic_text);
            topic_date_time=itemView.findViewById(R.id.topic_date_time);
            reply=itemView.findViewById(R.id.reply);
            no_of_replies=itemView.findViewById(R.id.no_of_replies);
            like=itemView.findViewById(R.id.like);
            no_of_likes=itemView.findViewById(R.id.no_of_likes);
        }
    }*/
}
