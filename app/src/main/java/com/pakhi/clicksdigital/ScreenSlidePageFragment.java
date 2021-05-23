package com.pakhi.clicksdigital;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import java.util.ArrayList;

public class ScreenSlidePageFragment extends Fragment {
    ImageView image1,image2,image3,image4;
    ArrayList<String> current, prev, next, all;
    FirebaseDatabaseInstance rootRef;
    FirebaseDatabase slide;
    public ScreenSlidePageFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);


        image1=rootView.findViewById(R.id.image1);
        image2=rootView.findViewById(R.id.image2);
        image3=rootView.findViewById(R.id.image3);
        image4=rootView.findViewById(R.id.image4);

        current = new ArrayList<>();
        prev = new ArrayList<>();
        next = new ArrayList<>();
        all = new ArrayList<>();

        rootRef.getsliderRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()){
                    all.add(snap.child(ConstFirebase.url).getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return rootView;
    }
}