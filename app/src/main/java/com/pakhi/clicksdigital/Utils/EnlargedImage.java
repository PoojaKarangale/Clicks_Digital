package com.pakhi.clicksdigital.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;

public class EnlargedImage extends AppCompatActivity {

    public static void enlargeImage(String s, Context context) {
        Intent fullScreenIntent=new Intent(context, EnlargedImage.class);
        fullScreenIntent.putExtra(Const.IMAGE_URL, s);
        context.startActivity(fullScreenIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlarged_image);

        Intent intent=getIntent();
        String image=intent.getStringExtra(Const.IMAGE_URL);
        ImageView myimage=(ImageView) findViewById(R.id.imageView);

        Picasso.get().load(image).placeholder(R.drawable.profile_image)
                .into(myimage);

        myimage.setScaleType(ImageView.ScaleType.MATRIX);
    }
}
