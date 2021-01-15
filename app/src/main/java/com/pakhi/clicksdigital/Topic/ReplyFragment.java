package com.pakhi.clicksdigital.Topic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReplyFragment extends Fragment {

    View view;
    FirebaseDatabaseInstance rootRef;
    private ImageView close;
    private Button    reply;
    private TextView  topic_reply, name;
    private Message message;
    private DatabaseReference UsersRef, GroupMessageKeyRef, GroupIdRef, groupChatRefForCurrentGroup, replyRef;
    private SharedPreference pref;
    private String           currentUserID;

    public ReplyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_reply, container, false);

        Bundle bundle=this.getArguments();
        if (bundle != null)
            message=(Message) bundle.getSerializable("message");

        rootRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=rootRef.getUserRef();
        GroupIdRef=rootRef.getGroupRef().child(message.getTo());
        replyRef=rootRef.getReplyRef();

        pref=SharedPreference.getInstance();
        currentUserID=pref.getData(SharedPreference.currentUserId, getContext());

        initialiseFields();

        reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reply=topic_reply.getText().toString();
                if (!TextUtils.isEmpty(reply)) {
                    //do something with data
                    topic_reply.setText("");
                    addDataToDatabase("reply", reply);
                } else {

                }
            }
        });
        return view;
    }

    private void initialiseFields() {
        close=view.findViewById(R.id.close);
        reply=view.findViewById(R.id.reply_btn);
        name=view.findViewById(R.id.replyingTo);
        topic_reply=view.findViewById(R.id.topic_reply);
    }

    private void addDataToDatabase(String type, String reply) {
        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd, yyyy");
        String currentDate=currentDateFormat.format(calForDate.getTime());

        SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
        String currentTime=currentTimeFormat.format(calForDate.getTime());

        String replykEY=replyRef.child(message.getMessageID()).push().getKey();

        Message reply1=new Message(currentUserID, reply,
                type, message.getTo(), replykEY, currentTime, currentDate);

        replyRef.child(message.getMessageID()).child(replykEY).setValue(reply1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUi();
            }
        });
    }

    private void updateUi() {
        //send user to topic replies activity
        Intent i=new Intent(getContext(), TopicRepliesActivity.class);
        i.putExtra("message", message);
        startActivity(i);
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager fm=getFragmentManager();
            fm.popBackStack();
        }
    }
}