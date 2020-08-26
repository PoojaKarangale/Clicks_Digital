package com.pakhi.clicksdigital.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    SharedPreference         pref;
    String                   currentUserId;
    FirebaseDatabaseInstance rootRef;
    private String chatType, currentGroupId="";
    private Message           messages;
    private List<Message>     userMessagesList;
    private DatabaseReference usersRef, personalChatRefFrom, personalChatRefTo, groupChatRef;

    public MessageAdapter(List<Message> userMessagesList, String chatType, String currentGroupId) {
        this.userMessagesList=userMessagesList;
        this.chatType=chatType;
        this.currentGroupId=currentGroupId;
    }

    public MessageAdapter(List<Message> userMessagesList, String chatType) {
        this.userMessagesList=userMessagesList;
        this.chatType=chatType;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view=LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, view.getContext());
        rootRef=FirebaseDatabaseInstance.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {

        messages=userMessagesList.get(position);

        final String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();

        if (chatType.equals(Const.personalChat)) {
            messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        } else {
            messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
            messageViewHolder.receiver_name.setVisibility(View.VISIBLE);

            usersRef=rootRef.getUserRef().child(fromUserID).child(Const.USER_DETAILS);
            final String[] receiverImage=new String[1];
            usersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String receiverName=dataSnapshot.child(Const.USER_NAME).getValue().toString();
                    messageViewHolder.receiver_name.setText(receiverName);

                    if (dataSnapshot.hasChild("image_url")) {
                        receiverImage[0]=dataSnapshot.child("image_url").getValue().toString();
                        Picasso.get().load(receiverImage[0]).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);

                        messageViewHolder.receiverProfileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                enlargeImage(receiverImage[0], v);

                            }
                        });

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        messageViewHolder.receiverlayout.setVisibility(View.GONE);
        messageViewHolder.senderlayout.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.download_image_receiver.setVisibility(View.GONE);

        messageViewHolder.senderLayoutPdf.setVisibility(View.GONE);
        messageViewHolder.receiverLayoutPdf.setVisibility(View.GONE);

        messageViewHolder.senderMessageText.setTextIsSelectable(true);
        messageViewHolder.receiverMessageText.setTextIsSelectable(true);

        if (messages.isSeen()) {
            messageViewHolder.isSeen.setText("Seen");
        }
        if (fromMessageType.equals("text")) {

            if (fromUserID.equals(currentUserId)) {
                messageViewHolder.receiver_name.setVisibility(View.GONE);
                messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                messageViewHolder.senderlayout.setVisibility(View.VISIBLE);
                messageViewHolder.senderDate.setText(messages.getTime() + " - " + messages.getDate());
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            } else {
                messageViewHolder.receiverlayout.setVisibility(View.VISIBLE);
                messageViewHolder.receiverDate.setText(messages.getTime() + " - " + messages.getDate());
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(currentUserId)) {
                messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                messageViewHolder.receiver_name.setVisibility(View.GONE);
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(String.valueOf(messages.getMessage()))
                        .into(messageViewHolder.messageSenderPicture);
                Log.d("messageAdapter", "sender-----------" + String.valueOf(messages.getMessage()));
            } else {
                //messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.download_image_receiver.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(String.valueOf(messages.getMessage()))
                        .into(messageViewHolder.messageReceiverPicture);
                Log.d("messageAdapter", String.valueOf(messages.getMessage()));
            }
        } else if (fromMessageType.equals("pdf")) {

            if (fromUserID.equals(currentUserId)) {
                messageViewHolder.receiver_name.setVisibility(View.GONE);
                messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                messageViewHolder.senderLayoutPdf.setVisibility(View.VISIBLE);
                messageViewHolder.senderLayoutPdf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri=Uri.parse(messages.getMessage()); // missing 'http://' will cause crashed
                       /*
                       // this witll redirect user to browser downloads and will download the pdf
                       Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        v.getContext().startActivity(intent); */

                        // this will open pdf in default pdf viewr of mobile
                        openPdf(uri, v);

                    }
                });
            } else {
                // messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverLayoutPdf.setVisibility(View.VISIBLE);
            }

        }
        messageViewHolder.messageSenderPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enlargeImage(String.valueOf(messages.getMessage()), v);

            }
        });
        messageViewHolder.messageReceiverPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enlargeImage(String.valueOf(messages.getMessage()), v);

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
                //messageViewHolder.checkBox_add.setVisibility(View.VISIBLE);
                if (fromUserID.equals(currentUserId)) {
                    openAlertBuilderWithOptions(new CharSequence[]{"Delete for me", "Delete for all"}, v, position);
                } else {
                    openAlertBuilderWithOptions(new CharSequence[]{"Delete for me"}, v, position);
                }

                return true;
            }
        });

    }

    private void openPdf(Uri uri, View v) {
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent in=Intent.createChooser(intent, "open file");
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        v.getContext().startActivity(in);
    }

    private void enlargeImage(String s, View v) {
        Intent fullScreenIntent=new Intent(v.getContext(), EnlargedImage.class);
        fullScreenIntent.putExtra(Const.IMAGE_URL, s);
        v.getContext().startActivity(fullScreenIntent);
    }

    private void openAlertBuilderWithOptions(CharSequence[] optionsGet, final View v, final int position) {
        CharSequence options[]=optionsGet;

        AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
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
        final Message messages=userMessagesList.get(position);
        String toUserId=messages.getTo();
        String fromUserID=messages.getFrom();

        personalChatRefFrom=rootRef.getMessagesRef().child(fromUserID).child(toUserId);
        personalChatRefTo=rootRef.getMessagesRef().child(toUserId).child(fromUserID);

        if (deleteScope.equals("deleteForMe")) {
            personalChatRefFrom.child(messages.getMessageID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(v.getContext(), "message deleted", Toast.LENGTH_SHORT).show();
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        } else if (deleteScope.equals("deleteForAll")) {
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
        Message messages=userMessagesList.get(position);
        groupChatRef=rootRef.getGroupChatRef();

        if (deleteScope.equals("deleteForMe")) {

            userMessagesList.remove(position);
            notifyItemRemoved(position);

        } else if (deleteScope.equals("deleteForAll")) {
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
        Bitmap bitmap=((BitmapDrawable) messageViewHolder.messageReceiverPicture.getDrawable()).getBitmap();
        String time=new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());
        File path=Environment.getExternalStorageDirectory();
        File dir=new File(path + "/DCIM/ClicksDigitalMedia/ClicksDigitalImages");
        dir.mkdirs();
        String imageName=time + ".PNG";
        File file=new File(dir, imageName);
        OutputStream out;
        try {
            out=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(v.getContext(), "Image is saved in DCIM/ClicksDigitalMedia/ClicksDigitalImages", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText, senderDate, receiverDate, receiver_name, isSeen;
        public CircleImageView receiverProfileImage;

        public ImageView messageSenderPicture, messageReceiverPicture, download_image_receiver, download_pdf_receiver;
        LinearLayout receiverlayout, senderlayout, receiverLayoutPdf, senderLayoutPdf;
        // CheckBox checkBox_add;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverlayout=itemView.findViewById(R.id.receiver_message_layout);
            senderlayout=itemView.findViewById(R.id.sender_message_layout);
            senderDate=itemView.findViewById(R.id.sender_messsage_date_time);
            receiverDate=itemView.findViewById(R.id.receiver_message_date_time);
            receiver_name=itemView.findViewById(R.id.receiver_name);
            senderMessageText=(TextView) itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText=(TextView) itemView.findViewById(R.id.receiver_message_text);

            receiverProfileImage=(CircleImageView) itemView.findViewById(R.id.message_profile_image);

            download_image_receiver=itemView.findViewById(R.id.download_image_receiver);
            messageReceiverPicture=itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture=itemView.findViewById(R.id.message_sender_image_view);

            receiverLayoutPdf=itemView.findViewById(R.id.layout_recevier_pdf);
            senderLayoutPdf=itemView.findViewById(R.id.layout_sender_pdf);
            download_pdf_receiver=itemView.findViewById(R.id.download_pdf);

            //checkBox_add = itemView.findViewById(R.id.checkbox_add);
            isSeen=itemView.findViewById(R.id.isSeen);
            isSeen.setText("deliverd");
        }
    }
}
