package com.pakhi.clicksdigital.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class ImageViewPagerAdapter extends PagerAdapter {
    android.content.Context context;
    ArrayList<String>       images;
    ArrayList<String>       eventName;
    LayoutInflater          mLayoutInflater;
    private boolean doNotifyDataSetChangedOnce=false;

    public ImageViewPagerAdapter(android.content.Context context, ArrayList<String> images, ArrayList<String> eventName) {
        this.context=context;
        this.images=images;
        this.eventName=eventName;
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
        nameOfEvent.setText(eventName.get(position));
        Picasso.get().load(images.get(position)).into(imageView);
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