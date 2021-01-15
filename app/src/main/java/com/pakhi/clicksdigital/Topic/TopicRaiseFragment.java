package com.pakhi.clicksdigital.Topic;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pakhi.clicksdigital.R;
import com.pakhi.clicksdigital.Utils.ToastClass;

public class TopicRaiseFragment extends Fragment {

    private ImageView            close;
    private FloatingActionButton fab_publish;
    private View                 view;
    private EditText             topic;

    public TopicRaiseFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_topic_raise, container, false);
        initialiseFields();
        fab_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic_str=topic.getText().toString();
                if (TextUtils.isEmpty(topic_str)) {
                    returnResultToGroupChat();
                }
                else ToastClass.makeText(getContext(),"Topic cannot be empty");
            }
        });
        return view;
    }

    private void returnResultToGroupChat() {

    }

    private void initialiseFields() {
     /*   close=view.findViewById(R.id.close);
        fab_publish=view.findViewById(R.id.fab_publish);
        topic=view.findViewById(R.id.topic);*/
    }
}
