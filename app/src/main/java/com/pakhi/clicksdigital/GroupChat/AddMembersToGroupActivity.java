package com.pakhi.clicksdigital.GroupChat;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.ArrayList;
import java.util.List;

public class AddMembersToGroupActivity extends AppCompatActivity {
    private AddMembersToGroupAdapter addMembersToGroupAdapter;
    private DatabaseReference        userRef, RootRef, groupRef;
    private String     groupId;
    private List<User> allUsers;
    private ImageView  send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members_to_group);

        FirebaseDatabaseInstance rootRef=FirebaseDatabaseInstance.getInstance();
        userRef=rootRef.getUserRef();
        groupRef=rootRef.getGroupRef();

        groupId=getIntent().getStringExtra("current_group_id");

        send=findViewById(R.id.send);

        RecyclerView findFriendsRecyclerList=(RecyclerView) findViewById(R.id.add_members_recycler_list);
        findFriendsRecyclerList.setHasFixedSize(true);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(AddMembersToGroupActivity.this));

        allUsers=new ArrayList<>();

        addMembersToGroupAdapter=new AddMembersToGroupAdapter(this, allUsers);
        findFriendsRecyclerList.setAdapter(addMembersToGroupAdapter);
        readAllMembers();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (User user : allUsers) {
                    if (user.getSelected()) {
                        groupRef.child(groupId).child("Users").child(user.getUser_id()).setValue("");
                        userRef.child(user.getUser_id()).child("groups").child(groupId).setValue("");
                        user.setSelected(false);
                    }
                }
                finish();
            }
        });
    }

    private void readAllMembers() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    allUsers.add(snapshot.child(Const.USER_DETAILS).getValue(User.class));
                }
                addMembersToGroupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
