package com.pakhi.clicksdigital.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Event.EventAdapter;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.ArrayList;
import java.util.List;

public class OnlineEventsFragment extends Fragment {
    FirebaseDatabaseInstance rootRef;
    private View              view;
    private EventAdapter      eventAdapter;
    private List<Event>       events;
    private RecyclerView      events_recycler;
    private DatabaseReference eventRef;

    public OnlineEventsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_online_events, container, false);

        rootRef=FirebaseDatabaseInstance.getInstance();
        eventRef=rootRef.getEventRef();

        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());

        events_recycler=view.findViewById(R.id.events_recycler);
        events_recycler.setHasFixedSize(true);
        events_recycler.setLayoutManager(layoutManager);

        events=new ArrayList<>();

        eventAdapter=new EventAdapter(getContext(), events);
        events_recycler.setAdapter(eventAdapter);

        SearchView searchView=view.findViewById(R.id.search_bar);

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

        return view;
    }

    private void searchEvents(final String s) {

        eventRef.child(ConstFirebase.eventOnline).orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event=dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                    if (event.getEventName().toLowerCase().contains(s)
                            || event.getDescription().toLowerCase().contains(s)
                            || event.getCategory().toLowerCase().contains(s)
                    ) {
                        events.add(event);
                    }
                }
                //eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        eventRef.child("Both").orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //  events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event=dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                    if (event.getEventName().toLowerCase().contains(s)
                            || event.getDescription().toLowerCase().contains(s)
                            || event.getCategory().toLowerCase().contains(s)
                    ) {
                        events.add(event);
                    }
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        searchEvents("");
    }
}
