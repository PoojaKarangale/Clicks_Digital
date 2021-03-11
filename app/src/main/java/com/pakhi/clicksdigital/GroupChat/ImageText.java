package com.pakhi.clicksdigital.GroupChat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.Notification;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ImageText extends AppCompatActivity {
    FirebaseDatabaseInstance rootRef;
    SharedPreference pref;
    DatabaseReference groupChatRefForCurrentGroup;
    String imageUri;
    Uri imgUri;
    String inp;
    String check;
    String messageSenderName;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime, currentGroupId;
    String flag;
    private ProgressDialog progressDialog;
    EditText textImage;
    ImageView image;
    Button button;
    boolean notify=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_text);
        imageUri=getIntent().getStringExtra("image");
        currentGroupId=getIntent().getStringExtra("grp_id");
        check=getIntent().getStringExtra("check");
        messageSenderName=getIntent().getStringExtra("name");
        flag=getIntent().getStringExtra("flag");

        imgUri = Uri.parse(imageUri);

        textImage = findViewById(R.id.input_group_text);
        image = findViewById(R.id.image_text);
        button = findViewById(R.id.button_image);


        //image.setImageURI(Uri.parse(imageUri));
        Log.i("The image URi -----",imgUri.toString());
       // image.setImageURI(imgUri);
        Picasso.get().load(imgUri).into(image);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Image");
        pref = SharedPreference.getInstance();
        currentUserID = pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef = FirebaseDatabaseInstance.getInstance();
        //UsersRef = rootRef.getUserRef();
        //GroupIdRef = rootRef.getGroupRef().child(currentGroupId);
        groupChatRefForCurrentGroup = rootRef.getGroupChatRef().child(currentGroupId);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inp = textImage.getText().toString();

                if(TextUtils.isEmpty(inp)){
                    inp="";
                    if(check.equals("personal")){
                        SendMessage("image",imageUri,inp);
                        //goToParent();

                    }else if(check.equals("grp")){
                        SaveMessageInfoToDatabase("image", imageUri, inp);
                        //goToParent();
                    }

                }
                else {
                    if(check.equals("personal")){
                        SendMessage("image", imageUri, inp);
                        //goToParent();
                    }else if(check.equals("grp")){
                        SaveMessageInfoToDatabase("image", imageUri, inp);
                        //goToParent();
                    }

                    //SaveMessageInfoToDatabase("image", imageUri, inp);
                }

            }
        });


    }

    private void goToParent() {
        Intent intent = new Intent(ImageText.this, ChatActivity.class);
        intent.putExtra(Const.visitUser, currentGroupId);
        startActivity(intent);
        finish();
    }
    private void goToParentGrp(){
        Intent intent = new Intent(ImageText.this, GroupChatActivity.class);
        intent.putExtra(ConstFirebase.groupId, currentGroupId);
        startActivity(intent);
        finish();
    }

    private void SaveMessageInfoToDatabase(String messageType, String message, String inp) {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForDate.getTime());

        HashMap<String, Object> groupMessageKey = new HashMap<>();

        groupChatRefForCurrentGroup.updateChildren(groupMessageKey);
        String messagekEY = groupChatRefForCurrentGroup.push().getKey();
        Long timestamp = calForDate.getTimeInMillis() / 1000L;

        Log.i("GROUPID --------- ",currentGroupId);

        Log.i("SUBSTRING --------- ",message.substring(94,113));
        if(flag.equals("1")){
            Message message1 = new Message(currentUserID, message,
                    "topic", currentGroupId, messagekEY, currentTime, currentDate, timestamp, inp);
            groupChatRefForCurrentGroup.child(messagekEY).setValue(message1);

            DatabaseReference topicRef = rootRef.getTopicRef();
            topicRef.child(messagekEY).setValue(currentGroupId);

            goToParentGrp();

        }

        else {
            Message message1 = new Message(currentUserID, message,
                    messageType, currentGroupId, messagekEY, currentTime, currentDate, timestamp, inp);
            groupChatRefForCurrentGroup.child(messagekEY).setValue(message1);
            goToParentGrp();


        }

        /*Message message1 = new Message(currentUserID, message,
                messageType, currentGroupId, messagekEY, currentTime, currentDate, timestamp, inp);*/


        /*if (messageType == "topic") {
            saveSeparateTopicNode(messagekEY);
        }*/


        //progressDialog.dismiss();
    }
    private void SendMessage(String messageType, String message, String inp) {

        //messageScroll.fullScroll(ScrollView.FOCUS_DOWN);

        String messageSenderRef = "MessagesList/" + currentUserID + "/" + currentGroupId;
        String messageReceiverRef = "MessagesList/" + currentGroupId + "/" + currentUserID;

        DatabaseReference userMessageKeyRef = rootRef.getMessagesRef().push();
        // .child(messageSenderID).child(messageReceiverID).push();

        String messagePushID = userMessageKeyRef.getKey();

        /* Map messageTextBody = new HashMap();*/

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calForDate.getTime());
        //"https://firebasestorage.googleapis.com/v0/b/clicksdigital-ad067.appspot.com/o/Group_photos%2F-MTRZMoFEivrWqxUFJp0%2Fphotos%2F1613245422700.jpg?alt=media&token=1d2b3f4c-598f-41e9-9cc7-c3cdc41c81dc"
        //"https://firebasestorage.googleapis.com/v0/b/clicksdigital-ad067.appspot.com/o/Group_photos%2F-MTRZMoFEivrWqxUFJp0%2Fphotos%2F1613250342248.jpg?alt=media&token=6727c447-396d-4a07-a7b7-bbb5b87ed4a8"

        Message message1 = new Message(currentUserID, message,
                messageType, currentGroupId, messagePushID, currentTime, currentDate, false, inp);




        /*  messageTextBody.put("from", messageSenderID);
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

        goToParent();
        rootRef.getRootRef().updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    // Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
                //MessageInputText.setText("");
            }
        });

        if (notify) {
            // Notification.sendPersonalNotifiaction(messageSenderID, messageReceiverID, "username + \": \" + message", "New Message");
            Notification.sendPersonalNotifiaction( currentUserID, currentGroupId, messageSenderName + ": " + message, "New Message","chat","");
        }
        notify = false;
    }

}
