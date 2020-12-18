package com.pakhi.clicksdigital.JoinGroup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.pakhi.clicksdigital.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinAdapaterReplacement extends FirebaseRecyclerAdapter<ModelJoinGroup,JoinAdapaterReplacement.myViewHolder> {
    public JoinAdapaterReplacement(@NonNull FirebaseRecyclerOptions<ModelJoinGroup> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull ModelJoinGroup model) {

        holder.grpName.setText(model.getGrpName());
       // Glide


    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_join_group,parent,false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        CircleImageView img;
        TextView grpName;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.image_of_grp);
            grpName=itemView.findViewById(R.id.grp_name);
        }
    }

    }

