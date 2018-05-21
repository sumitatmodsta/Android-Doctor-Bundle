package com.modastadoc.doctors.docconnect.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modastadoc.doctors.R;

/**
 * Created by vijay.hiremath on 23/11/16.
 */
public class AttachmentLayout extends LinearLayout
{
    Context mContext;
    ImageView iv_icon;
    TextView tv_type;
    String attachmentName;

    public AttachmentLayout(Context context )
    {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.attachment_layout, this);

        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        tv_type = (TextView) findViewById(R.id.tv_type);
    }


    public void setAttachmentAsUploaded()
    {
        iv_icon.setImageResource(R.drawable.attachment_uploaded);
    }


    public void setAttachmentName( String name )
    {
        attachmentName = name;
        tv_type.setText( name );
    }

    public String getAttachmentName()
    {
        return attachmentName;
    }


}
