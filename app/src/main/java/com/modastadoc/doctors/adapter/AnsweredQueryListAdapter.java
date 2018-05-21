package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.modastadoc.doctors.R;

import java.util.ArrayList;

/**
 * Created by vivek.c on 22/09/16.
 */
public class AnsweredQueryListAdapter extends RecyclerView.Adapter<AnsweredQueryListAdapter.ViewHolder>
{
    private ArrayList<String> mDataset;
    private ArrayList<String>quesrDateList;
    private ArrayList<String>quesrAnsList;
    private ArrayList<String> q_IdList;
    private static OptionClickListener mOptionClickListener;
    Context mContext;

    public AnsweredQueryListAdapter(ArrayList<String> q_IdList,ArrayList<String> mDataset,ArrayList<String>quesrDateList,ArrayList<String>quesrAnsList, Context context)
    {
        this.q_IdList=q_IdList;
        this.mDataset = mDataset;
        this.quesrDateList = quesrDateList;
        this.quesrAnsList = quesrAnsList;
        this.mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_ans_querylist, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        if ((position == 0) || ((position % 2) == 0)) {
            holder.linearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.query_list_row_background));
        }
        else {
            holder.linearLayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }

        String title = mDataset.get(position);
        holder.tv_title.setText(getStringWithoutSpecialChar(title));
        holder.tv_date.setText(quesrDateList.get(position));
        holder.tv_answer.setText(getStringWithoutSpecialChar(quesrAnsList.get(position)));
        holder.tv_orderId.setText("Order Id:#"+q_IdList.get(position));


    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tv_title,tv_date,tv_answer,tv_orderId;
        RelativeLayout linearLayout;
        public ViewHolder(View itemView){
            super(itemView);

            linearLayout =(RelativeLayout) itemView.findViewById(R.id.parentLayout);
            tv_title = (TextView) itemView.findViewById(R.id.question);
            tv_date = (TextView)itemView.findViewById(R.id.time);
            tv_answer = (TextView)itemView.findViewById(R.id.answer);
            tv_orderId=(TextView)itemView.findViewById(R.id.textViewOrderId);
            itemView.setOnClickListener(this);
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
