package com.pakhi.clicksdigital;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.JoinGroupAdapter;
import com.pakhi.clicksdigital.Model.GroupChat;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    private FloatingActionButton fab_create_group;
    private RecyclerView recyclerView;
    private JoinGroupAdapter groupAdapter;
    private List<GroupChat> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        fab_create_group = findViewById(R.id.fab_create_group);
        fab_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewGroup();
            }
        });


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groups = new ArrayList<>();

        groupAdapter = new JoinGroupAdapter(this, groups);
        recyclerView.setAdapter(groupAdapter);

        readGroup();
    }

    private void readGroup() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups");
        Log.d("JoinGroupActivity", reference + "----------------------------");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                Log.d("JoinGroupActivity", "on data change" + "----------------------------");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("JoinGroupActivity", snapshot.toString() + "----------------------------");
                    GroupChat group = snapshot.getValue(GroupChat.class);
                    Log.d("JoinGroupActivity", group.getGroup_name() + "----------------------------");
                    Log.d("JoinGroupActivity", group.getDate() + "----------------------------");
                    groups.add(group);
                }
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNewGroup() {

        String uid = firebaseUser.getUid();
        Log.d("JoinGroupActivity", uid + "----------------------------");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference userReference = databaseReference.child("Users").child(uid).child("user_type");

        Log.d("JoinGroupActivity", userReference.toString() + "----------------------------");

        final String[] user_type = new String[1];
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("JoinGroupActivity", "on data change" + "----------------------------");
                user_type[0] = dataSnapshot.getValue(String.class);
                Log.d("JoinGroupActivity", user_type[0] + " --- on data change----------------------------");

                Intent createGroupActivity = new Intent(getApplicationContext(), CreateNewGroupActivity.class);
                createGroupActivity.putExtra("user_type", user_type[0]);
                Log.d("JoinGroupActivity", user_type[0] + "----------------------------");
                startActivity(createGroupActivity);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("JoinGroupActivity", "on cancelled" + "----------------------------");
            }
        });

    }
}
