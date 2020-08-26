package com.pakhi.clicksdigital.Event;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private List<String> imagesUrls;
    private List<String> imagesNames;
    private Context      mContext;

    public ImageAdapter(List<String> imageUrls, List<String> imagesNames, Context mContext) {
        this.imagesUrls=imageUrls;
        this.imagesNames=imagesNames;
        this.mContext=mContext;
    }

    @Override
    public int getCount() {
        return imagesUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        //return imagesUrls.get(position);
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView=(ImageView) convertView;

        if (imageView == null) {
            imageView=new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(350, 450));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }


        Picasso.get().load(imagesUrls.get(position)).placeholder(R.drawable.profile_image).into(imageView);
        //imageView.setImageResource(imagesUrls.get(position));

        return imageView;
    }
}
