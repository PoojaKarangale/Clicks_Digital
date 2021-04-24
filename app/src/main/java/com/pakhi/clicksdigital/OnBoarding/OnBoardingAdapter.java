package com.pakhi.clicksdigital.OnBoarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.pakhi.clicksdigital.R;


public class OnBoardingAdapter extends PagerAdapter {

    Context context;
    private LayoutInflater layoutInflater;
    private Integer [] images = {R.drawable.on_1,R.drawable.on_2,R.drawable.on_3,R.drawable.on_4,R.drawable.on_5};
    private String [] heading = {"Welcome to Dialog", "Join Discussion Groups", "Participate & Learn","Network","Create & Join Events"};

    private String [] subTitle = {"A Global Invite-Only Professional Network for Digital Marketing & Technology Professionals!",
    "Once your profile is approved, you can join any/ all of the Discussion Groups. These groups cover categories from the world of DM and Tech only.",
    "Join the discussions in your favorite groups. Share knowledge. Raise Discussion Topics related to the group category. And, enjoy learning through the network.",
    "Connect with other Digital Marketing and Technology Professionals in the community via personal chats and grow your network.",
    "Check out various online/ offline events organized by the community members. Join and learn. Or, Create your own events."};

            OnBoardingAdapter(Context context){
        this.context=context;
    }
    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.on_boarding_separate, null);
        ImageView imageView = view.findViewById(R.id.on_board_image);
        imageView.setImageResource(images[position]);
        TextView headingText = view.findViewById(R.id.on_board_head);
        TextView subTitleText = view.findViewById(R.id.on_board_subtitle);

        headingText.setText(heading[position]);
        subTitleText.setText(subTitle[position]);


        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}
