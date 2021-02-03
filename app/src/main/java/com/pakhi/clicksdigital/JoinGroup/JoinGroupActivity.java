package com.pakhi.clicksdigital.JoinGroup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
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

    // AsyncOperation task = new AsyncOperation();
    ImageView                close;
    SearchView               searchView;
    String                   current_user_id;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    DatabaseReference        groupRef, usersRef;
    TextView txt_requested;
    private RecyclerView     recyclerView;
    private JoinGroupAdapter groupAdapter;
    private List<Group>      groups=new ArrayList<>();

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
        txt_requested=findViewById(R.id.txt_requested);
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

        rootRef.getApprovedUserRef().child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    //holder.join_btn.setVisibility(View.GONE);
                    txt_requested.setVisibility(View.VISIBLE);
                } else {
                    txt_requested.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUpRecycleView() {
        recyclerView=findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter=new JoinGroupAdapter(this, groups);
        recyclerView.setAdapter(groupAdapter);
    }

    private void searchGroups(final String s) {
       /* Query query=rootRef.getGroupRef().orderByChild(Const.GROUP_NAME)
                .startAt(s)
                .endAt(s + "\uf8ff");*/
        rootRef.getGroupRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groups.clear();
                for (final DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                    Log.d("SEARCH GROUP","--------------"+snapshot1.getKey()+" "+snapshot1.getValue());

                       rootRef.getUserRef().child(current_user_id).child(ConstFirebase.groups1).child(snapshot1.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("GROUPS","----------snapshot"+snapshot.getValue());
                            if(!snapshot.exists()){
                                Group group=snapshot1.getValue(Group.class);
                                if(group.getGroup_name().toLowerCase().contains(s))
                                groups.add(group);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                   /* Group group=snapshot1.getValue(Group.class);
                    groups.add(group);*/
                }
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        searchGroups("");
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
        onlineStateMap.put(Const.time, saveCurrentTime);
        onlineStateMap.put(Const.date, saveCurrentDate);
        onlineStateMap.put(Const.state, state);
        usersRef.child(current_user_id).child(ConstFirebase.userState)
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
