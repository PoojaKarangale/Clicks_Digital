package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.pakhi.clicksdigital.Model.Contact;
import com.pakhi.clicksdigital.PersonalChat.ChatActivity;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;

public class ContactUserAdapter extends RecyclerView.Adapter<ContactUserAdapter.ViewHolder> {
    ArrayList<Contact> userList;
    private Context       mcontext;
    private List<Contact> contacts;
    private FirebaseUser  firebaseUser;

    public ContactUserAdapter(Context mcontext, List<Contact> userList) {
        this.mcontext=mcontext;
        this.userList=(ArrayList<Contact>) userList;
    }

    @NonNull
    @Override
    public ContactUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(mcontext)
                .inflate(R.layout.item_user, parent, false);
        return new ContactUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.mName.setText(userList.get(position).getUser_name());
        holder.mPhone.setText(userList.get(position).getNumber());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid=userList.get(position).getUid();
                Intent chatActivityIntent=new Intent(mcontext, ChatActivity.class);
                chatActivityIntent.putExtra("user_id", uid);

                chatActivityIntent.putExtra("visit_user_id", uid);
                chatActivityIntent.putExtra("visit_user_name", userList.get(position).getUser_name());
                //  chatIntent.putExtra("visit_image", retImage[0]);
                mcontext.startActivity(chatActivityIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mName, mPhone;
        LinearLayout mLayout;

        ViewHolder(View view) {
            super(view);
            mName=view.findViewById(R.id.display_name);
            mPhone=view.findViewById(R.id.number);
            mLayout=view.findViewById(R.id.main_layout);
            mPhone.setVisibility(View.VISIBLE);
        }
    }
}
