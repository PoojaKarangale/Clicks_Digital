package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Topic.TopicRepliesActivity;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.List;

public class HomePageTopicAdapter extends RecyclerView.Adapter<HomePageTopicAdapter.HomePageTopivViewHolder> {

    private Context       mcontext;
    private List<Message> trendingTopis;
    FirebaseDatabaseInstance rootRef;
    public HomePageTopicAdapter(Context mcontext, List<Message> trendingTopis) {
        this.mcontext=mcontext;
        this.trendingTopis=trendingTopis;
    }

    @NonNull
    @Override
    public HomePageTopicAdapter.HomePageTopivViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_trending_topic, parent, false);
        rootRef=FirebaseDatabaseInstance.getInstance();
        return new HomePageTopicAdapter.HomePageTopivViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomePageTopicAdapter.HomePageTopivViewHolder holder, int position) {
        final Message m=trendingTopis.get(position);

        holder.topicText.setText(m.getMessage());

        holder.replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToTopicReplyActivity(m);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToTopicReplyActivity(m);
            }
        });

        holder.dateAndTime.setText(m.getDate() + " " + m.getTime());
        rootRef.getUserRef().child(m.getFrom()).child("DETAILS").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.publisherName.setText(snapshot.child(ConstFirebase.USER_NAME).getValue() + " " + snapshot.child("last_name").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rootRef.getTopicLikesRef().child(m.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.noOfLikes.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rootRef.getReplyRef().child(m.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.NoOfReplies.setText(String.valueOf(snapshot.getChildrenCount()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rootRef.getGroupRef().child(m.getTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.groupName.setText(snapshot.child("group_name").getValue().toString());
                //   final String image_url=snapshot.child("image_url").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return trendingTopis.size();
    }

    public void sendUserToTopicReplyActivity(Message m){
        Intent replyIntent=new Intent(mcontext, TopicRepliesActivity.class);
        replyIntent.putExtra(ConstFirebase.message, m);
        mcontext.startActivity(replyIntent);

    }

    public class HomePageTopivViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, topicText, dateAndTime, NoOfReplies, publisherName, replyButton, likeButton, noOfLikes;

        public HomePageTopivViewHolder(View view) {
            super(view);
            groupName=itemView.findViewById(R.id.group_name);
            topicText=itemView.findViewById(R.id.topic);
            dateAndTime=itemView.findViewById(R.id.date_time);
            NoOfReplies=itemView.findViewById(R.id.no_of_replies);
            publisherName=itemView.findViewById(R.id.publisher_name);
            replyButton=itemView.findViewById(R.id.reply);
            likeButton=itemView.findViewById(R.id.likes);
            noOfLikes=itemView.findViewById(R.id.no_of_likes);
        }
    }
}
