package com.pakhi.clicksdigital.Activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pakhi.clicksdigital.Adapter.ContactUserAdapter;
import com.pakhi.clicksdigital.Model.Contact;
import com.pakhi.clicksdigital.Model.User;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactUserActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    ArrayList<Contact> userList, contactList;
    SharedPreferences pref;
    private RecyclerView recyclerView;
    private ContactUserAdapter contactUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_user);

        pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF, 0); // 0 - for private mode

        contactList = new ArrayList<>();
        userList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_contact_user);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        contactUserAdapter = new ContactUserAdapter(this, userList);
        recyclerView.setAdapter(contactUserAdapter);


        showContacts();

    }

    private void getContactList() {

        //String ISOPrefix = pref.getString("country_ISO","+91");
        String ISOPrefix = "+91";

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if (!String.valueOf(phone.charAt(0)).equals("+"))
                phone = ISOPrefix + phone;

            Contact mContact = new Contact("", name, phone);
            contactList.add(mContact);
            getUserDetails(mContact);
        }
    }

    private void getUserDetails(Contact mContact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = mUserDB.orderByChild("number").equalTo(mContact.getNumber());
        Log.d("ContactUser", "--------query-------------------------------" + mContact.getNumber());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String phone="",name="";
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        if(snapshot.child("number").getValue()!=null){
                            phone=snapshot.child("number").getValue().toString().replace(" ","");
                        }
                        if(snapshot.child(Constants.USER_NAME).getValue()!=null){
                            phone=snapshot.child(Constants.USER_NAME).getValue().toString();
                        }
                        Contact contact=new Contact(snapshot.getKey(),name,phone);
                        userList.add(contact);
                        //contactUserAdapter.notifyDataSetChanged();
                    }
                }
                contactUserAdapter.notifyDataSetChanged();
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

    private void createChat() {
        String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();
        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("Chat").child(key).child("info");
        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference().child("user");

        HashMap newChatMap = new HashMap();
        newChatMap.put("id", key);
        newChatMap.put("Users/" + FirebaseAuth.getInstance().getUid(), true);

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
            userDb.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
                // getContactList();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

}