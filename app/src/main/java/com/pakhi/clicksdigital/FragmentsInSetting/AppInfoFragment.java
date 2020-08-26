package com.pakhi.clicksdigital.FragmentsInSetting;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.pakhi.clicksdigital.R;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class AppInfoFragment extends Fragment {
    View     view;
    TextView txtVisit;

    public AppInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_app_info, container, false);
        initializingFields();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            txtVisit.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }

        return view;
    }

    private void initializingFields() {
        txtVisit=view.findViewById(R.id.txtVisit);
    }
}
