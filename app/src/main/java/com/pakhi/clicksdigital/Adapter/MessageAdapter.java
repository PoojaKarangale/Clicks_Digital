package com.pakhi.clicksdigital.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Activities.Constants;
import com.pakhi.clicksdigital.Activities.EnlargedImage;
import com.pakhi.clicksdigital.Model.Messages;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    Bitmap bitmap;
    String chatType, currentGroupId = "";
    Messages messages;
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, usersRef, personalChatRefFrom, personalChatRefTo, groupChatRef;

    public MessageAdapter(List<Messages> userMessagesList, String chatType, String currentGroupId) {
        this.userMessagesList = userMessagesList;
        this.chatType = chatType;
        this.currentGroupId = currentGroupId;
    }

    public MessageAdapter(List<Messages> userMessagesList, String chatType) {
        this.userMessagesList = userMessagesList;
        this.chatType = chatType;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        messages = userMessagesList.get(position);
        //this.position = position;
        final String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = rootRef.child("Users").child(fromUserID).child(Constants.USER_DETAILS);
        final String[] receiverImage = new String[1];
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image_url")) {
                    receiverImage[0] = dataSnapshot.child("image_url").getValue().toString();
                    Picasso.get().load(receiverImage[0]).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra("image_url_string", receiverImage[0]);
                v.getContext().startActivity(fullScreenIntent);
            }
        });
        messageViewHolder.receiverlayout.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderlayout.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.download_image_receiver.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(currentUserId)) {
                messageViewHolder.senderlayout.setVisibility(View.VISIBLE);
                messageViewHolder.senderDate.setText(messages.getTime() + " - " + messages.getDate());
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverlayout.setVisibility(View.VISIBLE);
                messageViewHolder.receiverDate.setText(messages.getTime() + " - " + messages.getDate());
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(currentUserId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(String.valueOf(messages.getMessage()))
                        .into(messageViewHolder.messageSenderPicture);
                Log.d("messageAdapter", "sender-----------" + String.valueOf(messages.getMessage()));
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.download_image_receiver.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(String.valueOf(messages.getMessage()))
                        .into(messageViewHolder.messageReceiverPicture);
                Log.d("messageAdapter", String.valueOf(messages.getMessage()));
            }
        } else if (fromMessageType.equals("pdf")) {

        }
        messageViewHolder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra("image_url_string", String.valueOf(messages.getMessage()));
                v.getContext().startActivity(fullScreenIntent);
            }
        });
        messageViewHolder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                fullScreenIntent.putExtra("image_url_string", String.valueOf(messages.getMessage()));
                v.getContext().startActivity(fullScreenIntent);
            }
        });

        messageViewHolder.download_image_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(v, messageViewHolder);
                // messageViewHolder.download_image_receiver.setVisibility(View.GONE);
                /*
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, WRITE_EXTERNAL_STORAGE_CODE);
                    } else {
                        saveImage(v, messageViewHolder);
                    }
                }
                 */
            }
        });

        messageViewHolder.messageSenderPicture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openAlertBuilderWithOptions(new CharSequence[]{"Delete"}, v, position);
                return true;
            }
        });
        messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (fromUserID.equals(currentUserId)) {
                    openAlertBuilderWithOptions(new CharSequence[]{"Delete for me","Delete for all"}, v, position);
                } else {
                    openAlertBuilderWithOptions(new CharSequence[]{"Delete for me"}, v, position);
                }

                return true;
            }
        });
    }

    private void openAlertBuilderWithOptions(CharSequence[] optionsGet, final View v, final int position) {
        CharSequence options[] = optionsGet;

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        // builder.setTitle("");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: deleteMessage(v, position,"deleteForMe");
                        break;
                    case 1:
                        deleteMessage(v, position,"deleteForAll");
                        break;
                }
            }
        });
        builder.show();
    }

    private void deleteMessage(View v, int position,String deleteScope) {
        switch (chatType) {
            case "PersonalChat":
                deletePersonalChat(v, position,deleteScope);
                break;
            case "GroupChat":
                deleteGroupChat(v, position,deleteScope);
                break;
        }
    }

    private void deletePersonalChat(final View v, final int position,String deleteScope) {
        final Messages messages = userMessagesList.get(position);
        String toUserId = messages.getTo();
        String fromUserID = messages.getFrom();

        personalChatRefFrom = rootRef.child("Messages").child(fromUserID).child(toUserId);
        personalChatRefTo = rootRef.child("Messages").child(toUserId).child(fromUserID);

        if(deleteScope.equals("deleteForMe")){
            personalChatRefFrom.child(messages.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(v.getContext(), "message deleted", Toast.LENGTH_SHORT).show();
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }else if(deleteScope.equals("deleteForAll")){
            personalChatRefFrom.child(messages.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    personalChatRefTo.child(messages.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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
        Messages messages = userMessagesList.get(position);
        groupChatRef = rootRef.child("GroupChat");

        if(deleteScope.equals("deleteForMe")){

            userMessagesList.remove(position);
            notifyItemRemoved(position);

        }else if(deleteScope.equals("deleteForAll")){
            groupChatRef.child(currentGroupId).child(messages.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(v.getContext(), "message deleted", Toast.LENGTH_SHORT).show();
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
    }

    private void saveImage(View v, MessageViewHolder messageViewHolder) {
        bitmap = ((BitmapDrawable) messageViewHolder.messageReceiverPicture.getDrawable()).getBitmap();
        String time = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/DCIM/ClicksDigitalMedia/ClicksDigitalImages");
        dir.mkdirs();
        String imageName = time + ".PNG";
        File file = new File(dir, imageName);
        OutputStream out;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(v.getContext(), "Image is saved in DCIM", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText, senderDate, receiverDate;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture, download_image_receiver;
        LinearLayout receiverlayout, senderlayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverlayout = itemView.findViewById(R.id.receiver_message_layout);
            senderlayout = itemView.findViewById(R.id.sender_message_layout);
            senderDate = itemView.findViewById(R.id.sender_messsage_date_time);
            receiverDate = itemView.findViewById(R.id.receiver_message_date_time);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            download_image_receiver = itemView.findViewById(R.id.download_image_receiver);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }
}
