package com.pakhi.clicksdigital.GroupChat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Activities.UserRequestActivity;
import com.pakhi.clicksdigital.HelperClasses.NotificationCountDatabase;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.JoinGroup.JoinGroupActivity;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Settings.SettingActivity;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Notifications.Notification;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.ShareApp;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.TypeAndSeparateURL;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupChatActivity extends AppCompatActivity {

    private static boolean IS_TYPE_TOPIC = false;
    boolean notify = false;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    final static int PICK_PDF_CODE = 2342;
    static final int REQUESTCODE = 12;
    static int REQUEST_CODE = 1;
    private final List<Message> messagesList = new ArrayList<>();
    ImageView attach_file_btn, image_profile, back_btn, raise_topic;
    Uri imageUriGalary, imageUriCamera;
    User user;
    PermissionsHandling permissions;
    SharedPreference pref;
    String topic_str;
    FirebaseDatabaseInstance rootRef;
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private DatabaseReference UsersRef, GroupIdRef, groupChatRefForCurrentGroup;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime, currentGroupId;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private LinearLayoutManager linearLayoutManager;
    private ProgressDialog progressDialog;
    private TextView group_name;
    int checkIfTypeIsURL = 0;
    String separateURL = "";
    TextView withImage;
    String popUpMenuFlag;
    String name = "";
    boolean replyingToMessage = false;
    String typeOfSelectedMessage = "";
    String selectedMessageId = "";
    Message msg;
    int currentPage = 1;
    Button muteButton, unmuteButton;
    ImageView muteMenu;

    //replyOnClickHeader
    LinearLayout onLongClickOnMessage, replyHeader;
    ImageView replyCross;
    TextView replyMessageSenderName;

    //Reply On Text
    TextView replyOnText;

    //ReplyOnImage
    LinearLayout replyOnImageLayout;
    ImageView replyOnImageImage;
    TextView replyOnImageText;

    //Reply on PDf
    LinearLayout replyOnPDFLayout;

    //ReplyOnURL
    LinearLayout replyOnURLLayout;
    ImageView replyOnURLImage;
    TextView replyOnURLTitle, replyOnURLText;

    String someTextFromRaiseTopic = "";

    SwipeRefreshLayout freshLayout;
    private int itemPos = 0;

    String lastKey = "";
    private String prevKey = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
            NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(getApplicationContext());
            String mutOrNot = notificationCountDatabase.getSqliteUser_data(Const.mute, currentGroupId);

            if(mutOrNot.equals("true")){
                getMenuInflater().inflate(R.menu.options_menu_group, menu);
                //menu.getItem(0).setVisible(false);
                menu.findItem(R.id.mute).setVisible(false);
            }else {
                getMenuInflater().inflate(R.menu.options_menu_group, menu);
                //menu.getItem(1).setVisible(false);
                menu.findItem(R.id.un_mute).setVisible(false);
            }



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);




        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat2);


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));





        //currentGroupName=getIntent().getExtras().get("groupName").toString();
        //currentGroupId = getIntent().getExtras().get(ConstFirebase.groupId).toString();
        currentGroupId = getIntent().getStringExtra(Const.group_id);

        pref = SharedPreference.getInstance();
        currentUserID = pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef = FirebaseDatabaseInstance.getInstance();
        UsersRef = rootRef.getUserRef();
        GroupIdRef = rootRef.getGroupRef().child(currentGroupId);
        groupChatRefForCurrentGroup = rootRef.getGroupChatRef().child(currentGroupId);
        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name = snapshot.child(ConstFirebase.USER_NAME).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");


        getUserFromDb();
        GetUserInfo();
        /*replyCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLongClickOnMessage.setVisibility(View.GONE);
            }
        });*/

        GroupIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentGroupName = snapshot.child(ConstFirebase.GROUP_NAME).getValue().toString();
                    group_name.setText(currentGroupName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        InitializeFields();
        //muteMenu.setVisibility(View.GONE);
        muteMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpMenuForMute();
            }
        });

        //currentGroupName = "Name Here";
        // group_name.setText(currentGroupName);

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
                Glide.with(getApplicationContext())

                        .load(uri)
                        .transform(new CenterCrop(), new RoundedCorners(50)).into(image_profile);

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
                notify = true;
                onLongClickOnMessage.setVisibility(View.GONE);

                String message = userMessageInput.getText().toString();
                Log.i("Message check -----", message);

                if (TextUtils.isEmpty(message)) {
                    showToast("first write your message...");
                    selectedMessageId = "";
                    typeOfSelectedMessage = "";
                } else {


                    TypeAndSeparateURL typeAndSeparateURL = checkTypeAndSeparateURL(message);

                    userMessageInput.setText("");
                    if (typeAndSeparateURL.typeURLOrNot == 1) {
                        //Toast.makeText(getApplicationContext(), "URL type", Toast.LENGTH_LONG).show();
                        SaveMessageInfoToDatabase("url", message, typeAndSeparateURL.separateURL);
                    } else {
                        //Toast.makeText(getApplicationContext(), "Text type", Toast.LENGTH_LONG).show();
                        SaveMessageInfoToDatabase("text", message, "");
                    }
                }
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


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootRef.getUserRef().child(currentUserID).child(ConstFirebase.userIsIn).setValue("noWhere");
                finish();
            }
        });

        raise_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTopicRaiseFagmentForResult();
            }
        });

    }

    private void popUpMenuForMute() {
        PopupMenu popup = new PopupMenu(GroupChatActivity.this, muteMenu);

        NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(getApplicationContext());

        String muteOrUnmute = notificationCountDatabase.getSqliteUser_data(Const.mute, currentGroupId);

        if(muteOrUnmute.equals("true")){

            popup.getMenuInflater().inflate(R.menu.options_menu_group_2, popup.getMenu());
            //popup.
        }else {
            popup.getMenuInflater().inflate(R.menu.options_menu_group, popup.getMenu());
        }



        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return menuItemClicked(item);
            }
        });
        popup.show();
    }


    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            typeOfSelectedMessage = intent.getStringExtra(Const.typeOfMessageSelected);
            selectedMessageId = intent.getStringExtra(Const.selectedMessageId);
            replyingToMessage = intent.getBooleanExtra(Const.replyingToMessage, false);
            msg = (Message) intent.getSerializableExtra(Const.message);

            replyMessageSenderName.setVisibility(View.GONE);
            replyOnImageLayout.setVisibility(View.GONE);
            replyOnText.setVisibility(View.GONE);
            replyOnPDFLayout.setVisibility(View.GONE);
            replyOnURLLayout.setVisibility(View.GONE);
            final String[] nameOFSender = new String[1];
            rootRef.getUserRef().child(msg.getFrom()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    nameOFSender[0] = snapshot.child(ConstFirebase.userName).getValue().toString();
                    createReplyLayout(msg, nameOFSender);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            onLongClickOnMessage.setVisibility(View.VISIBLE);

            if (msg.getType().equals("text")) {
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                replyOnText.setText(msg.getMessage());
                replyOnText.setVisibility(View.VISIBLE);
            } else if (msg.getType().equals("image")) {
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                Picasso.get().load(msg.getMessage()).into(replyOnImageImage);
                replyOnImageLayout.setVisibility(View.VISIBLE);
                replyOnImageImage.setVisibility(View.VISIBLE);
                if (!msg.getExtra().equals("")) {
                    replyOnImageText.setText(msg.getExtra());
                    replyOnImageText.setVisibility(View.VISIBLE);
                }
            } else if (msg.getType().equals("pdf")) {
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                replyOnPDFLayout.setVisibility(View.VISIBLE);
            } else if (msg.getType().equals("url")) {
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                replyOnURLText.setText(msg.getMessage());
                replyOnURLLayout.setVisibility(View.VISIBLE);
            } else if (msg.getType().equals("topic")) {

            }

            replyCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLongClickOnMessage.setVisibility(View.GONE);
                    msg = null;
                    replyingToMessage = false;
                    //replyOnText.setText(null);
                    replyMessageSenderName.setVisibility(View.GONE);
                    replyOnImageLayout.setVisibility(View.GONE);
                    replyOnText.setVisibility(View.GONE);
                    replyOnPDFLayout.setVisibility(View.GONE);
                    replyOnURLLayout.setVisibility(View.GONE);
                }
            });
        }
    };

    private void createReplyLayout(Message message, String[] msg) {
        if (message.getFrom().equals(currentUserID)) {
            replyMessageSenderName.setText("You");
        } else {
            replyMessageSenderName.setText(msg[0]);
        }
    }

    private void startTopicRaiseFagmentForResult() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        /*builder.setTitle("Enter new Topic");*/

        LayoutInflater inflater = ((Activity) this).getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_topic_raise, null);
        final EditText topic = v.findViewById(R.id.topic);
        withImage = v.findViewById(R.id.frag_button);
        //final String maaa=topic.getText().toString();
        //  v.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT));
        builder.setView(v);

        withImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForPremission();
                popupMenuSettigns1();
                someTextFromRaiseTopic = topic.getText().toString();
            }
        });
        // Set up the buttons
        builder.setPositiveButton("Publish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String maaa = topic.getText().toString();

                TypeAndSeparateURL typeAndSeparateURL = checkTypeAndSeparateURL(maaa);

                topic_str = topic.getText().toString();
                SaveMessageInfoToDatabase("topic", topic_str, typeAndSeparateURL.separateURL);
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
        groupMembersIntent.putExtra(Const.groupName, currentGroupName);
        groupMembersIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(groupMembersIntent);
    }
    private void getUserFromDb() {
        UserDatabase db = new UserDatabase(this);
        user = db.getSqliteUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //setting user is in this groupActivity
        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.userIsIn).setValue(currentGroupId);

        messagesList.clear();
        messageAdapter.notifyDataSetChanged();

        Query msgQuery = groupChatRefForCurrentGroup.limitToLast(currentPage * 10);

        msgQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {

                    Message messages = dataSnapshot.getValue(Message.class);
                    itemPos++;

                    if (itemPos == 1) {
                        String msgKey = dataSnapshot.getKey();
                        lastKey = msgKey;
                        prevKey = msgKey;
                    }

                    messagesList.add(messages); ////-----------

                    /*Collections.sort(messagesList, new Comparator<Message>() {
                        public int compare(Message o1, Message o2) {
                            return o1.getTimestamp().compareTo(o2.getTimestamp());
                        }
                    });*/

                    messageAdapter.notifyDataSetChanged();

                    userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount() - 1);
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
        freshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemPos = 0;
                //messagesList.clear();
                loadMoreMessages();
            }
        });

    }

    private void loadMoreMessages() {
        Query msgQuery = groupChatRefForCurrentGroup.orderByKey().endAt(lastKey).limitToLast(10);

        msgQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {

                    Message messages = dataSnapshot.getValue(Message.class);
                    String msgKey = dataSnapshot.getKey();

                    //itemPos++;
                    //messagesList.add(itemPos++,messages); ////-----------

                    if (!prevKey.equals(msgKey)) {
                        messagesList.add(itemPos++, messages); ////-----------
                    } else {
                        prevKey = lastKey;
                    }

                    if (itemPos == 1) {
                        lastKey = msgKey;
                    }

                    messageAdapter.notifyDataSetChanged();

                    //userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount()-1);

                    freshLayout.setRefreshing(false);
                    linearLayoutManager.scrollToPositionWithOffset(10, 0);
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


    @Override
    protected void onPause() {
        super.onPause();
        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.userIsIn).setValue("noWhere");
    }

    private void InitializeFields() {
        muteButton = findViewById(R.id.mute);
        unmuteButton = findViewById(R.id.unmute);
        muteMenu = findViewById(R.id.menu_bar_mute);
        //Reply header
        onLongClickOnMessage = findViewById(R.id.reply_layout_long_click);
        replyHeader = findViewById(R.id.reply_header_and_cross);
        replyCross = findViewById(R.id.reply_cross);
        replyMessageSenderName = findViewById(R.id.reply_message_sender_name);

        //Reply On Text
        replyOnText = findViewById(R.id.reply_text_long_click);

        //Reply On Image
        replyOnImageLayout = findViewById(R.id.reply_image_layout_long_click);
        replyOnImageImage = findViewById(R.id.reply_image_long_click);
        replyOnImageText = findViewById(R.id.reply_image_text_long_click);

        //ReplyOnPDF
        replyOnPDFLayout = findViewById(R.id.reply_pdf_layout_long_click);

        //ReplyOnURL
        replyOnURLLayout = findViewById(R.id.reply_url_layout_long_click);
        replyOnURLImage = findViewById(R.id.reply_url_image_long_click);
        replyOnURLTitle = findViewById(R.id.reply_url_title_long_click);
        replyOnURLText = findViewById(R.id.reply_url_text_long_click);


        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        //  getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);

        attach_file_btn = findViewById(R.id.attach_file_btn);
        image_profile = findViewById(R.id.image_profile);
        group_name = findViewById(R.id.group_name);
        back_btn = findViewById(R.id.back_btn);
        raise_topic = findViewById(R.id.raise_topic);

        messageAdapter = new MessageAdapter(messagesList, "GroupChat", getApplicationContext());
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);

        freshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_message);
        userMessagesList.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(this);

        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messageAdapter);


    }

    private void GetUserInfo() {
        currentUserName = user.getUser_name();
    }

    private void SaveMessageInfoToDatabase(String messageType, final String message, String separateURL) {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForDate.getTime());

        HashMap<String, Object> groupMessageKey = new HashMap<>();

        groupChatRefForCurrentGroup.updateChildren(groupMessageKey);
        String messagekEY = groupChatRefForCurrentGroup.push().getKey();
        Long timestamp = calForDate.getTimeInMillis() / 1000L;
        Log.i("replyingToMessage", String.valueOf(replyingToMessage));
        Log.i("replyingToMessage", String.valueOf(selectedMessageId));
        Log.i("replyingToMessage", String.valueOf(typeOfSelectedMessage));
        if (replyingToMessage) {
            Message message1 = new Message(currentUserID, message,
                    messageType, currentGroupId, messagekEY, currentTime, currentDate, timestamp, false,separateURL, selectedMessageId, typeOfSelectedMessage);
            groupChatRefForCurrentGroup.child(messagekEY).setValue(message1);


        } else {

            Message message1 = new Message(currentUserID, message,
                    messageType, currentGroupId, messagekEY, currentTime, currentDate, timestamp, false,separateURL, "", "");
            groupChatRefForCurrentGroup.child(messagekEY).setValue(message1);
        }

        rootRef.getGroupRef().child(currentGroupId).child(ConstFirebase.timestamp).setValue(timestamp);
        MyGroupsAdapter grpAdapter = new MyGroupsAdapter();
        grpAdapter.notifyDataSetChanged();


        if (messageType == "topic") {
            Log.i("messageKey----", messagekEY);
            IS_TYPE_TOPIC = true;
            Log.i("IS_TOPIC_TYPE----", String.valueOf(IS_TYPE_TOPIC));

            notificationBhejoTopic(message, messagekEY);
            Log.i("after----", "after");
        } else if (messageType == "pdf") {
            String msg = "has shared a file";
            notificationBhejoPDF(msg, "");

        } else if (messageType == "text") {
            notificationBhejo(message, "");

        }

        if (messageType == "topic") {
            saveSeparateTopicNode(messagekEY);
        }
        progressDialog.dismiss();
        replyingToMessage = false;
        typeOfSelectedMessage = "";
        selectedMessageId = "";

    }



    private void setNumberOfMessages(String numberOfMessages) {
        int number = Integer.parseInt(numberOfMessages);
        number++;
        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.groups).child(currentGroupId).child(ConstFirebase.noOfMessages).setValue(number);
    }
    private void notificationBhejoPDF(final String msg, String s) {
        rootRef.getGroupChatRef().child(currentGroupId).child(ConstFirebase.users).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (!snap.getKey().equals(currentUserID) && snap.getValue().toString().equals("")) {
                        Notification.sendPersonalNotifiaction(currentGroupId, snap.getKey(), name + ": has sent a file", /*title*/ currentGroupName, "grpChat", "");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void notificationBhejoTopic(final String message, final String messageKey) {
        rootRef.getGroupChatRef().child(currentGroupId).child(ConstFirebase.users).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (!snap.getKey().equals(currentUserID) && snap.getValue().toString().equals("")) {
                        Notification.sendPersonalNotifiaction(currentGroupId, snap.getKey(), name + ": has raised a Dialog Topic " + message, /*title*/ currentGroupName, "topic", messageKey);
                    }
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void saveSeparateTopicNode(String messagekEY) {
        DatabaseReference topicRef = rootRef.getTopicRef();
        topicRef.child(messagekEY).setValue(currentGroupId);
    }

    private void popupMenuSettigns() {
        popUpMenuFlag=String.valueOf(0);
        PopupMenu popup = new PopupMenu(GroupChatActivity.this, attach_file_btn);
        popup.getMenuInflater().inflate(R.menu.attach_file_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return menuItemClicked(item);
            }
        });
        popup.show();
    }


    private void popupMenuSettigns1() {
        //j=1;
        popUpMenuFlag=String.valueOf(1);
        PopupMenu popup = new PopupMenu(GroupChatActivity.this, withImage );
        popup.getMenuInflater().inflate(R.menu.topic_menu, popup.getMenu());
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
        if (item.getItemId() == R.id.mute) {
            GroupIdRef.child("Users").child(currentUserID).setValue("mute");
            NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(getApplicationContext());
            HashMap<String, String> hmap = new HashMap<>();
            hmap.put(Const.grpOrUserID, currentGroupId);
            //Log.i("before adding msg --", notificationCountDatabase.getSqliteUser_data(Const.number,user));
            hmap.put(Const.number, String.valueOf(Integer.parseInt(notificationCountDatabase.getSqliteUser_data(Const.number, currentGroupId))));
            hmap.put(Const.mute, "true");
            notificationCountDatabase.updateDataNotification(hmap);
        }

        if (item.getItemId() == R.id.un_mute) {
            GroupIdRef.child("Users").child(currentUserID).setValue("");
            NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(getApplicationContext());
            HashMap<String, String> hmap = new HashMap<>();
            hmap.put(Const.grpOrUserID, currentGroupId);
            //Log.i("before adding msg --", notificationCountDatabase.getSqliteUser_data(Const.number,user));
            hmap.put(Const.number, String.valueOf(Integer.parseInt(notificationCountDatabase.getSqliteUser_data(Const.number, currentGroupId))));
            hmap.put(Const.mute, "false");
            notificationCountDatabase.updateDataNotification(hmap);
        }

        if (item.getItemId() == R.id.exit) {
            GroupIdRef.child("Users").child(currentUserID).removeValue();
            rootRef.getUserRef().child(currentUserID).child(ConstFirebase.groups).child(currentGroupId).removeValue();
            finish();
        }
        return true;
    }

    private void openFileGetDoc() {
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
                    uploadImage(imageUriGalary, popUpMenuFlag);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera = Uri.parse(path);
                    uploadImage(imageUriCamera,popUpMenuFlag);
                    break;
                case PICK_PDF_CODE:
                    // docUri = data.getData();
                    uploadFile(data.getData());
                    break;
            }
        } else {
            showToast("something went wrong");
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
                                        SaveMessageInfoToDatabase("pdf", String.valueOf(uri), "");
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

    private void uploadImage(final Uri imageUri, final String flag) {
        StorageReference sReference = FirebaseStorageInstance.getInstance().getRootRef().child(ConstFirebase.groupPhotos).child(currentGroupId).child(ConstFirebase.groupPhotos);
        final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));


        imgPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        progressDialog.dismiss();
                        Intent intent = new Intent(GroupChatActivity.this, ImageText.class);
                        intent.putExtra(Const.IMAGE_URL, uri.toString());
                        intent.putExtra(Const.group_id, currentGroupId);
                        intent.putExtra(Const.checkPersonalOrGroup, "grp");
                        intent.putExtra(Const.messageSenderName, "abc");
                        intent.putExtra(Const.flagOfGroup, flag);
                        intent.putExtra(Const.typeOfMessageSelected, typeOfSelectedMessage);
                        intent.putExtra(Const.selectedMessageId, selectedMessageId);
                        intent.putExtra(Const.someTextFromRaisedTopic, someTextFromRaiseTopic);
                        startActivity(intent);
                        finish();
                        //SaveMessageInfoToDatabase("image", uri.toString());
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

    public void notificationBhejo(final String message, final String messageKey) {
        rootRef.getGroupRef().child(currentGroupId).child(ConstFirebase.users).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Log.i("userID", snap.getValue().toString());
                    if (notify && (!snap.getKey().equals(currentUserID)) && (snap.getValue().toString().equals("")) ) {
                        Log.i("TOPIC TYPE", String.valueOf(IS_TYPE_TOPIC));


                        //checkPresence(snap.getKey());
                        Notification.sendPersonalNotifiaction(currentGroupId, snap.getKey(), name + ": " + message, /*title*/ currentGroupName, "grpChat", "");
                    }
                }
                notify = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.userIsIn).setValue("noWhere");
    }
     TypeAndSeparateURL checkTypeAndSeparateURL(String message){


         String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
         String separateURL="";
         Pattern p;
         Matcher m = null;
         String[] words = message.split(" ");
         checkIfTypeIsURL = 0;
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
}

