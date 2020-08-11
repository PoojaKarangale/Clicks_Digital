package com.pakhi.clicksdigital.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Event.CreateEventActivity;
import com.pakhi.clicksdigital.Event.EventAdapter;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;


public class OfflineEventsFragment extends Fragment {
    private View view;
    private DatabaseReference rootRef, userRef, eventRef;
    private FirebaseAuth firebaseAuth;
    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";
    private FloatingActionButton fab_create_event;
    String userID;
    private EventAdapter eventAdapter;
    private List<Event> events;
    private RecyclerView events_recycler;

    public OfflineEventsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_offline_events, container, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child("Users");
        eventRef = FirebaseDatabase.getInstance().getReference("Events");
        firebaseAuth = FirebaseAuth.getInstance();

        fab_create_event = view.findViewById(R.id.fab_create_event);
        fab_create_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CreateEventActivity createEventFragment = new CreateEventActivity();
                // replaceFragment(createEventFragment);
                startActivity(new Intent(getContext(), CreateEventActivity.class));
            }
        });

        events_recycler = view.findViewById(R.id.events_recycler);
        events_recycler.setHasFixedSize(true);
        events_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        events = new ArrayList<>();

        eventAdapter = new EventAdapter(getContext(), events);
        events_recycler.setAdapter(eventAdapter);

        RetrieveAndDisplayEvents();
        return view;
    }

    private void RetrieveAndDisplayEvents() {
        eventRef.child("Offline").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.child("EventDetails").getValue(Event.class);
                    events.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void replaceFragment(CreateEventActivity createEventFragment) {
        FragmentTransaction transaction = getChildFragmentManager()
                .beginTransaction();
        transaction.addToBackStack(null);
        //  transaction.add(R.id.createEventContainer, createEventFragment, TAG_FRAGMENT);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        Log.d("TESTINGSTART", "-----------frag count------" + getChildFragmentManager().getBackStackEntryCount());
    }
}
