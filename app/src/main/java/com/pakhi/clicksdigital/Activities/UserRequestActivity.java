package com.pakhi.clicksdigital.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.ContactUserAdapter;
import com.pakhi.clicksdigital.Adapter.UserRequestAdapter;
import com.pakhi.clicksdigital.Model.Contact;
import com.pakhi.clicksdigital.Model.GroupChat;
import com.pakhi.clicksdigital.Model.User_request;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;

public class UserRequestActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserRequestAdapter userRequestAdapter;
    private List<User_request> user_requests;
   // ArrayList<Contact> userList, contactList;
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);

        user_requests= new ArrayList<>();
        //userList= new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_requesting_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //contacts_user = new ArrayList<>();

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
}
