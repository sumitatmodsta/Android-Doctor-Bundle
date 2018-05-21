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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.VolleyCallback;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.network.GetApiArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GroupListActivity extends AppCompatActivity  implements VolleyCallback
{
    String TAG = GroupListActivity.class.getSimpleName();

    Context mContext;

    RecyclerView rvList;
    ArrayList<GroupModel> mList;
    GroupAdapter adapter;

    AlertDialog alertDialog;


    /******************************************************************************
     *
     * NOT USED. GROUPLISTFRAGMENT is used for this
     *
     *****************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        mContext = this;

        rvList    = (RecyclerView) findViewById(R.id.rv_group_list);

        try
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch (Exception e)
        {
            Log.e(TAG, "e. " + e.toString());
        }

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvList.setLayoutManager(mLayoutManager);


        showDialog("Loading...");
//        getSubGroups();
        getMainGroups();
    }

    private void getMainGroups()
    {
        String URL = ServerConstants.GET_MAIN_FORUMS;
        RequestQueue volleyQueue = Volley.newRequestQueue(mContext);
        GetApiArray api = new GetApiArray(this, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("errorVolley", String.valueOf(error));
                dismissDialog();
            }
        }, URL, 1);
        volleyQueue.add(api);
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

    public static Intent createIntent( Context context )
    {
        Intent intent = new Intent( context , GroupListActivity.class );
        return intent;
    }


    @Override
    public void volleyResponse(int error_code, String response)
    {
        dismissDialog();

        Log.e( TAG , "Response : " + response );
        try
        {
            mList = new ArrayList<>();

            JSONArray mainArr = new JSONArray( response );
            for( int i = 0 ; i < mainArr.length() ; i++ )
            {
                JSONObject groupObj = mainArr.getJSONObject( i );
                String group_id = groupObj.getString("ID");
                String group_name = groupObj.getString("post_title");
                String group_modified = groupObj.getString("post_modified");

                GroupModel model = new GroupModel( group_id , group_name , group_modified,"") ;
                mList.add( model );
            }

            adapter = new GroupAdapter(mList, mContext);
            rvList.setAdapter(adapter);

            adapter.setOnItemClickListener(new GroupAdapter.OptionClickListener()
            {
                @Override
                public void onItemClick(int positions, View v)
                {
                    GroupModel model = mList.get( positions );
                    startActivity( SubGroupListActivity.createIntent(mContext , model.getId(), model.getGroupName()));
                }
            });
        }
        catch( Exception e )
        {
            Log.e( TAG , "ex : " + e.toString() );
        }
    }
}
