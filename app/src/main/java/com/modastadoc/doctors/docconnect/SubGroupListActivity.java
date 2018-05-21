package com.modastadoc.doctors.docconnect;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.LoginActivity;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SubGroupListActivity extends AppCompatActivity
{
    String TAG = SubGroupListActivity.class.getSimpleName();

    Context mContext;

    RecyclerView rvList;
    ArrayList<SubGroupModel> mList;
    SubGroupAdapter adapter;
    TextView tv_zero_state;
    TextView tv_sub_group_title;

    String group_id = "0";
    String group_name = "";

    AlertDialog alertDialog;

    boolean isGroupJoinAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_group_list);

        mContext = this;

        /************************/
        Bundle bundle = getIntent().getExtras();
        if( bundle != null )
        {
            if( bundle.containsKey("g_id") );
            {
                group_id = bundle.getString("g_id");
            }

            if( bundle.containsKey("g_name"))
            {
                group_name = bundle.getString("g_name");
            }
        }
        Log.e( "hello_world1" , "Group ID : "  + group_id );
        /************************/

        rvList = (RecyclerView) findViewById(R.id.rv_subgroup_list);
        tv_zero_state = (TextView) findViewById(R.id.tv_zero_state);
        tv_sub_group_title = (TextView) findViewById(R.id.tv_sub_group_title);

        tv_sub_group_title.setText("" + group_name + " Sub-Groups" );

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvList.setLayoutManager(mLayoutManager);

        /***************************
         * User has used doc-connect
         ***************************/
        LocalDataManager.getInstance().set( PreferenceConstants.FIRST_TIME_LOGIN_DOC_CONNECT , "1" );
        /***************************/


        try
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(group_name);
            getSupportActionBar().setElevation(0);
        }
        catch (Exception e)
        {
            Log.e(TAG, "e. " + e.toString());
        }

        showDialog("Loading...");
        getAllSubGroups();
    }

    private void getAllSubGroups()
    {
        String userid = LocalDataManager.getInstance().get(PreferenceConstants.DOCID);
        String cookie = LocalDataManager.getInstance().get(PreferenceConstants.cookie);


        String URL = ServerConstants.GET_CHILD_FORUMS;
        Log.e(TAG, "sub-groups : " + URL);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("forumId", group_id);
        params.put("userId", userid);
        params.put("cookie", cookie);
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                dismissDialog();
                Log.e( TAG , "Sub group response : " + response);
                try
                {
                    mList = new ArrayList<>();
                    JSONArray mainArr = new JSONArray( response );
                    for( int i = 0 ; i < mainArr.length() ; i++ )
                    {
                        JSONObject sub_group_obj = mainArr.getJSONObject( i ) ;
                        String sub_group_id = sub_group_obj.getString("ID");
                        String sub_group_modified = sub_group_obj.getString("post_modified");
                        String sub_group_title = sub_group_obj.getString("post_title");
                        String sub_slug = sub_group_obj.getString("post_name");
                        String pic_url = sub_group_obj.getString("img");
                        String group_id = sub_group_obj.getString("group_id");
                        String isJoinedS = sub_group_obj.getString("is_joined");

                        boolean isJoined = false ;
                        if( isJoinedS.equalsIgnoreCase("false"))
                        {
                            isJoined = false;
                        }
                        else
                        {
                            isJoined = true;
                        }

                        SubGroupModel model  = new SubGroupModel(sub_group_id,sub_group_title,sub_group_modified, sub_slug, pic_url , isJoined ,group_id);

                        mList.add( model );
                    }

                    adapter = new SubGroupAdapter( mList , mContext ,isGroupJoinAvailable );
                    rvList.setAdapter( adapter );
                    adapter.setOnItemClickListener(new SubGroupAdapter.OptionClickListener()
                    {
                        @Override
                        public void onItemClick(int positions, View v)
                        {
                            SubGroupModel model = mList.get(positions);
                            startActivity( ForumListActivity.createIntent(mContext, model.getId() , model.getName() ,model.getSlug()));
                        }
                    });
                }
                catch( Exception e )
                {
                    Log.e( TAG , "Ex : " + e.toString() );
                    tv_zero_state.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if( error.networkResponse.statusCode == 403 )
                {
                    new AlertDialog.Builder( mContext )
                            .setTitle("Session Expired")
                            .setMessage("User session has expired. Please Login again")
                            .setCancelable(false)
                            .setPositiveButton("Login", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Intent loginIntent = new Intent( mContext , LoginActivity.class );
                                    startActivity(loginIntent);
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else
                {
                    tv_zero_state.setVisibility( View.VISIBLE );
                    dismissDialog();
                    Log.e( TAG , "Error Response : " + error.toString());
                    Toast.makeText(getApplicationContext(), "Network error.", Toast.LENGTH_LONG).show();
                }

            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public static Intent createIntent(Context context, String group_id ,String group_name )
    {
        Intent intent = new Intent(context, SubGroupListActivity.class);
        intent.putExtra("g_id", group_id);
        intent.putExtra("g_name", group_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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

}
