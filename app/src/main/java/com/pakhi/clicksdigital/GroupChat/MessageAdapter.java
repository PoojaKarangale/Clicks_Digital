package com.pakhi.clicksdigital.GroupChat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Topic.TopicRepliesActivity;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Notifications.Notification;
import com.pakhi.clicksdigital.Utils.SharedPreference;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //  int i = 0;
    SharedPreference pref;
    String currentUserId;
    FirebaseDatabaseInstance rootRef;
    private String chatType;
    String user_type;
    private Message message;
    private List<Message> userMessagesList;
    private DatabaseReference usersRef, personalChatRefFrom, personalChatRefTo, groupChatRef;
    Context context;
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private static final int VIEW_TYPE_TOPIC = 3;
    boolean replyingToMessage =false;

    public MessageAdapter(List<Message> userMessagesList, String chatType, Context context) {
        this.userMessagesList = userMessagesList;
        this.chatType = chatType;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        pref = SharedPreference.getInstance();
        currentUserId = pref.getData(SharedPreference.currentUserId, viewGroup.getContext());
        user_type = pref.getData(SharedPreference.user_type,viewGroup.getContext());
        rootRef = FirebaseDatabaseInstance.getInstance();

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.custom_message_layout_send, viewGroup, false);
                viewHolder = new MessageViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.custom_messages_layout_reveiver, viewGroup, false);
                viewHolder = new MessageViewHolderOther(viewChatOther);
                break;
            case VIEW_TYPE_TOPIC:
                View viewChatTopic = layoutInflater.inflate(R.layout.custom_group_topic_layout, viewGroup, false);
                viewHolder = new TopicViewHolder(viewChatTopic);
                break;
        }

        assert viewHolder != null;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder messageViewHolder, final int position) {

        message = userMessagesList.get(position);
        final String fromUserID = message.getFrom();

        boolean isNewGroup = false;
        long previousTs = 0;
        if(position >= 1){
            Message previousMessage = userMessagesList.get(position-1);
            previousTs = previousMessage.getTimestamp();
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(message.getTimestamp()*1000);
        cal2.setTimeInMillis(previousTs*1000);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        if (!sameDay) {
            isNewGroup = true;
        }

        if (TextUtils.equals(userMessagesList.get(position).getType(), "topic")) {
            configureTopicViewHolder((TopicViewHolder) messageViewHolder, position, message,isNewGroup);
        } else if (TextUtils.equals(fromUserID, currentUserId)
        ) {
            configureMessageViewHolder((MessageViewHolder) messageViewHolder, position, message,isNewGroup);
        } else {
            configureMessageViewHolderOther((MessageViewHolderOther) messageViewHolder, position, message,isNewGroup);
        }

        messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence options[];

                switch (chatType) {
                    case "PersonalChat":
                        //deletePersonalChat(v, position, deleteScope);
                        if(user_type.equals("admin") || message.getFrom().equals(currentUserId)){
                            options = new CharSequence[]
                                    {
                                            "Reply",
                                            "Delete For All",
                                            "Delete For Me"

                                            //, "Remove photo"
                                    };
                        }
                        else {
                            options = new CharSequence[]
                                    {

                                            "Reply"
                                            //, "Delete"
                                            //, "Remove photo"
                                    };
                        }
                        break;
                    case "GroupChat":
                        //deleteGroupChat(v, position, deleteScope);
                        if(user_type.equals("admin") || message.getFrom().equals(currentUserId)){
                            options = new CharSequence[]
                                    {
                                            "Reply",
                                            "Delete"

                                            //, "Remove photo"
                                    };
                        }
                        else {
                            options = new CharSequence[]
                                    {

                                            "Reply"
                                            //, "Delete"
                                            //, "Remove photo"
                                    };
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + chatType);
                }

                openAlertBuilderWithOptions(options, v, position);

                return false;
            }
        });


    }

    private void configureMessageViewHolder(final MessageViewHolder messageViewHolder, int position, final Message message, boolean isNewGroup) {
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.senderLayoutPdf.setVisibility(View.GONE);
        messageViewHolder.senderLayoutUrl.setVisibility(View.GONE);
        messageViewHolder.headerTimeTextLayout.setVisibility(View.GONE);
        messageViewHolder.senderDate.setText(message.getTime() + " - " + message.getDate());

        if (isNewGroup){

            //set date time header
            messageViewHolder.headerTimeTextLayout.removeAllViews();
            TextView dateView = new TextView(context);
            dateView.setText(message.getDate());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            //params.gravity = Gravity.CENTER_HORIZONTAL;
            dateView.setLayoutParams(params);
            messageViewHolder.headerTimeTextLayout.setVisibility(View.VISIBLE);
            messageViewHolder.headerTimeTextLayout.addView(dateView);

        }

        switch (message.getType()) {
            case "text":
                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    messageViewHolder.replyMessageSenderNameSender.setVisibility(View.VISIBLE);
                    messageViewHolder.onLongClickOnMessageSender.setBackgroundResource(R.drawable.reply_back_curve);
                    messageViewHolder.senderMain.setBackgroundResource(R.drawable.msg_back);
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }


                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setText(message.getMessage());

                messageViewHolder.senderLayoutUrl.setVisibility(View.GONE);
                messageViewHolder.senderImageLayout.setVisibility(View.GONE);
                //  messageViewHolder.senderDate.setText(message.getTime() + " - " + message.getDate());
                break;
            case "image":

                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    messageViewHolder.onLongClickOnMessageSender.setBackgroundResource(R.drawable.reply_back_curve);
                    messageViewHolder.senderMain.setBackgroundResource(R.drawable.msg_back);
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }

                messageViewHolder.senderLayoutUrl.setVisibility(View.GONE);
                messageViewHolder.senderMessageText.setVisibility(View.GONE);
                messageViewHolder.senderImageLayout.setVisibility(View.VISIBLE);
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);


                Glide.with(context).load(String.valueOf(message.getMessage()))
                        .transform(new CenterCrop(), new RoundedCorners(10))
                        .into(messageViewHolder.messageSenderPicture);
                if (message.getExtra() == "") {
                    messageViewHolder.senderImageText.setVisibility(View.GONE);
                } else {
                    messageViewHolder.senderImageText.setVisibility(View.VISIBLE);
                    messageViewHolder.senderImageText.setText(message.getExtra());
                }

                break;
            case "pdf":

                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    messageViewHolder.onLongClickOnMessageSender.setBackgroundResource(R.drawable.reply_back_curve);
                    messageViewHolder.senderMain.setBackgroundResource(R.drawable.msg_back);
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }
                messageViewHolder.senderLayoutPdf.setVisibility(View.VISIBLE);
                break;
            case "url":
                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    messageViewHolder.onLongClickOnMessageSender.setBackgroundResource(R.drawable.reply_back_curve);
                    messageViewHolder.senderMain.setBackgroundResource(R.drawable.msg_back);
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }
                messageViewHolder.senderImageLayout.setVisibility(View.GONE);
                messageViewHolder.senderMessageText.setVisibility(View.GONE);
                messageViewHolder.senderLayoutUrl.setVisibility(View.VISIBLE);
                messageViewHolder.senderUrlText.setText(message.getMessage());
                messageViewHolder.senderSeparateURL.setText(message.getExtra());

                new URLAsynk().execute(new Async(messageViewHolder, message.getExtra(), VIEW_TYPE_ME));
                // new URLAsynk(this).execute(new Async(messageViewHolder,message.getMessage()));
                // new URLAsynk().execute(message.getMessage());

                break;
        }
        messageViewHolder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enlargeImage(String.valueOf(message.getMessage()), v);
                Intent intent = new Intent(context, LoadImage.class);
                intent.putExtra(Const.IMAGE_URL, message.getMessage());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        messageViewHolder.senderLayoutPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(message.getMessage()); // missing 'http://' will cause crashed
                // this will open pdf in default pdf viewer of mobile
                openPdf(uri, v);

            }
        });
    }

    private void configurePDFTypeReply(final MessageViewHolder messageViewHolder, Message message) {



        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child(ConstFirebase.type).getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageSender.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnPDFLayoutSender.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child(ConstFirebase.type).getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageSender.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnPDFLayoutSender.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        messageViewHolder.replyOnTextSender.setVisibility(View.GONE);
        messageViewHolder.replyOnURLLayoutSender.setVisibility(View.GONE);
        messageViewHolder.replyOnImageLayoutSender.setVisibility(View.GONE);




    }

    private void configureURLTypeReply(final MessageViewHolder messageViewHolder, Message message) {


        messageViewHolder.onLongClickOnMessageSender.setVisibility(View.VISIBLE);
        messageViewHolder.replyOnURLLayoutSender.setVisibility(View.VISIBLE);


        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child(ConstFirebase.type).getValue().toString(), msg, messageViewHolder);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child(ConstFirebase.type).getValue().toString(), msg, messageViewHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        messageViewHolder.replyOnTextSender.setVisibility(View.GONE);
        messageViewHolder.replyOnImageLayoutSender.setVisibility(View.GONE);
        messageViewHolder.replyOnPDFLayoutSender.setVisibility(View.GONE);

    }

    private void configureImageTypeReply(final MessageViewHolder messageViewHolder, Message message) {


        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child(ConstFirebase.type).getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageSender.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnImageLayoutSender.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child(ConstFirebase.type).getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageSender.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnImageLayoutSender.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        messageViewHolder.replyOnTextSender.setVisibility(View.GONE);
        messageViewHolder.replyOnURLLayoutSender.setVisibility(View.GONE);
        messageViewHolder.replyOnPDFLayoutSender.setVisibility(View.GONE);

    }

    private void configureTextTypeReply(final MessageViewHolder messageViewHolder, Message message) {

        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageSender.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnTextSender.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageSender.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnTextSender.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        messageViewHolder.replyOnImageLayoutSender.setVisibility(View.GONE);
        messageViewHolder.replyOnURLLayoutSender.setVisibility(View.GONE);
        messageViewHolder.replyOnPDFLayoutSender.setVisibility(View.GONE);

    }

    private void configurePDFTypeReply(final MessageViewHolderOther messageViewHolder, Message message) {

        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnPDFLayoutReceiver.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnPDFLayoutReceiver.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        messageViewHolder.replyOnTextReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnURLLayoutReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnImageLayoutReceiver.setVisibility(View.GONE);

    }

    private void configureURLTypeReply(final MessageViewHolderOther messageViewHolder, Message message) {

        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnURLLayoutReceiver.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnURLLayoutReceiver.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        messageViewHolder.replyOnTextReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnImageLayoutReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnPDFLayoutReceiver.setVisibility(View.GONE);

    }

    private void configureImageTypeReply(final MessageViewHolderOther messageViewHolder, Message message) {

        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnImageLayoutReceiver.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnImageLayoutReceiver.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        messageViewHolder.replyOnTextReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnURLLayoutReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnPDFLayoutReceiver.setVisibility(View.GONE);

    }

    private void configureTextTypeReply(final MessageViewHolderOther messageViewHolder, Message message) {

        if(chatType.equals("GroupChat")){
            rootRef.getGroupChatRef().child(message.getTo()).child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnTextReceiver.setVisibility(View.VISIBLE);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            rootRef.getMessagesRef().child(message.getSelectedMessageId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        Message msg = snapshot.getValue(Message.class);
                        writeReplyMessage(snapshot.child("type").getValue().toString(), msg, messageViewHolder);
                        messageViewHolder.onLongClickOnMessageReceiver.setVisibility(View.VISIBLE);
                        messageViewHolder.replyOnTextReceiver.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        messageViewHolder.replyOnImageLayoutReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnURLLayoutReceiver.setVisibility(View.GONE);
        messageViewHolder.replyOnPDFLayoutReceiver.setVisibility(View.GONE);

    }

    private void writeReplyMessage(String type, Message msg, final MessageViewHolder messageViewHolder) {


        if(msg.getFrom().equals(currentUserId)){
            printNameOfReplyMessageSender(messageViewHolder, "You");
        }else{
            getNameOfReplyMessageSender(messageViewHolder,msg);
        }
        messageViewHolder.replyMessageSenderNameSender.setVisibility(View.VISIBLE);

        switch (type){
            case "text":

                messageViewHolder.replyOnTextSender.setText(msg.getMessage());
                break;
            case "image":
                messageViewHolder.replyOnImageImageSender.setVisibility(View.VISIBLE);

                Glide.with(context).load(msg.getMessage()).transform(new CenterCrop(), new RoundedCorners(10))
                        .into(messageViewHolder.replyOnImageImageSender);
                if(!msg.getExtra().equals("")){
                    messageViewHolder.replyOnImageTextSender.setVisibility(View.VISIBLE);
                    messageViewHolder.replyOnImageTextSender.setText(msg.getExtra());
                }
                break;
            case "pdf":
                messageViewHolder.replyOnPDFLayoutSender.setVisibility(View.VISIBLE);
                break;
            case "url":
                messageViewHolder.replyOnURLTextSender.setText(msg.getMessage());
        }
    }


    // This is for Receiver Side
    private void printNameOfReplyMessageSender(MessageViewHolderOther messageViewHolder, String you) {
        messageViewHolder.replyMessageSenderNameReceiver.setText(you);
    }
    private void getNameOfReplyMessageSender(final MessageViewHolderOther messageViewHolder, Message msg) {
        rootRef.getUserRef().child(msg.getFrom()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child(ConstFirebase.userName).getValue().toString();
                printNameOfReplyMessageSender(messageViewHolder, name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //This is for Sender Side
    private void getNameOfReplyMessageSender(final MessageViewHolder messageViewHolder, Message msg) {
        rootRef.getUserRef().child(msg.getFrom()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = snapshot.child(ConstFirebase.userName).getValue().toString();
                printNameOfReplyMessageSender(messageViewHolder, name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void printNameOfReplyMessageSender(MessageViewHolder messageViewHolder, String you) {
        messageViewHolder.replyMessageSenderNameSender.setText(you);
    }

    //For Reciever
    private void writeReplyMessage(String type, Message msg, MessageViewHolderOther messageViewHolder) {
        if(msg.getFrom().equals(currentUserId)){
            printNameOfReplyMessageSender(messageViewHolder, "You");
        }else{
            getNameOfReplyMessageSender(messageViewHolder,msg);
        }
        messageViewHolder.replyMessageSenderNameReceiver.setVisibility(View.VISIBLE);


        switch (type){
            case "text":
                messageViewHolder.replyOnTextReceiver.setText(msg.getMessage());
                break;
            case "image":
                messageViewHolder.replyOnImageImageReceiver.setVisibility(View.VISIBLE);
               Glide.with(context).load(msg.getMessage()).transform(new CenterCrop(), new RoundedCorners(10)).into(messageViewHolder.replyOnImageImageReceiver);
                if(!msg.getExtra().equals("")){
                    messageViewHolder.replyOnImageTextReceiver.setVisibility(View.VISIBLE);
                    messageViewHolder.replyOnImageTextReceiver.setText(msg.getExtra());
                }
                break;
            case "pdf":
                messageViewHolder.replyOnPDFLayoutReceiver.setVisibility(View.VISIBLE);
                break;
            case "url":
                messageViewHolder.replyOnURLTextReceiver.setText(msg.getMessage());
        }
    }


    private void configureMessageViewHolderOther(final MessageViewHolderOther messageViewHolder, final int position, final Message message, boolean isNewGroup) {

        messageViewHolder.headerTimeTextLayout.setVisibility(View.GONE);
        if (isNewGroup){

            //set date time header
            messageViewHolder.headerTimeTextLayout.removeAllViews();
            TextView dateView = new TextView(context);
            dateView.setText(message.getDate());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            //params.gravity = Gravity.CENTER_HORIZONTAL;
            dateView.setLayoutParams(params);
            messageViewHolder.headerTimeTextLayout.setVisibility(View.VISIBLE);
            messageViewHolder.headerTimeTextLayout.addView(dateView);

        }


        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.receiverLayoutPdf.setVisibility(View.GONE);
        // messageViewHolder.download_pdf_receiver.setVisibility(View.GONE);
        messageViewHolder.download_image_receiver.setVisibility(View.GONE);

        messageViewHolder.LayoutUrl.setVisibility(View.GONE);

        usersRef = rootRef.getUserRef().child(message.getFrom()).child(ConstFirebase.USER_DETAILS);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(chatType.equals("PersonalChat")){
                    messageViewHolder.receiver_name.setVisibility(View.GONE);
                    messageViewHolder.recMain.setBackgroundResource(R.drawable.receiver_messages_layout);

                }
                String receiverName = dataSnapshot.child(ConstFirebase.USER_NAME).getValue().toString();
                messageViewHolder.receiver_name.setText(receiverName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        messageViewHolder.receiver_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VisitProfileActivity.class);
                intent.putExtra(Const.visitUser, message.getFrom());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        messageViewHolder.receiverDate.setText(message.getTime() + " - " + message.getDate());
        switch (message.getType()) {
            case "text":
                //  messageViewHolder.receiverDate.setText(message.getTime() + " - " + message.getDate());

                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    messageViewHolder.replyMessageSenderNameReceiver.setVisibility(View.VISIBLE);
                    messageViewHolder.onLongClickOnMessageReceiver.setBackgroundResource(R.drawable.reply_back_rec);
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setText(message.getMessage());

                messageViewHolder.LayoutUrl.setVisibility(View.GONE);
                messageViewHolder.receiverLayoutImage.setVisibility(View.GONE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
                messageViewHolder.download_image_receiver.setVisibility(View.GONE);
                messageViewHolder.imageText.setVisibility(View.GONE);

                break;
            case "image":

                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }
                messageViewHolder.LayoutUrl.setVisibility(View.GONE);
                messageViewHolder.receiverMessageText.setVisibility(View.GONE);

                messageViewHolder.mainRecImageLayout.setVisibility(View.VISIBLE);
                messageViewHolder.receiverLayoutImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.download_image_receiver.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(String.valueOf(message.getMessage()))
                        .transform(new CenterCrop(), new RoundedCorners(10))
                        .into(messageViewHolder.messageReceiverPicture);
                if (message.getExtra() == "") {
                    messageViewHolder.imageText.setVisibility(View.GONE);
                } else {
                    messageViewHolder.imageText.setVisibility(View.VISIBLE);
                    messageViewHolder.imageText.setText(message.getExtra());
                }
                break;
            case "pdf":
                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }
                messageViewHolder.receiverLayoutPdf.setVisibility(View.VISIBLE);
                break;
            case "url":

                if(!message.getSelectedMessageId().equals("") && !message.getTypeOfSelectedMessage().equals("") ){
                    switch (message.getTypeOfSelectedMessage()){
                        case "text":
                            configureTextTypeReply(messageViewHolder, message);

                            break;
                        case "image":
                            configureImageTypeReply(messageViewHolder, message);

                            break;
                        case "pdf":
                            configurePDFTypeReply(messageViewHolder, message);

                            break;
                        case "url":
                            configureURLTypeReply(messageViewHolder, message);

                            break;
                    }

                }
                messageViewHolder.receiverMessageText.setVisibility(View.GONE);
                messageViewHolder.receiverLayoutImage.setVisibility(View.GONE);

                messageViewHolder.LayoutUrl.setVisibility(View.VISIBLE);
                messageViewHolder.urlText.setText(message.getMessage());
                messageViewHolder.separateURL.setText(message.getExtra());
                messageViewHolder.urlText.setMovementMethod(LinkMovementMethod.getInstance());

                //  new URLAsynkRec().execute(new AsyncRec(messageViewHolder, message.getMessage()));

                new URLAsynk().execute(new Async(messageViewHolder, message.getExtra(), VIEW_TYPE_OTHER));

                // messageViewHolder.senderLayoutUrl.setVisibility(View.VISIBLE);
                //  messageViewHolder.senderUrlText.setText(message.getMessage());
                break;
        }
        messageViewHolder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enlargeImage(String.valueOf(message.getMessage()), v);
                Intent intent = new Intent(context, LoadImage.class);
                intent.putExtra(Const.IMAGE_URL, message.getMessage());
                context.startActivity(intent);
            }
        });

        messageViewHolder.download_image_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(v, ((BitmapDrawable) messageViewHolder.messageReceiverPicture.getDrawable()).getBitmap());
            }
        });

        messageViewHolder.receiverLayoutPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(message.getMessage()); // missing 'http://' will cause crashed
                // this will open pdf in default pdf viewer of mobile
                openPdf(uri, v);

            }
        });
      /*  messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Forward",
                                "Delete"
                                //, "Remove photo"
                        };
                openAlertBuilderWithOptions(options, v, position);

                return false;
            }
        });*/


    }

    private void configureTopicViewHolder(final TopicViewHolder messageViewHolder, int position, final Message message, boolean isNewGroup) {

        messageViewHolder.headerTimeTextLayout.setVisibility(View.GONE);
        if (isNewGroup){

        //set date time header
            messageViewHolder.headerTimeTextLayout.removeAllViews();
        TextView dateView = new TextView(context);
        dateView.setText(message.getDate());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //params.gravity = Gravity.CENTER_HORIZONTAL;
        dateView.setLayoutParams(params);
        messageViewHolder.headerTimeTextLayout.setVisibility(View.VISIBLE);
        messageViewHolder.headerTimeTextLayout.addView(dateView);

        }
        usersRef = rootRef.getUserRef().child(message.getFrom()).child(ConstFirebase.USER_DETAILS);
        messageViewHolder.topicLayoutUrl.setVisibility(View.GONE);
        messageViewHolder.topic_text.setVisibility(View.GONE);
        messageViewHolder.raisedImageLayout.setVisibility(View.GONE);

        //new Topic URL implementation
        String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern p;
        Matcher m = null;
        String[] words = message.getMessage().split(" ");
        int i = 0;
        for (String word : words) {
            p = Pattern.compile(URL_REGEX);
            m = p.matcher(word);

            if (m.find()) {
                //Toast.makeText(context, "The String contains URL", Toast.LENGTH_LONG).show();
                i = 1;
                break;
            }
        }
        if (i == 1 && message.getMessage().length() > 113) {
            if (message.getMessage().substring(93, 113).equals(message.getTo())) {
                messageViewHolder.topic_text.setVisibility(View.GONE);
                messageViewHolder.topicLayoutUrl.setVisibility(View.GONE);


                messageViewHolder.raisedImageLayout.setVisibility(View.VISIBLE);
                messageViewHolder.raisedImageText.setText(message.getExtra());
                Glide.with(context)
                        .load(String.valueOf(message.getMessage()))
                        .transform(new CenterCrop(), new RoundedCorners(10))
                        .into(messageViewHolder.raisedImage);
            } else {
                messageViewHolder.topic_text.setVisibility(View.GONE);
                messageViewHolder.raisedImageLayout.setVisibility(View.GONE);
                messageViewHolder.topicLayoutUrl.setVisibility(View.VISIBLE);
                messageViewHolder.topicUrlText.setText(message.getMessage());
                Log.i("extra topic ---- ", message.getExtra());
                messageViewHolder.topicSearateURL.setText(message.getExtra());
                //new URLAsynkTopic().execute(new AsyncTopic(messageViewHolder, message.getMessage()));
                new URLAsynk().execute(new Async(messageViewHolder, message.getExtra(), VIEW_TYPE_TOPIC));
            }

        } else if (i == 1 && message.getMessage().length() < 113) {
            messageViewHolder.topic_text.setVisibility(View.GONE);
            messageViewHolder.raisedImageLayout.setVisibility(View.GONE);
            messageViewHolder.topicLayoutUrl.setVisibility(View.VISIBLE);
            messageViewHolder.topicUrlText.setText(message.getMessage());
            Log.i("extra topic ---- ", message.getExtra());
            //Toast.makeText(context, message.getExtra(), Toast.LENGTH_LONG).show();
            messageViewHolder.topicSearateURL.setText(message.getExtra());

            //  new URLAsynkTopic().execute(new AsyncTopic(messageViewHolder, message.getMessage()));
            new URLAsynk().execute(new Async(messageViewHolder, message.getExtra(), VIEW_TYPE_TOPIC));

        } else if (i == 1 && message.getMessage().substring(93, 113).equals(message.getTo())) {
            messageViewHolder.topic_text.setVisibility(View.GONE);
            messageViewHolder.topicLayoutUrl.setVisibility(View.GONE);

            messageViewHolder.raisedImageLayout.setVisibility(View.VISIBLE);
            messageViewHolder.raisedImageText.setText(message.getExtra());
            Glide.with(context)
                    .load(String.valueOf(message.getMessage()))
                    .transform(new CenterCrop(), new RoundedCorners(10))
                    .into(messageViewHolder.raisedImage);
        } else {
            messageViewHolder.topic_text.setVisibility(View.VISIBLE);
            messageViewHolder.raisedImageLayout.setVisibility(View.GONE);
            messageViewHolder.topicLayoutUrl.setVisibility(View.GONE);
            messageViewHolder.topic_text.setText(message.getMessage());
        }

        messageViewHolder.topic_date_time.setText(message.getTime() + " - " + message.getDate());

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(ConstFirebase.USER_ID).getValue().toString().equals(currentUserId)){
                    messageViewHolder.publisher_name.setText("You");
                }else{
                    String receiverName = dataSnapshot.child(ConstFirebase.USER_NAME).getValue().toString();
                    messageViewHolder.publisher_name.setText(receiverName);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootRef.getReplyRef().child(message.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    messageViewHolder.no_of_replies.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        messageViewHolder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), TopicRepliesActivity.class);
                i.putExtra("message", message);
                v.getContext().startActivity(i);
            }
        });
        final boolean[] isLiked = {false};
        final DatabaseReference topicLikesRef = rootRef.getTopicLikesRef();
        topicLikesRef.child(message.getMessageID()).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //topic is liked by user
                    isLiked[0] = true;
                    messageViewHolder.like.setImageResource(R.drawable.liked);
                } else {
                    //topic is not liked by user
                    isLiked[0] = false;
                    messageViewHolder.like.setImageResource(R.drawable.like_border);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        topicLikesRef.child(message.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    messageViewHolder.no_of_likes.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        messageViewHolder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLiked[0]) {
                    isLiked[0] = false;
                    // dislike the topic make hart black
                    topicLikesRef.child(message.getMessageID()).child(currentUserId).removeValue();

                    messageViewHolder.like.setImageResource(R.drawable.like_border);

                } else {
                    isLiked[0] = true;
                    // like the topic reden the heart
                    topicLikesRef.child(message.getMessageID()).child(currentUserId).setValue("");
                    messageViewHolder.like.setImageResource(R.drawable.liked);
                    //addNotification(message); activate it after the glitch is gone
                }
            }
        });
    }

    private void addNotification(final Message message) {
        final String[] name = new String[1];
        final String[] grpName = new String[1];

        rootRef.getUserRef().child(currentUserId).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rootRef.getGroupRef().child(message.getTo()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        grpName[0]=snapshot.child(ConstFirebase.GROUP_NAME).getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                name[0] = snapshot.child(ConstFirebase.USER_NAME).getValue().toString();
                sendLikeNotification(name[0], grpName[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void sendLikeNotification(String s, String s2) {
        if(!currentUserId.equals(message.getFrom())){
            Notification.sendPersonalNotifiaction(message.getTo(), message.getFrom(), s +"has liked your Dialog topic" + message.getMessage(), s2, "topic", message.getMessageID());
            String notificationKey = rootRef.getNotificationRef().push().getKey();

            rootRef.getNotificationRef().child(notificationKey).child("to").setValue(message.getFrom());
            rootRef.getNotificationRef().child(notificationKey).child("from").setValue(currentUserId);
            rootRef.getNotificationRef().child(notificationKey).child("go").setValue(message.getMessageID());
            rootRef.getNotificationRef().child(notificationKey).child("type").setValue("topicLike");

            //rootRef.getNotificationRefTopicLike().child(message.getFrom()).child(message.getMessageID()).setValue("");
        }
    }

    private void openPdf(Uri uri, View v) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent in = Intent.createChooser(intent, "open file");
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        v.getContext().startActivity(in);

        /*
        // this will redirect user to browser downloads and will download the pdf
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        v.getContext().startActivity(intent); */
    }

    private void enlargeImage(String s, View v) {

        EnlargedImage.enlargeImage(s, v.getContext());

    }

    private void openAlertBuilderWithOptions(CharSequence[] optionsGet, final View v, final int position) {
        CharSequence options[] = optionsGet;

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        // builder.setTitle("");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        //forwardMessage(v, position);
                        replyingToMessage=true;
                        replyToMessage(v, position, replyingToMessage);
                        break;
                    case 1:
                        deleteMessage(v, position, "deleteForAll");
                        break;
                    case 2:
                        deleteMessage(v, position, "deleteForMe");
                        break;
                }
            }
        });
        builder.show();
    }

    private void replyToMessage(View v, int position, boolean replyingToMessage) {
        Message msg = null;
        msg = userMessagesList.get(position);
        String typeOfSelectedMessage = msg.getType();
        String selectedMessageId = msg.getMessageID();
        Intent intent = new Intent("custom-message");
        intent.putExtra("typeOfSelectedMessage", typeOfSelectedMessage);
        intent.putExtra("selectedMessageId", selectedMessageId);
        intent.putExtra("replyingToMessage", replyingToMessage);
        intent.putExtra("message",msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


    }
   /* private void forwardMessage(View v, int position) {
        Toast.makeText(v.getContext(), "This feature is yet to implement", Toast.LENGTH_LONG).show();
        Message msg = userMessagesList.get(position);
        msg.setFrom(currentUserId);
                Intent intent = new Intent(context, forwarMessageActivity.class);
                intent.putExtra(Const.message, msg);
                intent.putExtra("chatType", chatType);
                context.startActivity(intent);


    }*/

    private void deleteMessage(View v, int position, String deleteScope) {
        switch (chatType) {
            case "PersonalChat":
                deletePersonalChat(v, position, deleteScope);
                break;
            case "GroupChat":
                deleteGroupChat(v, position, deleteScope);
                break;
        }
    }

    private void deletePersonalChat(final View v, final int position, String deleteScope) {
        final Message message = userMessagesList.get(position);
        String toUserId = message.getTo();
        String fromUserID = message.getFrom();

        personalChatRefFrom = rootRef.getMessagesListRef().child(fromUserID).child(toUserId);
        personalChatRefTo = rootRef.getMessagesListRef().child(toUserId).child(fromUserID);

        if (deleteScope.equals("deleteForMe")) {
            personalChatRefFrom.child(message.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    rootRef.getMessagesRef().child(message.getMessageID()).removeValue();
                    Toast.makeText(v.getContext(), "message deleted", Toast.LENGTH_SHORT).show();
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        } else if (deleteScope.equals("deleteForAll")) {
            personalChatRefFrom.child(message.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    personalChatRefTo.child(message.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            rootRef.getMessagesRef().child(message.getMessageID()).removeValue();
                            Toast.makeText(v.getContext(), "message deleted", Toast.LENGTH_SHORT).show();
                            userMessagesList.remove(position);
                            notifyItemRemoved(position);
                        }
                    });
                }
            });
        }
    }

    private void deleteGroupChat(final View v, final int position, String deleteScope) {
        final Message message = userMessagesList.get(position);
        groupChatRef = rootRef.getGroupChatRef();
        final String messageID = message.getMessageID();

       /* if (deleteScope.equals("deleteForMe")) {

            userMessagesList.remove(position);
            notifyItemRemoved(position);

        } else*/ if (deleteScope.equals("deleteForAll")) {

            if(message.getType().equals("topic")){
                rootRef.getReplyRef().child(messageID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap : snapshot.getChildren()){
                            rootRef.getReplyLikesRef().child(snap.getKey()).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                rootRef.getTopicLikesRef().child(messageID).removeValue();
                rootRef.getReplyRef().child(messageID).removeValue();
                rootRef.getTopicRef().child(messageID).removeValue();
            }

            groupChatRef.child(message.getTo()).child(messageID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(v.getContext(), "message deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveImage(View v, Bitmap bitmap) {
        //  Bitmap bitmap = ((BitmapDrawable) messageViewHolder.messageReceiverPicture.getDrawable()).getBitmap();

        String time = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/ClicksDigitalMedia/ClicksDigitalImages");
        dir.mkdirs();
        String imageName = time + ".PNG";
        File file = new File(dir, imageName);
        OutputStream out;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(v.getContext(), "Image is saved in ClicksDigitalMedia/ClicksDigitalImages", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // return super.getItemViewType(position);
        pref = SharedPreference.getInstance();
        currentUserId = pref.getData(SharedPreference.currentUserId, context);

        if (TextUtils.equals(userMessagesList.get(position).getType(), "topic")) {
            return VIEW_TYPE_TOPIC;
        } else if (TextUtils.equals(userMessagesList.get(position).getFrom(), currentUserId)
        ) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, senderDate, isSeen, urlTitle, urlDesc, senderUrlText, senderImageText, senderSeparateURL;
        public ImageView messageSenderPicture, urlImage;
        public LinearLayout senderLayoutPdf, senderLayoutUrl, senderImageLayout;
        public LinearLayout senderMain,headerTimeTextLayout;



        //replyOnClickHeader
        LinearLayout onLongClickOnMessageSender, replyHeaderSender;
        TextView replyMessageSenderNameSender;

        //Reply On Text
        TextView replyOnTextSender;

        //ReplyOnImage
        LinearLayout replyOnImageLayoutSender;
        ImageView replyOnImageImageSender;
        TextView replyOnImageTextSender;

        //Reply on PDf
        LinearLayout replyOnPDFLayoutSender;

        //ReplyOnURL
        LinearLayout replyOnURLLayoutSender;
        ImageView replyOnURLImageSender;
        TextView replyOnURLTitleSender, replyOnURLTextSender;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            //right side box sender

            senderMain = itemView.findViewById(R.id.sender_main);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            senderDate = itemView.findViewById(R.id.sender_messsage_date_time);
            senderLayoutPdf = itemView.findViewById(R.id.layout_sender_pdf);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);

            //right side
            isSeen = itemView.findViewById(R.id.isSeen);
            isSeen.setText("delivered");

            senderLayoutUrl = itemView.findViewById(R.id.layout_url_sender);
            urlImage = itemView.findViewById(R.id.url_image_sender);
            urlTitle = itemView.findViewById(R.id.title_of_url_sender);
            urlDesc = itemView.findViewById(R.id.desc_of_url_sender);
            senderUrlText = itemView.findViewById(R.id.url_text);
            senderSeparateURL = itemView.findViewById(R.id.separate_url);

            //image text
            senderImageLayout = itemView.findViewById(R.id.image_sender_linear);
            senderImageText = itemView.findViewById(R.id.image_text_sender);

            //ReplyInMessageHEader
            onLongClickOnMessageSender=itemView.findViewById(R.id.reply_main_sender);
            replyMessageSenderNameSender=itemView.findViewById(R.id.reply_message_sender_name_sender);

            //replyOnText
            replyOnTextSender=itemView.findViewById(R.id.reply_text_sender);

            //replyOnImage
            replyOnImageLayoutSender=itemView.findViewById(R.id.reply_image_layout_sender);
            replyOnImageImageSender=itemView.findViewById(R.id.reply_image_sender);
            replyOnImageTextSender=itemView.findViewById(R.id.reply_image_text_sender);

            //replyOnPdf
            replyOnPDFLayoutSender=itemView.findViewById(R.id.reply_pdf_layout_sender);

            //replyOnURL
            replyOnURLLayoutSender=itemView.findViewById(R.id.reply_url_layout_sender);
            replyOnURLImageSender=itemView.findViewById(R.id.reply_url_image_sender);
            replyOnURLTitleSender=itemView.findViewById(R.id.reply_url_title_sender);
            replyOnURLTextSender=itemView.findViewById(R.id.reply_url_text_sender);

            headerTimeTextLayout = itemView.findViewById(R.id.HeaderDateTime);

        }
    }

    public class MessageViewHolderOther extends RecyclerView.ViewHolder {
        public TextView receiverMessageText, receiverDate, receiver_name, urlTitle, urlDesc, urlText, imageText, separateURL;

        public ImageView messageReceiverPicture, download_image_receiver,
                download_pdf_receiver, urlImage;
        LinearLayout receiverLayoutPdf, receiverLayoutImage, LayoutUrl, mainRecImageLayout,headerTimeTextLayout;

        //replyOnClickHeader
        LinearLayout onLongClickOnMessageReceiver, replyHeaderReceiver;
        TextView replyMessageSenderNameReceiver;

        //Reply On Text
        TextView replyOnTextReceiver;

        //ReplyOnImage
        LinearLayout replyOnImageLayoutReceiver;
        ImageView replyOnImageImageReceiver;
        TextView replyOnImageTextReceiver;

        //Reply on PDf
        LinearLayout replyOnPDFLayoutReceiver;

        //ReplyOnURL
        LinearLayout replyOnURLLayoutReceiver;
        ImageView replyOnURLImageReceiver;
        TextView replyOnURLTitleReceiver, replyOnURLTextReceiver;
        LinearLayout recMain;

        public MessageViewHolderOther(@NonNull View itemView) {
            super(itemView);

            recMain = itemView.findViewById(R.id.rec_main);
            //left side box
            receiver_name = itemView.findViewById(R.id.receiver_name);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverDate = itemView.findViewById(R.id.receiver_message_date_time);

            download_image_receiver = itemView.findViewById(R.id.download_image_receiver);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);

            receiverLayoutPdf = itemView.findViewById(R.id.layout_recevier_pdf);
            download_pdf_receiver = itemView.findViewById(R.id.download_pdf);

            receiverLayoutImage = itemView.findViewById(R.id.receiver_image_layout);



            //url
            LayoutUrl = itemView.findViewById(R.id.layout_url_rec);
            urlImage = itemView.findViewById(R.id.url_image_rec);
            urlTitle = itemView.findViewById(R.id.title_of_url_rec);
            urlDesc = itemView.findViewById(R.id.desc_of_url_rec);
            urlText = itemView.findViewById(R.id.url_text_rec);
            separateURL = itemView.findViewById(R.id.separate_url_rec);

            //imagetext
            mainRecImageLayout = itemView.findViewById(R.id.main_rec_image_layout);
            imageText = itemView.findViewById(R.id.image_text_rec);

            //ReplyInMessageHEader
            onLongClickOnMessageReceiver=itemView.findViewById(R.id.reply_main_receiver);
            replyMessageSenderNameReceiver=itemView.findViewById(R.id.reply_message_sender_name_receiver);

            //replyOnText
            replyOnTextReceiver=itemView.findViewById(R.id.reply_text_receiver);

            //replyOnImage
            replyOnImageLayoutReceiver=itemView.findViewById(R.id.reply_image_layout_receiver);
            replyOnImageImageReceiver=itemView.findViewById(R.id.reply_image_receiver);
            replyOnImageTextReceiver=itemView.findViewById(R.id.reply_image_text_receiver);

            //replyOnPdf
            replyOnPDFLayoutReceiver=itemView.findViewById(R.id.reply_pdf_layout_receiver);

            //replyOnURL
            replyOnURLLayoutReceiver=itemView.findViewById(R.id.reply_url_layout_receiver);
            replyOnURLImageReceiver=itemView.findViewById(R.id.reply_url_image_receiver);
            replyOnURLTitleReceiver=itemView.findViewById(R.id.reply_url_title_receiver);
            replyOnURLTextReceiver=itemView.findViewById(R.id.reply_url_text_receiver);

            headerTimeTextLayout = itemView.findViewById(R.id.HeaderDateTime);



        }
    }

    public class TopicViewHolder extends RecyclerView.ViewHolder {

        public TextView no_of_likes, publisher_name, topic_text, urlTitle, urlDesc, topicUrlText, topicSearateURL,
                topic_date_time, reply, no_of_replies, raisedImageText;
        //public CircleImageView receiverProfileImage;
        public ImageView like, urlImage, raisedImage;
        public LinearLayout topicLayoutUrl, raisedImageLayout,headerTimeTextLayout;

        //replyOnClickHeader
        LinearLayout onLongClickOnMessageTopic, replyHeaderTopic;
        TextView replyMessageSenderNameTopic;

        //Reply On Text
        TextView replyOnTextTopic;

        //ReplyOnImage
        LinearLayout replyOnImageLayoutTopic;
        TextView replyOnImageTextTopic;

        //Reply on PDf
        LinearLayout replyOnPDFLayoutTopic;

        //ReplyOnURL
        LinearLayout replyOnURLLayoutTopic;
        ImageView replyOnURLImageTopic;
        TextView replyOnURLTitleTopic, replyOnURLTextTopic;


        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);

            //Discussion Topic
            publisher_name = itemView.findViewById(R.id.publisher_name);
            topic_text = itemView.findViewById(R.id.topic_text);
            topic_date_time = itemView.findViewById(R.id.topic_date_time);
            reply = itemView.findViewById(R.id.reply);
            no_of_replies = itemView.findViewById(R.id.no_of_replies);
            like = itemView.findViewById(R.id.like);
            no_of_likes = itemView.findViewById(R.id.no_of_likes);

            //URL
            topicLayoutUrl = itemView.findViewById(R.id.layout_url_topic);
            urlImage = itemView.findViewById(R.id.url_image_topic);
            urlTitle = itemView.findViewById(R.id.title_of_url_topic);
            urlDesc = itemView.findViewById(R.id.desc_of_url_topic);
            topicUrlText = itemView.findViewById(R.id.url_text_topic);
            topicSearateURL = itemView.findViewById(R.id.separate_url_topic);

            //IMAGE
            raisedImageLayout = itemView.findViewById(R.id.raised_image_layout);
            raisedImage = itemView.findViewById(R.id.raised_image);
            raisedImageText = itemView.findViewById(R.id.raised_image_text);
            headerTimeTextLayout = itemView.findViewById(R.id.HeaderDateTime);
        }
    }


    public class URLAsynk extends AsyncTask<Async, String, WebUrl> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(WebUrl output) {
            super.onPostExecute(output);

            switch (output.view_type) {
                case VIEW_TYPE_ME:
                    setupSelfUrlMessage(output);
                    break;
                case VIEW_TYPE_OTHER:
                    setupOtherUrlMessage(output);
                    break;
                case VIEW_TYPE_TOPIC:
                    setupTopicUrlMessage(output);
                    break;
            }

        }

        @Override
        protected WebUrl doInBackground(Async... object) {
            String value = null;
            Document doc = null;
            WebUrl webUrlObj = null;
            try {
                doc = Jsoup.connect(object[0].urlMessage).get();
                //value = doc.title();
            } catch (Exception e) {
                //value="No Title";
                e.printStackTrace();
            }
           /* value = doc.title();
            Log.i("Value of Title - ", value);
            String description = doc.select("meta[name=description]").get(0).attr("content");// ;
            Log.i("Value of desc - ", description);*/
            String imageUrl = "";
            String description = "";
            try {
                 /*description = doc.select("meta[name=description]").get(0).attr("content");
                Log.i("Value of desc - ", description);
*/
                value = doc.title();
                Log.i("Value of Title - ", value);
                description = doc.select("meta[name=description]").get(0).attr("content");// ;
                Log.i("Value of desc - ", description);


                if (!doc.select("meta[property=og:image]").get(0).attr("content").isEmpty()) {
                    imageUrl = doc.select("meta[property=og:image]").get(0).attr("content");
                    Log.i("Image URL - ", imageUrl);
                }


            } catch (Exception e) {
                //description="";
                e.printStackTrace();
            }
            webUrlObj = new WebUrl(object[0].viewHolder, value.toString(), description.toString(), imageUrl.toString(), object[0].view_type);

            return webUrlObj;
        }
    }

    private void setupTopicUrlMessage(WebUrl output) {
        ((TopicViewHolder) output.viewHolder).urlTitle.setText(output.title);
        ((TopicViewHolder) output.viewHolder).urlDesc.setText(output.description);
        if (!TextUtils.isEmpty(output.imageUrl.toString())) {

            Glide.with(context).load(output.imageUrl)
                    .transform(new CenterCrop(), new RoundedCorners(10)).
                    into(((TopicViewHolder) output.viewHolder).urlImage);
        } else ((TopicViewHolder) output.viewHolder).urlImage.setVisibility(View.GONE);

    }

    private void setupOtherUrlMessage(WebUrl output) {
        ((MessageViewHolderOther) output.viewHolder).urlTitle.setText(output.title);
        ((MessageViewHolderOther) output.viewHolder).urlDesc.setText(output.description);
        if (!TextUtils.isEmpty(output.imageUrl.toString())) {
            Glide.with(context).load(output.imageUrl)
                    .transform(new CenterCrop(), new RoundedCorners(10)).
                    into(((MessageViewHolderOther) output.viewHolder).urlImage);
        } else ((MessageViewHolderOther) output.viewHolder).urlImage.setVisibility(View.GONE);

    }

    private void setupSelfUrlMessage(WebUrl output) {
        ((MessageViewHolder) output.viewHolder).urlTitle.setText(output.title);
        ((MessageViewHolder) output.viewHolder).urlDesc.setText(output.description);
        if (!TextUtils.isEmpty(output.imageUrl.toString())) {


            Glide.with(context).load(output.imageUrl)
                    .transform(new CenterCrop(), new RoundedCorners(10)).
                    into(((MessageViewHolder) output.viewHolder).urlImage);
        } else ((MessageViewHolder) output.viewHolder).urlImage.setVisibility(View.GONE);

    }

}

class WebUrl {
    String title, description, imageUrl;
    // MessageAdapter.MessageViewHolder viewHolder;
    RecyclerView.ViewHolder viewHolder;
    int view_type;

    WebUrl(RecyclerView.ViewHolder viewHolder, String title, String description, String imageUrl, int view_type) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.viewHolder = viewHolder;
        this.view_type = view_type;
    }
}

class Async {
    String urlMessage;
    // MessageAdapter.MessageViewHolder viewHolder;
    RecyclerView.ViewHolder viewHolder;
    int view_type;

    Async(RecyclerView.ViewHolder viewHolder, String urlMessage, int view_type) {
        this.viewHolder = viewHolder;
        this.urlMessage = urlMessage;
        this.view_type = view_type;
    }
}



