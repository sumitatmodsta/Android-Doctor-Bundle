package com.modastadoc.doctors.docconnect.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;

import java.util.ArrayList;


/**
 * Created by vijay.hiremath on 21/11/16.
 */
public class AttachmentHorizontalAdapter extends RecyclerView.Adapter<AttachmentHorizontalAdapter.ViewHolder>
{
    String TAG = AttachmentHorizontalAdapter.class.getSimpleName();

    private ArrayList<String> mDataset;
    private static OptionClickListener mOptionClickListener;
    Context mContext;

    public AttachmentHorizontalAdapter(ArrayList<String> mDataset, Context context)
    {
        this.mDataset = mDataset;
        this.mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ImageView iv_attachment_image;

        public ViewHolder(View itemView)
        {
            super(itemView);
            iv_attachment_image = (ImageView) itemView.findViewById(R.id.iv_attachment_image);

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
        String icon_url = mDataset.get( position );

        String extension = "";

        int index = icon_url.lastIndexOf('.');
        if (index > 0)
        {
            extension = icon_url.substring(index+1);
        }

        /**********************************/
        if( extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpeg") )
        {
            Glide.with(mContext)
                    .load( icon_url )
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fitCenter()
                    .error(R.drawable.image_preview_icon)
                    .into(holder.iv_attachment_image);
        }
        else if( extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("m3u8"))
        {
            holder.iv_attachment_image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.video_ico));
        }
        else if( extension.equalsIgnoreCase("pdf") || extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("doc"))
        {
            holder.iv_attachment_image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.doc_ico));
        }
        /**********************************/


    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_list_item, parent, false);

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
