package com.pakhi.clicksdigital.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.R;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    final static int PICK_PDF_CODE = 2342;
    static int REQUEST_CODE = 1;

    DatabaseReference groupChatRefForCurrentGroup;
    ImageView attach_file_btn;
    LinearLayout layout;
    Uri imageUriGalary, imageUriCamera,docUri;
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef, databaseReference, GroupIdRef;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime, CurrentGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat2);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        CurrentGroupId = getIntent().getExtras().get("groupId").toString();

        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        GroupIdRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(CurrentGroupId);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        groupChatRefForCurrentGroup = FirebaseDatabase.getInstance().getReference("GroupChat").child(CurrentGroupId);

        InitializeFields();
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        attach_file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForPremission();
                popupMenuSettigns();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupChatRefForCurrentGroup.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
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
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display_for_others);
        //displayTextMessagesForCurrentUser = (TextView) findViewById(R.id.group_chat_text_display_for_current_user);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
        layout = findViewById(R.id.layout_for_text_msg);
        attach_file_btn = findViewById(R.id.attach_file_btn);

    }

    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child(Constants.USER_NAME).getValue().toString();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            //Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForDate.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();

            groupChatRefForCurrentGroup.updateChildren(groupMessageKey);
            String messagekEY = groupChatRefForCurrentGroup.push().getKey();
            GroupMessageKeyRef = groupChatRefForCurrentGroup.child(messagekEY);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("user_id", currentUserID);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            String chat_userID = (String) ((DataSnapshot) iterator.next()).getValue();

            TextView textView = new TextView(GroupChatActivity.this);

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.weight = 7.0f;
            if (chat_userID.equals(currentUserID)) {
                //displayTextMessages.setTextColor(Color.BLUE);
                textView.setTextColor(Color.BLUE);
                lp2.gravity = Gravity.RIGHT;
                textView.setBackgroundResource(R.drawable.sender_messages_layout);
            } else {
                //displayTextMessages.setTextColor(Color.BLACK);
                textView.setTextColor(Color.BLACK);
                lp2.gravity = Gravity.LEFT;
                textView.setBackgroundResource(R.drawable.receiver_messages_layout);
            }
            //displayTextMessages.setLayoutParams(lp2);
            textView.setBackgroundResource(R.drawable.back_edit_text);
            textView.setLayoutParams(lp2);
            textView.setText(chatName + " :\n" + chatMessage + "\n\n");
            layout.addView(textView);
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

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
            //dispatchTakePictureIntent();
            openCamera();
        }
        if (item.getItemId() == R.id.doc_file_menu) {
            openFileGetDoc();
        }
        if (item.getItemId() == R.id.audio_menu) {

        }
        if (item.getItemId() == R.id.contact_menu) {

        }
        if (item.getItemId() == R.id.location_menu) {

        }

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

        if (ContextCompat.checkSelfPermission(GroupChatActivity.this,
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
                logMessage("permission granted-----------");

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
        CropImage.activity().setAspectRatio(1, 1)
                .start(GroupChatActivity.this);

    }

    /*  private void checkForCameraPermission() {

          if (ContextCompat.checkSelfPermission(this,
                  Manifest.permission.CAMERA)
                  != PackageManager.PERMISSION_GRANTED) {

          } else {

              ActivityCompat.requestPermissions(this,
                      new String[]{Manifest.permission.CAMERA},
                      MY_PERMISSIONS_REQUEST_CAMERA);
          }
          openCamera();
      }


     */
    void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logMessage("--------------"+requestCode+"==="+CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE+"    --- "+resultCode+"===="+RESULT_OK );
        if (resultCode == RESULT_OK  ) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    imageUriGalary = result.getUri();
                    logMessage("image ---------"+imageUriGalary.toString());
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera = Uri.parse(path);
                    logMessage("camera ---------"+imageUriCamera.toString());
                    break;
                case PICK_PDF_CODE:
                        docUri=data.getData();
                        logMessage("doc ---------"+docUri.toString());
                    break;
            }
        } else {
            showToast("something gone wrong");
        }
    }

    private void logMessage(String s) {
        Log.d("Testing Developer mode ",s);

    }
    private void showToast(String s) {
        Toast.makeText(GroupChatActivity.this, s, Toast.LENGTH_SHORT).show();
    }
}
