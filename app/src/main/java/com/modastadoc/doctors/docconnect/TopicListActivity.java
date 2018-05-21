package com.modastadoc.doctors.docconnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.VolleyCallback;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.StorageUtil;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.docconnect.model.AttachmentModel;
import com.modastadoc.doctors.docconnect.widgets.AttachmentLayout;
import com.modastadoc.doctors.network.MultipartRequest;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CropImageView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TopicListActivity extends AppCompatActivity implements VolleyCallback
{
    String TAG = TopicListActivity.class.getSimpleName();

    Context mContext;

    RecyclerView rvList;
    ArrayList<TopicModel> mList;
    TopicAdapter adapter;

    String forum_id = "";
    String group_id = "";
    String forum_name = "";
    AlertDialog alertDialog;


    FrameLayout fl_new_topic_sections;
    EditText et_topic_name;
    EditText et_topic_content;
    Button bt_close_topic_sections;
    Button bt_save_topic_sections;


    TextView tv_zero_state;
    FloatingActionsMenu fam_menu;
    FloatingActionButton fab_request_new_group;
    TextView tv_add_attachments;
    //TextView tv_attachments_name;
    LinearLayout ll_attachment_holder;
    LinearLayout ll_upload_error_msg;
    Button bt_try_again;

    /***********************************/
    Uri attachment_uri = null;
    String attachment_name = "";//filename
    Uri uriSavedImage;
    File croppedFile;
    ArrayList<AttachmentModel> attachmentModels = new ArrayList<>();
    int attachment_index_to_upload;
    String parent_upload_id = "";
    TextView tv_topic_title;
    /***********************************/


    int CHOOSE_FILE = 145;
    int CAMERA_PIC_REQUEST = 1888;
    int CAMERA_VIDEO_REQUEST = 6790;


    /*************************/
    String twoHyphens = "--";
    String lineEnd = "\r\n";
    String boundary = "apiclient-" + System.currentTimeMillis();
    String mimeType = "multipart/form-data;boundary=" + boundary;
    /*************************/


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_list);

        mContext = this;

        rvList = (RecyclerView) findViewById(R.id.rv_topic_list);
        tv_zero_state = (TextView) findViewById(R.id.tv_zero_state);
        fl_new_topic_sections = (FrameLayout) findViewById(R.id.fl_new_topic_sections);
        fab_request_new_group = (FloatingActionButton) findViewById(R.id.fab_request_new_group);
        fam_menu = (FloatingActionsMenu) findViewById(R.id.fam_menu);
        et_topic_name = (EditText) findViewById(R.id.et_topic_name);
        et_topic_content = (EditText) findViewById(R.id.et_topic_content);
        bt_close_topic_sections = (Button) findViewById(R.id.bt_close_topic_sections);
        bt_save_topic_sections = (Button) findViewById(R.id.bt_save_topic_sections);
        //tv_attachments_name = (TextView) findViewById(R.id.tv_attachments_name);
        tv_add_attachments = (TextView) findViewById(R.id.tv_add_attachments);
        ll_attachment_holder = (LinearLayout) findViewById(R.id.ll_attachment_holder);
        ll_upload_error_msg = (LinearLayout) findViewById(R.id.ll_upload_error_msg);
        bt_try_again = (Button) findViewById(R.id.bt_try_again);
        tv_topic_title = (TextView) findViewById(R.id.tv_topic_title);

        tv_add_attachments.setPaintFlags( tv_add_attachments.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvList.setLayoutManager(mLayoutManager);

        /************************/
        Bundle bundle = getIntent().getExtras();
        if( bundle != null )
        {
            if( bundle.containsKey("f_id") );
            {
                forum_id = bundle.getString("f_id");
            }

            if( bundle.containsKey("g_id") )
            {
                group_id = bundle.getString("g_id");
            }

            if( bundle.containsKey("f_name"))
            {
                forum_name = bundle.getString("f_name");
            }
        }
        Log.e( TAG , "forum_id : " + forum_id);
        tv_topic_title.setText("Topics " + forum_name);
        /************************/

        try
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        catch (Exception e)
        {
            Log.e(TAG, "e. " + e.toString());
        }

        showDialog("Loading...");

        if( forum_id.equalsIgnoreCase("all-topics"))
        {
            fam_menu.setVisibility(View.GONE);
            String URL = ServerConstants.GET_FORUM_TOPICS;
            getAllTopics(URL);
        }
        else
        {
            String URL = ServerConstants.getForumTopics( forum_id );
            Log.e( TAG , "Forum Topics url :" + URL );
            getTopics(URL);
        }

        fab_request_new_group.setTitle("Create New Topic in : \r\n\n" + forum_name );
        fab_request_new_group.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fl_new_topic_sections.setVisibility(View.VISIBLE);
            }
        });

        bt_save_topic_sections.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String topic_name = et_topic_name.getText().toString();
                String topic_desc = et_topic_content.getText().toString();
                String user_cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

                topic_desc.trim();
                topic_name.trim();

                if( topic_name.length() <= 0 )
                {
                    et_topic_name.setError("");
                    return;
                }

                if( topic_desc.length() <= 0 )
                {
                    et_topic_content.setError("");
                    return;
                }


                Log.e( TAG , "user_cookie : " + user_cookie );

                HashMap<String, String> params = new HashMap<String, String>();
                params.put(  "forum_id" , forum_id   );
                params.put(  "content"  , topic_desc );
                params.put(  "title"    , topic_name );
                params.put(  "cookie"   , user_cookie );
                params.put(  "topic_id" , "0" );

                String url = ServerConstants.CREATE_NEW_TOPIC;

                createNewTopic(url, params);
            }
        });

        bt_close_topic_sections.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fl_new_topic_sections.setVisibility(View.GONE);
                ll_attachment_holder.removeAllViews();
                attachmentModels.clear();

                //attachment_uri = null;
                //attachment_name = "";
                if( ll_upload_error_msg.getVisibility() == View.VISIBLE )
                {
                    showDialog("Loading...");
                    String URL = ServerConstants.getForumTopics(forum_id);
                    getTopics(URL);
                }
                ll_upload_error_msg.setVisibility(View.GONE);
            }
        });

        tv_add_attachments.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog = new Dialog( mContext );
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.gallery_camera_layout);

                dialog.show();

                Button bt_gallery = (Button) dialog.findViewById(R.id.bt_gallery);
                Button bt_camera = (Button) dialog.findViewById(R.id.bt_camera);

                bt_gallery.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        startActivityForResult(intent, CHOOSE_FILE);
                    }
                });

                bt_camera.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();

                        int randomPIN = (int)(Math.random()*9000)+1000;
                        String file_index = String.valueOf(randomPIN);
                        String new_image_captured = "Topic_" + file_index + ".jpg";
                        attachment_name = new_image_captured;
                        File dir = Environment.getExternalStorageDirectory();
                        String targetFilename = dir.getAbsolutePath() + "/" + "Modasta" + "/" + new_image_captured;

                        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        uriSavedImage = Uri.fromFile(new File(targetFilename));
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

                        startActivityForResult(camera, CAMERA_PIC_REQUEST);
                    }
                });
            }
        });

        bt_try_again.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog("Loading...");
                uploadFilesOneByOne( parent_upload_id );
            }
        });

        /**********************************************
         * Directory created for saving image!
         **********************************************/
        boolean dirCreated = createDirIfNotExists("Modasta");
        Log.e( TAG , "Directory created? " + dirCreated );
        /**********************************************/
    }

    public static boolean createDirIfNotExists(String path)
    {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists())
        {
            if (!file.mkdirs())
            {
                Log.e("CREATE DIR", "Problem creating Image folder");
                ret = false;
            }
        }
        return ret;
    }

    private void attachmentRequest()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, CHOOSE_FILE);
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

    private void getAllTopics( String URL )
    {
        Log.e( TAG , " getAllTopics URL : " + URL );

        //URL = "http://192.168.1.7/api/user/allTopics/";

//        RequestQueue volleyQueue = Volley.newRequestQueue( mContext );
//        GetApiString api = new GetApiString( mContext , new Response.ErrorListener()
//        {
//            @Override
//            public void onErrorResponse(VolleyError error)
//            {
//                Log.e("errorVolley", String.valueOf(error));
//                dismissDialog();
//                tv_zero_state.setVisibility(View.VISIBLE);
//            }
//
//        } , URL , 154 );
//
//        volleyQueue.add(api);

        Random ran = new Random();
        int x = ran.nextInt(16) + 5;
        URL = URL + "?unused=" + x;

        Log.e( TAG , "URL : " + URL );
        new RequestTask().execute( URL );
    }


    class RequestTask extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... uri)
        {
            Log.e( TAG , "doInBackground" );

            HttpClient httpclient = new DefaultHttpClient();

            HttpResponse response;

            String responseString = null;

            try
            {
                HttpGet httpget = new HttpGet( uri[0] );
                httpget.addHeader("Cache-Control", "no-cache");
                response = httpclient.execute( httpget );

                StatusLine statusLine = response.getStatusLine();

                if(statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                }
                else
                {
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }
            catch (ClientProtocolException e)
            {
                //TODO Handle problems..
                Log.e( TAG , "ClientProtocolException : " + e.toString() );
                dismissDialog();
                Toast.makeText( mContext , "Network problem" , Toast.LENGTH_LONG).show();
            }
            catch (IOException e)
            {
                //TODO Handle problems..
                Log.e( TAG , "IOException : " + e.toString() );
                dismissDialog();
                Toast.makeText( mContext , "Network problem" , Toast.LENGTH_LONG).show();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            dismissDialog();

            Log.e( TAG , "onPostExecute");
            Log.e( TAG , "==> " + result );
            Log.e( TAG , "all-topics response!");

            try
            {
                mList = new ArrayList<>();

                JSONArray mainArr = new JSONArray(result);
                for( int i = 0 ; i < mainArr.length() ; i++ )
                {
                    JSONObject mainObj  = mainArr.getJSONObject( i );
                    String topic_title  = mainObj.getString("topicTitle");
                    String topic_id     = mainObj.getString("topicID");
                    String topic_date   = mainObj.getString("topicDate");

                    JSONObject authorObj = mainObj.getJSONObject("user_detail");
                    String author_name   = authorObj.getString("username");
                    String author_pic    = authorObj.getString("user_image");

                    TopicModel model = new TopicModel( topic_id , topic_title , author_name , topic_date, author_pic, false);
                    mList.add(model);
                }

                adapter = new TopicAdapter( mList , mContext);
                rvList.setAdapter( adapter );

                adapter.setOnItemClickListener(new TopicAdapter.OptionClickListener()
                {
                    @Override
                    public void onItemClick(int positions, View v)
                    {
                        TopicModel model = mList.get( positions );
                        startActivity( TopicDetailActivity.createIntent( mContext, model.getId() , "1" , group_id, "0") );
                    }
                });

                if( mList.size() <= 0 )
                {
                    tv_zero_state.setVisibility( View.VISIBLE );
                }
                else
                {
                    tv_zero_state.setVisibility( View.GONE );
                }
            }
            catch ( Exception e )
            {
                Log.e( TAG , "Ex : " + e.toString() );
                Toast.makeText( mContext , "Network problem." ,Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getTopics(String URL1 )
    {

        /**************************
         *
         * GET API FOR FORUM TOPICS
         *
         *
         *************************/
//        Log.e( TAG , "URL : " + URL );
//        RequestQueue volleyQueue = Volley.newRequestQueue(mContext);
//        GetApi api = new GetApi(this, new Response.ErrorListener()
//        {
//            @Override
//            public void onErrorResponse(VolleyError error)
//            {
//                Log.e("errorVolley", String.valueOf(error));
//                dismissDialog();
//                tv_zero_state.setVisibility(View.VISIBLE);
//            }
//        }, URL, 1);
//        volleyQueue.add(api);
        /*************************/


        String URL     = ServerConstants.TOPIC_DETAIL_EDITABLE;
        String cookie  = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

        Log.e( TAG , "cookie   : " + cookie);
        Log.e( TAG , "forum_id : " + forum_id);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("cookie"  , cookie);
        params.put("post_id" , forum_id);

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                dismissDialog();
                //Log.e(TAG, "getTopics : " + response);
                try
                {
                    JSONObject mainObj = new JSONObject( response );
                    JSONArray mainArr = mainObj.getJSONArray( "data" );

                    mList = new ArrayList<>();

                    for( int i = 0 ; i < mainArr.length() ; i++ )
                    {
                        JSONObject topicObj = mainArr.getJSONObject(i);
                        Log.e( TAG , "OBJ : " + topicObj.toString() );
                        String title    = topicObj.getString("post_title");
                        String modified = topicObj.getString("post_modified");
                        String id       = topicObj.getString("ID");
                        String flag     = topicObj.getString("flag");

                        boolean isEditable = false;
                        if( flag.equalsIgnoreCase("true") )
                        {
                            isEditable = true;
                        }

                        String author_name="";
                        String profile_pic_path="";
                        try
                        {
                            JSONObject authorObj = topicObj.getJSONObject("post_author");
                            author_name = authorObj.getString("user_nicename");

                            if( author_name == null || author_name.length() <= 0 )
                            {
                                author_name = authorObj.getString("display_name");
                            }

                            profile_pic_path = authorObj.getString("avatar");
                        }
                        catch( Exception e )
                        {
                            Log.e( TAG , "Ex : "+ e.toString());
                        }


                        TopicModel model = new TopicModel( id , title , author_name , modified, profile_pic_path , isEditable );
                        mList.add(model);
                    }

                    adapter = new TopicAdapter( mList , mContext);
                    rvList.setAdapter( adapter );

                    adapter.setOnItemClickListener(new TopicAdapter.OptionClickListener()
                    {
                        @Override
                        public void onItemClick(int positions, View v)
                        {
                            TopicModel model = mList.get( positions );
                            if( model.isEditable() )
                            {
                                startActivity(TopicDetailActivity.createIntent(mContext, model.getId(), "0", group_id, "1") );
                            }
                            else
                            {
                                startActivity( TopicDetailActivity.createIntent( mContext, model.getId(), "0" , group_id , "0" ) );
                            }

                        }
                    });

                    if( mList.size() <= 0 )
                    {
                        tv_zero_state.setVisibility( View.VISIBLE );
                    }
                    else
                    {
                        tv_zero_state.setVisibility( View.GONE );
                    }
                }
                catch( Exception e )
                {
                    Log.e( TAG , "Ex " + e.toString() );
                    tv_zero_state.setVisibility(View.VISIBLE);
                }
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                Log.e(TAG, "getTopics ex : " + error.toString());
                Toast.makeText(mContext, "Network Error", Toast.LENGTH_LONG).show();
            }

        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:

                if( fl_new_topic_sections.getVisibility() == View.VISIBLE )
                {
                    fl_new_topic_sections.setVisibility( View.GONE );
                }
                else
                {
                    super.onBackPressed();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static Intent createIntent( Context context , String forum_id, String group_id  , String forum_name)
    {
        Intent intent = new Intent( context , TopicListActivity.class );
        intent.putExtra("f_id"   , "" + forum_id);
        intent.putExtra("g_id"   , group_id);
        intent.putExtra("f_name" , forum_name);
        return intent;
    }

    private void createNewTopic(String URL, final HashMap<String, String> params)
    {
        showDialog("Loading...");

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                dismissDialog();
                Log.e(TAG, "Response1 : " + response );
                try
                {
                    JSONObject obj = new JSONObject( response );
                    String topic_id = obj.getString("topic_id");

                    parent_upload_id = topic_id;

                    String URL = ServerConstants.getForumTopics( forum_id );
                    showDialog("Loading...");

                    if ( attachmentModels.size() > 0 )
                    {
                        attachment_index_to_upload = 0;
                        uploadFilesOneByOne( topic_id );
                    }
                    else
                    {
                        Log.e(TAG, "No attachments added.");
                        et_topic_content.setText("");
                        et_topic_name.setText("");
                        fl_new_topic_sections.setVisibility(View.GONE);

                        getTopics(URL);

                    }

                }
                catch (Exception e)
                {
                    Log.e( TAG , "Ex :" + e.toString() );
                    Toast.makeText( mContext , "Network error. Please try again.", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText( mContext , "Network error. Please try again.", Toast.LENGTH_LONG).show();
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public void volleyResponse(int response_code, String response)
    {
        dismissDialog();
        Log.e(TAG, "Response : " + response);

        if( response_code == 154 )
        {
            Log.e(TAG, "all-topics response!");
            try
            {
                mList = new ArrayList<>();

                JSONArray mainArr = new JSONArray(response);
                for( int i = 0 ; i < mainArr.length() ; i++ )
                {
                    JSONObject mainObj  = mainArr.getJSONObject( i );
                    String topic_title  = mainObj.getString("topicTitle");
                    String topic_id     = mainObj.getString("topicID");
                    String topic_date   = mainObj.getString("topicDate");

                    JSONObject authorObj = mainObj.getJSONObject("user_detail");
                    String author_name   = authorObj.getString("username");
                    String author_pic    = authorObj.getString("user_image");

                    TopicModel model = new TopicModel( topic_id , topic_title , author_name , topic_date, author_pic, false);
                    mList.add(model);
                }

                adapter = new TopicAdapter( mList , mContext);
                rvList.setAdapter( adapter );

                adapter.setOnItemClickListener(new TopicAdapter.OptionClickListener()
                {
                    @Override
                    public void onItemClick(int positions, View v)
                    {
                        TopicModel model = mList.get( positions );
                        startActivity( TopicDetailActivity.createIntent( mContext, model.getId() , "1" , group_id, "0") );
                    }
                });

                if( mList.size() <= 0 )
                {
                    tv_zero_state.setVisibility( View.VISIBLE );
                }
                else
                {
                    tv_zero_state.setVisibility( View.GONE );
                }
            }
            catch ( Exception e )
            {
                Log.e( TAG , "Ex : " + e.toString() );
            }

        }
        else
        {
            try
            {
                JSONObject mainObj = new JSONObject( response );
                JSONArray mainArr = mainObj.getJSONArray( "data" );

                mList = new ArrayList<>();

                for( int i = 0 ; i < mainArr.length() ; i++ )
                {
                    JSONObject topicObj = mainArr.getJSONObject(i);

                    String title    = topicObj.getString("title");
                    String modified = topicObj.getString("modified");
                    String id       = topicObj.getString("ID");

                    String author_name="";
                    String profile_pic_path="";
                    try
                    {
                        JSONObject authorObj = topicObj.getJSONObject("author");
                        author_name = authorObj.getString("first_name");

                        if( author_name == null || author_name.length() <= 0 )
                        {
                            author_name = authorObj.getString("username");
                        }

                        profile_pic_path = authorObj.getString("avatar");
                    }
                    catch( Exception e )
                    {
                        Log.e( TAG , "Ex : "+ e.toString());
                    }


                    TopicModel model = new TopicModel( id , title , author_name , modified, profile_pic_path, false );
                    mList.add(model);
                }

                adapter = new TopicAdapter( mList , mContext);
                rvList.setAdapter( adapter );

                adapter.setOnItemClickListener(new TopicAdapter.OptionClickListener()
                {
                    @Override
                    public void onItemClick(int positions, View v)
                    {
                        TopicModel model = mList.get( positions );
                        startActivity( TopicDetailActivity.createIntent( mContext, model.getId(), "0" , group_id, "0" ) );
                    }
                });

                if( mList.size() <= 0 )
                {
                    tv_zero_state.setVisibility( View.VISIBLE );
                }
                else
                {
                    tv_zero_state.setVisibility( View.GONE );
                }
            }
            catch( Exception e )
            {
                Log.e( TAG , "Ex " + e.toString() );
                tv_zero_state.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onBackPressed()
    {
        if( fl_new_topic_sections.getVisibility() == View.VISIBLE )
        {
            fl_new_topic_sections.setVisibility(View.GONE);
        }
        else
        {
            super.onBackPressed();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_FILE && data != null && data.getData() != null)
        {
            final Uri uri    = data.getData();
            String uriString = uri.toString();
            String displayName = "";
            long file_size = 0;
            /*******************************************/
            try
            {
                if (uriString.startsWith("content://"))
                {
                    Cursor cursor = null;
                    try
                    {
                        cursor = this.getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst())
                        {
                            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                            file_size = ( size / 1024 ) / 1024 ;
                        }
                    }
                    finally
                    {
                        cursor.close();
                    }
                }
                else if (uriString.startsWith("file://"))
                {
                    File myFile = new File(uriString);
                    displayName = myFile.getName();
                    long size = myFile.length();
                    file_size = ( size / 1024 ) / 1024 ;
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "E : " + e.toString());
            }


            if (displayName.length() <= 0)
            {
                Toast.makeText( mContext , "Some thing went wrong :(", Toast.LENGTH_LONG).show();
                return;
            }

            if( file_size > 8 )
            {
                Toast.makeText( mContext , "File upload limit is 8 MB", Toast.LENGTH_LONG).show();
                return;
            }

            /*****************
             * uploading file
             ****************/
            attachment_uri = uri;
            attachment_name = displayName;
            //tv_attachments_name.setText("" + displayName);
            //tv_attachments_name.setVisibility(View.VISIBLE);
            //tv_add_attachments.setVisibility(View.GONE);
            /****************/


            /*************************************************
             * Multiple upload thing
             *************************************************/
            final AttachmentLayout valueTV = new AttachmentLayout(mContext);
            valueTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            valueTV.setAttachmentName(attachment_name);
            ll_attachment_holder.addView(valueTV);


            valueTV.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AttachmentLayout layout = (AttachmentLayout) v;
                    String attachment_name = layout.getAttachmentName();
                    Log.e( TAG , "Attachment Name : " + attachment_name );
                    for( int i = 0 ; i < attachmentModels.size() ; i++ )
                    {
                         if( attachmentModels.get(i).getDisplayName().equalsIgnoreCase(attachment_name))
                         {
                             Log.e( TAG , "remove" );
                             attachmentModels.remove(i);
                             break;
                         }
                    }

                    ((LinearLayout) v.getParent()).removeView(v);
                }
            });

             attachmentModels.add( new AttachmentModel( uri , displayName ) );
            /*************************************************/

        }
        else if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK)
        {
            try
            {
                final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.setContentView(R.layout.crop_image_layout);
                dialog.setTitle("Upload");
                dialog.show();

                final CropImageView cropImageView = (CropImageView) dialog.findViewById(R.id.iv_cropimageview);
                final LinearLayout ll_crop_optios = (LinearLayout) dialog.findViewById(R.id.ll_crop_options);
                final LinearLayout ll_upload_options = (LinearLayout) dialog.findViewById(R.id.ll_upload_options);
                final ImageView iv_crop = (ImageView) dialog.findViewById(R.id.iv_crop);
                final ImageView iv_delete = (ImageView) dialog.findViewById(R.id.iv_delete);
                final ImageView iv_crop_ok = (ImageView) dialog.findViewById(R.id.iv_crop_ok);
                final ImageView iv_crop_cance = (ImageView) dialog.findViewById(R.id.iv_crop_cancel);
                final Button bt_cancel = (Button) dialog.findViewById(R.id.bt_cancel);

                cropImageView.setCropEnabled(false);

                cropImageView.setFrameColor(getResources().getColor(R.color.colorPrimary));
                cropImageView.setHandleColor(getResources().getColor(R.color.fab_color));
                cropImageView.setGuideColor(getResources().getColor(R.color.colorPrimary));

                cropImageView.setFrameStrokeWeightInDp(1);
                cropImageView.setGuideStrokeWeightInDp(1);
                cropImageView.setHandleSizeInDp(8);
                cropImageView.setTouchPaddingInDp(16);


                Bitmap bitmap = decodeSampledBitmapFromFile(uriSavedImage.getPath(), 100, 100);

//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriSavedImage);
                cropImageView.setImageBitmap(bitmap);
                cropImageView.invalidate();

                Button bt_upload = (Button) dialog.findViewById(R.id.bt_upload);
                bt_upload.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                        //String filename = "" + new Date().getTime() + ".jpg";
                        //String filename1 = "" + new Date().getTime() + ".png";

                        if (croppedFile != null)
                        {
                            /**************************************
                             * Cropped imageview. Not supported now
                             *************************************/

                            //Uri uri = Uri.fromFile(croppedFile);
                            //askFileNameBeforeUploading(filename1, uri);

                            /*************************************/
                        }
                        else if ( uriSavedImage != null )
                        {
                            //askFileNameBeforeUploading(filename, uriSavedImage);
                            /*****************
                             * uploading file
                             ****************/
                            attachment_uri     = uriSavedImage ;
                            //tv_attachments_name.setText("" + attachment_name );
                            //tv_attachments_name.setVisibility(View.VISIBLE);
                            //tv_add_attachments.setVisibility(View.GONE);


                            /*************************************************
                             * Multiple upload thing
                             *************************************************/
                            final AttachmentLayout valueTV = new AttachmentLayout(mContext);
                            valueTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            valueTV.setAttachmentName(attachment_name);
                            ll_attachment_holder.addView(valueTV);


                            valueTV.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    AttachmentLayout layout = (AttachmentLayout) v;
                                    String attachment_name = layout.getAttachmentName();
                                    Log.e( TAG , "Attachment Name : " + attachment_name );
                                    for( int i = 0 ; i < attachmentModels.size() ; i++ )
                                    {
                                        if( attachmentModels.get(i).getDisplayName().equalsIgnoreCase(attachment_name))
                                        {
                                            Log.e( TAG , "remove" );
                                            attachmentModels.remove(i);
                                            break;
                                        }
                                    }

                                    ((LinearLayout) v.getParent()).removeView(v);
                                }
                            });

                            attachmentModels.add( new AttachmentModel( attachment_uri , attachment_name ) );
                            /*************************************************/
                            /****************/
                        }
                        else
                        {
                            Toast.makeText(mContext, "Sorry. Image upload failed.", Toast.LENGTH_LONG).show();
                        }

                    }
                });

                bt_cancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });

                iv_crop.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        cropImageView.setCropEnabled(true);
                        ll_crop_optios.setVisibility(View.VISIBLE);
                        ll_upload_options.setVisibility(View.GONE);
                    }
                });

                iv_crop_ok.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        final Toast d = Toast.makeText( mContext , "Cropping image...", Toast.LENGTH_LONG);
                        d.show();
                        cropImageView.setCropEnabled(false);
                        ll_crop_optios.setVisibility(View.GONE);
                        ll_upload_options.setVisibility(View.VISIBLE);

                        AsyncTask.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                String path = StorageUtil.saveToCacheFile(cropImageView.getCroppedBitmap());
                                Log.e(TAG, "Image saved at : " + path);
                                croppedFile = new File(path);

                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if ( croppedFile.isFile() )
                                        {
                                            d.cancel();
                                            Toast.makeText( mContext , "Done!", Toast.LENGTH_LONG).show();
                                            Bitmap myBitmap = BitmapFactory.decodeFile(croppedFile.getAbsolutePath());
                                            cropImageView.setImageBitmap(myBitmap);
                                        }
                                        else
                                        {
                                            d.cancel();
                                            Toast.makeText( mContext , "Image cropping failed.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        });


                    }
                });

                iv_crop_cance.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ll_crop_optios.setVisibility(View.GONE);
                        ll_upload_options.setVisibility(View.VISIBLE);
                        cropImageView.setCropEnabled(false);
                    }
                });

            } catch (Exception e)
            {
                Toast.makeText(this, "Picture Not taken " + e.toString(), Toast.LENGTH_LONG).show();
            }
        } else
        {
            Log.e(TAG, "Oops :(");

        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight)
    {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth)
            {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void uploadFilesOneByOne( String topic_id )
    {
        try
        {
            if( attachment_index_to_upload < attachmentModels.size() )
            {
                AttachmentModel model = attachmentModels.get( attachment_index_to_upload );
                uploadFileUsingVolley( topic_id , model.getDisplayName() , model.getImageUri() );
            }
            else
            {

                Log.e(TAG, "Finished upload all images.");
                ll_upload_error_msg.setVisibility(View.GONE);
                et_topic_content.setText("");
                et_topic_name.setText("");
                fl_new_topic_sections.setVisibility(View.GONE);

                ll_attachment_holder.removeAllViews();
                attachmentModels.clear();
                String URL = ServerConstants.getForumTopics(forum_id);
                getTopics(URL);
            }
        }
        catch( Exception e )
        {
            Log.e( TAG , "Ex . " + e.toString()  );
        }
    }

    public void uploadFileUsingVolley( final String topic_id , String attachment_name , Uri attachment_uri )
    {
        Log.e( TAG , "uploading : " + attachment_name );

        if( attachment_name.length() <= 0 || attachment_uri == null )
        {
            String URL = ServerConstants.getForumTopics( forum_id );
            getTopics(URL);
            return;
        }

        byte[] multipartBody = null;

        byte[] fileData1 = null;
        try
        {
            fileData1 = getBytes( mContext , attachment_uri);
        } catch (Exception e)
        {

            Log.e(TAG, "Ex : " + e.toString());
            return;
        }


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try
        {
            String token = LocalDataManager.getInstance().get(PreferenceConstants.Token);
            buildPart(dos, fileData1, attachment_name, topic_id, token);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            multipartBody = bos.toByteArray();
        } catch (IOException e)
        {

            Log.e(TAG, "E. " + e.toString());
            return;
        }

        showDialog("Loading...");

        String uploadServerUri = ServerConstants.UPLOAD_DOCS;

        MultipartRequest multipartRequest = new MultipartRequest(uploadServerUri, null, mimeType, multipartBody, new Response.Listener<String>()
        {

            @Override
            public void onResponse(String response)
            {
                dismissDialog();

                Log.e( TAG , "Upload Response : " + response );

                try
                {
                    JSONObject obj = new JSONObject( response );
                    String msg = obj.getString("msg");

                    if( msg.contains("Success") )
                    {
                        ll_upload_error_msg.setVisibility(View.GONE);
                        setThisAttachmentAsUploaded(attachment_index_to_upload );
                        attachment_index_to_upload++;
                        uploadFilesOneByOne( topic_id );
                    }
                    else
                    {
                        ll_upload_error_msg.setVisibility(View.VISIBLE);
                        Toast.makeText(mContext,"Upload image error.",Toast.LENGTH_LONG).show();
                    }

                }
                catch( Exception e )
                {
                    Log.e( TAG , "Ex . " + e.toString() );
                    Toast.makeText(mContext,"Network Error!",Toast.LENGTH_LONG).show();
                    ll_upload_error_msg.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                Log.e(TAG, "Er. " + error.toString());
                Toast.makeText(mContext,"Network Error!",Toast.LENGTH_LONG).show();
                ll_upload_error_msg.setVisibility(View.VISIBLE);
            }
        });

        RequestQueue volleyQueue = Volley.newRequestQueue( mContext );
        volleyQueue.add(multipartRequest);
    }

    private void setThisAttachmentAsUploaded( int index )
    {
        try
        {
            AttachmentLayout view = (AttachmentLayout) ll_attachment_holder.getChildAt( index );
            view.setAttachmentAsUploaded();
        }
        catch( Exception  e )
        {
            Log.e( TAG , "Ex setThisAttachmentAsUploaded " + e.toString() );
        }
    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName12, String parentid , String token ) throws IOException
    {
        String fileName = fileName12 + "::" + parentid + "::" + token ;

        Log.e( TAG , "==> " + fileName );

        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0)
        {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException
    {

        byte[] bytesResult = null;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        try
        {
            int len;
            while ((len = inputStream.read(buffer)) != -1)
            {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        } finally
        {
            // close the stream
            try
            {
                byteBuffer.close();
            } catch (IOException ignored)
            { /* do nothing */ }

        }
        return bytesResult;
    }

    public static byte[] getBytes(Context context, Uri uri) throws IOException
    {
        InputStream iStream = context.getContentResolver().openInputStream(uri);
        try
        {
            return getBytes(iStream);
        } finally
        {
            try
            {

                iStream.close();
            } catch (Exception ignored)
            {
                Log.e("uploadFile", "" + ignored.toString());
            }
        }
    }
}
