package com.pakhi.clicksdigital.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    String user_type;
    ImageView close_post, home_btn;
    private FloatingActionButton fab_create_group;
    private RecyclerView recyclerView;
    private JoinGroupAdapter groupAdapter;
    private List<GroupChat> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        close_post = findViewById(R.id.close_post);
        home_btn = findViewById(R.id.home_btn);

        fab_create_group = findViewById(R.id.fab_create_group);
        fab_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewGroup();
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        user_type = pref.getString("user_type", "user");
        if (user_type.equals("user")) {
            close_post.setVisibility(View.INVISIBLE);
            home_btn.setVisibility(View.VISIBLE);
            //home_btn.setEnabled(false);
        }
        if (user_type.equals("admin")) {
            home_btn.setVisibility(View.INVISIBLE);
            close_post.setVisibility(View.VISIBLE);
        }

        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(JoinGroupActivity.this, com.pakhi.clicksdigital.StartActivity.class));
                finish();
            }
        });

        close_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(JoinGroupActivity.this, StartActivity.class));
                finish();
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
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GroupChat group = snapshot.getValue(GroupChat.class);
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
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference userReference = databaseReference.child("Users").child(uid).child("user_type");


        final String[] user_type = new String[1];
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_type[0] = dataSnapshot.getValue(String.class);

                Intent createGroupActivity = new Intent(getApplicationContext(), CreateNewGroupActivity.class);
                //createGroupActivity.putExtra("user_type", user_type[0]);
                startActivity(createGroupActivity);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
