package com.pakhi.clicksdigital.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.rengwuxian.materialedittext.MaterialEditText;

public class AddNewCertificateActivity extends AppCompatActivity {
    final static int PICK_PDF_CODE=2342;
    String currentUserId, fileUri="";

    StorageReference storageRootReference;
    String           name, institute;
    private MaterialEditText name_of_certificate, name_of_institute;
    private Button choose_certificate, add_certificate;
    private ProgressDialog progressDialog;
    private Certificates   certificate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_certificate);

        SharedPreference pref=SharedPreference.getInstance();
        currentUserId=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        storageRootReference=FirebaseStorage.getInstance().getReference();

        name_of_certificate=findViewById(R.id.name_of_certificate);
        name_of_institute=findViewById(R.id.name_of_institute);
        choose_certificate=findViewById(R.id.choose_certificate);
        add_certificate=findViewById(R.id.add_certificate);
        progressDialog=new ProgressDialog(AddNewCertificateActivity.this);
        progressDialog.setMessage("Loading...");

        choose_certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // progressDialog.show();
                getPDF();
            }
        });

        add_certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=name_of_certificate.getText().toString();
                institute=name_of_institute.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(AddNewCertificateActivity.this, "name of certificate cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    certificate=new Certificates(name, institute, fileUri);
                    Intent sendDataBackIntent=new Intent();
                    //sendDataBackIntent.putExtra("certificate", certificate);
                    sendDataBackIntent.putExtra("certificate", certificate);
                    setResult(RESULT_OK, sendDataBackIntent);
                    finish();
                }
            }
        });
    }

    private void getPDF() {
        //for greater than lolipop versions we need the permissions asked on runtime
        //so if the permission is not available user will go to the screen to allow storage permission
        Toast.makeText(this, "Allow all the required permissions for this app", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        //creating an intent for file chooser
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PDF_CODE);
    }

    private void uploadFile(Uri data) {
        Toast.makeText(this, "Wait for file to be uploaded", Toast.LENGTH_SHORT).show();
        progressDialog.show();
        StorageReference sRef=storageRootReference.child(Const.USER_MEDIA_PATH).child(currentUserId).child("Files/" + name + " " + System.currentTimeMillis() + ".pdf");
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        fileUri=String.valueOf(uri);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_PDF_CODE:
                    if (data.getData() != null) {
                        uploadFile(data.getData());
                    }
                    break;
                default:
                    Toast.makeText(this, "nothing is selected", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
