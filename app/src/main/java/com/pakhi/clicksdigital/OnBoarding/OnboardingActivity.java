package com.pakhi.clicksdigital.OnBoarding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pakhi.clicksdigital.Activities.StartActivity;
import com.pakhi.clicksdigital.Profile.SetProfileActivity;
import com.pakhi.clicksdigital.R;

public class OnboardingActivity extends AppCompatActivity {

    ViewPager viewPager;
    LinearLayout sliderDotspanel;
    private ImageView[] dots;
    Button proceedButton;
    boolean profileCreatedOrNot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        profileCreatedOrNot=getIntent().getBooleanExtra("profileCreatedOrNot", false);

        viewPager = findViewById(R.id.on_boarding);
        sliderDotspanel = findViewById(R.id.dots);
        proceedButton = findViewById(R.id.proceed_btton);

        OnBoardingAdapter onBoardingAdapter = new OnBoardingAdapter(this);

        viewPager.setAdapter(onBoardingAdapter);

        dots = new ImageView[5];
        for(int i = 0; i < 5; i++){

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.not_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            sliderDotspanel.addView(dots[i], params);

        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                for(int i = 0; i< 5; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.not_active_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));
                if(position==4){
                    proceedButton.setEnabled(true);
                    proceedButton.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profileCreatedOrNot){
                    Intent intent = new Intent(OnboardingActivity.this, StartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(OnboardingActivity.this, SetProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);
                    finish();
                }

            }
        });

    }
}
