package com.pakhi.clicksdigital.JoinGroup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class JoinGroupActivity extends AppCompatActivity implements View.OnClickListener {

    AsyncOperation task = new AsyncOperation();
    ImageView close;
    EditText searchView;
    String current_user_id;
    private RecyclerView recyclerView;
    private RecyclerView recycler_requested_groups;
    private JoinGroupAdapter groupAdapter, requestedGroupAdapter;
    private List<Group> groups = new ArrayList<>();
    private List<Group> requestedGroups = new ArrayList<>();
    private List<Group> usersGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        current_user_id = FirebaseAuth.getInstance().getUid();
        searchView = findViewById(R.id.search_bar);
        close = findViewById(R.id.close);
        close.setOnClickListener(this);
        setUpRecycleView();

        task.execute();
//        readRequestedGroups();
//        readUsersGroups();
//        readGroup();
        // readRequestedGroups();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // searchGroups(s.toString().trim().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void setUpRecycleView() {
        recyclerView = findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new JoinGroupAdapter(this, groups);
        recyclerView.setAdapter(groupAdapter);

        recycler_requested_groups = findViewById(R.id.recycler_requested_groups);
        recycler_requested_groups.setHasFixedSize(true);
        recycler_requested_groups.setLayoutManager(new LinearLayoutManager(this));
        requestedGroupAdapter = new JoinGroupAdapter(this, groups);
        recycler_requested_groups.setAdapter(requestedGroupAdapter);

    }
  /*  private void readRequestedGroups() {
        String uid = FirebaseAuth.getInstance().getUid();
//    Query query = FirebaseDatabase.getInstance().getReference("Groups")
//                .orderByChild(Const.GROUP_NAME)
//                .orderByChild("requesting_user")
//                .equalTo(uid)
//                .endAt("\uf8ff");
        DatabaseReference queryref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("GroupRequests");
        queryref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                requestedGroups.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String groupId = snapshot.getKey();

                    //  User_request request = snapshot.getValue(User_request.class);

                    DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
                    groupRef.child(groupId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Group group = snapshot.getValue(Group.class);
                            requestedGroups.add(group);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                requestedGroupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchGroups(final String s) {
        Query query = FirebaseDatabase.getInstance().getReference("Groups").orderByChild(Const.GROUP_NAME)
                .startAt(s)
                .endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                    Group group = snapshot1.getValue(Group.class);
                    groups.add(group);
                }
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

    private void readGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Group group = snapshot.getValue(Group.class);
                        groups.add(group);
                    }
                    groups.removeAll(requestedGroups);
                    groups.removeAll(usersGroups);
                    groupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readRequestedGroups() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child(ConstFirebase.groupRequests);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestedGroups.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String groupId = snapshot.getKey();

                        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
                        groupRef.child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Group group = snapshot.getValue(Group.class);
                                requestedGroups.add(group);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    requestedGroupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsersGroups() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id).child("groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersGroups.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String groupId = snapshot.getKey();

                        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
                        groupRef.child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Group group = snapshot.getValue(Group.class);
                                usersGroups.add(group);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    //requestedGroupAdapter.notifyDataSetChanged();
                }
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
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.child(FirebaseAuth.getInstance().getUid()).child("userState")
                .updateChildren(onlineStateMap);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                finish();
                break;
        }
    }

    private final class AsyncOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            readRequestedGroups();
            readUsersGroups();
          /*  String param = params[0];
            String s = "";
            switch (param) {
                case "readGroups":
                    // readGroup();
                    break;
                case "online":
                    updateUserStatus("online");
                    break;
                case "offline":
                    updateUserStatus("offline");
                    break;
            }*/
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            readGroup();
        }
    }
}
















