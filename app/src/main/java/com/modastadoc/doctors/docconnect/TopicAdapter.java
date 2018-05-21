package com.modastadoc.doctors.docconnect;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.utils.UtilityMethods;

import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by vijay.hiremath on 10/11/16.
 */
public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder>
{
    String TAG = TopicAdapter.class.getSimpleName();

    private ArrayList<TopicModel> mDataset;
    private static OptionClickListener mOptionClickListener;
    Context mContext;

    public TopicAdapter(ArrayList<TopicModel> mDataset, Context context)
    {
        this.mDataset = mDataset;
        this.mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        CircleImageView civ_topic_author_pic;
        TextView tv_topic_author;
        TextView tv_topic_date;
        TextView tv_topic_title;

        public ViewHolder(View itemView)
        {
            super(itemView);
            civ_topic_author_pic = (CircleImageView) itemView.findViewById(R.id.civ_topic_author_pic);
            tv_topic_author = (TextView) itemView.findViewById(R.id.tv_topic_author);
            tv_topic_date = (TextView) itemView.findViewById(R.id.tv_topic_date);
            tv_topic_title = (TextView) itemView.findViewById(R.id.tv_topic_title);

            itemView.setOnClickListener(this);
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
        TopicModel model = mDataset.get(position);

        if( model.getTopicAuthor() != null || !model.getTopicAuthor().equalsIgnoreCase("null") )
        {
            holder.tv_topic_author.setText(model.getTopicAuthor());
        }

        holder.tv_topic_title.setText(Html.fromHtml(model.getTopicName()) );

        String icon_url = "";
        try
        {
            String sub_icon = model.getProfilePic().substring(0,2);

            if( sub_icon.equalsIgnoreCase("//"))
            {
                icon_url = model.getProfilePic().substring(2);
            }
            else
            {
                icon_url = model.getProfilePic();
            }
        }
        catch ( Exception e )
        {
            icon_url = model.getProfilePic();
        }

        Log.e( TAG , "icon_url : " + icon_url );

        Glide.with(mContext)
                .load( icon_url )
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.profilepic)
                .into(holder.civ_topic_author_pic);

        try
        {
            String json_txt = UtilityMethods.convertToLocalTime(model.getUpdatedDate());
            JSONObject obj  = new JSONObject( json_txt );
            String time     = obj.getString("TIME");
            String date     = obj.getString("DATE_DETAIL");
            holder.tv_topic_date.setText("on " + date + " " + time );
        }
        catch (Exception e)
        {
            Log.e(TAG, "Ex : " + e.toString());
            holder.tv_topic_date.setText("on " + model.getUpdatedDate());
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_list_item, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }

    public interface OptionClickListener
    {
        void onItemClick(int positions, View v);
    }


    public void setOnItemClickListener(OptionClickListener myClickListener)
    {
        this.mOptionClickListener = myClickListener;
    }


}
