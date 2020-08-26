package com.pakhi.clicksdigital.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.GroupChat.CreateNewGroupActivity;
import com.pakhi.clicksdigital.GroupChat.MyGroupsAdapter;
import com.pakhi.clicksdigital.JoinGroup.JoinGroupActivity;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;

import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment implements View.OnClickListener {

    String                   userID;
    String                   user_type;
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;

    LinearLayout    join_group_layout;
    MyGroupsAdapter groupAdapter;
    private View                 groupFragmentView;
    private List<Group>          groups;
    private FloatingActionButton fab_create_group, fab_join_group;
    private RecyclerView      recyclerView;
    private DatabaseReference GroupRef, userGroupRef, UsersRef;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView=inflater.inflate(R.layout.fragment_groups, container, false);
        pref=SharedPreference.getInstance();
        rootRef=FirebaseDatabaseInstance.getInstance();

        GroupRef=rootRef.getGroupRef();
        UsersRef=rootRef.getUserRef();

        userID=pref.getData(SharedPreference.currentUserId, getContext());
        userGroupRef=UsersRef.child(userID).child("groups");

        initializeFields();

        userGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                } else {
                    join_group_layout.setVisibility(View.VISIBLE);
                    fab_join_group.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        user_type=pref.getData(SharedPreference.user_type, getContext());
        if (user_type.equals("admin")) {
            fab_create_group.setVisibility(View.VISIBLE);
            fab_join_group.setVisibility(View.GONE);

        } else {
            fab_create_group.setVisibility(View.GONE);
            fab_join_group.setVisibility(View.VISIBLE);
        }

        join_group_layout.setOnClickListener(this);
        fab_create_group.setOnClickListener(this);
        fab_join_group.setOnClickListener(this);

        setUpRecyclerView();
        RetrieveAndDisplayGroups();

        return groupFragmentView;
    }

    private void setUpRecyclerView() {
        recyclerView=groupFragmentView.findViewById(R.id.recycler_groups);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groups=new ArrayList<>();

        groupAdapter=new MyGroupsAdapter(getContext(), groups);
        recyclerView.setAdapter(groupAdapter);
    }

    private void initializeFields() {
        join_group_layout=groupFragmentView.findViewById(R.id.join_group_layout);
        fab_create_group=groupFragmentView.findViewById(R.id.fab_create_group);
        fab_join_group=groupFragmentView.findViewById(R.id.fab_join_group);

    }

    private void RetrieveAndDisplayGroups() {
        Log.d("GroupFragments", "-------------" + userGroupRef);

        userGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String group_key=snapshot.getKey();
                    groups.clear();
                    GroupRef.child(group_key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Group group=dataSnapshot.getValue(Group.class);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_create_event:
                startActivity(new Intent(getContext(), JoinGroupActivity.class));
                break;
            case R.id.fab_create_group:
                startActivity(new Intent(getContext(), CreateNewGroupActivity.class));
                break;
            case R.id.join_group_layout:
                startActivity(new Intent(getContext(), JoinGroupActivity.class));
                break;
        }
    }
/*

    @Override
    public void onStart() {
        super.onStart();
        Log.d("GroupFragmentsTESTING", "onStart----------------");
        FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(userGroupRef, Group.class)
                        .build();
        FirebaseRecyclerAdapter<Group, GroupsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Group, GroupsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final GroupsViewHolder holder, int position, @NonNull Group model) {

                        final String groupId = getRef(position).getKey();

                        Log.d("GroupFragmentsTESTING", "bind----------------" + groupId);
                        GroupRef.child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                final String image_url = snapshot.child(Const.IMAGE_URL).getValue().toString();
                                final String group_name = snapshot.child(Const.GROUP_NAME).getValue().toString();
                                holder.displayName.setText(group_name);

                                Picasso.get()
                                        .load(image_url).placeholder(R.drawable.profile_image)
                                        .resize(120, 120)
                                        .into(holder.image_profile);

                                Log.d("GroupFragmentsTESTING", "datachange----------------" + group_name);
                                holder.image_profile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent fullScreenIntent = new Intent(v.getContext(), EnlargedImage.class);
                                        fullScreenIntent.putExtra(Const.IMAGE_URL, image_url);

                                        v.getContext().startActivity(fullScreenIntent);
                                    }
                                });

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent groupChatActivity = new Intent(getContext().getApplicationContext(), GroupChatActivity.class);
                                        groupChatActivity.putExtra("groupName", group_name);
                                        groupChatActivity.putExtra("groupId", groupId);
                                        getContext().startActivity(groupChatActivity);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_group, viewGroup, false);
                        return new GroupsViewHolder(view);
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }
*/

 /*   private class GroupsViewHolder extends RecyclerView.ViewHolder {
        TextView displayName;
        CircleImageView image_profile;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.display_name);
            image_profile = itemView.findViewById(R.id.image_profile);


        }
    }*/
}
