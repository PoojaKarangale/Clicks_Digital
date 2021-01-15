package com.pakhi.clicksdigital.Fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

class ViewPagerAdapter extends PagerAdapter {

    Context context;

    ArrayList<String> images;
    ArrayList<String> eventName;

    LayoutInflater mLayoutInflater;


    public ViewPagerAdapter(Context context, ArrayList<String> images, ArrayList<String> eventName) {
        this.context = context;
        this.images = images;
        this.eventName = eventName;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        int size = images.size();
        return size;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view ==  object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.item, container, false);

        ImageView imageView =  itemView.findViewById(R.id.imageViewMain);
        TextView nameOfEvent = itemView.findViewById(R.id.name_of_event);
        nameOfEvent.setText(eventName.get(position));
        Picasso.get().load(images.get(position)).into(imageView);

        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        notifyDataSetChanged();

        container.removeView((LinearLayout) object);
    }
}