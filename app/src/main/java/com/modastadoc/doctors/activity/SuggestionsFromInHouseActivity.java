package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.SuggestionsListAdapter;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by vivek.c on 04/10/16.
 */
public class SuggestionsFromInHouseActivity extends AppCompatActivity
{
    private static final String QUESTION_POOL_ARRAY = "reviewQuestions";
    private static final String QUESTION_ID = "question_id";
    private static final String QUESTION = "question";
    private static final String QUERY_TIME = "date";
    private static final String QUESTION_STATUS = "status";
    SuggestionsListAdapter adapter;
    AlertDialog alertDialog;
    ArrayList<String> qList = new ArrayList<>();
    ArrayList<String> qDateList = new ArrayList<>();
    ArrayList<String> qIDList = new ArrayList<>();
    ArrayList<String> qStatusList = new ArrayList<>();
    RecyclerView recycleView;

    TextView tv_zero_state;

    //    QueryAdapterTest adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Suggestions Given By Inhouse");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tv_zero_state = (TextView) findViewById(R.id.tv_zero_state);
        recycleView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(mLayoutManager);

        getAcceptedQuery();

    }

    private void getAcceptedQuery()
    {
        String URL = ServerConstants.DOMAIN + ServerConstants.INHOUSE_REVIEW;

        HashMap<String, String> params = new HashMap<String, String>();
        showDialog("Loading...");
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.v("Suggestions", "Response : " + response);
                buildQuestionsView(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.v("Error in Suggestions", "Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();

            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    private void buildQuestionsView(String response)
    {

        if (!response.equals("[]") && !response.equals("[ ]"))
        {
            try
            {
                JSONObject jsonRootObject = new JSONObject(response);
                //Get the instance of JSONArray that contains JSONObjects
                JSONArray jsonArray = jsonRootObject.optJSONArray(QUESTION_POOL_ARRAY);
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int question_Id = jsonObject.getInt(QUESTION_ID);
                    qIDList.add(String.valueOf(question_Id));
                    String question = jsonObject.optString(QUESTION).toString();
                    qList.add(question);
                    String date_time = jsonObject.optString(QUERY_TIME).toString();
                    qDateList.add(date_time);
                    String status = jsonObject.optString(QUESTION_STATUS).toString();
                    qStatusList.add(status);

                }
                Collections.reverse(qIDList);
                Collections.reverse(qList);
                Collections.reverse(qDateList);
                Collections.reverse(qStatusList);

                if( qList.size() <= 0 )
                {
                    tv_zero_state.setVisibility(View.VISIBLE);
                    tv_zero_state.setText("No results found");
                }

                adapter = new SuggestionsListAdapter(qIDList, qList, qDateList, qStatusList, getApplicationContext());
                Log.v("adapter", "" + adapter);
                recycleView.setAdapter(adapter);
                dismissDialog();

            } catch (JSONException e)
            {
                e.printStackTrace();
                tv_zero_state.setText("Network error. Please try again.");
                tv_zero_state.setVisibility(View.VISIBLE);
            }

            adapter.setOnItemClickListener(new SuggestionsListAdapter.OptionClickListener()
            {
                @Override
                public void onItemClick(int position, View v)
                {
                    Intent intent = new Intent(getApplicationContext(), SuggestionDetailActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("question_Id", qIDList.get(position));
                    intent.putExtra("clickstatus", "true");
                    startActivity(intent);

                }
            });
        }
    }

    private void showDialog(String message)
    {

        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(SuggestionsFromInHouseActivity.this);
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

