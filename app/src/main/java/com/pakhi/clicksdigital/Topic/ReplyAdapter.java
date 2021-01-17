package com.pakhi.clicksdigital.Topic;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder> {
    String           currentUserId;
    SharedPreference pref;
    private Context                  mcontext;
    private List<Message>            replies;
    private FirebaseDatabaseInstance rootRef;
    private DatabaseReference        UsersRef;

    ReplyAdapter(Context mcontext, List<Message> replies) {
        this.mcontext=mcontext;
        this.replies=replies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_reply, parent, false);
        rootRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=rootRef.getUserRef();
        pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, mcontext);

        return new ReplyAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Message reply=replies.get(position);
        holder.replyText.setText(reply.getMessage());
        holder.date_time.setText(reply.getTime() + " " + reply.getDate());
        UsersRef.child(reply.getFrom()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Picasso.get()
                            .load(snapshot.child(ConstFirebase.IMAGE_URL).getValue(String.class))
                            .resize(120, 120)
                            .into(holder.profile_img);
                    holder.name.setText(snapshot.child(ConstFirebase.USER_NAME).getValue().toString());
                    holder.name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mcontext, VisitProfileActivity.class);
                            intent.putExtra(ConstFirebase.visitUser,reply.getFrom());
                            mcontext.startActivity(intent);
                        }
                    });

                    holder.profile_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EnlargedImage.enlargeImage(ConstFirebase.IMAGE_URL,mcontext);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final boolean[] isLiked={false};
        final DatabaseReference replyLikesRef=rootRef.getReplyLikesRef();
        replyLikesRef.child(reply.getMessageID()).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //topic is liked by user
                    isLiked[0]=true;
                    holder.like.setImageResource(R.drawable.liked);
                } else {
                    //topik is not liked by user
                    isLiked[0]=false;
                    holder.like.setImageResource(R.drawable.like_border);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        replyLikesRef.child(reply.getMessageID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.no_of_likes.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLiked[0]) {
                    isLiked[0]=false;
                    // dislike the topic make hart black
                    replyLikesRef.child(reply.getMessageID()).child(currentUserId).removeValue();
                    holder.like.setImageResource(R.drawable.like_border);
                } else {
                    isLiked[0]=true;
                    // like the topic reden the heart
                    replyLikesRef.child(reply.getMessageID()).child(currentUserId).setValue("");
                    holder.like.setImageResource(R.drawable.liked);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profile_img, like;
        private TextView name, replyText, date_time, no_of_likes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_img=itemView.findViewById(R.id.profile_img);
            name=itemView.findViewById(R.id.name);
            replyText=itemView.findViewById(R.id.reply);
            date_time=itemView.findViewById(R.id.date_time);
            like=itemView.findViewById(R.id.like);
            no_of_likes=itemView.findViewById(R.id.no_of_likes);
        }
    }
}