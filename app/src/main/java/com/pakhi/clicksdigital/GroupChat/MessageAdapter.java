package com.pakhi.clicksdigital.GroupChat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Topic.TopicRepliesActivity;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    SharedPreference pref;
    String currentUserId;
    FirebaseDatabaseInstance rootRef;
    private String chatType;
    private Message message;
    private List<Message> userMessagesList;
    private DatabaseReference usersRef, personalChatRefFrom, personalChatRefTo, groupChatRef;
    Context context;
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;
    private static final int VIEW_TYPE_TOPIC = 3;

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
                View viewChatTopic = layoutInflater.inflate(R.layout.group_topic_layout, viewGroup, false);
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

        if (TextUtils.equals(userMessagesList.get(position).getType(), "topic")) {
            configureTopicViewHolder((TopicViewHolder) messageViewHolder, position, message);
        } else if (TextUtils.equals(fromUserID, currentUserId)
        ) {
            configureMessageViewHolder((MessageViewHolder) messageViewHolder, position, message);
        } else {
            configureMessageViewHolderOther((MessageViewHolderOther) messageViewHolder, position, message);
        }

    }

    private void configureMessageViewHolder(MessageViewHolder messageViewHolder, int position, final Message message) {
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.senderLayoutPdf.setVisibility(View.GONE);
        switch (message.getType()) {
            case "text":
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setText(message.getMessage());
                messageViewHolder.senderDate.setText(message.getTime() + " - " + message.getDate());
                break;
            case "image":
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(String.valueOf(message.getMessage()))
                        .into(messageViewHolder.messageSenderPicture);
                break;
            case "pdf":
                messageViewHolder.senderLayoutPdf.setVisibility(View.VISIBLE);
                break;
        }
        messageViewHolder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enlargeImage(String.valueOf(message.getMessage()), v);
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

    private void configureMessageViewHolderOther(final MessageViewHolderOther messageViewHolder, int position, final Message message) {
        usersRef = rootRef.getUserRef().child(message.getFrom()).child(Const.USER_DETAILS);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String receiverName = dataSnapshot.child(Const.USER_NAME).getValue().toString();
                messageViewHolder.receiver_name.setText(receiverName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        switch (message.getType()) {
            case "text":
                messageViewHolder.receiverDate.setText(message.getTime() + " - " + message.getDate());
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setText(message.getMessage());
                break;
            case "image":
                messageViewHolder.receiverLayoutImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(String.valueOf(message.getMessage()))
                        .into(messageViewHolder.messageReceiverPicture);
                break;
            case "pdf":
                messageViewHolder.receiverLayoutPdf.setVisibility(View.VISIBLE);
                break;
        }
        messageViewHolder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enlargeImage(String.valueOf(message.getMessage()), v);

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
    }

    private void configureTopicViewHolder(final TopicViewHolder messageViewHolder, int position, final Message message) {
        usersRef = rootRef.getUserRef().child(message.getFrom()).child(Const.USER_DETAILS);

        messageViewHolder.topic_text.setText(message.getMessage());
        messageViewHolder.topic_date_time.setText(message.getTime() + " - " + message.getDate());

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String receiverName = dataSnapshot.child(Const.USER_NAME).getValue().toString();
                messageViewHolder.publisher_name.setText(receiverName);
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
                    //topik is not liked by user
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
                }
            }
        });
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
                        deleteMessage(v, position, "deleteForMe");
                        break;
                    case 1:
                        deleteMessage(v, position, "deleteForAll");
                        break;
                }
            }
        });
        builder.show();
    }

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

        personalChatRefFrom = rootRef.getMessagesRef().child(fromUserID).child(toUserId);
        personalChatRefTo = rootRef.getMessagesRef().child(toUserId).child(fromUserID);

        if (deleteScope.equals("deleteForMe")) {
            personalChatRefFrom.child(message.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
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
        Message message = userMessagesList.get(position);
        groupChatRef = rootRef.getGroupChatRef();

        if (deleteScope.equals("deleteForMe")) {

            userMessagesList.remove(position);
            notifyItemRemoved(position);

        } else if (deleteScope.equals("deleteForAll")) {
            groupChatRef.child(message.getTo()).child(message.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(v.getContext(), "message deleted", Toast.LENGTH_SHORT).show();
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
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

        public TextView senderMessageText, senderDate, isSeen;
        public ImageView messageSenderPicture;
        public LinearLayout senderLayoutPdf;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            //right side box sender
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            senderDate = itemView.findViewById(R.id.sender_messsage_date_time);
            senderLayoutPdf = itemView.findViewById(R.id.layout_sender_pdf);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);

            //right side
            isSeen = itemView.findViewById(R.id.isSeen);
            isSeen.setText("deliverd");

        }
    }

    public class MessageViewHolderOther extends RecyclerView.ViewHolder {
        public TextView receiverMessageText, receiverDate, receiver_name;

        public ImageView messageReceiverPicture, download_image_receiver,
                download_pdf_receiver;
        LinearLayout receiverLayoutPdf, receiverLayoutImage;

        public MessageViewHolderOther(@NonNull View itemView) {
            super(itemView);

            //left side box
            receiver_name = itemView.findViewById(R.id.receiver_name);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverDate = itemView.findViewById(R.id.receiver_message_date_time);

            download_image_receiver = itemView.findViewById(R.id.download_image_receiver);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);

            receiverLayoutPdf = itemView.findViewById(R.id.layout_recevier_pdf);
            download_pdf_receiver = itemView.findViewById(R.id.download_pdf);

            receiverLayoutImage = itemView.findViewById(R.id.receiver_image_layout);

        }
    }

    public class TopicViewHolder extends RecyclerView.ViewHolder {

        public TextView no_of_likes, publisher_name, topic_text,
                topic_date_time, reply, no_of_replies;
        public CircleImageView receiverProfileImage;
        public ImageView like;

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

        }
    }
}