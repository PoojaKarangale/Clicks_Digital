package com.pakhi.clicksdigital.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.GroupChat.MessageAdapter;
import com.pakhi.clicksdigital.LoadImage;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.Topic.TopicRepliesActivity;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomePageTopicAdapter extends RecyclerView.Adapter<HomePageTopicAdapter.HomePageTopivViewHolder> {

    private Context       mcontext;
    private List<Message> trendingTopis;
    int i;
    FirebaseDatabaseInstance rootRef;
    SharedPreference pref;
    String currentUserID;
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

        //holder.topicText.setText(m.getMessage());
        //holder.separateURLText.setText(m.getExtra());
        String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
        Pattern p;
        Matcher ma = null;
        String[] words = m.getMessage().split(" ");
        i = 0;
        for (String word : words) {
            p = Pattern.compile(URL_REGEX);
            ma = p.matcher(word);

            if (ma.find()) {
                //Toast.makeText(, "The String contains URL", Toast.LENGTH_LONG).show();
                i = 1;
                break;
            }

        }
        holder.raisedImageHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog builder = new Dialog(mcontext);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //nothing;
                    }
                });

                ImageView imageView = new ImageView(mcontext);
                Glide.with(mcontext).
                        load(m.getMessage())
                        .transform(new CenterCrop(), new RoundedCorners(15))
                        .into(imageView);
                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                        800,
                        800));
                builder.show();

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mcontext, LoadImage.class);
                        intent.putExtra("image_url", m.getMessage());
                        mcontext.startActivity(intent);
                    }
                });


            }
        });
        if (i == 1 && m.getMessage().length()>113) {
            if( m.getMessage().substring(93,113).equals(m.getTo())){
                holder.topicText.setVisibility(View.GONE);
                holder.layOutURL.setVisibility(View.GONE);
                holder.raisedImageLayoutHome.setVisibility(View.VISIBLE);
                holder.raisedImageTextHome.setText(m.getExtra());

                Glide.with(mcontext).load(String.valueOf(m.getMessage())).
                        transform(new CenterCrop(), new RoundedCorners(10)).
                        into(holder.raisedImageHome);

            }
            else {
                holder.layOutURL.setVisibility(View.VISIBLE);
                holder.urlDesc.setVisibility(View.GONE);
                //holder.separateURLText.setText(m.getExtra());
                holder.topicText.setVisibility(View.GONE);
                new HomePageTopicAdapter.URLAsynk().execute(new Async(holder, m.getExtra()));

                holder.topicText.setVisibility(View.GONE);
                holder.layOutURL.setVisibility(View.VISIBLE);
                holder.urlText.setText(m.getMessage());
                holder.separateURLText.setText(m.getExtra());
                holder.raisedImageLayoutHome.setVisibility(View.GONE);

            }


            //  new URLAsynkTopic().execute(new AsyncTopic(messageViewHolder, message.getMessage()));
            //new MessageAdapter.URLAsynk().execute(new com.pakhi.clicksdigital.GroupChat.Async(messageViewHolder, message.getMessage(),VIEW_TYPE_TOPIC));

        }else if(i == 1 && m.getMessage().length()<=113){
            holder.layOutURL.setVisibility(View.VISIBLE);
            holder.urlDesc.setVisibility(View.GONE);

            //holder.separateURLText.setText(m.getExtra());
            holder.topicText.setVisibility(View.GONE);
            new HomePageTopicAdapter.URLAsynk().execute(new Async(holder, m.getExtra()));

            holder.topicText.setVisibility(View.GONE);
            holder.layOutURL.setVisibility(View.VISIBLE);
            holder.urlText.setText(m.getMessage());
            holder.raisedImageLayoutHome.setVisibility(View.GONE);
  //          holder.separateURLText.setText(m.getExtra());

        }
        else if(i==1 && m.getMessage().substring(93,113).equals(m.getTo())) {
            holder.topicText.setVisibility(View.GONE);
            holder.layOutURL.setVisibility(View.GONE);
            holder.raisedImageLayoutHome.setVisibility(View.VISIBLE);
            holder.raisedImageTextHome.setText(m.getExtra());
            Glide.with(mcontext).load(String.valueOf(m.getMessage())).
                    transform(new CenterCrop(), new RoundedCorners(10)).
                    into(holder.raisedImageHome);


        }
        else {
            holder.layOutURL.setVisibility(View.GONE);
            holder.raisedImageLayoutHome.setVisibility(View.GONE);
            holder.topicText.setVisibility(View.VISIBLE);
            holder.topicText.setText(m.getMessage());
        }


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
        rootRef.getUserRef().child(m.getFrom()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.publisherName.setText(snapshot.child(ConstFirebase.USER_NAME).getValue() + " " + snapshot.child(ConstFirebase.last_name).getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.publisherName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pref = SharedPreference.getInstance();
                currentUserID = pref.getData(SharedPreference.currentUserId, mcontext);

                Intent intent = new Intent(mcontext, VisitProfileActivity.class);
                intent.putExtra(Const.visitUser, m.getFrom());
                mcontext.startActivity(intent);
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
                holder.groupName.setText(snapshot.child(ConstFirebase.GROUP_NAME).getValue().toString());
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
        replyIntent.putExtra(Const.message, m);
        mcontext.startActivity(replyIntent);
    }

    public class HomePageTopivViewHolder extends RecyclerView.ViewHolder {
        TextView groupName, topicText, dateAndTime, NoOfReplies, publisherName, replyButton, likeButton, noOfLikes, urlText, urlTitle, urlDesc, raisedImageTextHome;
        ImageView urlImage, raisedImageHome;
        LinearLayout layOutURL, raisedImageLayoutHome;
        TextView separateURLText;


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

            urlText = itemView.findViewById(R.id.url_text);
            urlTitle = itemView.findViewById(R.id.title_of_url_sender);
            urlDesc = itemView.findViewById(R.id.desc_of_url_sender);
            urlImage = itemView.findViewById(R.id.url_image_sender);
            layOutURL = itemView.findViewById(R.id.layout_url_sender);
            //separateURLText = itemView.findViewById(R.id.separate_url_trend);

            // IMAGE
            raisedImageLayoutHome= itemView.findViewById(R.id.raised_image_layout_home);
            raisedImageHome = itemView.findViewById(R.id.raised_image_home);
            raisedImageTextHome = itemView.findViewById(R.id.raised_image_text_home);



        }
    }
    public class URLAsynk extends AsyncTask<Async, String, WebUrl1> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(WebUrl1 output) {
            super.onPostExecute(output);
            //output.viewHolder.layOutURL.setVisibility(View.VISIBLE);
            output.viewHolder.urlTitle.setText(output.title);
            //output.viewHolder.urlDesc.setText(output.description);
            //output.viewHolder.urlText.setText(output.description);

            if (!TextUtils.isEmpty(output.imageUrl.toString())) {
                Glide.with(mcontext).load(output.imageUrl).transform(new CenterCrop(), new RoundedCorners(10)).into((output.viewHolder).urlImage);
                //Picasso.get().load(output.imageUrl).into((output.viewHolder).urlImage);
            } else (output.viewHolder).urlImage.setVisibility(View.GONE);


            //output.viewHolder.separateURLText.setText(output.);
            /*switch (output.view_type) {
                case VIEW_TYPE_ME:
                    setupSelfUrlMessage(output);
                    break;
                case VIEW_TYPE_OTHER:
                    setupOtherUrlMessage(output);
                    break;
                case VIEW_TYPE_TOPIC:
                    setupTopicUrlMessage(output);
                    break;
            }*/

        }

        @Override
        protected WebUrl1 doInBackground(Async... object) {
            String value = null;
            Document doc = null;
            WebUrl1 webUrlObj = null;
            try {
                doc = Jsoup.connect(object[0].urlMessage).get();
                //value = doc.title();
            } catch (Exception e) {
                //value="No Title";
                e.printStackTrace();
            }
            value = doc.title();
            Log.i("Value of Title - ", value);
            //String description =doc.select("meta[name=description]").get(0).attr("content");// ;
            //Log.i("Value of desc - ", description);
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
            webUrlObj = new WebUrl1(object[0].viewHolder, value.toString(), imageUrl.toString());

            return webUrlObj;
        }
    }


}
class WebUrl1 {
    String title, description, imageUrl;
    // MessageAdapter.MessageViewHolder viewHolder;
    HomePageTopicAdapter.HomePageTopivViewHolder viewHolder;
    //int view_type;

    WebUrl1(HomePageTopicAdapter.HomePageTopivViewHolder viewHolder, String title, String imageUrl) {
        this.title = title;
        //this.description = description;
        this.imageUrl = imageUrl;
        this.viewHolder = viewHolder;
        //this.view_type=view_type;

    }
}

class Async {
    String urlMessage;
    // MessageAdapter.MessageViewHolder viewHolder;
    HomePageTopicAdapter.HomePageTopivViewHolder viewHolder;
    int view_type;
    Async(HomePageTopicAdapter.HomePageTopivViewHolder viewHolder, String urlMessage) {
        this.viewHolder = viewHolder;
        this.urlMessage = urlMessage;
        //this.view_type=view_type;
    }
}