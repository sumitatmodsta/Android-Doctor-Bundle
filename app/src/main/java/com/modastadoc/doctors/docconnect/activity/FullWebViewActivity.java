package com.modastadoc.doctors.docconnect.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
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

import java.util.HashMap;

public class FullWebViewActivity extends AppCompatActivity
{
    public static final String TOPIC_DETAIL_SOURCE   = "topic_detail_source";
    public static final String COMMENT_DETAIL_SOURCE = "comment_detail_source";

    String TAG = FullWebViewActivity.class.getSimpleName();
    CustomProgressDialog customDialog;
    Context mContext;
    ImageView iv_delete_doc;

    String url        = "";
    String parent_id  = "";
    String att_id     = "";
    String can_delete = "";
    String source     = "";

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_web_view);

        mContext = this;

        try
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Topic Details");
            getSupportActionBar().setElevation(0);
        } catch (Exception e)
        {
            Log.e(TAG, "e. " + e.toString());
        }

        iv_delete_doc = (ImageView) findViewById(R.id.iv_delete_doc);

        WebView webview = (WebView) findViewById(R.id.wb_content);
        webview.getSettings().setJavaScriptEnabled(true);


        /************************/
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            if (bundle.containsKey("url")) ;
            {
                url = bundle.getString("url");
            }

            if( bundle.containsKey("parent_id"))
            {
                parent_id = bundle.getString("parent_id");
            }

            if( bundle.containsKey("att_id"))
            {
                att_id = bundle.getString("att_id");
            }

            if( bundle.containsKey("can_delete") )
            {
                can_delete = bundle.getString("can_delete");
            }

            if( bundle.containsKey("source"))
            {
                source = bundle.getString("source");
            }
        }
        /************************/

        if( can_delete.equalsIgnoreCase("1") )
        {
            iv_delete_doc.setVisibility(View.VISIBLE);
        }
        else
        {
            iv_delete_doc.setVisibility(View.GONE);
        }

        Log.e( TAG , "Doc URL : " + url );

        showCustomDialog();
        webview.loadUrl("https://docs.google.com/viewer?url=" + url);
        webview.setWebViewClient(new WebViewClient()
        {

            public void onPageFinished(WebView view, String url)
            {
                dismissCustomDialog();
            }
        });

        iv_delete_doc.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteAttachment();
            }
        });
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

    private void deleteAttachment()
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
                Toast.makeText(mContext, "Network error.", Toast.LENGTH_LONG).show();
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

    public static Intent createIntent(Context mContext, String doc_url , String parent_id , String att_id , String can_delete , String source )
    {
        Intent intent = new Intent(mContext, FullWebViewActivity.class);
        intent.putExtra("url"        , doc_url);
        intent.putExtra("parent_id"  , parent_id);
        intent.putExtra("att_id"     , att_id);
        intent.putExtra("can_delete" , can_delete);
        intent.putExtra("source"     , source );
        return intent;
    }

    private void showCustomDialog()
    {

        customDialog = new CustomProgressDialog(mContext);
        customDialog.setCancelable(true);
        customDialog.show();

    }

    private void dismissCustomDialog()
    {

        if (customDialog != null && customDialog.isShowing())
        {
            customDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:

                super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}
