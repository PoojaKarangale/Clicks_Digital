package com.pakhi.clicksdigital.Event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pakhi.clicksdigital.Model.Image;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    List<Image> images;
    Context     context;

    public ImageAdapter(Context context, List<Image> images) {
        this.images=images;
        this.context=context;
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
        Picasso.get()
                .load(images.get(position).getImage_url())
                .into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnlargedImage.enlargeImage(images.get(position).getImage_url(), v.getContext());

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