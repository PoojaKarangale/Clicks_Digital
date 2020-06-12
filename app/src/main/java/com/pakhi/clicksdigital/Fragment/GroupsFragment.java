package com.pakhi.clicksdigital.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Activities.CreateNewGroupActivity;
import com.pakhi.clicksdigital.Activities.JoinGroupActivity;
import com.pakhi.clicksdigital.Activities.RegisterActivity;
import com.pakhi.clicksdigital.Adapter.JoinGroupAdapter;
import com.pakhi.clicksdigital.Model.GroupChat;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    String userID;
    private View groupFragmentView;
    private JoinGroupAdapter groupAdapter;
    private List<GroupChat> groups;
    private FloatingActionButton fab_create_group, fab_join_group;
    private RecyclerView recyclerView;
    private DatabaseReference GroupRef, userGroupRef, UsersRef;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userID = firebaseAuth.getCurrentUser().getUid();
        userGroupRef = UsersRef.child(userID).child("groups");

        fab_create_group = groupFragmentView.findViewById(R.id.fab_create_group);
        fab_join_group = groupFragmentView.findViewById(R.id.fab_join_group);

        final String[] user_type = new String[1];
        UsersRef.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                user_type[0] = dataSnapshot.child("user_type").getValue(String.class);
                if (user_type[0].equals("admin")) {
                    fab_create_group.setVisibility(View.VISIBLE);
                    fab_join_group.setVisibility(View.GONE);

                } else {
                    fab_create_group.setVisibility(View.GONE);
                    fab_join_group.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fab_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateNewGroupActivity.class));
            }
        });

        fab_join_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), JoinGroupActivity.class));
            }
        });

        recyclerView = groupFragmentView.findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groups = new ArrayList<>();

        groupAdapter = new JoinGroupAdapter(getContext(), groups, "users_group");
        recyclerView.setAdapter(groupAdapter);

        RetrieveAndDisplayGroups();

        return groupFragmentView;
    }

    public void onStart() {
        super.onStart();
    }

    private void SendUserToRegisterActivity() {
        startActivity(new Intent(getContext(), RegisterActivity.class));

    }

    private void RetrieveAndDisplayGroups() {
        Log.d("GroupFragments", "-------------" + userGroupRef);

        userGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String group_key = snapshot.getKey();
                    groups.clear();
                    GroupRef.child(group_key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            GroupChat group = dataSnapshot.getValue(GroupChat.class);

                            groups.add(group);

                            groupAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
