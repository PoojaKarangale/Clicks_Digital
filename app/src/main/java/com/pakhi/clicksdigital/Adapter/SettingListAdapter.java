package com.pakhi.clicksdigital.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pakhi.clicksdigital.R;

public class SettingListAdapter extends BaseAdapter {
    String[] title;
    int[]    image_resources;
    private Context mcontext;
    LayoutInflater inflater;

    public SettingListAdapter(@NonNull Context context, String[] title, int[] image_resources) {
        this.mcontext=context;
        this.title=title;
        this.image_resources=image_resources;
        inflater=(LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view=inflater.inflate(R.layout.item_list_view_settings, null);
        ImageView row_image=view.findViewById(R.id.row_image);
        TextView row_title=view.findViewById(R.id.row_title);

        row_image.setImageResource(image_resources[position]);
        row_title.setText(title[position]);

        return view;
    }
}
