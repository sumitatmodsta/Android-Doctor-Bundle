package com.modastadoc.doctors.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.VolleyCallback;
import com.modastadoc.doctors.activity.FollowupActivity;
import com.modastadoc.doctors.activity.HomeActivity;
import com.modastadoc.doctors.activity.SuggestionDetailActivity;
import com.modastadoc.doctors.adapter.FollowupQueryListAdapter;
import com.modastadoc.doctors.adapter.QueryAdapter;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
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

/**
 * Created by vijay.hiremath on 15/09/16.
 * Updated by Vivek on 20/09/2016.
 */
public class HomeFragment extends Fragment implements VolleyCallback
{
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

    ArrayList<String> qList = new ArrayList<>();
    ArrayList<String> qDateList = new ArrayList<>();
    ArrayList<String> qIDList = new ArrayList<>();
    ArrayList<String> replyToList = new ArrayList<>();
    ArrayList<String> fList = new ArrayList<>();
    ArrayList<String> fDateList = new ArrayList<>();
    ArrayList<String> fIDList = new ArrayList<>();
    ArrayList<String> fStatusList = new ArrayList<>();

    ArrayList<String> fParrentIdList = new ArrayList<>();
    ArrayList<String> reviwerCommentList = new ArrayList<>();
    AlertDialog alertDialog;
    int newFollowupCount;
    TextView tv_NewFollowupCount;

    TabLayout tabLayout;

    public HomeFragment()
    {

    }

    public static HomeFragment newInstance(int sectionNumber)
    {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    private void getAppointments() {
        String URL = ServerConstants.GET_APPOINTMENTS;

        HashMap<String, String> params = new HashMap<String, String>();
        //showDialog("Loading...");
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override

            public void onResponse(String response)
            {
                Log.e(TAG, "Response : " + response);

                //buildQuestionsView(response);
                Log.i("kunasi -- ", response);

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.i("kunasi -- ", "appointment error - "+error.getMessage());
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                4000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void getAllQuestion()
    {
        String URL = ServerConstants.DOMAIN + ServerConstants.UNANSWERED_QUERY;

        HashMap<String, String> params = new HashMap<String, String>();
        showDialog("Loading...");
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override

            public void onResponse(String response)
            {
                Log.e(TAG, "Response : " + response);
                buildQuestionsView(response);

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText(getContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();

            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        volleyQueue.add(postApi);
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }


    private void getAllLabtests(String fromDate, String toDate)
    {

        String URL = ServerConstants.DOMAIN + ServerConstants.VIEW_LAB_TESTS + fromDate.replace(" ", "%") + "&" + "to" + "=" + toDate.replace(" ", "%");
        showDialog("Loading...");
        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
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

                JSONArray jsonArray = jsonRootObject.optJSONArray(QUESTION_POOL_ARRAY);
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int question_Id = jsonObject.getInt(QUESTION_ID);
                    String replyTOId = jsonObject.getString(REPLY_TO);

                    Log.v("question_Id", "" + question_Id);

                    qIDList.add(String.valueOf(question_Id));
                    replyToList.add(replyTOId);

                    String question = jsonObject.optString(QUESTION).toString();
                    qList.add(question);
                    String date_time = jsonObject.optString(QUERY_TIME).toString();
                    qDateList.add(date_time);
                }

                Collections.reverse(qIDList);
                Collections.reverse(qList);
                Collections.reverse(qDateList);

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

                //adapter = new QueryAdapter(qIDList, qList, qDateList, getActivity());
                rvList.setAdapter(adapter);

                followupQueryAdapter = new FollowupQueryListAdapter(fParrentIdList, fIDList, fList, fDateList, fStatusList, reviwerCommentList, getActivity());

                rvList2.setAdapter(followupQueryAdapter);

                int diffrenceValue = 0;
                SharedPreferences preferences = getActivity().getPreferences(getContext().MODE_PRIVATE);
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
                //todo make zero-state visible
                Log.e(TAG, "Question Count : " + qList.size());
                if (qList.size() <= 0)
                {
                    tv_zero_state.setVisibility(View.VISIBLE);
                } else
                {
                    tv_zero_state.setVisibility(View.GONE);
                }
            } catch (Exception e)
            {
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
        alertDialog = new ProgressDialog(getContext());
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rvList = (ListView) rootView.findViewById(R.id.rv_query_list);
        rvList2 = (RecyclerView) rootView.findViewById(R.id.rv_query_list2);

        tv_zero_state = (TextView) rootView.findViewById(R.id.tv_zero_state);

        ll_button_link = (LinearLayout) rootView.findViewById(R.id.ll_button_link);
        bt_boarding = (Button) rootView.findViewById(R.id.bt_boarding);

        tabs = (TabLayout) rootView.findViewById(R.id.tabs);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvList2.setLayoutManager(mLayoutManager);
        tv_NewFollowupCount = (TextView) rootView.findViewById(R.id.tv_badge);
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
        }
        else
        {
            ll_button_link.setVisibility(View.GONE);
            tabs.setVisibility(View.VISIBLE);
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


        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Pool Queries"));
        tabLayout.addTab(tabLayout.newTab().setText("Followup Queries"));

        String slug_available = GCMContracts.NEW_QUERY_NOTIFY;
        try
        {
            HomeActivity home = (HomeActivity) getActivity();
            slug_available = home.getSlug();
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
        } else
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
                    //adapter = new QueryAdapter(qIDList, qList, qDateList, getActivity());
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

                    followupQueryAdapter = new FollowupQueryListAdapter(fParrentIdList, fIDList, fList, fDateList, fStatusList, reviwerCommentList, getActivity());
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
                                    Intent intent = new Intent(getActivity(), FollowupActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("question_Id", replyTOid);
                                    intent.putExtra("clickstatus", editableStatus);
                                    getActivity().startActivity(intent);
                                }
                            } else
                            {
                                editableStatus = "true";
                                Intent intent = new Intent(getActivity(), FollowupActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("question_Id", replyTOid);
                                intent.putExtra("clickstatus", editableStatus);
                                getActivity().startActivity(intent);
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

        getAppointments();

        return rootView;
    }

    private void getIntentCall(String q_Id, Class t, String status)
    {
        Intent intent = new Intent(getActivity(), t);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("question_Id", q_Id);
        intent.putExtra("clickstatus", status);
        getActivity().startActivity(intent);
    }

    @Override
    public void volleyResponse(int error_code, String response)
    {

        Log.v("lABTESTS", response);
        //createFileLabTests(response);
        LocalDataManager.getInstance().set(AppConstants.LAB_TESTS, response);
    }
}
