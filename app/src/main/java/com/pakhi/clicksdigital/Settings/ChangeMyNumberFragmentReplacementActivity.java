package com.pakhi.clicksdigital.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.RegisterLogin.PhoneVerify;
import com.pakhi.clicksdigital.Utils.Const;

public class ChangeMyNumberFragmentReplacementActivity extends AppCompatActivity {
    String number;
    private EditText mobileNo_reg;
    private View     view;
    private Button verify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_my_number_fragment_replacement);
        //getSupportActionBar().setTitle("Change my Number");

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
    }
    private void sendUserToPhoneVerify() {
        Intent i=new Intent(ChangeMyNumberFragmentReplacementActivity.this, PhoneVerify.class);
        i.putExtra(Const.number, number);
        i.putExtra(Const.PreviousActivity, "changeNumber");
        startActivity(i);
    }

    private void initializingFields() {
        mobileNo_reg=findViewById(R.id.mobileNo_reg);
        verify=findViewById(R.id.verify);
    }
}
