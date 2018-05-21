package com.modastadoc.doctors.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.NewQueryActivity;
import com.modastadoc.doctors.common.utils.UtilityMethods;

import java.util.ArrayList;

/**
 * Created by Vivek on 15/09/16.
 * Updated by Vivek on 28/09/2016.
 *
 * Stupid-est code ever seen! - Vijay on 23/03/2017
 */
public class QueryAdapter extends ArrayAdapter
{
    private ArrayList<String> mDataset;
    private ArrayList<String> quesrDateList;
    private ArrayList<String> q_Idlist;
    private ArrayList<String> mPatientFileIds;
    private ArrayList<String> mAudioLanguages;
    Context mContext;
    private LayoutInflater inflater;
    String q_id;


    public QueryAdapter(ArrayList<String> q_Idlist,
                        ArrayList<String> mDataset,
                        ArrayList<String> quesrDateList,
                        Context context ,
                        ArrayList<String> patientFileIds,
                        ArrayList<String> audioLanguages)
    {
        super(context, R.layout.query_list_item, mDataset);

        this.mDataset = mDataset;
        this.quesrDateList = quesrDateList;
        this.mContext = context;
        this.q_Idlist = q_Idlist;
        this.mAudioLanguages = audioLanguages;
        this.mPatientFileIds = patientFileIds;
    }

    @Override
    public int getCount()
    {
        return mDataset.size();
    }

    @Override
    public Object getItem(int location)
    {
        return mDataset.get(location);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {

        if (inflater == null)
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.query_list_item, null);

        if ((position == 0) || ((position % 2) == 0))
        {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.query_list_row_background));
        }
        else
        {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.mdtp_white));
        }

        TextView tv_title      = (TextView) convertView.findViewById(R.id.tv_title);
        TextView tv_date       = (TextView) convertView.findViewById(R.id.textViewDate);
        TextView tv_status     = (TextView) convertView.findViewById(R.id.reviewStatus);
        TextView tv_orderId    = (TextView) convertView.findViewById(R.id.textViewOrderId);
        TextView tv_query_type = (TextView) convertView.findViewById(R.id.tv_query_type);

        tv_status.setVisibility(View.GONE);
        tv_query_type.setVisibility(View.VISIBLE);
        String title = mDataset.get(position);
        tv_title.setText(getStringWithoutSpecialChar(title));
        tv_date.setText(quesrDateList.get(position));

        tv_orderId.setText("Order Id:#" + q_Idlist.get(position));

//        tv_title.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                q_id = q_Idlist.get(position);
//                getIntentCall(q_id);
//            }
//        });
//
//        tv_date.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                q_id = q_Idlist.get(position);
//                getIntentCall(q_id);
//            }
//        });

        String patientUploadedFileId = mPatientFileIds.get( position );
        String uploadedAudioFileLanguage = mAudioLanguages.get( position );

        if( title.length() > 0 && patientUploadedFileId.length() <= 0 )
        {
            tv_query_type.setText( "Text Query" );
        }
        else if( title.length() > 0 && uploadedAudioFileLanguage.length() <= 0 )
        {
            tv_query_type.setText( "Text Query" );
        }
        else if( title.length() > 0 && patientUploadedFileId.length() > 0 )
        {
            String language = UtilityMethods.getLanguageFromLangaugeCode(uploadedAudioFileLanguage);
            tv_query_type.setText(" Text + Audio Query (" + "" + language + ")");
        }
        else if (title.length() <= 0 && patientUploadedFileId.length() > 0 )
        {
            String language = "";
            if(uploadedAudioFileLanguage.equalsIgnoreCase("hi") )
            {
                language = "Hindi";
            }
            else if (uploadedAudioFileLanguage.equalsIgnoreCase("en"))
            {
                language = "English";
            }
            tv_query_type.setText("Audio Query (" + language + ")" );
        }

        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                q_id = q_Idlist.get(position);
                getIntentCall(q_id);
            }
        });

        return convertView;
    }

    private void getIntentCall(String q_Id)
    {
        Intent intent = new Intent(mContext, NewQueryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("question_Id", q_Id);
        mContext.startActivity(intent);

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
