package com.pakhi.clicksdigital.Fragment;

import android.os.Bundle;
import android.util.Log;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class OfflineEventsFragment extends Fragment {
    FirebaseDatabaseInstance rootRef;

    User user;
    String city;
    private View view;
    private DatabaseReference eventRef;
    private EventAdapter eventAdapter;
    private List<Event> events;
    private RecyclerView events_recycler;

    public OfflineEventsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_offline_events, container, false);

        rootRef = FirebaseDatabaseInstance.getInstance();
        eventRef = rootRef.getEventRef();

        events_recycler = view.findViewById(R.id.events_recycler);
        events_recycler.setHasFixedSize(true);
        events_recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        events = new ArrayList<>();

        eventAdapter = new EventAdapter(getContext(), events);
        events_recycler.setAdapter(eventAdapter);

        readUserData();
        //RetrieveAndDisplayEvents();
        SearchView searchView = view.findViewById(R.id.search_bar);
        showEvents(city.toLowerCase());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                newText = newText.equals("") ? city : newText;
                searchEvents(newText.trim().toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.equals("") ? city : newText;
                searchEvents(newText.trim().toLowerCase());
                return false;
            }
        });

        return view;
    }

    private void searchEvents(final String s) {
        final Calendar calendar = Calendar.getInstance();
        //  calendar.add(Calendar.DAY_OF_MONTH,1);
        Timestamp ts = new Timestamp(calendar.getTimeInMillis() / 1000L);
        final Date current = calendar.getTime();

        /*.orderByChild("timeStamp")*/
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                eventAdapter.notifyDataSetChanged();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(ConstFirebase.eventDetails).exists()) {
                        Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                        Timestamp ts = new Timestamp(event.getTimeStamp());
                        Date eventDate = new Date(ts.getTime());
                        if (!eventDate.before(current)) {
                            if (event.getEventType().equals(ConstFirebase.eventOffline) || event.getEventType().equals(ConstFirebase.eventBoth)) {
                                Log.i("eventType--", event.getEventType());
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
                        }
                    }
                }
                Collections.sort(events, new Comparator<Event>() {
                    public int compare(Event o1, Event o2) {
                        return o1.getTimeStamp().compareTo(o2.getTimeStamp());
                    }
                });
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showEvents(final String s) {
        Log.i("my city--", s);
        eventRef.orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(ConstFirebase.eventDetails).exists()) {
                        Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                        Log.i("OFFLINE_EVENTS_CITY" , event.getCity());
                        if (event.getEventType().equals(ConstFirebase.eventOffline) || event.getEventType().equals(ConstFirebase.eventBoth)) {
                            if (event.getCity().toLowerCase().contains(s)
                                    || event.getAddress().toLowerCase().contains(s)
                                    || event.getVenu().toLowerCase().contains(s)
                                    || event.getDescription().toLowerCase().contains(s)
                            ) {
                                events.add(event);
                            }
                        }
                    }
                }
                Collections.sort(events, new Comparator<Event>() {
                    public int compare(Event o1, Event o2) {
                        return o1.getTimeStamp().compareTo(o2.getTimeStamp());
                    }
                });
                eventAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readUserData() {
        UserDatabase db = new UserDatabase(getContext());
        city = db.getSqliteUser_data(ConstFirebase.CITY);
        Log.d("OFFLINE_EVENTS_CITY", "city : " + city);
    }

    @Override
    public void onStart() {
        super.onStart();
        //searchEvents("");
        // showEvents(city);
    }
}