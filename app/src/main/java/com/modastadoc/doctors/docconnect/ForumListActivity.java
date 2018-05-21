package com.modastadoc.doctors.docconnect;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ForumListActivity extends AppCompatActivity
{
    String TAG = ForumListActivity.class.getSimpleName();

    /*************************/
    String twoHyphens = "--";
    String lineEnd = "\r\n";
    String boundary = "apiclient-" + System.currentTimeMillis();
    String mimeType = "multipart/form-data;boundary=" + boundary;
    /*************************/

    Context mContext;

    RecyclerView rvList;
    ArrayList<SubGroupModel> mList;
    SubGroupAdapter adapter;
    TextView tv_zero_state;

    String sub_group_id = "0";
    String sub_group_name = "";
    String sub_groups_slug ="";

    AlertDialog alertDialog;
    FloatingActionButton fab_new_group;
    FloatingActionsMenu fam_menu;

    FrameLayout fl_new_group;
    EditText et_request_group;
    Button bt_new_group;
    TextView tv_response;
    Button bt_close_window;
    TextView tv_forum_title;

    int CHOOSE_FILE = 145;
    boolean isGroupJoinAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forum_list);

        mContext = this;

        /************************/
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            if (bundle.containsKey("s_g_id")) ;
            {
                sub_group_id = bundle.getString("s_g_id");
            }


            if (bundle.containsKey("s_g_name"))
            {
                sub_group_name = bundle.getString("s_g_name");
            }

            if( bundle.containsKey("slug"))
            {
                sub_groups_slug = bundle.getString("slug");
            }
        }
        Log.e( TAG , "Forum ID   : " + sub_group_id );
        Log.e( TAG , "Forum Slug : " + sub_groups_slug );
        /************************/


        /***************************
         * User has used doc-connect
         ***************************/
        LocalDataManager.getInstance().set(PreferenceConstants.FIRST_TIME_LOGIN_DOC_CONNECT, "1");
        /***************************/

        rvList = (RecyclerView) findViewById(R.id.rv_forum_list);
        tv_zero_state = (TextView) findViewById(R.id.tv_zero_state);
        fab_new_group = (FloatingActionButton) findViewById(R.id.fab_request_new_group);
        fam_menu = (FloatingActionsMenu) findViewById(R.id.fam_menu);
        fl_new_group = (FrameLayout) findViewById(R.id.fl_new_group);

        et_request_group = (EditText) findViewById(R.id.et_request_group);
        bt_new_group = (Button) findViewById(R.id.bt_new_group);
        tv_response = (TextView) findViewById(R.id.tv_response);
        bt_close_window = (Button) findViewById(R.id.bt_close_window);
        tv_forum_title = (TextView) findViewById(R.id.tv_forum_title);

        tv_forum_title.setText("" + sub_group_name + " Forums" );

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvList.setLayoutManager(mLayoutManager);

        try
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(sub_group_name);
            getSupportActionBar().setElevation(0);
        }
        catch (Exception e)
        {
            Log.e(TAG, "e. " + e.toString());
        }

        showDialog("Loading...");
        getAllSubGroups();

        // show join button for all except ima-ka
        if( !sub_group_name.equalsIgnoreCase("ima-ka") )
        {
            isGroupJoinAvailable = true;
        }


        if( sub_groups_slug.equalsIgnoreCase("academic"))
        {
            fab_new_group.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    proceedNewGroupRequest();
                }
            });
        }
        else
        {
            fam_menu.setVisibility(View.GONE);
        }

    }



    private void proceedNewGroupRequest()
    {
        fl_new_group.setVisibility(View.VISIBLE);
        bt_new_group.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String new_group_name = et_request_group.getText().toString();
                if (new_group_name.length() > 0)
                {
                    requestNewGroup(new_group_name);
                }
            }
        });

        bt_close_window.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fl_new_group.setVisibility(View.GONE);
                fam_menu.collapse();
            }
        });
    }

    private void requestNewGroup( String response )
    {
        showDialog("Processing...");

        String user_id = LocalDataManager.getInstance().get(PreferenceConstants.DOCID);

        String URL = ServerConstants.REQUEST_NEW_GROUP;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("content" , response );
        params.put("userId"  , user_id);

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e( TAG , "requestNewGroup : " + response);

                dismissDialog();

                if( response.equalsIgnoreCase("true") )
                {
                    tv_response.setText("Your request is processed, admin will create the group.");
                    tv_response.setVisibility(View.VISIBLE);
                    bt_close_window.setVisibility(View.VISIBLE);

                    et_request_group.setVisibility(View.GONE);
                    bt_new_group.setVisibility(View.GONE);
                }
                else
                {
                    tv_response.setVisibility(View.VISIBLE);
                    tv_response.setText("Network error. Please try again");
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                Log.e(TAG, "E--> " + error.toString());
            }
        },params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void getAllSubGroups()
    {
        String userid = LocalDataManager.getInstance().get(PreferenceConstants.DOCID);
        String cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);

        String URL = ServerConstants.GET_CHILD_FORUMS;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("forumId", sub_group_id);
        params.put("userId",userid);
        params.put("cookie",cookie);
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                dismissDialog();
                Log.e(TAG, "getAllSubGroups Response : " + response);
                try
                {
                    mList = new ArrayList<>();
                    JSONArray mainArr = new JSONArray(response);
                    for (int i = 0; i < mainArr.length(); i++)
                    {
                        JSONObject sub_group_obj = mainArr.getJSONObject(i);
                        String sub_group_id = sub_group_obj.getString("ID");
                        String sub_group_modified = sub_group_obj.getString("post_modified");
                        String sub_group_title = sub_group_obj.getString("post_title");
                        String post_name = sub_group_obj.getString("post_name");
                        String pic_url = sub_group_obj.getString("img");
                        String isJoinedS = sub_group_obj.getString("is_joined");
                        String group_id = sub_group_obj.getString("group_id");

                        boolean isJoined = false ;
                        if( isJoinedS.equalsIgnoreCase("false"))
                        {
                            isJoined = false;
                        }
                        else
                        {
                            isJoined = true;
                        }


                        SubGroupModel model = new SubGroupModel(sub_group_id, sub_group_title, sub_group_modified, post_name, pic_url, isJoined, group_id);
                        mList.add(model);
                    }

                    Collections.reverse(mList);

                    adapter = new SubGroupAdapter(mList, mContext , isGroupJoinAvailable);
                    rvList.setAdapter(adapter);

                    if (mList.size() <= 0)
                    {
                        tv_zero_state.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        tv_zero_state.setVisibility(View.GONE);

                        adapter.setOnItemClickListener(new SubGroupAdapter.OptionClickListener()
                        {
                            @Override
                            public void onItemClick(int positions, View v)
                            {

                                SubGroupModel model = mList.get(positions);
                                if (model.getSlug().equalsIgnoreCase("all-topics"))
                                {
                                    startActivity(TopicListActivity.createIntent(mContext, model.getSlug() , model.getGroupId(),model.getName()));
                                }
                                else
                                {
                                    if( model.isJoined() )
                                    {
                                        startActivity(TopicListActivity.createIntent(mContext, model.getId() , model.getGroupId(),model.getName()));
                                    }
                                    else
                                    {
                                        Toast.makeText(mContext,"Please join " + model.getName() ,Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });

                        adapter.setOnButtonClickListener(new SubGroupAdapter.ButtonClickListener()
                        {
                            @Override
                            public void onButtonClick(int positions, View v)
                            {
                                SubGroupModel model = mList.get(positions);
                                Log.e( TAG , "Do you want to join? "  + model.getName() );
                                joinGroup( model.getGroupId() );
                            }
                        });
                    }

                } catch (Exception e)
                {
                    Log.e(TAG, "Ex : " + e.toString());
                    tv_zero_state.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                tv_zero_state.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error Response : " + error.toString());
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void joinGroup( String group_id )
    {
        showDialog( "Loading...");
        Log.e( TAG , "joinGroup : " + group_id);

        String cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);
        Log.e( TAG , "Cookie - > " + cookie );


        //todo hardcoded url
//        String URL = "http://192.168.1.7/api/user/joingroup";
        //String URL = "http://192.168.1.7/api/user/joinDocGroup";
        String URL = ServerConstants.JOIN_GROUP;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put( "group_id", group_id);
        params.put( "cookie" , cookie);

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e( TAG ,"Join Group response : " + response );
                dismissDialog();
                showDialog("Loading...");
                getAllSubGroups();
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                tv_zero_state.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error Response : " + error.toString());
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
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
                if( fl_new_group.getVisibility() == View.VISIBLE )
                {
                    fl_new_group.setVisibility( View.GONE );
                    fam_menu.collapse();
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

    public static Intent createIntent(Context context, String sub_group_id, String sub_group_name, String slug )
    {
        Intent intent = new Intent(context, ForumListActivity.class);
        intent.putExtra("s_g_id", "" + sub_group_id);
        intent.putExtra("s_g_name", sub_group_name);
        intent.putExtra("slug" , slug );
        return intent;
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

    @Override
    public void onBackPressed()
    {
        if( fl_new_group.getVisibility() == View.VISIBLE )
        {
            fl_new_group.setVisibility( View.GONE );
            fam_menu.collapse();
        }
        else
        {
            super.onBackPressed();
        }
    }

}
