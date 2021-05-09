package com.pakhi.clicksdigital.Event;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.pakhi.clicksdigital.Model.Image;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FullScreenGallaryAdapter extends PagerAdapter {

    Context context;
    ArrayList<Image> images;

    LayoutInflater layoutInflater;
    boolean doNotifyDataSetChangedOnce = false;

    FullScreenGallaryAdapter(Context context, ArrayList<Image> images){
        this.images=images;
        this.context=context;
        layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        if (doNotifyDataSetChangedOnce) {
            doNotifyDataSetChangedOnce = false;
            notifyDataSetChanged();
        }
        Log.i("size -----", String.valueOf(images.size()));
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View itemView=layoutInflater.inflate(R.layout.full_screen_image_galary, container, false);

        final PhotoView imageView=  itemView.findViewById(R.id.imageViewMain__);
        //Glide.with(this.context).load(images.get(position).getImage_url()).into(imageView);
        Picasso.get().load(images.get(position).getImage_url()).into(imageView);

        doNotifyDataSetChangedOnce = true;
        container.addView(itemView);

        return itemView;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
