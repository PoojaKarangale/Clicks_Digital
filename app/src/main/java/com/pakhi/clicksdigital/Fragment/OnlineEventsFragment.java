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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Event.EventAdapter;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class OnlineEventsFragment extends Fragment {
    FirebaseDatabaseInstance rootRef;
    private View view;
    private EventAdapter eventAdapter;
    private List<Event> events;
    private RecyclerView events_recycler;
    private DatabaseReference eventRef;

    public OnlineEventsFragment() {
    }

    public static String previousDateString(String dateString)
            throws ParseException {
        // Create a date formatter using your format string
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        // Parse the given date string into a Date object.
        // Note: This can throw a ParseException.
        Date myDate = dateFormat.parse(dateString);

        // Use the Calendar class to subtract one day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(myDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.add(Calendar.MONTH, -2);
        // Use the date formatter to produce a formatted date string
        Date previousDate = calendar.getTime();
        String result = dateFormat.format(previousDate);

        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_online_events, container, false);

        rootRef = FirebaseDatabaseInstance.getInstance();
        eventRef = rootRef.getEventRef();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        events_recycler = view.findViewById(R.id.events_recycler);
        events_recycler.setHasFixedSize(true);
        events_recycler.setLayoutManager(layoutManager);

        events = new ArrayList<>();

        eventAdapter = new EventAdapter(getContext(), events);
        events_recycler.setAdapter(eventAdapter);

        SearchView searchView = view.findViewById(R.id.search_bar);
        searchEvents("");
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

        removeOldEvents();
        return view;
    }

    /*    private long fieldToTimestamp(int year, int month, int day) {
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return (long) (calendar.getTimeInMillis() / 1000L);
    }*/

    private void removeOldEvents() {

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        Timestamp ts = new Timestamp(calendar.getTimeInMillis() / 1000L);
        final Date twoMonthAgo = new Date(ts.getTime());

        DatabaseReference eventRef = rootRef.getEventRef();
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(ConstFirebase.eventDetails).exists()) {

                        Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);

                        Timestamp ts = new Timestamp(event.getTimeStamp());
                        Date eventDate = new Date(ts.getTime());
                        if (eventDate.before(twoMonthAgo)) {
                            dataSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(ConstFirebase.eventDetails).exists()) {

                        Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);

                        Timestamp ts = new Timestamp(event.getTimeStamp());
                        Date eventDate = new Date(ts.getTime());
                        if (eventDate.before(twoMonthAgo)) {
                            dataSnapshot.getRef().removeValue();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(ConstFirebase.eventDetails).exists()) {

                        Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);

                        Timestamp ts = new Timestamp(event.getTimeStamp());
                        Date eventDate = new Date(ts.getTime());
                        if (eventDate.before(twoMonthAgo)) {
                            dataSnapshot.getRef().removeValue();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });

    }

    private void searchEvents(final String s) {
        final Calendar calendar = Calendar.getInstance();
        //  calendar.add(Calendar.DAY_OF_MONTH,1);
        Timestamp ts = new Timestamp(calendar.getTimeInMillis() / 1000L);
        final Date current = new Date(ts.getTime());

        //Query query = eventRef.orderByChild("event_type").equalTo(ConstFirebase.eventOnline);
        eventRef.orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(ConstFirebase.eventDetails).exists()) {
                        Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                        Timestamp ts = new Timestamp(event.getTimeStamp());
                        Date eventDate = new Date(ts.getTime());
                        if (!eventDate.before(current)) {
                            if (event.getEventName().toLowerCase().contains(s)
                                    || event.getDescription().toLowerCase().contains(s)
                                    || event.getCategory().toLowerCase().contains(s)
                            ) {
                                if(event.getEventType().equals(ConstFirebase.eventOnline)){

                                    events.add(event);

                                }
                            }
                        }
                    }
                }

                //eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        eventRef.orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //  events.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(ConstFirebase.eventDetails).exists()) {
                        Event event = dataSnapshot.child(ConstFirebase.eventDetails).getValue(Event.class);
                        Timestamp ts = new Timestamp(event.getTimeStamp());
                        Date eventDate = new Date(ts.getTime());
                        if (!eventDate.before(current)) {
                            if (event.getEventName().toLowerCase().contains(s)
                                    || event.getDescription().toLowerCase().contains(s)
                                    || event.getCategory().toLowerCase().contains(s)
                            ) {
                                if(event.getEventType().equals("Both")){

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

    @Override
    public void onStart() {
        super.onStart();
        //searchEvents("");
    }
}