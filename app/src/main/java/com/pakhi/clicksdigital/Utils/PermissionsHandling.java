package com.pakhi.clicksdigital.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsHandling {

    Context context;

    public PermissionsHandling(Context context) {
        this.context=context;
    }

    public boolean isPermissionGranted() {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.WRITE_CONTACTS) +
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public boolean isRequestPermissionable() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_CONTACTS) ||
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_CONTACTS) ||
                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA)) {
            return true;
        }
        return false;
    }

    public void showAlertDialog(final int REQUEST_CODE) {
        androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Grant permissioms");
        builder.setMessage("Camera, read & write Contacts, read & write Storage");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.CAMERA
                        },
                        REQUEST_CODE
                );
            }
        });

        //builder.setNegativeButton("Cancel",null);
        androidx.appcompat.app.AlertDialog alertDialog=builder.create();
        alertDialog.show();
    }

    public void requestPermission(int REQUEST_CODE) {
        ActivityCompat.requestPermissions(
                (Activity) context,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.CAMERA
                },
                REQUEST_CODE
        );

    }
    /*      if (ContextCompat.checkSelfPermission(GroupChatActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.READ_CONTACTS) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.WRITE_CONTACTS) +
                ContextCompat.checkSelfPermission(GroupChatActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //when permissions not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.WRITE_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(GroupChatActivity.this, Manifest.permission.CAMERA)) {
                //creating alertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Grant permissioms");
                builder.setMessage("Camera, read & write Contacts, read & write Storage");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        ActivityCompat.requestPermissions(
                                GroupChatActivity.this,
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS,
                                        Manifest.permission.CAMERA
                                },
                                REQUEST_CODE
                        );
                    }
                });

                //builder.setNegativeButton("Cancel",null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else {
                ActivityCompat.requestPermissions(
                        GroupChatActivity.this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS,
                                Manifest.permission.CAMERA
                        },
                        REQUEST_CODE
                );

            }
        } else {
            //when those permissions are already granted
            //popupMenuSettigns();
            logMessage("when those permissions are already granted=----------");
        }*/
}
