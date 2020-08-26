package com.pakhi.clicksdigital.FragmentsInSetting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.pakhi.clicksdigital.R;

public class DeleteAccountFragment extends Fragment {
    View view;

    public DeleteAccountFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_delete_account, container, false);
        return view;
    }
}
