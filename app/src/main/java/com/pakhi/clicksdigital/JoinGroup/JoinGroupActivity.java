package com.pakhi.clicksdigital.JoinGroup;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.ArrayList;
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
        searchGroups("");
        //searchGroups("");
        if(!searchView.isFocused()) {
            searchView.clearFocus();
        }
        searchView.onActionViewExpanded(); //new Added line
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search Here");

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
                groupAdapter.notifyDataSetChanged();
                for (final DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                    Log.d("SEARCH GROUP","--------------"+snapshot1.getKey()+" "+snapshot1.getValue());

                       rootRef.getUserRef().child(current_user_id).child(ConstFirebase.groups).child(snapshot1.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("GROUPS","----------snapshot"+snapshot.getValue());
                            if(!snapshot.exists()){
                                Group group=snapshot1.getValue(Group.class);
                                if(group.getGroup_name().toLowerCase().contains(s))
                                groups.add(group);
                            }
                            //groupAdapter.notifyDataSetChanged();
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
          //searchGroups("");
          //setUpRecycleView();

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        Intent intent = new Intent(JoinGroupActivity.this, StartActivity.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "You have joined new Dialog communities, do check in groups section", Toast.LENGTH_LONG).show();
        finish();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                Intent intent = new Intent(JoinGroupActivity.this, StartActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "You have joined new Dialog communities, do check in groups section", Toast.LENGTH_LONG).show();
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

            }*/
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            //readGroup();
        }
    }
}
