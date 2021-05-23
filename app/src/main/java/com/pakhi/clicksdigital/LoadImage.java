package com.pakhi.clicksdigital;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.pakhi.clicksdigital.Utils.Const;
import com.squareup.picasso.Picasso;

public class LoadImage extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);
        Toast.makeText(getApplicationContext(), "Loading Image", Toast.LENGTH_LONG).show();
        String image= getIntent().getStringExtra(Const.IMAGE_URL);
        PhotoView photo = findViewById(R.id.load_image);

        /*Glide
                .with(LoadImage.this)
                .load(image)
                .into(photo);*/

        Picasso.get().load(image).into(photo);
    }
}
