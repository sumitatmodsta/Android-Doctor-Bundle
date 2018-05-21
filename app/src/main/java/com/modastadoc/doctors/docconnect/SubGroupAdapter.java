package com.modastadoc.doctors.docconnect;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.utils.UtilityMethods;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by vijay.hiremath on 10/11/16.
 */
public class SubGroupAdapter extends RecyclerView.Adapter<SubGroupAdapter.ViewHolder>
{
    String TAG = SubGroupAdapter.class.getSimpleName();

    private ArrayList<SubGroupModel> mDataset;
    private static OptionClickListener mOptionClickListener;
    private static ButtonClickListener mButtonClickListener;
    Context mContext;
    boolean isJoinAvailable;

    public SubGroupAdapter(ArrayList<SubGroupModel> mDataset, Context context , boolean value )
    {
        this.mDataset = mDataset;
        this.mContext = context;
        this.isJoinAvailable = value;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tv_subgroup_name;
        TextView tv_subgroup_freshness;
        CircleImageView civ_sub_group_pic;
        Button bt_join_group;

        public ViewHolder(View itemView)
        {
            super(itemView);
            tv_subgroup_name = (TextView) itemView.findViewById(R.id.tv_subgroup_name);
            tv_subgroup_freshness = (TextView) itemView.findViewById(R.id.tv_subgroup_freshness);
            civ_sub_group_pic = (CircleImageView) itemView.findViewById(R.id.civ_sub_group_pic);
            bt_join_group = (Button) itemView.findViewById(R.id.bt_join_group);

            itemView.setOnClickListener(this);

            bt_join_group.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mButtonClickListener.onButtonClick( getAdapterPosition() , v );
                }

            } );
        }

        @Override
        public void onClick(View v)
        {
            mOptionClickListener.onItemClick(getAdapterPosition(), v);
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        SubGroupModel model = mDataset.get(position);

        holder.tv_subgroup_name.setText( Html.fromHtml( model.getName() ) );
        holder.tv_subgroup_freshness.setText(UtilityMethods.parseSimpleDate(model.getFreshness()) );

        Glide.with(mContext)
                .load( model.getPicUrl() )
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.profilepic)
                .into(holder.civ_sub_group_pic);

        if( isJoinAvailable )
        {
            if( model.isJoined() )
            {
                holder.bt_join_group.setVisibility(View.GONE);
            }
            else
            {
                // show join button for everyting except all-topics
                if( !model.getSlug().equalsIgnoreCase("all-topics") )
                {
                    holder.bt_join_group.setVisibility(View.VISIBLE);
                }
            }
        }
        else
        {
            holder.bt_join_group.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subgroup_list_item, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }

    public interface OptionClickListener
    {
        void onItemClick(int positions, View v);
    }

    public interface ButtonClickListener
    {
        void onButtonClick( int positions , View v );
    }

    public void setOnItemClickListener(OptionClickListener myClickListener)
    {
        this.mOptionClickListener = myClickListener;
    }

    public void setOnButtonClickListener( ButtonClickListener mlistener )
    {
        this.mButtonClickListener = mlistener;
    }
}
