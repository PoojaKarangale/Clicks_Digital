package com.pakhi.clicksdigital.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

class ImageViewPagerAdapter extends PagerAdapter {
    android.content.Context context;
    ArrayList<String>       images;
    ArrayList<String>       eventName;
    ArrayList<String>       uploader;
    LayoutInflater          mLayoutInflater;
    FirebaseDatabaseInstance rootRef;
    private boolean doNotifyDataSetChangedOnce=false;

    public ImageViewPagerAdapter(android.content.Context context, ArrayList<String> images, ArrayList<String> eventName, ArrayList<String> uploader) {
        this.context=context;
        this.images=images;
        this.eventName=eventName;
        this.uploader=uploader;
        //  mLayoutInflater=(LayoutInflater) context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
        mLayoutInflater=LayoutInflater.from(context);
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
        View itemView=mLayoutInflater.inflate(R.layout.slider_image_home, container, false);

        ImageView imageView=itemView.findViewById(R.id.imageViewMain);
        TextView nameOfEvent=itemView.findViewById(R.id.name_of_event);
        final TextView upload = itemView.findViewById(R.id.upload_value);
        nameOfEvent.setText(eventName.get(position));
        Log.i("Length of uploader -- ", String.valueOf(uploader.size()));
        Log.i("Uploader ---- ", uploader.get(position));
        rootRef = FirebaseDatabaseInstance.getInstance();
        rootRef.getUserRef().child(uploader.get(position)).child("DETAILS").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upload.setText(snapshot.child("user_name").getValue().toString()+" "+snapshot.child("last_name").getValue().toString());
                //Log.i("snap ----", snapshot.child("user_name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //upload.setText(uploader.get(position));


        Picasso.get().load(images.get(position)).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnlargedImage.enlargeImage( images.get(position),context);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VisitProfileActivity.class);
                intent.putExtra(ConstFirebase.visitUser, uploader.get(position));
                context.startActivity(intent);
            }
        });

        doNotifyDataSetChangedOnce = true;
        //  Objects.requireNonNull(container).addView(itemView);
        container.addView(itemView, 0);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}