package com.modastadoc.doctors.docconnect.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;

/**
 * Created by vijay.hiremath on 21/11/16.
 */
public class ImageLayout extends LinearLayout
{
    Context mContext;
    ImageView iv_icon;
    TextView tv_type;

    public ImageLayout(Context context )
    {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_attachment_view, this);

        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_type = (TextView) findViewById(R.id.tv_type);
    }


    public void setImage( String image_url )
    {
        Glide.with(mContext)
                .load( image_url )
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .error(R.drawable.jpg_ico)
                .into(iv_icon);
    }

    public void setImageAsVideo()
    {
        iv_icon.setImageResource(R.drawable.video_ico);
    }

    public void setImageAdDocument()
    {
        iv_icon.setImageResource(R.drawable.doc_ico);
    }

    public void setTextAdVideo()
    {
        tv_type.setText("Watch video");
    }

    public void setTetAsDocument()
    {
        tv_type.setText("View");
    }

}
