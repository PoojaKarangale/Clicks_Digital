package com.pakhi.clicksdigital.FragmentsInSetting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.pakhi.clicksdigital.R;

public class ContactusFragmentReplacementActivity extends AppCompatActivity {
TextView textVisit,textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactus_fragment_replacement);
        textVisit=findViewById(R.id.txtVisit);
        textView=findViewById(R.id.textView2);


        //getSupportActionBar().setTitle("Contact Us");
    }


    public void finishAcivityContact(View view) {
        finish();
    }
}
