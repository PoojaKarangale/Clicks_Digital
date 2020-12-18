package com.pakhi.clicksdigital.JoinGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;


public class joinGroupActivityReplacement extends AppCompatActivity {
    FirebaseDatabaseInstance rootRef;
    DatabaseReference groupRef, usersRef;
    SharedPreference pref;
    String current_user_id;
    JoinAdapaterReplacement joinAdapaterReplacement;

    RecyclerView recyclerView1,recyclerView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group_replacement);

        rootRef=FirebaseDatabaseInstance.getInstance();
        groupRef=rootRef.getGroupRequests();
        usersRef=rootRef.getUserRef();


        pref=SharedPreference.getInstance();
        current_user_id=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        final DatabaseReference reference=rootRef.getGroupRef();
        final String groupid=reference.getKey();

        groupRef.child(groupid).setValue(current_user_id);


        recyclerView1 = findViewById(R.id.rec_one);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<ModelJoinGroup> options =
                new FirebaseRecyclerOptions.Builder<ModelJoinGroup>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("GroupRequests"), ModelJoinGroup.class ).build();
        joinAdapaterReplacement = new JoinAdapaterReplacement(options);
        recyclerView1.setAdapter(joinAdapaterReplacement);

        recyclerView2 = findViewById(R.id.rec_two);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        FirebaseRecyclerOptions<ModelJoinGroup> options2 =
                new FirebaseRecyclerOptions.Builder<ModelJoinGroup>()
                        .setQuery(groupRef, ModelJoinGroup.class ).build();
        joinAdapaterReplacement = new JoinAdapaterReplacement(options2);
        recyclerView2.setAdapter(joinAdapaterReplacement);





    }
}
