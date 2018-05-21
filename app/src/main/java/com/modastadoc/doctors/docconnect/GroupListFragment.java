package com.modastadoc.doctors.docconnect;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.VolleyCallback;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.network.GetApiArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vijay.hiremath on 14/11/16.
 */
public class GroupListFragment extends Fragment implements VolleyCallback
{
    String TAG = GroupListFragment.class.getSimpleName();

    Context mContext;

    RecyclerView rvList;
    ArrayList<GroupModel> mList = new ArrayList<>();
    GroupAdapter adapter;

    AlertDialog alertDialog;

    FrameLayout fl_loading;
    TextView tv_zero_state;

    public GroupListFragment()
    {
    }


    public static GroupListFragment newInstance()
    {
        GroupListFragment fragment = new GroupListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.activity_group, container, false);

        mContext = getActivity();

        rvList    = (RecyclerView) rootView.findViewById(R.id.rv_group_list);
        fl_loading = (FrameLayout) rootView.findViewById(R.id.fl_loading);
        tv_zero_state = (TextView) rootView.findViewById(R.id.tv_zero_state);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvList.setLayoutManager(mLayoutManager);


        if( mList.size() <= 0 )
        {
            showDialog("Loading...");
            getMainGroups();
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private void getMainGroups()
    {
        String userId = LocalDataManager.getInstance().get(PreferenceConstants.DOCID);

        String URL = ServerConstants.GET_MAIN_FORUMS ;

        Log.e( TAG , "URL : " + URL );
        RequestQueue volleyQueue = Volley.newRequestQueue(mContext);
        GetApiArray api = new GetApiArray(this, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("errorVolley : ", String.valueOf(error));
                tv_zero_state.setVisibility(View.VISIBLE);
                dismissDialog();
            }
        }, URL, 1);
        volleyQueue.add(api);
    }

    private void showDialog(String message)
    {
        fl_loading.setVisibility(View.VISIBLE);
    }

    private void dismissDialog()
    {
        fl_loading.setVisibility(View.GONE);
    }

    @Override
    public void volleyResponse(int error_code, String response)
    {
        dismissDialog();

        Log.e(TAG, "Response : " + response);
        try
        {
            mList = new ArrayList<>();
            String user_type = LocalDataManager.getInstance().get(PreferenceConstants.USER_TYPE);

            JSONArray mainArr = new JSONArray( response );
            for( int i = 0 ; i < mainArr.length() ; i++ )
            {
                JSONObject groupObj = mainArr.getJSONObject( i );

                String group_id = groupObj.getString("ID");
                String group_name = groupObj.getString("post_title");
                String group_modified = groupObj.getString("post_modified");
                String group_icon = groupObj.getString("img");

                GroupModel model = new GroupModel( group_id , group_name , group_modified, group_icon) ;
                if( group_name.equalsIgnoreCase("ima") )
                {
                    if( user_type.equalsIgnoreCase( PreferenceConstants.ONLY_PHYSICIAN ))
                    {

                    }
                    else
                    {
                        mList.add(model);
                    }
                }
                else
                {
                    mList.add(model);
                }
            }

            adapter = new GroupAdapter(mList, mContext);
            rvList.setAdapter(adapter);

            if( mList.size() <= 0)
            {
                tv_zero_state.setVisibility( View.VISIBLE );
            }
            else
            {
                tv_zero_state.setVisibility( View.GONE );
            }

            adapter.setOnItemClickListener(new GroupAdapter.OptionClickListener()
            {
                @Override
                public void onItemClick(int positions, View v)
                {
                    //todo use slug here :)
                    GroupModel model = mList.get( positions );
                    if( model.getGroupName().equalsIgnoreCase("Professional Networking") )
                    {
                        startActivity( ForumListActivity.createIntent( mContext , model.getId() , model.getGroupName() , model.getGroupName() ) );
                    }
                    else
                    {
                        startActivity( SubGroupListActivity.createIntent(mContext , model.getId(), model.getGroupName()));
                    }


                }
            });

            adapter.setButtonClickListener(new GroupAdapter.ButtonClickListener()
            {
                @Override
                public void onButtonClick(int positions, View v)
                {
                    GroupModel model = mList.get( positions );
                    Log.e( TAG , "" + model.getGroupIcon() );
                    requestToJoinGroup();
                }
            });
        }
        catch( Exception e )
        {
            Log.e( TAG , "ex : " + e.toString() );
        }
    }

    private void requestToJoinGroup()
    {
        Log.e( TAG , "There you are!" );
    }
}
