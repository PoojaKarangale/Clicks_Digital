package com.pakhi.clicksdigital.Activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.Adapter.GroupMembersAdapter;
import com.pakhi.clicksdigital.Model.GroupChat;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GroupMembersActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentGroupId, group_image_url, group_name_str;
    ImageView app_bar_image, set_group_name, change_group_icon, edit_group_name,add_member;
    EditText get_group_name;
    TextView group_name, group_info, group_description, number_of_participants;
    FirebaseAuth firebaseAuth;
    String user_type;
    Uri imageUriGalary, imageUriCamera;
    long number_of_participants_in_number;
    GroupChat group;
    private RecyclerView memberListRecyclerView;
    private DatabaseReference groupMembersRef, UsersRef, GroupRef;
    private GroupMembersAdapter groupMembersAdapter;
    private List<User> members;
Button exit_group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        group_image_url = getIntent().getStringExtra("image_url");
        group_name_str = getIntent().getStringExtra("group_name");
        currentGroupId = getIntent().getStringExtra("group_id");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        groupMembersRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupId).child("Users");
        firebaseAuth = FirebaseAuth.getInstance();

        //change_group_icon = findViewById(R.id.change_group_icon);
        exit_group = findViewById(R.id.exit_group);
        add_member = findViewById(R.id.add_member);
        edit_group_name = findViewById(R.id.edit_group_name);
        group_name = findViewById(R.id.group_name);
        set_group_name = findViewById(R.id.set_group_name);
        get_group_name = findViewById(R.id.get_group_name);
        app_bar_image = findViewById(R.id.app_bar_image);
        group_info = findViewById(R.id.group_info);
        group_description = findViewById(R.id.group_description);
        number_of_participants = findViewById(R.id.number_of_participants);

        //seting group info
       Picasso.get().load(group_image_url).placeholder(R.drawable.default_profile_for_groups).into(app_bar_image);
      /*  group_name.setText(group_name_str);
        get_group_name.setText(group_name_str);

     */
        final String[] date = new String[1];
        final String[] group_creater_id = new String[1];
        Log.d("GroupMebersTESTING", GroupRef.toString() + "------------" + currentGroupId.toString());

        GroupRef.child(currentGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("GroupMebersTESTING", dataSnapshot.toString());

               group = dataSnapshot.getValue(GroupChat.class);

                group_name.setText(group.getGroup_name());
                get_group_name.setText(group.getGroup_name());
                group_description.setText(group.getDescription());
                Picasso.get().load(group.getImage_url())
                        .placeholder(R.drawable.default_profile_for_groups)
                        .into(app_bar_image);

                Log.d("GroupMebersTESTING", group.getGroup_name());

                date[0] = dataSnapshot.child("date").getValue().toString();

                Log.d("GroupMebersTESTING", dataSnapshot.child("uid_creater").getValue().toString());
                group_creater_id[0] = dataSnapshot.child("uid_creater").getValue().toString();
                UsersRef.child(group_creater_id[0])
                        .child("user_name")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                group_info.setText("Created by " + dataSnapshot.getValue().toString() + ", on " + date[0]);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersRef.child(firebaseAuth.getCurrentUser().getUid())
                .child("user_type")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            user_type = dataSnapshot.getValue().toString();

                            if (user_type.equals("admin")) {
                                //only adim can edit group info
                               edit_group_name.setVisibility(View.VISIBLE);
                                add_member.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

       edit_group_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                group_name.setVisibility(View.GONE);
                get_group_name.setVisibility(View.VISIBLE);
                set_group_name.setVisibility(View.VISIBLE);
            }
        });

        memberListRecyclerView = findViewById(R.id.memberList);
        memberListRecyclerView.setHasFixedSize(true);
        memberListRecyclerView.setLayoutManager(new LinearLayoutManager(GroupMembersActivity.this));

        members = new ArrayList<>();

        groupMembersAdapter = new GroupMembersAdapter(this, members);
        memberListRecyclerView.setAdapter(groupMembersAdapter);

        readGroupMembers();

      //  number_of_participants.setText(String.valueOf(number_of_participants_in_number) + " participants");

    }

    public void setGroupName(View view) {
        GroupRef.child(currentGroupId)
                .child("group_name")
                .setValue(get_group_name.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        set_group_name.setVisibility(View.GONE);
                        get_group_name.setVisibility(View.GONE);
                        group_name.setVisibility(View.VISIBLE);
                        Toast.makeText(GroupMembersActivity.this, "Group name updated successfully", Toast.LENGTH_SHORT);
                    }
                });

    }

    public void changeGroupIcon(View view) {
        CharSequence options[] = new CharSequence[]
                {
                        "Gallary",
                        "Camera"
                        //, "Remove photo"
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupMembersActivity.this);
        // builder.setTitle(requestUserName  + "  Chat Request");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        openGalary();
                        break;
                    case 1:
                        openCamera();
                        break;
                    // case 2: removePhotoSetDefault(); break;
                }
            }
        });
        builder.show();
    }

    void openGalary() {

        CropImage.activity().setAspectRatio(1, 1)
                .start(GroupMembersActivity.this);

    }

    void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    imageUriGalary = result.getUri();

                    uploadImage(imageUriGalary);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera = Uri.parse(path);

                    uploadImage(imageUriCamera);
                    break;
            }
        } else {
        }

    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(final Uri imageUri) {
        StorageReference sReference = FirebaseStorage.getInstance().getReference().child("Group_photos").child("Group_profile");
        final StorageReference imgPath = sReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));

        imgPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        GroupRef.child(currentGroupId)
                                .child("image_url")
                                .setValue(uri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        app_bar_image.setImageURI(uri);
                                        Toast.makeText(GroupMembersActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT);
                                    }
                                });
                    }
                });

            }
        });
    }

    public void backToGroupChat(View view) {
        Intent groupMembersIntent = new Intent(GroupMembersActivity.this, GroupChatActivity.class);
        groupMembersIntent.putExtra("group_id", currentGroupId);
        groupMembersIntent.putExtra("group_name", group_name_str);
        startActivity(groupMembersIntent);
    }

    public void exitGroup(View view) {
        groupMembersRef.child(firebaseAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                UsersRef.child(firebaseAuth.getCurrentUser().getUid()).child("groups").child(currentGroupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        exit_group.setEnabled(false);
                        exit_group.setTextColor(Color.rgb(128,128,128));
                        Toast.makeText(GroupMembersActivity.this, "Group left", Toast.LENGTH_SHORT);

                    }
                });
            }
        });


    }

    private void readGroupMembers() {
        groupMembersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                number_of_participants_in_number = dataSnapshot.getChildrenCount();
                 number_of_participants.setText(String.valueOf(number_of_participants_in_number) + " participants");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    members.clear();
                    UsersRef.child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            members.add(user);
                            groupMembersAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    // groupMembersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void add_member(View view) {

    }
}