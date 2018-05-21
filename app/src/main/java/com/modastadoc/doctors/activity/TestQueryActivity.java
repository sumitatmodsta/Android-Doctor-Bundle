package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.VolleyCallback;
import com.modastadoc.doctors.adapter.FollowupQueryListAdapter;
import com.modastadoc.doctors.adapter.QueryAdapter;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.UtilityMethods;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.fragment.HomeFragment;
import com.modastadoc.doctors.gcm.GCMContracts;
import com.modastadoc.doctors.network.GetApi;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

public class TestQueryActivity extends AppCompatActivity implements VolleyCallback, View.OnClickListener {

    String TAG = HomeFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String MY_PREFS_NAME = "Followupnumber";
    private static final String QUESTION_POOL_ARRAY = "questions";
    private static final String QUESTION_FOLLOWUP_ARRAY = "listfollowup";
    private static final String QUESTION_ID = "question_id";
    private static final String REPLY_TO = "reply_to";
    private static final String QUESTION = "question";
    private static final String QUERY_TIME = "date";
    private static final String QUESTION_STATUS = "status";
    private static final String PARENT_ID = "reply_to";
    private static final String REVIEWER_COMMENT = "reviewer_comment";

    TabLayout tabs;
    LinearLayout ll_button_link;
    ListView rvList;
    RecyclerView rvList2;
    QueryAdapter adapter;
    FollowupQueryListAdapter followupQueryAdapter;
    Button bt_boarding;
    TextView tv_zero_state;
    Button bt_refresh;

    private TextView acceptedQueries, suggestionsByInHouse, closedQueries;

    // WTF!! Who does this???
    ArrayList<String> qList               = new ArrayList<>();
    ArrayList<String> patientAudioFileIds = new ArrayList<>();
    ArrayList<String> queryLanguageList   = new ArrayList<>();
    ArrayList<String> qDateList           = new ArrayList<>();
    ArrayList<String> qIDList             = new ArrayList<>();
    ArrayList<String> replyToList         = new ArrayList<>();
    ArrayList<String> fList               = new ArrayList<>();
    ArrayList<String> fDateList           = new ArrayList<>();
    ArrayList<String> fIDList             = new ArrayList<>();
    ArrayList<String> fStatusList         = new ArrayList<>();

    ArrayList<String> fParrentIdList     = new ArrayList<>();
    ArrayList<String> reviwerCommentList = new ArrayList<>();

