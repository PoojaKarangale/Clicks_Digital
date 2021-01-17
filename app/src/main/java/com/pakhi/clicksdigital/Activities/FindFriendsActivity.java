package com.pakhi.clicksdigital.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.FindFriendsAdapter;
import com.pakhi.clicksdigital.Model.User;
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

public class FindFriendsActivity extends AppCompatActivity {
    String                   currentUserId;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    List<User>               userList=new ArrayList<>();
    FindFriendsAdapter       findFriendsAdapter;
    Query                    query1;
    private Toolbar           mToolbar;
    private RecyclerView      FindFriendsRecyclerList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=rootRef.getUserRef();
        query1=UsersRef.orderByChild(ConstFirebase.USER_NAME).equalTo("");
        FindFriendsRecyclerList=(RecyclerView) findViewById(R.id.find_friends_recycler_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        findFriendsAdapter=new FindFriendsAdapter(getApplicationContext(), userList);
        FindFriendsRecyclerList.setAdapter(findFriendsAdapter);
        mToolbar=findViewById(R.id.find_friends_toolbar);

        SearchView searchView=findViewById(R.id.search_bar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchEvents(query.toString().trim().toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchEvents(newText.toString().trim().toLowerCase());
                return false;
            }
        });

        ImageView close=findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");
        searchEvents("");
    }

    private void searchEvents(final String s) {

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.child(ConstFirebase.USER_DETAILS).exists()) {
                            Log.i("------datasnap user ", dataSnapshot.getValue().toString());
                            User user=dataSnapshot.child(ConstFirebase.USER_DETAILS).getValue(User.class);
                            if (user.getUser_name().toLowerCase().contains(s) || user.getLast_name().contains(s)) {
                                userList.add(user);
                            }
                        }
                    }
                    findFriendsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        UsersRef.child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}