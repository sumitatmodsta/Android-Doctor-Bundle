package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modastadoc.doctors.R;

import java.util.ArrayList;

/**
 * Created by vivek.c on 11/10/16.
 */
public class FollowupQueryListAdapter extends RecyclerView.Adapter<FollowupQueryListAdapter.ViewHolder>
{
    private ArrayList<String> mDataset;
    private ArrayList<String> quesrDateList;
    private ArrayList<String> q_Idlist;
    private ArrayList<String> parentIdlist;
    ArrayList<String> q_Statuslist;
    ArrayList<String> reviwerCommentList;
    Context mContext;
    private static OptionClickListener mOptionClickListener;

    public FollowupQueryListAdapter(ArrayList<String> parentIdlist, ArrayList<String> q_Idlist, ArrayList<String> mDataset, ArrayList<String> quesrDateList, ArrayList<String> q_Statuslist, ArrayList<String> reviwerCommentList, Context context)
    {
        this.mDataset = mDataset;
        this.quesrDateList = quesrDateList;
        this.mContext = context;
        this.q_Idlist = q_Idlist;
        this.q_Statuslist = q_Statuslist;
        this.parentIdlist = parentIdlist;
        this.reviwerCommentList = reviwerCommentList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.query_list_item, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        if ((position == 0) || ((position % 2) == 0))
        {
            holder.linearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        } else
        {
            holder.linearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.query_list_row_background));
        }
        String qStatus = q_Statuslist.get(position);
        if (qStatus.equalsIgnoreCase("R"))

        {
            holder.tv_status.setVisibility(View.VISIBLE);
            if (reviwerCommentList.get(position).trim().length() > 0)
            {
                holder.tv_status.setText("[Review given by inhouse]");
                holder.tv_status.setTextColor(mContext.getResources().getColor(R.color.query_dash_tabcolor));
            } else
            {
                holder.tv_status.setText("[Pending Review]");
                holder.tv_status.setTextColor(mContext.getResources().getColor(R.color.query_order_id_color));
            }
        }
        holder.tv_orderId.setText("Order Id:#" + parentIdlist.get(position));

        String title = mDataset.get(position);
        holder.tv_title.setText(getStringWithoutSpecialChar(title));
        holder.tv_date.setText(quesrDateList.get(position));

    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tv_title, tv_date, tv_status, tv_orderId;
        LinearLayout linearLayout;

        public ViewHolder(View convertView)
        {
            super(convertView);

            linearLayout = (LinearLayout) convertView.findViewById(R.id.parentLayout);
            tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            tv_date = (TextView) convertView.findViewById(R.id.textViewDate);
            tv_status = (TextView) convertView.findViewById(R.id.reviewStatus);
            tv_orderId = (TextView) convertView.findViewById(R.id.textViewOrderId);
            convertView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            mOptionClickListener.onItemClick(getAdapterPosition(), v);
        }

    }

    public interface OptionClickListener
    {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(OptionClickListener myClickListener)
    {
        this.mOptionClickListener = myClickListener;
    }

    private String getStringWithoutSpecialChar(String str){
        char[] array = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != '\\'){
                stringBuffer.append(array[i]);
            }
        }
        return stringBuffer.toString();
    }
}
