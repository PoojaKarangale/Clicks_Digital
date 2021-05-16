package com.pakhi.clicksdigital.Topic;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
//import com.pakhi.clicksdigital.Adapter.WebUrl1;
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.Notification;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicRepliesActivity extends AppCompatActivity {

    Message   topic;
    ImageView profile_img, like;
    TextView name, profession, topic_detail, date_time, reply, no_of_replies, no_of_likes;
    RecyclerView     replies_list;
    List<Message>    replies;
    ReplyAdapter     replyAdapter;
    String           currentUserId;
    SharedPreference pref;
    private DatabaseReference UsersRef, replyRef;
    private FirebaseDatabaseInstance rootRef;
    ImageView topicImage;
    LinearLayout topicImageLayout;
    TextView topicImageTextView;
    int i;
    TextView text, separateUrl, title;
    ImageView image;
    ImageButton replyButton;
    EditText topic_reply;
    ImageView crossTopic;
    boolean notify=false;
    String rep;
    public String grpName;
    public String nameOfSender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_replies);
        topic=(Message) getIntent().getSerializableExtra(Const.message);

        rootRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=rootRef.getUserRef();
        replyRef=rootRef.getReplyRef();
        rootRef.getGroupRef().child(topic.getTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                grpName = snapshot.child(ConstFirebase.group_name).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*topicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnlargedImage.enlargeImage(topic.getMessage(), TopicRepliesActivity.this);
            }
        });*/

        pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());
        rootRef.getUserRef().child(currentUserId).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nameOfSender=snapshot.child(ConstFirebase.USER_NAME).getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        initialiseFields();
        loadData();
        readReplies();
        crossTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topic_reply.setVisibility(View.GONE);
                replyButton.setVisibility(View.GONE);
                crossTopic.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(topic_reply.getWindowToken(), 0);

            }
        });

        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                topic_reply.setVisibility(View.VISIBLE);
                replyButton.setVisibility(View.VISIBLE);
                //crossTopic.setVisibility(View.VISIBLE);
                topic_reply.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(topic_reply, InputMethodManager.SHOW_IMPLICIT);

                // Notification.sendPersonalNotifiaction(currentUserId, );
                /*Bundle bundle=new Bundle();
                bundle.putSerializable(Const.message, (Serializable) topic);

                ReplyFragment fragment=new ReplyFragment();
                fragment.setArguments(bundle);

                AppCompatActivity activity=(AppCompatActivity) v.getContext();
                FragmentTransaction transaction=activity.getSupportFragmentManager()
                        .beginTransaction();
                // transaction.addToBackStack(null);
                transaction.add(R.id.fragmentContainer, fragment, "TAG_FRAGMENT");
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();*/
            }
        });
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reply=topic_reply.getText().toString();
                if (!TextUtils.isEmpty(reply)) {
                    //do something with data
                    rep = topic_reply.getText().toString();
                    topic_reply.setText("");
                    addDataToDatabase("reply", reply);
                } else {
                    Toast.makeText(TopicRepliesActivity.this, "Please type something", Toast.LENGTH_SHORT).show();
                }
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(topic_reply.getWindowToken(), 0);

                notify=true;
                Log.i("notification -----", topic.getTo());

                rootRef.getGroupRef().child(topic.getTo()).child(ConstFirebase.users).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot snap : snapshot.getChildren()){
                            if(notify&& !snap.getKey().equals(currentUserId)){


                                Notification.sendPersonalNotifiaction(topic.getTo(), snap.getKey(), nameOfSender+" has replied to Dialog topic: "+topic.getMessage().substring(0, 12)+"...", /*title*/ grpName  , "topic", topic.getMessageID());
                                //rootRef.getNotificationRefTopicReply().child(snap.getKey()).child(topic.getMessageID()).setValue("");
                                String notificationKey = rootRef.getNotificationRef().push().getKey();

                                rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationRecieverID).setValue(topic.getFrom());
                                rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationFrom).setValue(currentUserId);
                                rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.goToNotificationId).setValue(topic.getMessageID());
                                rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.typeOfNotification).setValue("topicReply");


                            }

                        }
                        notify=false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


        final boolean[] isLiked={false};
        final DatabaseReference topicLikesRef=rootRef.getTopicLikesRef();
        topicLikesRef.child(topic.getMessageID()).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //topic is liked by user
                    isLiked[0]=true;
                    like.setImageResource(R.drawable.liked);
                } else {
                    //topik is not liked by user
                    isLiked[0]=false;
                    like.setImageResource(R.drawable.like_border);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        topicLikesRef.child(topic.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    no_of_likes.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLiked[0]) {
                    isLiked[0]=false;
                    // dislike the topic make hart black
                    topicLikesRef.child(topic.getMessageID()).child(currentUserId).removeValue();
                    like.setImageResource(R.drawable.like_border);
                    rootRef.getNotificationRef().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot snap : snapshot.getChildren()){
                                if(snap.child("go").getValue().toString().equals(topic.getMessageID())){
                                    if(snap.child("from").getValue().toString().equals(currentUserId)){
                                        rootRef.getNotificationRef().child(snap.getKey()).removeValue();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    isLiked[0]=true;
                    // like the topic reden the heart
                    topicLikesRef.child(topic.getMessageID()).child(currentUserId).setValue("");

                    like.setImageResource(R.drawable.liked);

                    addNotifications(topic);
                }
            }
        });
    }

    private void addNotifications(final Message topic) {
        final String[] name = new String[1];
        final String[] grpName = new String[1];

        rootRef.getUserRef().child(currentUserId).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rootRef.getGroupRef().child(topic.getTo()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        grpName[0]=snapshot.child(ConstFirebase.group_name).getValue().toString();
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
        if(!currentUserId.equals(topic.getFrom())){
            Notification.sendPersonalNotifiaction(topic.getTo(), topic.getFrom(), s +" has liked your Dialog topic" + topic.getMessage(), s2, "topic", topic.getMessageID());
            String notificationKey = rootRef.getNotificationRef().push().getKey();

            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationRecieverID).setValue(topic.getFrom());
            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.notificationFrom).setValue(currentUserId);
            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.goToNotificationId).setValue(topic.getMessageID());
            rootRef.getNotificationRef().child(notificationKey).child(ConstFirebase.typeOfNotification).setValue("topicLike");


            //rootRef.getNotificationRefTopicLike().child(message.getFrom()).child(message.getMessageID()).setValue("");
        }

    }


    private void addDataToDatabase(String type, String reply) {
        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
        String currentDate=currentDateFormat.format(calForDate.getTime());

        SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
        String currentTime=currentTimeFormat.format(calForDate.getTime());

        String replykEY=replyRef.child(topic.getMessageID()).push().getKey();

        Message reply1=new Message(currentUserId, reply,
                type, topic.getTo(), replykEY, currentTime, currentDate);

        replyRef.child(topic.getMessageID()).child(replykEY).setValue(reply1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }


    private void initialiseFields() {
        profile_img=findViewById(R.id.profile_img);
        name=findViewById(R.id.name);
        profession=findViewById(R.id.profession);
        topic_detail=findViewById(R.id.topic);
        date_time=findViewById(R.id.date_time);
        reply=findViewById(R.id.reply);
        no_of_replies=findViewById(R.id.no_of_replies);
        like=findViewById(R.id.like);
        no_of_likes=findViewById(R.id.no_of_likes);

        //IMAGE
        topicImageLayout = findViewById(R.id.reply_image_layout);
        topicImage = findViewById(R.id.reply_image);
        topicImageTextView = findViewById(R.id.reply_text);

        //URL
        image = findViewById(R.id.reply_image_url);
        title = findViewById(R.id.reply_title_url);
        text = findViewById(R.id.reply_url_text);
        separateUrl = findViewById(R.id.reply_separate_url);

        //REPLY
        topic_reply = findViewById(R.id.reply_to_topic);
        replyButton = findViewById(R.id.reply_button);
        crossTopic = findViewById(R.id.cross_reply);



        replies_list=findViewById(R.id.replies_list);
        replies_list.setHasFixedSize(true);
        replies_list.setLayoutManager(new LinearLayoutManager(this));

        replies=new ArrayList<>();

        replyAdapter=new ReplyAdapter(this, replies);
        replies_list.setAdapter(replyAdapter);

    }

    private void loadData() {
        String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern p;
        Matcher ma = null;
        String[] words = topic.getMessage().split(" ");

        i = 0;
        p = Pattern.compile(URL_REGEX);
        ma = p.matcher(topic.getExtra());

        if(ma.find()){
            i=1;
        }
        if(i==1){
            topic_detail.setVisibility(View.GONE);
            text.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
            separateUrl.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);

            text.setText(topic.getMessage());
            separateUrl.setText(topic.getExtra());

            new URL().execute(topic.getExtra());
        }
        if(topic.getMessage().length()>113){
            if(topic.getMessage().substring(93,113).equals(topic.getTo())){

                topic_detail.setVisibility(View.GONE);
                topicImage.setVisibility(View.VISIBLE);
                topicImageTextView.setVisibility(View.VISIBLE);


                //topicImageLayout.setVisibility(View.VISIBLE);
                topicImageTextView.setText(topic.getExtra());
                Glide.with(getApplicationContext())
                        .load(String.valueOf(topic.getMessage())).
                        transform(new CenterCrop(), new RoundedCorners(12))
                        .into(topicImage);

                //date_time.setLayoutParams();
            }

            else {
                topic_detail.setText(topic.getMessage());

            }
        }
        else {
            topic_detail.setText(topic.getMessage());
        }

        topicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TopicRepliesActivity.this, LoadImage.class);
                intent.putExtra("image_url", topic.getMessage());
                startActivity(intent);
            }
        });

        UsersRef.child(topic.getFrom()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Glide.with(getApplicationContext())
                            .load(snapshot.child(ConstFirebase.IMAGE_URL).getValue(String.class))
                            .transform(new CenterCrop(), new RoundedCorners(10))
                            .into(profile_img);
                    String n = snapshot.child(ConstFirebase.USER_NAME).getValue().toString()
                            +" " + snapshot.child(ConstFirebase.last_name).getValue().toString();
                    name.setText(n);
                    String p = snapshot.child("work_profession").getValue().toString()
                            +", "+snapshot.child("company").getValue().toString();
                    profession.setText(p);

                    name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rootRef.getBlockRef().child(topic.getFrom()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.child(currentUserId).exists()){
                                        Toast.makeText(getApplicationContext(), "You can't visit the Profile of this person", Toast.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(getApplicationContext(), VisitProfileActivity.class);
                                        intent.putExtra(Const.visitUser, topic.getFrom());
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        date_time.setText(topic.getTime() + " " + topic.getDate());
        topic_detail.setText(topic.getMessage());


    }

    private void readReplies() {

      /*  FirebaseRecyclerOptions<Message> options
                = new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(replyRef.child(topic.getMessageID()), Message.class)
                .build();*/

        replyRef.child(topic.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                replies.clear();
                if (snapshot.exists()) {
                    no_of_replies.setText(String.valueOf(snapshot.getChildrenCount()));
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Message reply1=dataSnapshot.getValue(Message.class);
                        replies.add(reply1);
                    }
                }
                replyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }
    public class URL extends AsyncTask<String, String, web>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(web s) {
            super.onPostExecute(s);

            title.setText(s.title);
            Glide.with(getApplicationContext())
                    .load(String.valueOf(s.imageURL)).transform(new CenterCrop(), new RoundedCorners(12))
                    .into(image);
        }

        @Override
        protected web doInBackground(String... strings) {
            String value = null;
            Document doc = null;
            web webUrlObj = null;
            try {
                doc = Jsoup.connect(strings[0]).get();
                //value = doc.title();
            } catch (Exception e) {
                //value="No Title";
                e.printStackTrace();
            }
            value = doc.title();
            Log.i("Value of Title - ", value);
            //String description =doc.select("meta[name=description]").get(0).attr("content");// ;
           // Log.i("Value of desc - ", description);
            String imageUrl = "";
            try {
                 /*description = doc.select("meta[name=description]").get(0).attr("content");
                Log.i("Value of desc - ", description);
*/


                if (!doc.select("meta[property=og:image]").get(0).attr("content").isEmpty()) {
                    imageUrl = doc.select("meta[property=og:image]").get(0).attr("content");
                    Log.i("Image URL - ", imageUrl);
                }


            } catch (Exception e) {
                //description="";
                e.printStackTrace();
            }
            webUrlObj = new web( value.toString(), imageUrl.toString());

            return webUrlObj;

        }
    }
}
class web{
    String  title, imageURL;
    web( String title, String imageURL){
        this.title=title;
        this.imageURL=imageURL;
    }
}
