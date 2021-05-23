package com.pakhi.clicksdigital.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pakhi.clicksdigital.R;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class AppInfoFragmentReplacementActivity extends AppCompatActivity {
    View view;
    TextView txtVisit,appName,appVersion,textView;
    ImageView logoImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info_fragment_replacement);
//getSupportActionBar().setTitle("About Us");
        initializingFields();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            txtVisit.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
    }
    private void initializingFields() {
        txtVisit=findViewById(R.id.txtVisit);
        appName=findViewById(R.id.app_name);
        appVersion=findViewById(R.id.app_version);
        logoImage=findViewById(R.id.logo);
        textView = findViewById(R.id.textView);

    }
}
