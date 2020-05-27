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
import com.pakhi.clicksdigital.Activities.RegisterActivity;
import com.pakhi.clicksdigital.Activities.SetProfileActivity;
import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.Adapter.JoinGroupAdapter;
import com.pakhi.clicksdigital.Model.GroupChat;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment {
    private View groupFragmentView;
    // private ListView list_view;
    // private ArrayAdapter<String> arrayAdapter;
    // private ArrayList<String> list_of_groups = new ArrayList<>();
    private JoinGroupAdapter groupAdapter;
    private List<GroupChat> groups;
    private FloatingActionButton fab_create_group;
    private RecyclerView recyclerView;
    private DatabaseReference GroupRef,userGroupRef;
    FirebaseAuth firebaseAuth;
    String userID;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()!=null){
        //startActivity(new Intent(getContext(), RegisterActivity.class));
            userID=firebaseAuth.getCurrentUser().getUid();
            userGroupRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("groups");
            }
        else startActivity(new Intent(getContext(),RegisterActivity.class));


        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

     /*   //IntializeFields();
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
        list_view.setAdapter(arrayAdapter);
*/

        recyclerView = groupFragmentView.findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groups = new ArrayList<>();

        groupAdapter = new JoinGroupAdapter(getContext(), groups, "users_group");
        recyclerView.setAdapter(groupAdapter);

        RetrieveAndDisplayGroups();

        return groupFragmentView;
    }

    public void onStart()
    {
        super.onStart();

        if (firebaseAuth.getCurrentUser() == null)
        {
            SendUserToRegisterActivity();
        }
        else
        {

        }
    }

    private void SendUserToRegisterActivity() {
        startActivity(new Intent(getContext(), RegisterActivity.class));

    }

    private void RetrieveAndDisplayGroups() {
    Log.d("GroupFragments","-------------"+userGroupRef);

        userGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    //GroupChat group = snapshot.getValue(GroupChat.class);
                    //groups.add(group);
                    String group_key = snapshot.getKey();
                    groups.clear();
                    GroupRef.child(group_key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Log.d("GroupFragments","-------------"+dataSnapshot);
                            GroupChat group = dataSnapshot.getValue(GroupChat.class);

                            Log.d("GroupFragments","-------------"+group);
                            groups.add(group);

                            Log.d("GroupFragments","-------------"+groups.size());
                            groupAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
               // groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
