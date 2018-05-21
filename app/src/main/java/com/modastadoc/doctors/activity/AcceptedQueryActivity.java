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
import com.modastadoc.doctors.adapter.AcceptedQueryListAdapter;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by vivek.c on 21/09/16.
 */
public class AcceptedQueryActivity extends AppCompatActivity
{

    String TAG = AcceptedQueryActivity.class.getSimpleName();

    private static final String QUESTION_POOL_ARRAY = "acceptedquestion";
    private static final String QUESTION_ID = "question_id";
    private static final String QUESTION = "question";
    private static final String QUERY_TIME = "date";
    private static final String QUESTION_STATUS = "status";
    AcceptedQueryListAdapter adapter;
    AlertDialog alertDialog;
    ArrayList<String> qList = new ArrayList<>();
    ArrayList<String> qDateList = new ArrayList<>();
    ArrayList<String> qIDList = new ArrayList<>();
    ArrayList<String> qStatusList = new ArrayList<>();
    RecyclerView recycleView;

    TextView tv_zero_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptedquery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Accepted Queries");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        recycleView = (RecyclerView) findViewById(R.id.recycler_view);
        tv_zero_state = (TextView) findViewById(R.id.tv_zero_state);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(mLayoutManager);

        getAcceptedQuery();

    }

    private void getAcceptedQuery() {
        String URL = ServerConstants.DOMAIN + ServerConstants.ACCEPTED_QUERY;

        HashMap<String, String> params = new HashMap<String, String>();
        showDialog("Loading...");
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e( TAG , "Response : " + response);
                buildQuestionsView(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e( TAG , "getAcceptedQuery Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    private void buildQuestionsView(String response) {
        if (!response.equals("[]") && !response.equals("[ ]")) {
            try {
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

                if( qList.size() <= 0 ) {
                    tv_zero_state.setText("No results found.");
                    tv_zero_state.setVisibility(View.VISIBLE);
                } else {
                    tv_zero_state.setVisibility(View.GONE);
                }
                adapter = new AcceptedQueryListAdapter(qIDList, qList, qDateList, qStatusList, getApplicationContext());
                Log.v("adapter", "" + adapter);
                recycleView.setAdapter(adapter);
                dismissDialog();

            } catch (JSONException e) {
                Log.e( TAG , "Exc : " + e.toString() );
                tv_zero_state.setText("Network error. Please try again.");
                tv_zero_state.setVisibility(View.VISIBLE);
            }

            adapter.setOnItemClickListener(new AcceptedQueryListAdapter.OptionClickListener()
            {
                @Override
                public void onItemClick(int position, View v)
                {
                    String status = qStatusList.get(position);
                    if (status.equalsIgnoreCase("R")) {
                        Toast.makeText(getApplicationContext(), "Pending Review", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), QueryAnswerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("question_Id", qIDList.get(position));
                        startActivity(intent);
                    }

                }
            });
        } else {
            tv_zero_state.setText("Network error. Please try again.");
            tv_zero_state.setVisibility(View.VISIBLE);
        }
    }

    private void showDialog(String message) {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(AcceptedQueryActivity.this);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void dismissDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}