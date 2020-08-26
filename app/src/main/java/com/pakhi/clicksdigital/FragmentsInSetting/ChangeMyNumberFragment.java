package com.pakhi.clicksdigital.FragmentsInSetting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.RegisterLogin.PhoneVerify;
import com.pakhi.clicksdigital.Utils.Const;

public class ChangeMyNumberFragment extends Fragment {

    String number;
    private EditText mobileNo_reg;
    private View     view;
    private Button   verify;

    public ChangeMyNumberFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_change_my_number, container, false);
        initializingFields();
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number=mobileNo_reg.getText().toString();
                if (TextUtils.isEmpty(number)) {
                    mobileNo_reg.requestFocus();
                    mobileNo_reg.setError("Required");
                } else {
                    sendUserToPhoneVerify();
                }

            }
        });
        return view;
    }

    private void sendUserToPhoneVerify() {
        Intent i=new Intent(getContext(), PhoneVerify.class);
        i.putExtra(Const.MO_NUMBER, number);
        i.putExtra("previousActivity", "changeNumber");
        startActivity(i);
    }

    private void initializingFields() {
        mobileNo_reg=view.findViewById(R.id.mobileNo_reg);
        verify=view.findViewById(R.id.verify);
    }
}
