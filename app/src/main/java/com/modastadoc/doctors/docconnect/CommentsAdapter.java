package com.modastadoc.doctors.docconnect;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.utils.UtilityMethods;
import com.modastadoc.doctors.docconnect.activity.FullImageActivity;
import com.modastadoc.doctors.docconnect.activity.FullWebViewActivity;
import com.modastadoc.doctors.docconnect.activity.VideoPlayActivity;
import com.modastadoc.doctors.docconnect.widgets.ImageLayout;

import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;

/**
 * Created by vijay.hiremath on 11/11/16.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>
{
    String TAG = SubGroupAdapter.class.getSimpleName();
    public static AttachmentClickListener mListener;
    private ArrayList<CommentModel> mDataset;
    Context mContext;

    public CommentsAdapter(ArrayList<CommentModel> mDataset, Context context)
    {
        this.mDataset = mDataset;
        this.mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        HtmlTextView html_comment;
        TextView tv_comment_author;
        TextView tv_comment_date;
        LinearLayout ll_video_holder;
        LinearLayout ll_image_holder;
        LinearLayout ll_dynamic_holder;
        ImageView iv_edit_comment;

        public ViewHolder(View itemView)
        {
            super(itemView);
            html_comment = (HtmlTextView) itemView.findViewById(R.id.html_comment);
            tv_comment_author = (TextView) itemView.findViewById(R.id.tv_comment_author_name);
            tv_comment_date = (TextView) itemView.findViewById(R.id.tv_comment_date);
            ll_image_holder = (LinearLayout) itemView.findViewById(R.id.ll_image_holder);
            ll_video_holder = (LinearLayout) itemView.findViewById(R.id.ll_video_holder);
            ll_dynamic_holder = (LinearLayout) itemView.findViewById(R.id.ll_dynamic_holder);
            iv_edit_comment = (ImageView) itemView.findViewById(R.id.iv_edit_comment);

            iv_edit_comment.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mListener.onItemClick( getAdapterPosition() , v );
                }
            });
        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position)
    {
        final CommentModel model = mDataset.get(position);

        String content = model.getComment();
//        if (content.contains("<style>"))
//        {
//            int s = content.lastIndexOf(">");
//            String ss = content.substring(s + 1, content.length());
//            holder.html_comment.setText(ss);
//        }
//        else
//        {
//            holder.html_comment.setText(Html.fromHtml(content));
//        }

        /***************************
         *
         * comment html textview
         *
         **************************/
        if ( content.contains("<style>") )
        {
            int s = content.lastIndexOf("</style>");
            String ss = content.substring(s + 1, content.length());

            try
            {
                String style_end_index = content.substring( content.lastIndexOf("</style>") + 8 , content.length());
                holder.html_comment.setHtml( style_end_index );
            }
            catch( Exception e )
            {
                Log.e(TAG, "HTML error : " + e.toString());
                holder.html_comment.setHtml( content );
            }
        }
        else
        {
            holder.html_comment.setHtml( content );
        }


        holder.tv_comment_author.setText(model.getAuthor());
        try
        {
            String json_txt = UtilityMethods.convertToLocalTime( model.getPostDate() );
            JSONObject obj = new JSONObject( json_txt );
            String time = obj.getString("TIME");
            String date = obj.getString("DATE_DETAIL");
            holder.tv_comment_date.setText("on " + date + " " + time );
        }
        catch (Exception e)
        {
            Log.e( TAG , "Ex : " + e.toString());
            holder.tv_comment_date.setText("" + model.getPostDate() );
        }

        if( model.isEditable() )
        {
            holder.iv_edit_comment.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.iv_edit_comment.setVisibility(View.GONE);
        }

        /*********************
         *
         * Attachment listener
         *
         ********************/
        if( model.getImageList().size() > 0 )
        {
            holder.ll_dynamic_holder.removeAllViews();
            for( int i = 0 ; i < model.getImageList().size() ; i++ )
            {
                final String image_url = model.getImageList().get( i ).getImg_url();
                final String attch_id  = model.getImageList().get( i ).getId();
                String extension = "";

                int index = image_url.lastIndexOf('.');
                if (index > 0)
                {
                    extension = image_url.substring(index+1);
                }

                Log.e(TAG, "File Extension : " + extension);
                if( extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpeg") )
                {
                    ImageLayout valueTV = new ImageLayout( mContext );
                    valueTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    valueTV.setImage( image_url );
                    holder.ll_dynamic_holder.addView(valueTV);

                    valueTV.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            String can_edit = "0";
                            if(model.isEditable())
                            {
                                can_edit = "1";
                            }

                            mContext.startActivity( FullImageActivity.createIntent(
                                    mContext  ,
                                    image_url ,
                                    can_edit  ,
                                    model.getId() ,
                                    attch_id,
                                    FullImageActivity.COMMENT_DETAIL_SOURCE) );
                        }
                    });
                }
                else if( extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("m3u8"))
                {
                    ImageLayout valueTV = new ImageLayout(mContext);
                    valueTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    valueTV.setImageAsVideo();
                    valueTV.setTextAdVideo();
                    holder.ll_dynamic_holder.addView(valueTV);

                    valueTV.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            // todo video attachment delete functionality not there :(
                            mContext.startActivity(VideoPlayActivity.createIntent(mContext, image_url));
                        }
                    });
                }
                else if( extension.equalsIgnoreCase("pdf") || extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("doc"))
                {
                    ImageLayout valueTV = new ImageLayout(mContext);
                    valueTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    valueTV.setImageAdDocument();
                    valueTV.setTetAsDocument();
                    holder.ll_dynamic_holder.addView(valueTV);

                    valueTV.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {

                            String can_edit = "0";
                            if(model.isEditable())
                            {
                                can_edit = "1";
                            }

                            Log.e( TAG , "There you are! pdf : " + image_url);
                            mContext.startActivity(FullWebViewActivity.createIntent(
                                    mContext,
                                    image_url,
                                    model.getId(),
                                    attch_id,
                                    can_edit,
                                    FullWebViewActivity.COMMENT_DETAIL_SOURCE));
                        }
                    });
                }
                else
                {
                    Log.e( TAG , "format not supported :(");
                }

            }
        }
        else
        {

        }
        /***********************************/


    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);

        ViewHolder dataObjectHolder = new ViewHolder(view);

        return dataObjectHolder;
    }


    public interface AttachmentClickListener
    {
        void onItemClick(int position, View v);
    }

    public void editCommentListener( AttachmentClickListener param )
    {
        this.mListener = param;
    }

}
