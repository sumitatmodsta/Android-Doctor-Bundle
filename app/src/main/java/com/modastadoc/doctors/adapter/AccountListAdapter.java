package com.modastadoc.doctors.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.interfaces.ItemClickListener;

import java.util.ArrayList;

/**
 * Created by vivek.c on 19/09/16.
 */
public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.MyViewHolder> {
    private ArrayList<String> keyFeatures;
    private ItemClickListener mListener;

    public AccountListAdapter(ArrayList<String> keyFeatures, ItemClickListener listener) {
        this.keyFeatures = keyFeatures;
        mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_accountlist_row, viewGroup, false);

        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.bind(position);
    }
    @Override
    public int getItemCount() {
        return keyFeatures.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        CardView parent;
        MyViewHolder(View view) {
            super(view);
            parent = (CardView)view.findViewById(R.id.card);
            name = (TextView)view.findViewById(R.id.txtFeatures);
            icon = (ImageView)view.findViewById(R.id.imageIcon);
        }

        void bind(final int position) {
            name.setText(keyFeatures.get(position));
            icon.setImageResource(getResourceID(position));
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemClicked(position);
                }
            });
        }
    }

    private int getResourceID(int position) {
        int resourceID;
        if(position == 0) {
            resourceID = R.drawable.profile_pic;
        }else if(position ==1) {
            resourceID = R.drawable.myreport;
        }else {
            resourceID = R.drawable.consultation_fee;
        }

        return resourceID;
    }
}