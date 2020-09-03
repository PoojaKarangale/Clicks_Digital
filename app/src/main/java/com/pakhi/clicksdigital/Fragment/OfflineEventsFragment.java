package com.pakhi.clicksdigital.Fragment;

import android.database.Cursor;
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
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.ArrayList;
import java.util.List;


public class OfflineEventsFragment extends Fragment {
    FirebaseDatabaseInstance rootRef;
    UserDatabase             db;
    User                     user;
    String                   city;
    private View              view;
    private DatabaseReference eventRef;
    private EventAdapter      eventAdapter;
    private List<Event>       events;
    private RecyclerView      events_recycler;

    public OfflineEventsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_offline_events, container, false);

        rootRef=FirebaseDatabaseInstance.getInstance();
        eventRef=rootRef.getEventRef();

        events_recycler=view.findViewById(R.id.events_recycler);
        events_recycler.setHasFixedSize(true);
        events_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        events=new ArrayList<>();

        eventAdapter=new EventAdapter(getContext(), events);
        events_recycler.setAdapter(eventAdapter);

        readUserData();
        //RetrieveAndDisplayEvents();
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

        eventRef.child(ConstFirebase.eventOffline).orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event=dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                    if (event.getEventName().toLowerCase().contains(s)
                            || event.getDescription().toLowerCase().contains(s)
                            || event.getCategory().toLowerCase().contains(s)
                            || event.getAddress().toLowerCase().contains(s)
                            || event.getCity().toLowerCase().contains(s)
                            || event.getVenu().toLowerCase().contains(s)
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
                            || event.getAddress().toLowerCase().contains(s)
                            || event.getCity().toLowerCase().contains(s)
                            || event.getVenu().toLowerCase().contains(s)
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

    private void showEvents(final String s) {

        eventRef.child(ConstFirebase.eventOffline).orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event=dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                    if (event.getCity().toLowerCase().contains(s)
                            || event.getAddress().toLowerCase().contains(s)
                            || event.getVenu().toLowerCase().contains(s)
                            || event.getDescription().toLowerCase().contains(s)
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
                    if (event.getCity().toLowerCase().contains(s)
                            || event.getAddress().toLowerCase().contains(s)
                            || event.getVenu().toLowerCase().contains(s)
                            || event.getDescription().toLowerCase().contains(s)
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

    private void readUserData() {
        db=new UserDatabase(getContext());
        db.getReadableDatabase();
        Cursor res=db.getAllData();
        if (res.getCount() == 0) {

        } else {
            res.moveToFirst();
            city=res.getString(5);
         /*   user=new User(res.getString(0), res.getString(1),
                    res.getString(2), res.getString(3), res.getString(4),
                    res.getString(5), res.getString(6), res.getString(7),
                    res.getString(8), res.getString(9), res.getString(10),
                    res.getString(11), res.getString(12), res.getString(13),
                    res.getString(14), res.getString(15), res.getString(16));
*/
        }
        // db.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        //searchEvents("");
        showEvents(city);
    }
}
