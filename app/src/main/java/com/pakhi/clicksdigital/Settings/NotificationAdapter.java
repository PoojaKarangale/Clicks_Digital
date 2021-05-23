package com.pakhi.clicksdigital.Settings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.ArrayList;

class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationAdapterViewHolder> {

    ArrayList<String> notificationList;
    Context context;
    FirebaseDatabaseInstance rootRef;

    public NotificationAdapter(Context context, ArrayList<String> notificationList){
        this.context=context;
        this.notificationList=notificationList;
    }

    @NonNull
    @Override
    public NotificationAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        rootRef= FirebaseDatabaseInstance.getInstance();

        View view= LayoutInflater.from(context)
                .inflate(R.layout.notification_layout, parent, false);
        return new NotificationAdapter.NotificationAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationAdapterViewHolder holder, int position) {
        String notification = notificationList.get(position);
        Log.i("notification", notification);

        rootRef.getNotificationRef().child(notification).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type=snapshot.child(ConstFirebase.typeOfNotification).getValue().toString();
                Log.i("type---", type);
                switch (type){
                    case "eventPhoto":
                        holder.headingOfNotification.setText("New Photo added in event");
                        //holder.boldName.setText(getName(snapshot));
                        getName(snapshot, holder);
                        holder.description.setText(" has added a photo in event ");
                        //holder.boldTypeCred.setText(getEventName(snapshot));
                        getEventName(snapshot,holder);
                        break;

                    case "editEvent":
                        //Log.i("name ----", getName(snapshot));
                        holder.headingOfNotification.setText("Changes in an Event");
                        getName(snapshot, holder);
                        //holder.boldName.setText(getName(snapshot, holder));
                        holder.description.setText(" has made changes in the event ");
                        //holder.boldTypeCred.setText(getEventName(snapshot, holder));
                        getEventName(snapshot,holder);
                        break;

                    case "createEvent":
                        Log.i("create event", type);
                        holder.headingOfNotification.setText("An Event has been created");
                        //holder.boldName.setText(getName(snapshot, holder));
                        getEventName(snapshot,holder);
                        holder.description.setText(" has created the event ");
                        getName(snapshot,holder);
                        break;

                    case "topicLike":
                        holder.headingOfNotification.setText("Your topic has been liked");
                        //holder.boldName.setText(getName(snapshot, holder));
                        holder.description.setText(" has liked a topic you have created ");
                        //holder.boldTypeCred.setText(getTopic(snapshot));
                        getName(snapshot,holder);
                        getTopic(snapshot,holder);
                        break;

                    case "topicReply":
                        holder.headingOfNotification.setText("A reply to your topic");
                        //holder.boldName.setText(getName(snapshot, holder));
                        holder.description.setText(" has replied to a topic you have created -");
                        //holder.boldTypeCred.setText(getTopic(snapshot));
                        getName(snapshot,holder);
                        getTopic(snapshot,holder);


                        break;
                    case "profileRequest":
                        holder.headingOfNotification.setText("A profile verification request");
                        //holder.boldName.setText(getName(snapshot, holder));
                        holder.description.setText(" has sent you his profile for verification");
                        getName(snapshot,holder);
                        //getEventName(snapshot,holder);


                        break;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String getTopic(final DataSnapshot snapshot, final NotificationAdapterViewHolder holder) {
        final String topic[] = new String[1];
        //topic[0]=null;
        final String topicName[] = new String[1];
        //topicName[0]=null;
        rootRef.getTopicRef().child(snapshot.child(ConstFirebase.goToNotificationId).getValue().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapy) {
                topic[0] = snapy.getValue().toString();
                rootRef.getGroupChatRef().child(topic[0]).child(snapshot.child(ConstFirebase.goToNotificationId).getValue().toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapy) {
                        topicName[0]=snapy.child(ConstFirebase.message).getValue().toString();
                        holder.notificationCredentialName.setText(topicName[0]);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return topicName[0];
    }

    private String getEventName(DataSnapshot snapshot, final NotificationAdapterViewHolder holder) {
        final String eventName[] = new String[1];
        //eventName[0]=null;
        rootRef.getEventRef().child(snapshot.child(ConstFirebase.goToNotificationId).getValue().toString()).child(ConstFirebase.EventDetails).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapy) {
                eventName[0]=snapy.child(ConstFirebase.eventName1).getValue().toString();
                holder.notificationCredentialName.setText(eventName[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return eventName[0];
    }

    private String getName(DataSnapshot snapshot, final NotificationAdapterViewHolder holder) {
        final String[] name = new String[1];
        //name[0]=null;
        rootRef.getUserRef().child(snapshot.child(ConstFirebase.notificationFrom).getValue().toString()).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapy) {
                Log.i("name--",snapy.child(ConstFirebase.USER_NAME).getValue().toString());
                name[0] = snapy.child(ConstFirebase.USER_NAME).getValue().toString()+" "+snapy.child(ConstFirebase.LAST_NAME).getValue().toString();
                holder.nameOfNotificationSender.setText(name[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Log.i("return---",name[0]);

        return name[0];
    }

    private void addPhoto(DataSnapshot snapshot) {

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationAdapterViewHolder extends RecyclerView.ViewHolder{
        TextView headingOfNotification, nameOfNotificationSender, notificationCredentialName, description;

        public NotificationAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            headingOfNotification = itemView.findViewById(R.id.heading);
            nameOfNotificationSender = itemView.findViewById(R.id.bold_name);
            notificationCredentialName = itemView.findViewById(R.id.bold_type_cred);
            description = itemView.findViewById(R.id.bakwas);
        }
    }
}
