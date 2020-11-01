package com.pakhi.clicksdigital.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseInstance {
    private static FirebaseDatabaseInstance single_instance=null;
    DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
    /*  DatabaseReference userRef;
            DatabaseReference groupRef ;
            DatabaseReference eventRef ;
            DatabaseReference eventCatRef ;
            DatabaseReference groupChatRef ;
            DatabaseReference messagesRef ;
            DatabaseReference messagesListRef;
            DatabaseReference tokensRef ;
            DatabaseReference groupRequests ;
            DatabaseReference chatRequestsRef;*/
    //DatabaseReference replyRef;
    // DatabaseReference topicLikesRef, replyLikesRef;

    private FirebaseDatabaseInstance() {
    }

    // static method to create instance of Singleton class
    public static FirebaseDatabaseInstance getInstance() {
        if (single_instance == null)
            single_instance=new FirebaseDatabaseInstance();
        return single_instance;
    }

    public DatabaseReference getTopicLikesRef() {
        return rootRef.child(ConstFirebase.topicLikesRef);
    }

    public DatabaseReference getReplyLikesRef() {
        return rootRef.child(ConstFirebase.replyLikesRef);
    }

    public DatabaseReference getReplyRef() {
        return rootRef.getRef().child(ConstFirebase.replyRef);
    }

    public DatabaseReference getChatRequestsRef() {
        return rootRef.child(ConstFirebase.chatRequests);
    }

    public DatabaseReference getGroupRequests() {
        return rootRef.child(ConstFirebase.groupRequests);
    }

    public DatabaseReference getUserRequestsRef() {
        return rootRef.child(ConstFirebase.userRequests);
    }

    public DatabaseReference getUserRef() {
        return rootRef.child(ConstFirebase.users);
    }

    public DatabaseReference getGroupRef() {
        return rootRef.child(ConstFirebase.groups);
    }

    public DatabaseReference getEventRef() {
        return rootRef.child(ConstFirebase.events);
    }

    public DatabaseReference getEventCatRef() {
        return rootRef.child(ConstFirebase.eventCategory);
    }

    public DatabaseReference getGroupChatRef() {
        return rootRef.child(ConstFirebase.groupChat);
    }

    public DatabaseReference getMessagesRef() {
        return rootRef.child(ConstFirebase.messages);
    }

    public DatabaseReference getMessagesListRef() {
        return rootRef.child(ConstFirebase.messagesList);
    }

    public DatabaseReference getTokensRef() {
        return rootRef.child(ConstFirebase.tokens);
    }

    public DatabaseReference getRootRef() {
        return rootRef;
    }

    public DatabaseReference getContactRef() {
        return rootRef.child(ConstFirebase.contacts);
    }
}
