package com.pakhi.clicksdigital.FragmentsInSetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.pakhi.clicksdigital.R;

public class ContactUsFragment extends Fragment {
    View view;

    public ContactUsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_contact_us, container, false);
       /* // 1) How to replace link by text like "Click Here to visit Google" and
        // the text is linked with the website url ?
        TextView link = (TextView) findViewById(R.id.textView1);
        String linkText = "Visit the <a href='http://stackoverflow.com'>StackOverflow</a> web page.";
        link.setText(Html.fromHtml(linkText));
        link.setMovementMethod(LinkMovementMethod.getInstance());
        // 2) How to place email address
        TextView email = (TextView) findViewById(R.id.textView2);
        String emailText = "Send email: <a href=\"mailto:person@stackoverflow.com\">Click Me!</a>";
        email.setText(Html.fromHtml(emailText));
        email.setMovementMethod(LinkMovementMethod.getInstance());*/
        return view;
    }
}
