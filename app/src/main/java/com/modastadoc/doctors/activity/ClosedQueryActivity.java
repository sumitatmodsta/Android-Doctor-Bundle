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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.AnsweredQueryListAdapter;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by vivek.c on 22/09/16.
 */
public class ClosedQueryActivity extends AppCompatActivity
{
    String TAG = ClosedQueryActivity.class.getSimpleName();

    RecyclerView recycleView;
    private static final String QUESTION_POOL_ARRAY = "answeredQuestion";
    private static final String QUESTION_ID = "question_id";
    private static final String QUESTION = "question";
    private static final String QUERY_TIME = "date_time";
    private static final String QUERY_ANSWER = "answer";
    private static final String QUERY_STATUS = "status";
    AnsweredQueryListAdapter adapter;
    AlertDialog alertDialog;
    ArrayList<String> qList = new ArrayList<>();
    ArrayList<String> qDateList = new ArrayList<>();
    ArrayList<String> qAnsList = new ArrayList<>();
    ArrayList<String> qIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closedquery);

        Log.e(TAG, "onCreate!");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Closed Queries");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        recycleView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(mLayoutManager);
        getAcceptedQuery();

    }

    private void getAcceptedQuery()
    {
        String URL = ServerConstants.DOMAIN + ServerConstants.ANSWERED_QUERY;

        HashMap<String, String> params = new HashMap<String, String>();
        showDialog("Loading...");

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e("closedQueries", "Response : " + response);
                buildQuestionsView(response);
                dismissDialog();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.v("Error in closedQuery", "Error Response : " + error.toString());
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

                    String status = jsonObject.optString(QUERY_STATUS).toString();
                    if (status.equalsIgnoreCase("C"))
                    {
                        int question_Id = jsonObject.getInt(QUESTION_ID);
                        qIdList.add(String.valueOf(question_Id));
                        String question = jsonObject.optString(QUESTION).toString();
                        qList.add(question);
                        String date_time = jsonObject.optString(QUERY_TIME).toString();
                        qDateList.add(date_time);
                        String answer = jsonObject.optString(QUERY_ANSWER).toString();
                        qAnsList.add(answer);
                    }
                }
                Collections.reverse(qIdList);
                Collections.reverse(qList);
                Collections.reverse(qDateList);
                Collections.reverse(qAnsList);

                adapter = new AnsweredQueryListAdapter(qIdList, qList, qDateList, qAnsList, getApplicationContext());
                recycleView.setAdapter(adapter);

            } catch (JSONException e)
            {
                e.printStackTrace();
            }
            adapter.setOnItemClickListener(new AnsweredQueryListAdapter.OptionClickListener()
            {
                @Override
                public void onItemClick(int position, View v)
                {
                    Log.v("AcceptedQuestionClick", ": " + position);
                    Intent intent = new Intent(getApplicationContext(), Closed_QueryDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("question_Id", qIdList.get(position));
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
        alertDialog = new ProgressDialog(ClosedQueryActivity.this);
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
