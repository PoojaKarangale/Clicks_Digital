package com.pakhi.clicksdigital.GroupChat;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pakhi.clicksdigital.HelperClasses.UserDatabase;
import com.pakhi.clicksdigital.Model.Group;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE=1;
    String currentGroupId, group_image_url, group_name_str;
    ImageView app_bar_image, set_group_name, set_description, edit_group_name, edit_description, add_member;
    EditText get_group_name, get_description;
    TextView group_name, group_info, group_description, number_of_participants;
    String user_type;
    Uri    imageUriGalary, imageUriCamera;
    long                     number_of_participants_in_number;
    Group                    group;
    Button                   exit_group;
    UserDatabase             db;
    User                     user;
    FirebaseDatabaseInstance rootRef;
    SharedPreference         pref;
    private RecyclerView      memberListRecyclerView;
    private DatabaseReference groupMembersRef, UsersRef, GroupRef;
    private GroupMembersAdapter groupMembersAdapter;
    private List<User>          members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        group_image_url=getIntent().getStringExtra("image_url");
        group_name_str=getIntent().getStringExtra("group_name");
        currentGroupId=getIntent().getStringExtra("group_id");

        pref=SharedPreference.getInstance();
        rootRef=FirebaseDatabaseInstance.getInstance();
        UsersRef=rootRef.getUserRef();
        GroupRef=rootRef.getGroupRef();

        groupMembersRef=GroupRef.child(currentGroupId).child("Users");
        db=new UserDatabase(this);
        getUserFromDb();

        initiateFields();

        //seting group info
        Picasso.get().load(group_image_url).placeholder(R.drawable.default_profile_for_groups).into(app_bar_image);

        final String[] date=new String[1];
        final String[] group_creater_id=new String[1];

        GroupRef.child(currentGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                group=dataSnapshot.getValue(Group.class);

                group_name.setText(group.getGroup_name());
                get_group_name.setText(group.getGroup_name());
                get_description.setText(group.getDescription());
                group_description.setText(group.getDescription());
                Picasso.get().load(group.getImage_url())
                        .placeholder(R.drawable.default_profile_for_groups)
                        .into(app_bar_image);

                date[0]=dataSnapshot.child("date").getValue().toString();

                group_creater_id[0]=dataSnapshot.child("uid_creater").getValue().toString();

             /*   UsersRef.child(group_creater_id[0])
                        .child(Const.USER_DETAILS)
                        .child(Const.USER_NAME)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                group_info.setText("Created by " + dataSnapshot.getValue().toString() + ", on " + date[0]);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        String user_type=pref.getData(SharedPreference.user_type, getApplicationContext());
        if (user_type.equals("admin")) {
            //only adim can edit group info
            edit_group_name.setVisibility(View.VISIBLE);
            edit_description.setVisibility(View.VISIBLE);
            add_member.setVisibility(View.VISIBLE);
        }
        edit_group_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_group_name.setVisibility(View.GONE);
                group_name.setVisibility(View.GONE);
                get_group_name.setVisibility(View.VISIBLE);
                set_group_name.setVisibility(View.VISIBLE);
            }
        });
        edit_description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_description.setVisibility(View.GONE);
                group_description.setVisibility(View.GONE);
                get_description.setVisibility(View.VISIBLE);
                set_description.setVisibility(View.VISIBLE);
            }
        });

        memberListRecyclerView=findViewById(R.id.memberList);
        memberListRecyclerView.setHasFixedSize(true);
        memberListRecyclerView.setLayoutManager(new LinearLayoutManager(GroupDetailsActivity.this));

        members=new ArrayList<>();

        groupMembersAdapter=new GroupMembersAdapter(this, members, currentGroupId);
        memberListRecyclerView.setAdapter(groupMembersAdapter);

        readGroupMembers();
        //  number_of_participants.setText(String.valueOf(number_of_participants_in_number) + " participants");
    }

    private void initiateFields() {
        exit_group=findViewById(R.id.exit_group);
        add_member=findViewById(R.id.add_member);
        edit_group_name=findViewById(R.id.edit_group_name);
        edit_description=findViewById(R.id.edit_description);
        group_name=findViewById(R.id.group_name);
        set_group_name=findViewById(R.id.set_group_name);
        set_description=findViewById(R.id.set_description);
        get_group_name=findViewById(R.id.get_group_name);
        get_description=findViewById(R.id.get_description);
        app_bar_image=findViewById(R.id.app_bar_image);
        group_info=findViewById(R.id.group_info);
        group_description=findViewById(R.id.group_description);
        number_of_participants=findViewById(R.id.number_of_participants);
    }

    private void getUserFromDb() {
        db.getReadableDatabase();
        Cursor res=db.getAllData();
        if (res.getCount() == 0) {

        } else {
            res.moveToFirst();
            user=new User(res.getString(0), res.getString(1),
                    res.getString(2), res.getString(3), res.getString(4),
                    res.getString(5), res.getString(6), res.getString(7),
                    res.getString(8), res.getString(9), res.getString(10),
                    res.getString(11), res.getString(12), res.getString(13),
                    res.getString(14));
        }
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
                        Toast.makeText(GroupDetailsActivity.this, "Group name updated successfully", Toast.LENGTH_SHORT);
                    }
                });

    }

    public void setDescription(View view) {
        GroupRef.child(currentGroupId)
                .child("description")
                .setValue(get_description.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        set_description.setVisibility(View.GONE);
                        get_description.setVisibility(View.GONE);
                        //group_name.setVisibility(View.VISIBLE);
                        Toast.makeText(GroupDetailsActivity.this, "Group description updated successfully", Toast.LENGTH_SHORT);
                    }
                });
    }

    public void changeGroupIcon(View view) {
        CharSequence options[]=new CharSequence[]
                {
                        "Gallary",
                        "Camera"
                        //, "Remove photo"
                };

        AlertDialog.Builder builder=new AlertDialog.Builder(GroupDetailsActivity.this);
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
                .start(GroupDetailsActivity.this);

    }

    void openCamera() {
        Intent takePictureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                    CropImage.ActivityResult result=CropImage.getActivityResult(data);
                    imageUriGalary=result.getUri();

                    uploadImage(imageUriGalary);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras=data.getExtras();
                    Bitmap imageBitmap=(Bitmap) extras.get("data");
                    ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path=MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "Title", null);
                    imageUriCamera=Uri.parse(path);

                    uploadImage(imageUriCamera);
                    break;
            }
        } else {
        }

    }

    String getFileExtention(Uri uri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(final Uri imageUri) {
        StorageReference sReference=FirebaseStorage.getInstance().getReference().child("Group_photos").child("Group_profile");
        final StorageReference imgPath=sReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));

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
                                        Toast.makeText(GroupDetailsActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT);
                                    }
                                });
                    }
                });

            }
        });
    }

    public void backToGroupChat(View view) {
        Intent groupMembersIntent=new Intent(GroupDetailsActivity.this, GroupChatActivity.class);
        groupMembersIntent.putExtra("group_id", currentGroupId);
        groupMembersIntent.putExtra("group_name", group_name_str);
        startActivity(groupMembersIntent);
    }

    public void exitGroup(View view) {
        final String uid=user.getUser_id();
        groupMembersRef.child(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                UsersRef.child(uid).child("groups").child(currentGroupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        exit_group.setEnabled(false);
                        exit_group.setTextColor(Color.rgb(128, 128, 128));
                        Toast.makeText(GroupDetailsActivity.this, "Group left", Toast.LENGTH_SHORT);

                    }
                });
            }
        });
        GroupRef.child(currentGroupId).child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GroupRef.child(currentGroupId).child("Users").child(uid).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readGroupMembers() {
        groupMembersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                number_of_participants_in_number=dataSnapshot.getChildrenCount();
                number_of_participants.setText(String.valueOf(number_of_participants_in_number) + " participants");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    members.clear();
                    UsersRef.child(snapshot.getKey()).child(Const.USER_DETAILS).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user=dataSnapshot.getValue(User.class);
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
        Intent addMembersIntent=new Intent(this, AddMembersToGroupActivity.class);
        addMembersIntent.putExtra("current_group_id", currentGroupId);
        startActivity(addMembersIntent);
    }
}
