package com.pakhi.clicksdigital.Activities;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.UserRequestAdapter;
import com.pakhi.clicksdigital.Model.User_request;
import com.pakhi.clicksdigital.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class UserRequestActivity extends AppCompatActivity {
    DatabaseReference RootRef;
    FirebaseAuth firebaseAuth;
    private RecyclerView recyclerView;
    private UserRequestAdapter userRequestAdapter;
    private List<User_request> user_requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);

        RootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        user_requests = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_requesting_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRequestAdapter = new UserRequestAdapter(getApplicationContext(), user_requests);
        recyclerView.setAdapter(userRequestAdapter);

        showRequestingUsers();

    }

    private void showRequestingUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("User_requests");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_requests.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User_request userRequest = snapshot.getValue(User_request.class);
                    user_requests.add(userRequest);
                }
                userRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

        RootRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("userState")
                .updateChildren(onlineStateMap);

    }
}
