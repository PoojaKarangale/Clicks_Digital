package com.pakhi.clicksdigital.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.pakhi.clicksdigital.Profile.VisitProfileActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

class ImageViewPagerAdapter extends PagerAdapter {
    android.content.Context context;
    ArrayList<String> images;
    ArrayList<String> eventName;
    ArrayList<String> uploader;
    LayoutInflater mLayoutInflater;
    FirebaseDatabaseInstance rootRef;
    private boolean doNotifyDataSetChangedOnce = false;

    public ImageViewPagerAdapter(android.content.Context context, ArrayList<String> images, ArrayList<String> eventName, ArrayList<String> uploader) {
        this.context = context;
        this.images = images;
        this.eventName = eventName;
        this.uploader = uploader;
        //  mLayoutInflater=(LayoutInflater) context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
        mLayoutInflater = LayoutInflater.from(context);
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
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.slider_image_home, container, false);

        ImageView imageView = itemView.findViewById(R.id.imageViewMain);
        TextView nameOfEvent = itemView.findViewById(R.id.name_of_event);
        final TextView upload = itemView.findViewById(R.id.upload_value);
        nameOfEvent.setText(eventName.get(position));
        rootRef = FirebaseDatabaseInstance.getInstance();
        rootRef.getUserRef().child(uploader.get(position)).child(ConstFirebase.USER_DETAILS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upload.setText(snapshot.child(ConstFirebase.USER_NAME).getValue().toString() + " " + snapshot.child(ConstFirebase.last_name).getValue().toString());
                //Log.i("snap ----", snapshot.child("user_name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //upload.setText(uploader.get(position));
        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(15)
                .oval(false)
                .build();

        //Picasso.get().load(images.get(position)).transform(new RoundedTransformation(15,0)).into(imageView);

        Glide.with(context).load(images.get(position)).transform(new CenterCrop(), new RoundedCorners(25)).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullScreenTopicImageView.class);
                intent.putExtra(Const.position, position);
                context.startActivity(intent);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VisitProfileActivity.class);
                intent.putExtra(Const.visitUser, uploader.get(position));
                context.startActivity(intent);
            }
        });

        doNotifyDataSetChangedOnce = true;
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}

class FixedSpeedScroller extends Scroller {

    private int mDuration = 1500;

    public FixedSpeedScroller(Context context) {
        super(context);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }


    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}