package com.modastadoc.doctors.docconnect.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.docconnect.TopicDetailActivity;
import com.modastadoc.doctors.network.PostApiSimple;
import com.modastadoc.doctors.widget.CustomProgressDialog;
import com.modastadoc.doctors.widget.TouchImageView;

import java.io.InputStream;
import java.util.HashMap;

public class FullImageActivity extends AppCompatActivity
{
    public static final String TOPIC_DETAIL_SOURCE   = "topic_detail_source";
    public static final String COMMENT_DETAIL_SOURCE = "comment_detail_source";

    String TAG = FullImageActivity.class.getSimpleName();

    CustomProgressDialog customDialog;
    Context mContext;
    String imageURL;

    TouchImageView imageView;
    TextView tv_error;
    Button share;
    Bitmap imageBit;
    RelativeLayout rl_holder;
    ImageView iv_delete_image;
    String can_delete;
    String att_id;
    String source;
    String parent_id;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);


        if (getSupportActionBar() != null)
        {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }

        Bundle extras = getIntent().getExtras();

        mContext = this;

        if (extras != null)
        {
            if (extras.containsKey("image_url"))
            {
                imageURL = extras.getString("image_url");
            }

            if (extras.containsKey("can_delete"))
            {
                can_delete = extras.getString("can_delete");
            }

            if (extras.containsKey("parent_id"))
            {
                parent_id = extras.getString("parent_id");
            }

            if (extras.containsKey("att_id"))
            {
                att_id = extras.getString("att_id");
            }

            if( extras.containsKey("source"))
            {
                source = extras.getString("source");
            }
        }

        Log.e(TAG, "URL : " + imageURL);
        if (imageURL.length() <= 0)
        {
            finish();
        }

        imageView = (TouchImageView) findViewById(R.id.network_image);
        tv_error = (TextView) findViewById(R.id.tv_error);
        share = (Button) findViewById(R.id.share);
        rl_holder = (RelativeLayout) findViewById(R.id.rl_holder);
        iv_delete_image = (ImageView) findViewById(R.id.iv_delete_image);

        if (can_delete.equalsIgnoreCase("1"))
        {
            iv_delete_image.setVisibility(View.VISIBLE);
        }
        else
        {
            iv_delete_image.setVisibility(View.GONE);
        }

        showCustomDialog();

        new DownloadImageTask(imageURL).execute();

        ImageView iv_close = (ImageView) findViewById(R.id.iv_closeimage);
        iv_close.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(TAG, "!");
                finish();
            }
        });

        iv_delete_image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog("Deleting...");
                String url = ServerConstants.DELETE_ATTACHMENT;
                String cookie  = LocalDataManager.getInstance().get(PreferenceConstants.cookie);
                Log.e(TAG, "delete the thing");
                Log.e(TAG, "reply_id : " + parent_id);
                Log.e(TAG, "url      : " + url);
                Log.e(TAG, "att_id   : " + att_id);


                HashMap<String, String> params = new HashMap<String, String>();
                params.put("topic_id"  , parent_id );
                params.put("attachment_id"  , att_id );
                params.put("cookie", cookie);

                PostApiSimple postApiSimple = new PostApiSimple(new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        dismissDialog();
                        Log.e(TAG, "Edit Comment Response : " + response);
                        if(source.equalsIgnoreCase(TOPIC_DETAIL_SOURCE))
                        {
                            TopicDetailActivity.TOPIC_ATTCH_DELETE = 1;
                        }
                        else if( source.equalsIgnoreCase(COMMENT_DETAIL_SOURCE))
                        {
                            TopicDetailActivity.COMMENT_ATTCH_DELETE = 1;
                        }

                        finish();
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        dismissDialog();
                        Toast.makeText( mContext , "Network error." , Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error Response : " + error.toString());
                    }

                }, params, url);

                RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
                volleyQueue.add(postApiSimple);
                postApiSimple.setRetryPolicy(new DefaultRetryPolicy(
                        5000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            }
        });

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        String image_url;


        public DownloadImageTask(String imgurl)
        {
            this.image_url = imgurl;
        }

        protected Bitmap doInBackground(String... urls)
        {

            Bitmap mIcon11 = null;
            try
            {
                InputStream in = new java.net.URL(image_url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result)
        {
            if (result == null)
            {
                tv_error.setVisibility(View.VISIBLE);
            }
            dismissCustomDialog();
            imageView.setImageBitmap(result);
            imageBit = result;

        }
    }

    private void showDialog(String message)
    {

        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(mContext);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void dismissDialog()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
    }

    public static Intent createIntent(Context context, String image_url, String can_delete, String parent_id, String attachment_id, String source )
    {
        Intent intent = new Intent(context, FullImageActivity.class);
        intent.putExtra("image_url", image_url);
        intent.putExtra("can_delete", can_delete);
        intent.putExtra("parent_id", parent_id);
        intent.putExtra("att_id", attachment_id);
        intent.putExtra("source", source);

        return intent;
    }

    private void showCustomDialog()
    {

        customDialog = new CustomProgressDialog(this);
        customDialog.setCancelable(true);
        customDialog.show();

    }

    private void dismissCustomDialog()
    {
        if (customDialog != null)
        {
            customDialog.dismiss();
        }
    }
}
