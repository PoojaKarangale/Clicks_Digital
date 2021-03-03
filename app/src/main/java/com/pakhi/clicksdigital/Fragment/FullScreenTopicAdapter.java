package com.pakhi.clicksdigital.Fragment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

//import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FullScreenTopicAdapter extends PagerAdapter {

    ArrayList<String> images;
    ArrayList<String> eventName;
    ArrayList<String> uploader;
    Context context;
    FirebaseDatabaseInstance rootRef;
    private boolean doNotifyDataSetChangedOnce=false;
    LayoutInflater layoutInflater;
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;


    FullScreenTopicAdapter(ArrayList<String> images, ArrayList<String> eventName, ArrayList<String>  uploader, Context context){
        this.images=images;
        this.eventName=eventName;
        this.uploader=uploader;
        this.context=context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (doNotifyDataSetChangedOnce) {
            doNotifyDataSetChangedOnce = false;
            notifyDataSetChanged();
        }
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        View itemView=layoutInflater.inflate(R.layout.full_screen_image, container, false);

        final PhotoView imageView=  itemView.findViewById(R.id.imageViewMain_);
        TextView nameOfEvent=itemView.findViewById(R.id.name_of_event_);
        final TextView upload = itemView.findViewById(R.id.upload_value_);
        //scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        nameOfEvent.setText(eventName.get(position));

        rootRef = FirebaseDatabaseInstance.getInstance();
        rootRef.getUserRef().child(uploader.get(position)).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upload.setText(snapshot.child(ConstFirebase.USER_NAME).getValue().toString()+" "+snapshot.child(ConstFirebase.last_name).getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*Glide
                .with(this.context)
                .load(images.get(position))
                .into(imageView);*/

        Picasso.get().load(images.get(position)).into(imageView);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootRef.getUserRef().child(uploader.get(position)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String userID = snapshot.getKey();
                        Intent intent = new Intent(context, VisitProfileActivity.class);
                        intent.putExtra(Const.visitUser, userID);
                        context.startActivity(intent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        doNotifyDataSetChangedOnce = true;
        container.addView(itemView);

        return itemView;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}

