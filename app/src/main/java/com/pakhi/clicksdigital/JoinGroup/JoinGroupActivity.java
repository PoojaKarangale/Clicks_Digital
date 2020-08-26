package com.pakhi.clicksdigital.JoinGroup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Model.Group;
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

public class JoinGroupActivity extends AppCompatActivity implements View.OnClickListener {

    //    AsyncOperation task = new AsyncOperation();
    ImageView                close;
    EditText                 searchView;
    String                   current_user_id;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    DatabaseReference        groupRef, usersRef;
    private RecyclerView     recyclerView;
    private RecyclerView     recycler_requested_groups;
    private JoinGroupAdapter groupAdapter, requestedGroupAdapter;
    private List<Group> groups         =new ArrayList<>();
    private List<Group> requestedGroups=new ArrayList<>();
    private List<Group> usersGroups    =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        rootRef=FirebaseDatabaseInstance.getInstance();
        groupRef=rootRef.getGroupRef();
        usersRef=rootRef.getUserRef();

        pref=SharedPreference.getInstance();
        current_user_id=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        searchView=findViewById(R.id.search_bar);
        close=findViewById(R.id.close);
        close.setOnClickListener(this);
        setUpRecycleView();

        SearchView searchView=findViewById(R.id.search_bar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchGroups(query.toString().trim().toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchGroups(newText.toString().trim().toLowerCase());
                return false;
            }
        });
    }

    private void setUpRecycleView() {
        recyclerView=findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter=new JoinGroupAdapter(this, groups);
        recyclerView.setAdapter(groupAdapter);

        /*recycler_requested_groups = findViewById(R.id.recycler_requested_groups);
        recycler_requested_groups.setHasFixedSize(true);
        recycler_requested_groups.setLayoutManager(new LinearLayoutManager(this));
        requestedGroupAdapter = new JoinGroupAdapter(this, groups);
        recycler_requested_groups.setAdapter(requestedGroupAdapter);*/

    }

    /* private void readRequestedGroups() {
          String uid = FirebaseAuth.getInstance().getUid();
  //    Query query = FirebaseDatabase.getInstance().getReference("Groups")
  //                .orderByChild(Const.GROUP_NAME)
  //                .orderByChild("requesting_user")
  //                .equalTo(uid)
  //                .endAt("\uf8ff");
          DatabaseReference queryref = usersRef.child(uid).child("GroupRequests");
          queryref.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                  requestedGroups.clear();
                  for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                      String groupId = snapshot.getKey();

                      //  User_request request = snapshot.getValue(User_request.class);

                      DatabaseReference groupRef = rootRef.getGroupRef();
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
      }*/

    private void searchGroups(final String s) {
        Query query=rootRef.getGroupRef().orderByChild(Const.GROUP_NAME)
                .startAt(s)
                .endAt(s + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                    Group group=snapshot1.getValue(Group.class);
                    groups.add(group);
                }
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readGroup() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //groups.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Group group=snapshot.getValue(Group.class);
                        groups.add(group);
                    }
                    Log.d("JOINGROUpTESTING", "---------group size before" + groups.size());
                    groups.removeAll(requestedGroups);
                    groups.removeAll(usersGroups);

                    Log.d("JOINGROUpTESTING", "---------req grp size before" + requestedGroups.size());
                    Log.d("JOINGROUpTESTING", "---------user grp size before" + usersGroups.size());
                    Log.d("JOINGROUpTESTING", "---------group size before" + groups.size());
                    groupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readRequestedGroups() {
        DatabaseReference reference=groupRef.child(current_user_id).child(ConstFirebase.groupRequests);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestedGroups.clear();
                // groups.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String groupId=snapshot.getKey();

                        groupRef.child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Group group=snapshot.getValue(Group.class);
                                requestedGroups.add(group);

                                Log.d("JOINGROUpTESTING", "---------requested size before" + requestedGroups.size());
                                groups.removeAll(requestedGroups);
                                // groups.add(group);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    //  requestedGroupAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsersGroups() {
        DatabaseReference reference=usersRef.child(current_user_id).child("groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersGroups.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String groupId=snapshot.getKey();

                        groupRef.child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Group group=snapshot.getValue(Group.class);
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
        // updateUserStatus("online");
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
        usersRef.child(current_user_id).child("userState")
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
          /*  readRequestedGroups();
            readUsersGroups();*/
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
            //readGroup();
        }
    }
}
















