package com.pakhi.clicksdigital.PersonalChat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.GroupChat.ImageText;
import com.pakhi.clicksdigital.GroupChat.MessageAdapter;
import com.pakhi.clicksdigital.HelperClasses.NotificationCountDatabase;
import com.pakhi.clicksdigital.Utils.TypeAndSeparateURL;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.FirebaseStorageInstance;
import com.pakhi.clicksdigital.Notifications.Notification;
import com.pakhi.clicksdigital.Utils.PermissionsHandling;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.pakhi.clicksdigital.Utils.TypeAndSeparateURL;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    final static int PICK_PDF_CODE = 2342;
    static final int REQUESTCODE = 12;
    static int REQUEST_CODE = 1;
    private final List<Message> messagesList = new ArrayList<>();
    Uri imageUriGalary, imageUriCamera;
    User user;
    ImageView attach_file_btn, back_btn;
    PermissionsHandling permissions;
    ValueEventListener seenListener;
    DatabaseReference reference;
    int checkIfTypeURL=0;
    boolean notify = false;
    SharedPreference pref;
    FirebaseDatabaseInstance rootRef;
    private ScrollView messageScroll;
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID, messageSenderName;
    private TextView userName, userLastSeen;
    private ImageView userImage;

    private ImageButton SendMessageButton;
    private EditText MessageInputText;
    private LinearLayoutManager linearLayoutManager;
    ;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String saveCurrentTime, saveCurrentDate;
    private ProgressDialog progressDialog;

    boolean replyingToMessage=false;
    String typeOfSelectedMessage="";
    String selectedMessageId="";
    Message msg;

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
    RelativeLayout chatMessageLayout;
    LinearLayout blockLayout;
    TextView blockText;

    SwipeRefreshLayout freshLayout;
    private int itemPos=0;

    String lastKey="";
    private String prevKey="";
    int currentPage=1;

    Long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabaseInstance.getInstance();
        pref = SharedPreference.getInstance();
        messageSenderID = pref.getData(SharedPreference.currentUserId, getApplicationContext());



        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        messageReceiverID = getIntent().getExtras().get(Const.visitUser).toString();



        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));
        IntializeControllers();


        blockLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, VisitProfileActivity.class);
                intent.putExtra(Const.visitUser, messageReceiverID);
                startActivity(intent);
            }
        });
        rootRef.getBlockRef().child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(messageReceiverID).exists()){
                        chatMessageLayout.setVisibility(View.GONE);
                        blockLayout.setVisibility(View.VISIBLE);
                        blockText.setText("You can't communicate with this person anymore, click here to unblock");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rootRef.getBlockRef().child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(messageSenderID).exists()){
                        chatMessageLayout.setVisibility(View.GONE);
                        blockLayout.setVisibility(View.VISIBLE);
                        blockText.setText("You cant't communicate with this person anymore");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rootRef.getApprovedUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child(messageSenderID).exists()){
                    chatMessageLayout.setVisibility(View.GONE);
                    blockLayout.setVisibility(View.VISIBLE);
                    blockText.setText("You can't communicate with this person as your profile is not approved yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference databaseReference = rootRef.getUserRef().child(messageReceiverID);
        databaseReference.child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                    messageReceiverName = user.getUser_name()+" "+user.getLast_name();
                    userName.setText(messageReceiverName);
                    Glide.with(getApplicationContext())
                            .load(user.getImage_url())
                            .transform(new CenterCrop(), new RoundedCorners(50))
                            //.resize(120, 120)
                            .into(userImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference = rootRef.getUserRef().child(messageSenderID);
        databaseReference.child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                messageSenderName = dataSnapshot.child(ConstFirebase.USER_NAME).getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootRef.getUserRef().child(messageSenderID).child(ConstFirebase.userIsIn).setValue("NoWhere");
                finish();
            }
        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rootRef.getMessagesListRef().child(messageSenderID).child(messageReceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            NotificationCountDatabase notificationCountDatabase = new NotificationCountDatabase(getApplicationContext());
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put(Const.grpOrUserID, messageReceiverID);
                            hashMap.put(Const.number, "0");
                            hashMap.put(Const.mute, "false");
                            notificationCountDatabase.insertData(hashMap);
                            Toast.makeText(getApplicationContext(), "New Contact", Toast.LENGTH_LONG).show();

                            notify = true;
                            onLongClickOnMessage.setVisibility(View.GONE);

                            String messageText = MessageInputText.getText().toString();

                            if (TextUtils.isEmpty(messageText)) {
                                showToast("first write your message...");
                                selectedMessageId="";
                                typeOfSelectedMessage="";
                            } else {

                                TypeAndSeparateURL typeAndSeparateURL=checkTypeAndSeparateURL(messageText);
                                MessageInputText.setText("");
                                if(typeAndSeparateURL.typeURLOrNot==1){
                                    //Toast.makeText(getApplicationContext(), "URL type", Toast.LENGTH_LONG).show();
                                    SendMessage("url", messageText, typeAndSeparateURL.separateURL);
                                }else{
                                    //Toast.makeText(getApplicationContext(), "Text type", Toast.LENGTH_LONG).show();
                                    SendMessage("text", messageText,"");
                                }

                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Old Contact", Toast.LENGTH_LONG).show();

                            notify = true;
                            onLongClickOnMessage.setVisibility(View.GONE);

                            String messageText = MessageInputText.getText().toString();

                            if (TextUtils.isEmpty(messageText)) {
                                showToast("first write your message...");
                                selectedMessageId="";
                                typeOfSelectedMessage="";
                            } else {

                                TypeAndSeparateURL typeAndSeparateURL=checkTypeAndSeparateURL(messageText);
                                MessageInputText.setText("");
                                if(typeAndSeparateURL.typeURLOrNot==1){
                                    //Toast.makeText(getApplicationContext(), "URL type", Toast.LENGTH_LONG).show();
                                    SendMessage("url", messageText, typeAndSeparateURL.separateURL);
                                }else{
                                    //Toast.makeText(getApplicationContext(), "Text type", Toast.LENGTH_LONG).show();
                                    SendMessage("text", messageText,"");
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });

               /* if (TextUtils.isEmpty(messageText)) {
                    showToast("first write your message...");
                } else {
                    MessageInputText.setText("");
                    SendMessage("text", messageText);
                }*/

            }
        });

        DisplayLastSeen();
        permissions = new PermissionsHandling(this);
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
                    nameOFSender[0] =snapshot.child(ConstFirebase.userName).getValue().toString();
                    createReplyLayout(msg,nameOFSender);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            onLongClickOnMessage.setVisibility(View.VISIBLE);

            if(msg.getType().equals("text")){
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                replyOnText.setText(msg.getMessage());
                replyOnText.setVisibility(View.VISIBLE);
            }else if(msg.getType().equals("image")){
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                Picasso.get().load(msg.getMessage()).into(replyOnImageImage);
                replyOnImageLayout.setVisibility(View.VISIBLE);
                replyOnImageImage.setVisibility(View.VISIBLE);
                if(!msg.getExtra().equals("")){
                    replyOnImageText.setText(msg.getExtra());
                    replyOnImageText.setVisibility(View.VISIBLE);
                }
            }else if(msg.getType().equals("pdf")){
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                replyOnPDFLayout.setVisibility(View.VISIBLE);
            }else if(msg.getType().equals("url")){
                replyMessageSenderName.setText(nameOFSender[0]);
                replyMessageSenderName.setVisibility(View.VISIBLE);
                replyOnURLText.setText(msg.getMessage());
                replyOnURLLayout.setVisibility(View.VISIBLE);
            }else if(msg.getType().equals("topic")){

            }

            replyCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLongClickOnMessage.setVisibility(View.GONE);
                    msg=null;
                    //replyOnText.setText(null);
                    replyMessageSenderName.setVisibility(View.GONE);
                    replyOnImageLayout.setVisibility(View.GONE);
                    replyOnText.setVisibility(View.GONE);
                    replyOnPDFLayout.setVisibility(View.GONE);
                    replyOnURLLayout.setVisibility(View.GONE);
                    replyingToMessage=false;
                }
            });


        }
    };

    private void createReplyLayout(Message msg, String[] nameOFSender) {
        if(msg.getFrom().equals(messageSenderID)){
            replyMessageSenderName.setText("You");
        }else{
            replyMessageSenderName.setText(nameOFSender[0]);
        }

    }

    private void IntializeControllers() {

        //Reply header
        onLongClickOnMessage = findViewById(R.id.reply_layout_long_click_per);
        replyHeader = findViewById(R.id.reply_header_and_cross_per);
        replyCross = findViewById(R.id.reply_cross_per);
        replyMessageSenderName = findViewById(R.id.reply_message_sender_name_per);

        //Reply On Text
        replyOnText = findViewById(R.id.reply_text_long_click_per);

        //Reply On Image
        replyOnImageLayout = findViewById(R.id.reply_image_layout_long_click_per);
        replyOnImageImage = findViewById(R.id.reply_image_long_click_per);
        replyOnImageText = findViewById(R.id.reply_image_text_long_click_per);

        //ReplyOnPDF
        replyOnPDFLayout = findViewById(R.id.reply_pdf_layout_long_click_per);

        //ReplyOnURL
        replyOnURLLayout = findViewById(R.id.reply_url_layout_long_click_per);
        replyOnURLImage = findViewById(R.id.reply_url_image_long_click_per);
        replyOnURLTitle = findViewById(R.id.reply_url_title_long_click_per);
        replyOnURLText = findViewById(R.id.reply_url_text_long_click_per);

      /*  ChatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);*/
        //messageScroll = findViewById(R.id.scroll_view);
        back_btn = findViewById(R.id.back_btn);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage =  findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);
        chatMessageLayout = findViewById(R.id.chat_linear_layout);
        blockLayout = findViewById(R.id.block_layout);
        blockText = findViewById(R.id.block_text);

        freshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_message);

        messageAdapter = new MessageAdapter(messagesList, ConstFirebase.personalChat, getApplicationContext());
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        userMessagesList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setStackFromEnd(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        //userMessagesList.setItemAnimator(null);

        userMessagesList.setAdapter(messageAdapter);
        attach_file_btn = findViewById(R.id.attach_file_btn);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
        timestamp = calendar.getTimeInMillis()/1000L;

    }

    private void seenMessage() {
        reference = FirebaseDatabase.getInstance().getReference("Messages");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message chat = snapshot.getValue(Message.class);
                    if (chat.getTo().equals(messageSenderID) && chat.getFrom().equals(messageReceiverID)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
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
                        if (dataSnapshot.child(ConstFirebase.userState).hasChild(ConstFirebase.state)) {
                            String state = dataSnapshot.child(ConstFirebase.userState).child(ConstFirebase.state).getValue().toString();
                            String date = dataSnapshot.child(ConstFirebase.userState).child(ConstFirebase.date).getValue().toString();
                            String time = dataSnapshot.child(ConstFirebase.userState).child(ConstFirebase.time).getValue().toString();

                            if (state.equals(ConstFirebase.onlineStatus)) {
                                userLastSeen.setText(ConstFirebase.onlineStatus);
                            } else {
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
    public void onBackPressed() {
        super.onBackPressed();
        rootRef.getUserRef().child(messageSenderID).child(ConstFirebase.userIsIn).setValue("NoWhere");
    }

    @Override
    protected void onStart() {
        super.onStart();
        //messageScroll.fullScroll(ScrollView.FOCUS_DOWN);
        rootRef.getUserRef().child(messageSenderID).child(ConstFirebase.userIsIn).setValue(messageReceiverID);

        messagesList.clear();
        messageAdapter.notifyDataSetChanged();

        Query msgQuery = rootRef.getMessagesListRef().child(messageSenderID).child(messageReceiverID).limitToLast(currentPage*10);
        msgQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        itemPos++;

                        if(itemPos==1){
                            String msgKey = dataSnapshot.getValue().toString();
                            lastKey=msgKey;
                            prevKey=msgKey;
                        }
                        String messageId = dataSnapshot.getValue().toString();
                        rootRef.getMessagesRef().child(messageId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Message messages = snapshot.getValue(Message.class);
                                    //messagesList.clear();
                                    messagesList.add(messages);
                                }
                                messageAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        //Message messages = dataSnapshot.getValue(Message.class);
                        //messagesList.add(messages);

                        //  messageAdapter.notifyDataSetChanged();
                        if(!(userMessagesList.getAdapter().getItemCount()==0)){
                            userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                        }

                        //messageScroll.fullScroll(NestedScrollView.FOCUS_DOWN);
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
        freshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemPos=0;
                //messagesList.clear();
                loadMoreMessages();
            }
        });
    }
    private void loadMoreMessages() {
        Query msgQuery = rootRef.getMessagesListRef().child(messageSenderID).child(messageReceiverID).orderByKey().endAt(lastKey).limitToLast(10);
        msgQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String msgKey = dataSnapshot.getValue().toString();

                //itemPos++;
                //messagesList.add(itemPos++,messages); ////-----------


                String messageId = dataSnapshot.getValue().toString();
                rootRef.getMessagesRef().child(messageId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Message messages = snapshot.getValue(Message.class);
                            //messagesList.clear();
                            if(!prevKey.equals(msgKey)){
                                messagesList.add(itemPos++,messages); ////-----------
                            }else {
                                prevKey=lastKey;
                            }

                            if(itemPos==1){
                                lastKey=msgKey;
                            }
                        }
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Message messages = dataSnapshot.getValue(Message.class);
                //messagesList.add(messages);

                //  messageAdapter.notifyDataSetChanged();

                freshLayout.setRefreshing(false);
                linearLayoutManager.scrollToPositionWithOffset(10,0);

                //userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount()-1);
                //messageScroll.fullScroll(NestedScrollView.FOCUS_DOWN);
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

    private void SendMessage(String messageType, String message, String separteURL) {

        //messageScroll.fullScroll(ScrollView.FOCUS_DOWN);

        String messageSenderRef = "MessagesList/" + messageSenderID + "/" + messageReceiverID;
        String messageReceiverRef = "MessagesList/" + messageReceiverID + "/" + messageSenderID;

        DatabaseReference userMessageKeyRef = rootRef.getMessagesRef().push();
        // .child(messageSenderID).child(messageReceiverID).push();

        String messagePushID = userMessageKeyRef.getKey();

        /* Map messageTextBody = new HashMap();*/

        Message message1 = new Message(messageSenderID, message,
                messageType, messageReceiverID, messagePushID, saveCurrentTime, saveCurrentDate,timestamp,false, separteURL,
                selectedMessageId, typeOfSelectedMessage);

        /* messageTextBody.put("from", messageSenderID);
        messageTextBody.put("to", messageReceiverID);
        messageTextBody.put("messageID", messagePushID);
        messageTextBody.put("time", saveCurrentTime);
        messageTextBody.put("date", saveCurrentDate);
        messageTextBody.put("message", message);
        messageTextBody.put("type", messageType);*/

        Map messageBodyDetails = new HashMap();
        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messagePushID);
        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messagePushID);

        rootRef.getMessagesRef().child(messagePushID).setValue(message1);
        replyingToMessage=false;
        typeOfSelectedMessage="";
        selectedMessageId="";


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
           // Notification.sendPersonalNotifiaction(messageSenderID, messageReceiverID, "username + \": \" + message", "New Message");
            Notification.sendPersonalNotifiaction(messageSenderID, messageReceiverID,message, messageSenderName, "chat","");
            checkPresence(messageReceiverID);
        }
        notify = false;
    }

    private void checkPresence(String messageReceiverID) {

    }


    private void popupMenuSettigns() {
        PopupMenu popup = new PopupMenu(ChatActivity.this, attach_file_btn);
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
            switch (requestCode) {
               /* case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    imageUriGalary = result.getUri();
                    uploadImage(imageUriGalary);
                    break;

                */
                case REQUESTCODE:
                    imageUriGalary = data.getData();
                    progressDialog.show();
                    Log.d("ChatActivity", "-----------on rersult---gallery img---------" + data.getData().toString());
                    uploadImage(imageUriGalary);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera = Uri.parse(path);
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
        StorageReference storageRootReference = FirebaseStorageInstance.getInstance().getRootRef();
        StorageReference sRef = storageRootReference.child(ConstFirebase.USER_MEDIA_PATH).child(messageSenderID).child(ConstFirebase.FILES_PATH).child("Sent_Pdf").child(messageReceiverID).child(System.currentTimeMillis() + "");
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
                                        SendMessage("pdf", String.valueOf(uri),"");
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
        Log.d("ChatActivity", "-----------uploading image----------------------");
        StorageReference sReference = FirebaseStorageInstance.getInstance().getRootRef().child("User_Media").child(messageSenderID).child(ConstFirebase.PHOTOS).child("Sent_Photos").child(messageReceiverID);
        final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));

        imgPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        Log.d("ChatActivity", "-----------uploading image----------------------" + uri.toString());
                        progressDialog.dismiss();
                        MessageInputText.setText("");
                        //messageScroll.fullScroll(ScrollView.FOCUS_DOWN);
                        Intent intent = new Intent(ChatActivity.this, ImageText.class);
                        intent.putExtra(Const.IMAGE_URL, uri.toString());
                        intent.putExtra(Const.group_id, messageReceiverID);
                        intent.putExtra(Const.checkPersonalOrGroup,"personal");
                        intent.putExtra(Const.messageSenderName, messageSenderName);
                        intent.putExtra(Const.typeOfMessageSelected, typeOfSelectedMessage);
                        intent.putExtra(Const.selectedMessageId, selectedMessageId);
                        intent.putExtra(Const.someTextFromRaisedTopic,"");
                        startActivity(intent);
                        finish();
                        //SendMessage("image", uri.toString());
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
        rootRef.getUserRef().child(messageSenderID).child(ConstFirebase.userIsIn).setValue("NoWhere");
        reference.removeEventListener(seenListener);

    }
    TypeAndSeparateURL checkTypeAndSeparateURL(String message){


        String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        String separateURL="";
        Pattern p;
        Matcher m = null;
        String[] words = message.split(" ");
        checkIfTypeURL = 0;
        for (String word : words) {
            p = Pattern.compile(URL_REGEX);
            m = p.matcher(word);

            if (m.find()) {
                separateURL=word;
                Toast.makeText(getApplicationContext(), "The String contains URL", Toast.LENGTH_LONG).show();
                checkIfTypeURL = 1;
                break;
            }

        }
        TypeAndSeparateURL typeAndSeparateURL = new TypeAndSeparateURL(separateURL, checkIfTypeURL);
        return typeAndSeparateURL;
    }
}

