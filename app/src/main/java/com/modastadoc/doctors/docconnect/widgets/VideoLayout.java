package com.modastadoc.doctors.docconnect.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.modastadoc.doctors.R;

/**
 * Created by vijay.hiremath on 21/11/16.
 */
public class VideoLayout extends LinearLayout
{
    Context mContext;


    public VideoLayout(Context context )
    {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.video_attachment_layout, this);
    }
}
