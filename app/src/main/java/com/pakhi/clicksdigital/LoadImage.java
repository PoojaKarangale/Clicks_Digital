package com.pakhi.clicksdigital;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

//import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class LoadImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);
        String image= getIntent().getStringExtra("image_url");
        PhotoView photo = findViewById(R.id.load_image);

        /*Glide
                .with(LoadImage.this)
                .load(image)
                .into(photo);*/

        Picasso.get().load(image).into(photo);
    }
}
