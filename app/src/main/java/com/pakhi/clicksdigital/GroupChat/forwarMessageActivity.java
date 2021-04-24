package com.pakhi.clicksdigital.GroupChat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Message;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.ArrayList;
import java.util.List;

public class forwarMessageActivity extends AppCompatActivity {

    ArrayList<String> listOfGroupsAndUsers = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forwar_message);
        Message msg =(Message) getIntent().getSerializableExtra(Const.message);
        String chatType = getIntent().getStringExtra("chatType");
        SharedPreference pref = SharedPreference.getInstance();
        String currentUserID = pref.getData(SharedPreference.currentUserId, getApplicationContext());


        FirebaseDatabaseInstance rootRef = FirebaseDatabaseInstance.getInstance();
        rootRef.getUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOfGroupsAndUsers.add(snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        rootRef.getUserRef().child(currentUserID).child(ConstFirebase.groups1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        listOfGroupsAndUsers.add(snap.getKey());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        


    }
}
