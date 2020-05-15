package com.pakhi.clicksdigital;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

public class RegisterActivity extends AppCompatActivity {
    private String number;
    private CountryCodePicker ccp;
    private EditText mobileNo_reg;
    private Button verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mobileNo_reg = (EditText) findViewById(R.id.mobileNo_reg);
        verify = findViewById(R.id.verify);
        ccp = findViewById(R.id.ccp);

        //number="+918380952992";
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ccp.registerCarrierNumberEditText(mobileNo_reg);
                String num=mobileNo_reg.getText().toString().trim();
                number = ccp.getDefaultCountryCodeWithPlus()+""+num;
                Log.d("RegisterActivity",number+"----------------------------");
                if(TextUtils.isEmpty(num)){
                    mobileNo_reg.setFocusable(true);
                    mobileNo_reg.setError("mobile number is required");
                   // return;
                }else{
                    Intent i = new Intent(RegisterActivity.this, PhoneVerify.class);
                    i.putExtra("PhoneNumber", number);
                    startActivity(i);
                    //finish();
                }
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

}