package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.pakhi.clicksdigital.Model.Contact;
import com.pakhi.clicksdigital.Model.GroupChat;
import com.pakhi.clicksdigital.R;

import java.util.ArrayList;
import java.util.List;

public class ContactUserAdapter  extends RecyclerView.Adapter<ContactUserAdapter.ViewHolder> {
    ArrayList<Contact> userList;
    private Context mcontext;
    private List<Contact> contacts;
    private FirebaseUser firebaseUser;

    public ContactUserAdapter(Context mcontext, List<Contact> userList) {
        this.mcontext = mcontext;
        this.userList = (ArrayList<Contact>) userList;
    }

    @NonNull
    @Override
    public ContactUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontext)
                .inflate(R.layout.item_group_chat, parent, false);
        return new ContactUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mName.setText(userList.get(position).getName());
        holder.mPhone.setText(userList.get(position).getPhone());

        holder.mAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userList.get(holder.getAdapterPosition()).setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView mName, mPhone;
        LinearLayout mLayout;
        CheckBox mAdd;
        ViewHolder(View view){
            super(view);
            mName = view.findViewById(R.id.display_name);
            mPhone = view.findViewById(R.id.phone);
            mAdd = view.findViewById(R.id.add);
            mLayout = view.findViewById(R.id.main_layout);
        }
    }
}
