package com.pakhi.clicksdigital.Activities;


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
import com.pakhi.clicksdigital.Adapter.UserRequestAdapter;
import com.pakhi.clicksdigital.Model.User_request;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class UserRequestActivity extends AppCompatActivity {

  FirebaseDatabaseInstance rootRef;
  //  String                   groupId, groupName;
  private RecyclerView       recyclerView;
  private UserRequestAdapter userRequestAdapter;
  //  private List<User_request> user_requests  = new ArrayList<>();
  private List<String>       requestingUsers = new ArrayList<>();
  ImageView cross;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_request);
    cross = findViewById(R.id.close_post);
    rootRef=FirebaseDatabaseInstance.getInstance();

    recyclerView=findViewById(R.id.recycler_requesting_users);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

        /* userRequestAdapter = new UserRequestAdapter(getApplicationContext(), user_requests);
        recyclerView.setAdapter(userRequestAdapter);*/
    userRequestAdapter=new UserRequestAdapter(getApplicationContext(), requestingUsers);
    recyclerView.setAdapter(userRequestAdapter);

    //showRequestingUsers();
    readRequestingUsersId();
    cross.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

  }

  private void readRequestingUsersId() {
    DatabaseReference reference=rootRef.getUserRequestsRef();
    reference.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        requestingUsers.clear();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
          requestingUsers.add(snapshot.getKey());
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

  }
}