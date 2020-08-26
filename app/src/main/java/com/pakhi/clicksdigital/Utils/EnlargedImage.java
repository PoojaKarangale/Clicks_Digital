package com.pakhi.clicksdigital.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

public class EnlargedImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlarged_image);

        Intent intent=getIntent();
        String image=intent.getStringExtra(Const.IMAGE_URL);
        //  InputStream is = this.getResources().openRawResource(imageId1);
        // Bitmap originalBitmap = BitmapFactory.decodeStream(is);
        ImageView myimage=(ImageView) findViewById(R.id.imageView);

        Picasso.get().load(image).placeholder(R.drawable.profile_image)
//                .resize(550, 550)
                .into(myimage);

        //  myimage.setImageBitmap(originalBitmap);
        myimage.setScaleType(ImageView.ScaleType.MATRIX);
    }
}
