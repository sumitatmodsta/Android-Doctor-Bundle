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
 * Created by vijay.hiremath on 08/11/16.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder>
{
    String TAG = GroupAdapter.class.getSimpleName();

    private ArrayList<GroupModel> mDataset;
    private static OptionClickListener mOptionClickListener;
    private static ButtonClickListener mButtonClickListener;
    Context mContext;

    public GroupAdapter(ArrayList<GroupModel> mDataset, Context context)
    {
        this.mDataset = mDataset;
        this.mContext = context;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tv_title;
        TextView tv_last_updated;
        CircleImageView civ_group_icon;
        Button bt_join_group;

        public ViewHolder(View itemView)
        {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_last_updated = (TextView) itemView.findViewById(R.id.tv_last_updated);
            civ_group_icon = (CircleImageView) itemView.findViewById(R.id.civ_group_icon);
            bt_join_group = (Button) itemView.findViewById(R.id.bt_join_group);


            itemView.setOnClickListener(this);

            bt_join_group.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mButtonClickListener.onButtonClick( getAdapterPosition() , v );
                }
            });
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
        GroupModel model = mDataset.get(position);
        holder.tv_title.setText( Html.fromHtml(model.getGroupName() ) );

        Glide.with(mContext)
                .load(model.getGroupIcon())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.profilepic)
                .into(holder.civ_group_icon);

        holder.tv_last_updated.setText( UtilityMethods.parseSimpleDate( model.getGroupModified() ) );
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_list_item, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }

    public interface OptionClickListener
    {
        void onItemClick(int positions, View v);
    }

    public interface ButtonClickListener
    {
        void onButtonClick(int positions, View v);
    }

    public void setOnItemClickListener(OptionClickListener myClickListener)
    {
        this.mOptionClickListener = myClickListener;
    }

    public void setButtonClickListener( ButtonClickListener mButtonClickListener )
    {
        this.mButtonClickListener = mButtonClickListener;
    }
}
