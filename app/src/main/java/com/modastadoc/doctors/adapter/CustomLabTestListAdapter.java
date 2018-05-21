package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.modastadoc.doctors.R;

import java.util.ArrayList;

/**
 * Created by vivek.c on 04/10/16.
 */
public class CustomLabTestListAdapter  extends ArrayAdapter {
    private ArrayList<String> q_Idlist;
    Context mContext;
    private LayoutInflater inflater;
    String q_id;


    public CustomLabTestListAdapter(ArrayList<String> q_Idlist,Context context) {
        super(context, R.layout.layout_lab_list_item, q_Idlist);

        this.mContext = context;
        this.q_Idlist = q_Idlist;
    }

    @Override
    public int getCount() {
        return q_Idlist.size();
    }

    @Override
    public Object getItem(int location) {
        return q_Idlist.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.query_list_item, null);

        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);

        String title = q_Idlist.get(position);
        tv_title.setText(title);


        return convertView;
    }

}

