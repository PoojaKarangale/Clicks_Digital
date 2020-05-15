package com.pakhi.clicksdigital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
String lastActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(StartActivity.this,RegisterActivity.class));
    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        final Class<? extends Activity> activityClass;
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        int activityID = pref.getInt("whichActivity", -1);
        lastActivity=pref.getString("Activity","StartActivity");
     /*   if (activityID  == Constants.ACTIVITY_ID_MAINSCREEN) {
            activityClass = MainScreen.class;
        } else {
            activityClass = null; return;
        }

        Intent newActivity = new Intent(StartActivity.this, Class.forName(lastActivity));
        this.startActivity(newActivity);
    }

 */

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        new AlertDialog.Builder(this)

                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
