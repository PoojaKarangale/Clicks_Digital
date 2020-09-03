package com.pakhi.clicksdigital.Profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pakhi.clicksdigital.Model.Certificates;
import com.pakhi.clicksdigital.R;

import java.util.List;


public class ShowCertificatesFragment extends Fragment {
    View view;
    private List<Certificates>         certificates;
    private RecyclerView               recyclerView;
    private RecyclerView.Adapter       mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    public ShowCertificatesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_show_certificates, container, false);

        //Bundle arguments;
        Bundle bundle=this.getArguments();
        if (bundle != null)
            certificates=(List<Certificates>) bundle.getSerializable("certificates");

        recyclerView=(RecyclerView) view.findViewById(R.id.certificates_list);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager=new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter=new MyAdapter(certificates);
        recyclerView.setAdapter(mAdapter);
        return view;
    }

}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Certificates> values;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<Certificates> myDataset) {
        values=myDataset;
    }

    /*   public void add(int position, String item) {
           values.add(position, item);
           notifyItemInserted(position);
       }

       public void remove(int position) {
           values.remove(position);
           notifyItemRemoved(position);
       }

       // Create new views (invoked by the layout manager)*/
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        LayoutInflater inflater=LayoutInflater.from(
                parent.getContext());
        View v=
                inflater.inflate(R.layout.item_certificate, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh=new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Certificates certificate=values.get(position);
        holder.txtHeader.setText(certificate.getName());
        holder.txtFooter.setText(certificate.getInstitute());
        if (holder.certi != null)
            holder.certi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (certificate.getFileUri().equals("")) {
                        Toast.makeText(view.getContext(), "No Certificates Provided", Toast.LENGTH_SHORT).show();
                    } else {

                        Uri uri=Uri.parse(certificate.getFileUri()); // missing 'http://' will cause crashed
                        Intent intent=new Intent(Intent.ACTION_VIEW, uri);
                        view.getContext().startActivity(intent);
                    }
                }
            });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtHeader;
        public TextView txtFooter;
        public View     layout;
        ImageView certi;

        public ViewHolder(View v) {
            super(v);
            layout=v;
            txtHeader=(TextView) v.findViewById(R.id.name);
            txtFooter=(TextView) v.findViewById(R.id.institute);
            certi=v.findViewById(R.id.certi);
        }
    }

}
