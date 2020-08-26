package com.pakhi.clicksdigital.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.ContactUserAdapter;
import com.pakhi.clicksdigital.Model.Contact;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.Const;
import com.pakhi.clicksdigital.Utils.ConstFirebase;
import com.pakhi.clicksdigital.Utils.EnlargedImage;
import com.pakhi.clicksdigital.Utils.FirebaseDatabaseInstance;
import com.pakhi.clicksdigital.Utils.SharedPreference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactUserActivity extends AppCompatActivity {
    //// fields for previous contact list
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS=100;
    ArrayList<Contact> userList, contactList;
    private RecyclerView       recyclerView;
    private ContactUserAdapter contactUserAdapter;
    //  DatabaseReference RootRef;
    ////
    SharedPreference         pref;
    FirebaseDatabaseInstance rootRef;
    private RecyclerView      myContactsList;
    private DatabaseReference ContacsRef, UsersRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_user);
/*
        pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF, 0); // 0 - for private mode

        contactList = new ArrayList<>();
        userList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_contact_user);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        contactUserAdapter = new ContactUserAdapter(this, userList);
        recyclerView.setAdapter(contactUserAdapter);
        RootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        showContacts();
 */
        //  firebaseAuth = FirebaseAuth.getInstance();

        pref=SharedPreference.getInstance();
        currentUserID=pref.getData(SharedPreference.currentUserId, getApplicationContext());

        rootRef=FirebaseDatabaseInstance.getInstance();

        ContacsRef=rootRef.getContactRef().child(currentUserID);
        UsersRef=rootRef.getUserRef();

        myContactsList=findViewById(R.id.recycler_contact_user);
        myContactsList.setLayoutManager(new LinearLayoutManager(ContactUserActivity.this));
    }

    private void getContactList() {

        //String ISOPrefix = pref.getString("country_ISO","+91");
        String ISOPrefix="+91";

        Cursor phones=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone=phone.replace(" ", "");
            phone=phone.replace("-", "");
            phone=phone.replace("(", "");
            phone=phone.replace(")", "");

            if (!String.valueOf(phone.charAt(0)).equals("+"))
                phone=ISOPrefix + phone;

            Contact mContact=new Contact("", name, phone);
            contactList.add(mContact);
            getUserDetails(mContact);
        }
    }

    private void getUserDetails(Contact mContact) {
        DatabaseReference mUserDB=rootRef.getUserRef();
        Query query=mUserDB.orderByChild(ConstFirebase.number).equalTo(mContact.getNumber());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone="", name="";
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child(ConstFirebase.number).getValue() != null) {
                            phone=snapshot.child(ConstFirebase.number).getValue().toString().replace(" ", "");
                        }
                        if (snapshot.child(Const.USER_NAME).getValue() != null) {
                            phone=snapshot.child(Const.USER_NAME).getValue().toString();
                        }
                        Contact contact=new Contact(snapshot.getKey(), name, phone);

                        userList.add(contact);

                        contactUserAdapter.notifyDataSetChanged();
                    }
                }
                // contactUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            getContactList();
        }
    }

  /*  private void createChat() {
        String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();
        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("Chat").child(key).child("info");
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");

        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("Users/" + currentUserID, true);

        Boolean validChat = false;
        for (Contact mUser : userList) {
            if (mUser.getSelected()) {
                validChat = true;
                newChatMap.put("Users/" + mUser.getUid(), true);
                userDb.child(mUser.getUid()).child("chat").child(key).setValue(true);
            }
        }

        if (validChat) {
            chatInfoDb.updateChildren(newChatMap);
            userDb.child(currentUserID).child("chat").child(key).setValue(true);
        }

    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
     /*   if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                //showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
        */
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("online");


        FirebaseRecyclerOptions options=
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(ContacsRef, Contact.class)
                        .build();


        final FirebaseRecyclerAdapter<Contact, ContactsViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contact model) {
                final String userIDs=getRef(position).getKey();
                final String[] userImage=new String[1];

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatActivity=new Intent(ContactUserActivity.this, ChatActivity.class);
                        chatActivity.putExtra("visit_user_id", userIDs);
                        startActivity(chatActivity);
                    }
                });

                UsersRef.child(userIDs).child(Const.USER_DETAILS).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //retrive user details
                        userImage[0]=dataSnapshot.child(Const.IMAGE_URL).getValue().toString();
                        Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        String profileName=dataSnapshot.child(Const.USER_NAME).getValue().toString();
                        String profileStatus=dataSnapshot.child(Const.USER_BIO).getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(profileStatus);

                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent fullScreenIntent=new Intent(v.getContext(), EnlargedImage.class);
                                fullScreenIntent.putExtra(Const.IMAGE_URL, userImage[0]);
                                v.getContext().startActivity(fullScreenIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            //retrive user state
                            if (dataSnapshot.child("userState").hasChild("state")) {
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                } else if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_user, viewGroup, false);
                ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();

    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        rootRef.getUserRef().child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView       onlineIcon;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.display_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.image_profile);
            onlineIcon=(ImageView) itemView.findViewById(R.id.user_online_status);
            // onlineIcon.setVisibility(View.VISIBLE);
        }
    }
}