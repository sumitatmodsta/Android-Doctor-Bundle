package com.modastadoc.doctors.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.AppointmentActivity;
import com.modastadoc.doctors.activity.HomeActivity;
import com.modastadoc.doctors.activity.TestQueryActivity;
import com.modastadoc.doctors.adapter.UpcomingApmtAdapter;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.constants.UserConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.DateUtil;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.gcm.GCMContracts;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kunasi on 16/08/17.
 */
public class NewHomeFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = NewHomeFragment.class.getSimpleName();

    private RecyclerView apmtRecyclerView, queriesRecyclerView;
    private UpcomingApmtAdapter apmtAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private String slug;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_new_home, container, false);

        slug = GCMContracts.NEW_QUERY_NOTIFY;
        try
        {
            HomeActivity home = (HomeActivity) getActivity();
            slug = home.getSlug();
        } catch (Exception ex)
        {
            Log.e(TAG, "Slug ex :" + ex.toString());
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        apmtRecyclerView = (RecyclerView) view.findViewById(R.id.apmt_recycler_view);
        //queriesRecyclerView = (RecyclerView) view.findViewById(R.id.queries_recycler_view);

        /*
        * OnClickListener
        * */
        view.findViewById(R.id.video_consultation).setOnClickListener(this);
        view.findViewById(R.id.text_queries).setOnClickListener(this);

        apmtAdapter = new UpcomingApmtAdapter(getActivity());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        apmtRecyclerView.setLayoutManager(mLayoutManager);
        apmtRecyclerView.setItemAnimator(new DefaultItemAnimator());
        apmtRecyclerView.setAdapter(apmtAdapter);

        Log.i(TAG, LocalDataManager.getInstance().get(UserConstants.GCMTOKEN));

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_consultation:
                startActivity(new Intent(getActivity(), AppointmentActivity.class));
                break;
            case R.id.text_queries:
                Intent in = new Intent(getActivity(), TestQueryActivity.class);
                in.putExtra("slug", slug);
                startActivity(in);
                break;
            default:
                break;
        }
    }

    private void refresh() {
        getTodayAppointments();
    }

    @Override
    public void onResume() {
        super.onResume();
        getTodayAppointments();
    }

    private void getTodayAppointments() {
        HashMap<String, String> params = new HashMap<>();
        params.put("bookstatus", "CD");
        params.put("aptfilter", "selectdate");
        params.put("date", DateUtil.todayDate());
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override

            public void onResponse(String response)
            {
                AppCoreUtil.log(TAG, "getTodayAppointments Response : ", response);
                mSwipeRefreshLayout.setRefreshing(false);
                buildTodayAppointment(response);

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, params, ServerConstants.GET_APPOINTMENTS);

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        volleyQueue.add(postApi);
    }

    private void buildTodayAppointment(String response) {
        try {
            if(response != null && response.trim().length() > 0) {
                JSONObject obj = new JSONObject(response);
                JSONArray arr = obj.optJSONArray("data");
                if(arr != null) {
                    int size = arr.length();
                    if (size > 2) {
                            List<Appointment> list = new ArrayList<>();
                            JSONObject o;
                            for (int i = size-1; i >= 0; i--) {
                                o = arr.optJSONObject(i);
                                if(o != null) {
                                    list.add(new Appointment(o.optString("order_id"), o.optString("name"), o.optString("date"),
                                            o.optString("time"), o.optString("profileimg"), o.optString("status"), o.optString("delayed_by")));
                                }
                            }
                            apmtAdapter.refresh(list);
                    }else {
                        getThreeAppointments();
                    }
                }else {
                    getThreeAppointments();
                }
            }else {
                getThreeAppointments();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getThreeAppointments() {
        HashMap<String, String> params = new HashMap<>();
        params.put("bookstatus", "CD");
        params.put("count", "3");
        PostApi postApi = new PostApi(new Response.Listener<String>() {
            @Override

            public void onResponse(String response) {
                AppCoreUtil.log(TAG, "getThreeAppointments response : ", response);
                buildThreeUpcomingList(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                AppCoreUtil.log(TAG, "getThreeAppointments error", error.getMessage());
            }
        }, params, ServerConstants.GET_APPOINTMENTS);

        RequestQueue volleyQueue = Volley.newRequestQueue(getActivity());
        volleyQueue.add(postApi);
    }

    private void buildThreeUpcomingList(String response) {
        try {
            if(response != null && response.trim().length() > 0) {
                JSONObject obj = new JSONObject(response);
                JSONArray arr = obj.optJSONArray("data");
                if(arr != null) {
                    int size = arr.length();
                    if (size > 0) {
                        List<Appointment> list = new ArrayList<>();
                        JSONObject o;
                        for (int i = 0; i < size; i++) {
                            o = arr.getJSONObject(i);
                            list.add(new Appointment(o.optString("order_id"), o.optString("name"), o.optString("date"),
                                    o.optString("time"), o.optString("profileimg"), o.optString("status"), o.optString("delayed_by")));
                        }
                        apmtAdapter.refresh(list);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
