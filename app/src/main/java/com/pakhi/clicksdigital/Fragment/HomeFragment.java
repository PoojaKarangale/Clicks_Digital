package com.pakhi.clicksdigital.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemChangeListener;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.GroupChat.TopicRepliesActivity;
import com.pakhi.clicksdigital.Model.GroupTopic;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    Context context;
    DatabaseReference topicReference;
    ArrayList<String> arayOfTopicID = new ArrayList<>();
   // String publisherKey,currentUserID;
    RecyclerView display;
    Message message;
    FirebaseDatabase shortCut,shortCut2;
    SharedPreference pref;
    String messageType, messagekEY, currentTime,currentDate, messagePass, currentGroupId,publisher;
    ImageSlider imageSlider;
    FirebaseDatabaseInstance rootRef;

    public HomeFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        pref= SharedPreference.getInstance();
        imageSlider = homeView.findViewById(R.id.image_slider);
        final List<SlideModel> images = new ArrayList<>();

        rootRef=FirebaseDatabaseInstance.getInstance();
        rootRef.getsliderRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot mysnap : snapshot.getChildren()){
                    images.add(new SlideModel(mysnap.child("URL").getValue().toString(), mysnap.child("NameOfEvent").getValue().toString(), ScaleTypes.FIT));
                }
                imageSlider.setImageList(images, ScaleTypes.FIT);

                imageSlider.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onItemSelected(int i) {
                        EnlargedImage.enlargeImage(images.get(i).getImageUrl(), getContext());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        topicReference = FirebaseDatabase.getInstance().getReference().child("Topic");
        Log.i("topicReference", String.valueOf(topicReference));
        display = (RecyclerView) homeView.findViewById(R.id.display);
        display.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return homeView;
    }


    @Override
    public void onStart() {


        super.onStart();


        FirebaseRecyclerOptions<GroupTopic> options=
                new FirebaseRecyclerOptions.Builder<GroupTopic>()
                        .setQuery(topicReference, GroupTopic.class)
                        .build();

        FirebaseRecyclerAdapter<GroupTopic, HomeFragment.TopicDisplayHome> adapter
                = new FirebaseRecyclerAdapter<GroupTopic, TopicDisplayHome>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final TopicDisplayHome holder, final int position, @NonNull GroupTopic model) {


                Log.i("group id ------------", String.valueOf(getRef(position).getKey()));

                final String grpID = getRef(position).getKey();
                //GroupTopic groupTopic = (GroupTopic) getRef(position);
                Log.i("group id ------------", String.valueOf(grpID));
                topicReference.child(grpID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(final DataSnapshot mysnap : snapshot.getChildren()){

                            Log.i("topic id -----------", String.valueOf(mysnap.getKey()));

                            FirebaseDatabase.getInstance().getReference().child("TopicReply").child(mysnap.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        holder.NoOfReplies.setText(String.valueOf(snapshot.getChildrenCount()));
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            FirebaseDatabase.getInstance().getReference().child("Groups").child(grpID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    holder.groupName.setText(snapshot.child("group_name").getValue().toString());
                                    final String image_url=snapshot.child("image_url").getValue().toString();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            FirebaseDatabase.getInstance().getReference().child("GroupChat").child(grpID).child(mysnap.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    //Log.i("The message ", String.valueOf(snapshot.child("message").getValue().toString()));
                                    holder.topicText.setText(snapshot.child("message").getValue().toString());
                                    messageType = snapshot.child("type").getValue().toString();
                                    messagePass = snapshot.child("message").getValue().toString();
                                    messagekEY = snapshot.child("messageID").getValue().toString();
                                    currentTime = snapshot.child("date").getValue().toString();
                                    currentDate = snapshot.child("time").getValue().toString();
                                    currentGroupId = grpID;
                                    publisher = snapshot.child("from").getValue().toString();

                                    Log.i("The topic Text - ",String.valueOf(snapshot.child("message").getValue().toString()));
                                  //  publisherKey = snapshot.child("from").getKey();
                                    holder.dateAndTime.setText(snapshot.child("date").getValue().toString() + " " +  snapshot.child("time").getValue().toString());

                                    FirebaseDatabase.getInstance().getReference().child("Users").child(publisher).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            holder.publisherName.setText(snapshot.child("DETAILS").child("user_name").getValue().toString() + " " + snapshot.child("DETAILS").child("last_name").getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    holder.publisherName.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            visitUsersProfile(publisher);
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            arayOfTopicID.add(mysnap.getKey());

                            holder.replyButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Message message1=new Message(publisher, messagePass ,
                                            messageType, currentGroupId, messagekEY, currentTime, currentDate);

                                    Intent replyIntent = new Intent(getContext(), TopicRepliesActivity.class);
                                    replyIntent.putExtra("message", message1);
                                    startActivity(replyIntent);

                                }
                            });
                        }
                        Log.i("Length of arrayOfTopic", String.valueOf(arayOfTopicID.size()));



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





            }

            @NonNull
            @Override
            public TopicDisplayHome onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending_topic, parent, false);
                HomeFragment.TopicDisplayHome viewHolder=new HomeFragment.TopicDisplayHome(view);
                return new TopicDisplayHome(view);

            }
        };

        display.setAdapter(adapter);
        adapter.startListening();
    }

    private void visitUsersProfile(String userId) {
        Intent profileActivity=new Intent(getContext(), VisitProfileActivity.class);
        profileActivity.putExtra("visit_user_id", userId);
        getContext().startActivity(profileActivity);
    }

    public void backPressed() {
        new AlertDialog.Builder(getContext())

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    public class TopicDisplayHome extends RecyclerView.ViewHolder{

        TextView groupName, topicText, dateAndTime, NoOfReplies, publisherName, replyButton;
        public TopicDisplayHome(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.group_name);
            topicText = itemView.findViewById(R.id.topic);
            dateAndTime = itemView.findViewById(R.id.date_time);
            NoOfReplies = itemView.findViewById(R.id.no_of_replies);
            publisherName = itemView.findViewById(R.id.publisher_name);
            replyButton = itemView.findViewById(R.id.reply);


        }
    }
}