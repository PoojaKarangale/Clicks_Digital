package com.pakhi.clicksdigital.Activities;

import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class JoinGroupActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    String user_type;
    ImageView home_btn;
    DatabaseReference UsersRef;
    private FloatingActionButton fab_create_group;
    private RecyclerView recyclerView;
    private JoinGroupAdapter groupAdapter;
    private List<GroupChat> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        home_btn = findViewById(R.id.home_btn);

        fab_create_group = findViewById(R.id.fab_create_group);
        fab_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewGroup();
            }
        });

        home_btn.setVisibility(View.GONE);

        UsersRef.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("groups").exists())) {
                    home_btn.setVisibility(View.VISIBLE);
                } else {
                    home_btn.setVisibility(View.GONE);
                }

                user_type = dataSnapshot.child(Constants.USER_DETAILS).child("user_type").getValue(String.class);
                assert user_type != null;
                if (user_type.equals("admin")) {
                    home_btn.setVisibility(View.VISIBLE);
                    fab_create_group.setVisibility(View.VISIBLE);
                } else
                    fab_create_group.setVisibility(View.INVISIBLE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(JoinGroupActivity.this, StartActivity.class));
                finish();
            }
        });

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
        Intent createGroupActivity = new Intent(getApplicationContext(), CreateNewGroupActivity.class);
        startActivity(createGroupActivity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        UsersRef.child(firebaseUser.getUid()).child("userState")
                .updateChildren(onlineStateMap);

    }
}
