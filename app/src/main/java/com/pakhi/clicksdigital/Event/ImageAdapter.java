package com.pakhi.clicksdigital.Event;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.pakhi.clicksdigital.Model.Event;
import com.pakhi.clicksdigital.Model.Image;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.EnlargedImage;


import java.io.Serializable;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<Image> images;
    Context     context;
    Event event;

    public ImageAdapter(Context context, List<Image> images, Event event) {
        this.images=images;
        this.context=context;
        this.event=event;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(context)
                .inflate(R.layout.item_gallery, parent, false);

        return new ImageAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, final int position) {


        Glide.with(context).load(images.get(position).getImage_url())
                .transform(new CenterCrop(), new RoundedCorners(5)).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(context, FullGalaryView.class);
                intent.putExtra("position", position);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST",(Serializable)images);
                intent.putExtra("BUNDLE",args);

                context.startActivity(intent);*/
                Intent intent = new Intent(context, FullGalaryView.class);
                intent.putExtra("position", position);
                intent.putExtra(Const.event, event);
                context.startActivity(intent);
                //EnlargedImage.enlargeImage(images.get(position).getImage_url(), v.getContext());

            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.item_img);
        }
    }


}