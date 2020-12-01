package com.pakhi.clicksdigital.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    Context context;
    FirebaseDatabaseInstance rootRef;

    public HomeFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootRef = FirebaseDatabaseInstance.getInstance();
        FirebaseRecyclerOptions<String> options=
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(rootRef., String.class)
                        .build();

        FirebaseRecyclerAdapter<String, HomeFragment.ItemOfTopic> adapter = new FirebaseRecyclerAdapter<String, ItemOfTopic>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemOfTopic holder, int position, @NonNull String model) {

            }

            @NonNull
            @Override
            public ItemOfTopic onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return null;
            }
        }
        return inflater.inflate(R.layout.fragment_home, container, false);

    }


    public void backPressed() {
        new AlertDialog.Builder(getContext())

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public static class ItemOfTopic extends RecyclerView.ViewHolder{

        TextView grpName, topicText, date_time, noOfReplies, publisherName;
        CircleImageView topicImage;
        public ItemOfTopic(@NonNull View itemView) {
            super(itemView);
            grpName = itemView.findViewById(R.id.group_name);
            topicText = itemView.findViewById(R.id.topic);
            date_time = itemView.findViewById(R.id.date_time);
            noOfReplies = itemView.findViewById(R.id.no_of_replies);
            publisherName = itemView.findViewById(R.id.publisher_name);
            topicImage = itemView.findViewById(R.id.image_profile);



        }
    }
}