    AlertDialog alertDialog;
    int newFollowupCount;
    TextView tv_NewFollowupCount;

    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_query);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Text Queries");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        rvList = (ListView) findViewById(R.id.rv_query_list);
        rvList2 = (RecyclerView) findViewById(R.id.rv_query_list2);

        tv_zero_state = (TextView) findViewById(R.id.tv_zero_state);

        ll_button_link = (LinearLayout) findViewById(R.id.ll_button_link);
        bt_boarding = (Button) findViewById(R.id.bt_boarding);

        bt_refresh = (Button) findViewById(R.id.bt_refresh);

        acceptedQueries = (TextView) findViewById(R.id.accepted_queries);
        suggestionsByInHouse = (TextView) findViewById(R.id.suggestions_by_house);
        closedQueries = (TextView) findViewById(R.id.closed_queries);

        tabs = (TabLayout) findViewById(R.id.tabs);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvList2.setLayoutManager(mLayoutManager);
        tv_NewFollowupCount = (TextView) findViewById(R.id.tv_badge);
        tv_NewFollowupCount.setVisibility(View.GONE);

        qList.clear();
        qDateList.clear();
        fList.clear();
        fDateList.clear();
        qIDList.clear();
        fIDList.clear();
        fStatusList.clear();
        fParrentIdList.clear();
        reviwerCommentList.clear();

        /****************************************
         * Permission change required.
         ***************************************/
        String user_type = LocalDataManager.getInstance().get(PreferenceConstants.USER_TYPE);
        Log.e(TAG, "USER_TYPE ==> " + user_type);
        if (user_type.equalsIgnoreCase(PreferenceConstants.ONLY_IMAKA))
        {
            ll_button_link.setVisibility(View.VISIBLE);
            tabs.setVisibility(View.GONE);
            bt_refresh.setVisibility(View.GONE);
        }
        else
        {
            ll_button_link.setVisibility(View.GONE);
            tabs.setVisibility(View.VISIBLE);
            bt_refresh.setVisibility(View.VISIBLE);
            getAllQuestion();

            Calendar c = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String todaysDate = df.format(c.getTime());

            getAllLabtests("2016-08-21 00:00:00", todaysDate);
        }


        bt_boarding.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.modasta.com/register-to-join-the-modasta-as-physiciandoctor/"));
                startActivity(browserIntent);
            }
        });
        /***************************************/


        bt_refresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getAllQuestion();
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Pool Queries"));
        tabLayout.addTab(tabLayout.newTab().setText("Followup Queries"));

        String slug_available = GCMContracts.NEW_QUERY_NOTIFY;
        try
        {
            slug_available = getIntent().getStringExtra("slug");
        } catch (Exception ex)
        {
            Log.e(TAG, "Slug ex :" + ex.toString());
        }


        Log.e(TAG, "Slug avail :" + slug_available);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        tabLayout.setSelectedTabIndicatorHeight(0);

        if (slug_available.equalsIgnoreCase(GCMContracts.NEW_QUERY_NOTIFY))
        {
            tabLayout.getTabAt(0).select();
        }
        else
        {
            tabLayout.getTabAt(1).select();
        }


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                if (tab.getPosition() == 0)
                {
                    rvList.setVisibility(View.VISIBLE);
                    rvList2.setVisibility(View.GONE);
                    adapter = new QueryAdapter(qIDList, qList, qDateList, TestQueryActivity.this,patientAudioFileIds, queryLanguageList);
                    rvList.setAdapter(adapter);
                    if (qList.size() <= 0)
                    {
                        tv_zero_state.setVisibility(View.VISIBLE);
                    } else
                    {


                        tv_zero_state.setVisibility(View.GONE);
                    }
                } else
                {
                    rvList.setVisibility(View.GONE);
                    rvList2.setVisibility(View.VISIBLE);

                    followupQueryAdapter = new FollowupQueryListAdapter(fParrentIdList, fIDList, fList, fDateList, fStatusList, reviwerCommentList, TestQueryActivity.this);
                    rvList2.setAdapter(followupQueryAdapter);

                    if (fIDList.size() <= 0)
                    {
                        tv_zero_state.setVisibility(View.VISIBLE);
                    } else
                    {
                        tv_zero_state.setVisibility(View.GONE);
                    }

                    followupQueryAdapter.setOnItemClickListener(new FollowupQueryListAdapter.OptionClickListener()
                    {
                        @Override
                        public void onItemClick(int position, View v)
                        {
                            String q_id = fIDList.get(position);
                            String fStatus = fStatusList.get(position);
                            String replyTOid = replyToList.get(position);

                            String editableStatus;

                            if (fStatus.equalsIgnoreCase("R"))
                            {
                                if (reviwerCommentList.get(position).trim().length() > 0)
                                {
                                    editableStatus = "true";
                                    getIntentCall(q_id, SuggestionDetailActivity.class, editableStatus);
                                } else
                                {
                                    editableStatus = "false";
                                    Intent intent = new Intent(TestQueryActivity.this, FollowupActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("question_Id", replyTOid);
                                    intent.putExtra("clickstatus", editableStatus);
                                    startActivity(intent);
                                }
                            } else
                            {
                                editableStatus = "true";
                                Intent intent = new Intent(TestQueryActivity.this, FollowupActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("question_Id", replyTOid);
                                intent.putExtra("clickstatus", editableStatus);
                                startActivity(intent);
                            }

                        }
                    });
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
            }
        });

        /*
        *  OnClick Listeners
        * */

        acceptedQueries.setOnClickListener(this);
        suggestionsByInHouse.setOnClickListener(this);
        closedQueries.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.accepted_queries:
                startActivity(new Intent(this, AcceptedQueryActivity.class));
                break;
            case R.id.suggestions_by_house:
                startActivity(new Intent(this, SuggestionsFromInHouseActivity.class));
                break;
            case R.id.closed_queries:
                startActivity(new Intent(this, ClosedQueryActivity.class));
                break;
        }
    }

    private void getAllQuestion()
    {

        qList               = new ArrayList<>();
        patientAudioFileIds = new ArrayList<>();
        queryLanguageList   = new ArrayList<>();
        qDateList           = new ArrayList<>();
        qIDList             = new ArrayList<>();
        replyToList         = new ArrayList<>();
        fList               = new ArrayList<>();
        fDateList           = new ArrayList<>();
        fIDList             = new ArrayList<>();
        fStatusList         = new ArrayList<>();

        String URL = ServerConstants.DOMAIN + ServerConstants.UNANSWERED_QUERY;
        Log.e( TAG , "getAllQuestion " + URL );
        HashMap<String, String> params = new HashMap<String, String>();
        showDialog("Loading...");
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "getAllQuestion Response : " + response);
                response = UtilityMethods.removeEscapedCharacters( response );
                buildQuestionsView(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                Log.e(TAG, "getAllQuestion Error Response : " + error.toString());
                if( error.networkResponse.statusCode == 403 )
                {
                    new AlertDialog.Builder( TestQueryActivity.this)
                            .setTitle("Session Expired")
                            .setMessage("User session has expired. Please Login again")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    gotoLoginScreen();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else
                {
                    Toast.makeText(TestQueryActivity.this, "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        volleyQueue.add(postApi);

    }

    private void gotoLoginScreen()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void getAllLabtests(String fromDate, String toDate)
    {

        String URL = ServerConstants.DOMAIN + ServerConstants.VIEW_LAB_TESTS + fromDate.replace(" ", "%") + "&" + "to" + "=" + toDate.replace(" ", "%");
        showDialog("Loading...");
        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        GetApi api = new GetApi(this, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("errorVolley", String.valueOf(error));
            }
        }, URL, 1);
        volleyQueue.add(api);
    }

    private void buildQuestionsView(String response)
    {

        if (!response.equals("[]") && !response.equals("[]"))
        {
            try
            {
                JSONObject jsonRootObject = new JSONObject(response);

                /*****************************
                 *
                 * New Question arrival.
                 *
                 *****************************/
                JSONArray jsonArray = jsonRootObject.optJSONArray(QUESTION_POOL_ARRAY);
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int question_Id = jsonObject.getInt(QUESTION_ID);
                    String replyTOId = jsonObject.getString(REPLY_TO);

                    String question  = jsonObject.optString(QUESTION).toString();
                    String date_time = jsonObject.optString(QUERY_TIME).toString();
                    String patient_audio_file_id = jsonObject.optString("patient_audio_file_id").toString();

                    String queryLanguage = jsonObject.optString("lang").toString();
                    String doctorPreferredLanguage = LocalDataManager.getInstance().get( PreferenceConstants.USER_PREFERRED_LANGUAGE);
                    Log.e( TAG , "queryLanguage           : " + queryLanguage);
                    Log.e( TAG , "doctorPreferredLanguage : " + doctorPreferredLanguage) ;

                    if( !UtilityMethods.checkIfQueryIsLanguagePreferred( queryLanguage , doctorPreferredLanguage)
                            && queryLanguage.length() > 0 )
                    {
                        Log.e( TAG , "skipped!" );
                        continue;
                    }

                    queryLanguageList.add( queryLanguage );

                    qIDList.add(String.valueOf(question_Id));
                    replyToList.add(replyTOId);

                    qList.add(question);

                    qDateList.add(date_time);

                    patientAudioFileIds.add( patient_audio_file_id );
                }

                Collections.reverse(qIDList);
                Collections.reverse(qList);
                Collections.reverse(qDateList);
                Collections.reverse(patientAudioFileIds);
                Collections.reverse(queryLanguageList);
                /***********************************/


                /***********************************
                 *
                 * Followup Queries.
                 *
                 ***********************************/
                JSONArray jsonFollowupArray = jsonRootObject.optJSONArray(QUESTION_FOLLOWUP_ARRAY);
                for (int i = 0; i < jsonFollowupArray.length(); i++)
                {
                    JSONObject jsonObject = jsonFollowupArray.getJSONObject(i);
                    int question_ParentId = jsonObject.getInt(PARENT_ID);
                    fParrentIdList.add(String.valueOf(question_ParentId));

                    int question_Id = jsonObject.getInt(QUESTION_ID);
                    String replyTOId = jsonObject.getString(REPLY_TO);

                    Log.v("question_Id", "" + question_Id);
                    fIDList.add(String.valueOf(question_Id));
                    String question = jsonObject.optString(QUESTION).toString();
                    fList.add(question);
                    String date_time = jsonObject.optString(QUERY_TIME).toString();
                    fDateList.add(date_time);
                    String status = jsonObject.optString(QUESTION_STATUS).toString();
                    fStatusList.add(status);
                    String reviewer_Comment = jsonObject.optString(REVIEWER_COMMENT).toString();
                    reviwerCommentList.add(reviewer_Comment);

                    replyToList.add(replyTOId);
                }

                Collections.reverse(fIDList);
                Collections.reverse(fList);
                Collections.reverse(fDateList);
                Collections.reverse(fStatusList);
                Collections.reverse(fParrentIdList);
                Collections.reverse(reviwerCommentList);
                Collections.reverse(replyToList);
                /******************************************/


                adapter = new QueryAdapter(qIDList, qList, qDateList, TestQueryActivity.this, patientAudioFileIds, queryLanguageList);
                rvList.setAdapter(adapter);

                followupQueryAdapter = new FollowupQueryListAdapter(fParrentIdList, fIDList, fList, fDateList, fStatusList, reviwerCommentList, TestQueryActivity.this);

                rvList2.setAdapter(followupQueryAdapter);

                int diffrenceValue = 0;
                SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                int previousCount = preferences.getInt("count", 0);

                newFollowupCount = fIDList.size();
                SharedPreferences.Editor editor = preferences.edit();  // Put the values from the UI
                editor.putInt("count", newFollowupCount); // value to store
                // Commit to storage
                editor.commit();


                if (newFollowupCount > previousCount)
                {
                    diffrenceValue = newFollowupCount - previousCount;
                    tv_NewFollowupCount.setVisibility(View.VISIBLE);
                    tv_NewFollowupCount.setText(String.valueOf(diffrenceValue));
                }

                dismissDialog();

                if (qList.size() <= 0)
                {
                    tv_zero_state.setVisibility(View.VISIBLE);
                }
                else
                {
                    tv_zero_state.setVisibility(View.GONE);
                }
            }
            catch (Exception e)
            {
                dismissDialog();
                Log.e(TAG, "Ex : " + e.toString());
            }

        } else
        {
            tv_zero_state.setVisibility(View.VISIBLE);
        }
    }

    private void showDialog(String message)
    {

        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(this);
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

    private void getIntentCall(String q_Id, Class t, String status)
    {
        Intent intent = new Intent(this, t);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("question_Id", q_Id);
        intent.putExtra("clickstatus", status);
        startActivity(intent);
    }

    @Override
    public void volleyResponse(int error_code, String response)
    {
        Log.e("lABTESTS", response);
        //createFileLabTests(response);
        LocalDataManager.getInstance().set(AppConstants.LAB_TESTS, response);
    }
}
