package com.modastadoc.doctors.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.FileAdapter;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.PatientFile;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewSummaryActivity extends AppCompatActivity {

    private static final String TAG = "ViewSummaryActivity";

    private TextView doctorName, date, diagnosis, medicalAdvice, labTests, other;
    private RecyclerView recyclerView;
    private FileAdapter mAdapter;

    private CustomProgressDialog mCustomDialog;
    private RelativeLayout mReportView;
    private WebView mReportWebView;
    private Button mCloseReport;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    //private Appointment appointment;
    private String mOrderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_summary);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mOrderID = b.getString("order_id");
            if(mOrderID == null) {
                AppCoreUtil.showToast(this, R.string.error_something_wrong_message);
                finish();
            }
        }else {
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("View Summary");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        doctorName = (TextView) findViewById(R.id.doctor_name);
        date = (TextView) findViewById(R.id.date);
        diagnosis = (TextView) findViewById(R.id.diagnosis);
        medicalAdvice = (TextView) findViewById(R.id.medical_advice);
        labTests = (TextView) findViewById(R.id.lab_tests);
        other = (TextView) findViewById(R.id.other);

        // Report View
        mReportView = (RelativeLayout) findViewById(R.id.patient_report_view);
        mReportWebView = (WebView) mReportView.findViewById(R.id.patient_report_webview);
        mCloseReport = (Button) mReportView.findViewById(R.id.report_close);
        mCloseReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReportView.setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            }
        });
        mReportView.setVisibility(View.GONE);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new FileAdapter(this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        getSummary();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mAdapter.permissionRequestCallback();
        } else {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    public void ShowPatientDetails(String url, HashMap<String, String> params) {
        showCustomDialog();
        mReportView.setVisibility(View.VISIBLE);
        mReportWebView.getSettings().setJavaScriptEnabled(true);
        mReportWebView.getSettings().setLoadWithOverviewMode(true);
        mReportWebView.getSettings().setUseWideViewPort(true);
        mReportWebView.loadUrl(url, params);

        mReportWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                Toast.makeText(ViewSummaryActivity.this, "Image loaded", Toast.LENGTH_SHORT).show();
                dismissCustomDialog();
                mSwipeRefreshLayout.setVisibility(View.GONE);
            }
        });
    }

    private void showCustomDialog()
    {
        mCustomDialog = new CustomProgressDialog(this);
        mCustomDialog.setCancelable(true);
        mCustomDialog.show();
    }

    private void dismissCustomDialog()
    {
        if (mCustomDialog != null)
        {
            mCustomDialog.dismiss();
        }
    }

    private void refresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        getSummary();
    }

    private void getSummary() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_ORDER_SUMMARY +mOrderID;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+mOrderID);
            params.put("orderId", mOrderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getSummary --> onResponse --> " + response);
                    buildSummary(response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getSummary --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(ViewSummaryActivity.this, R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void buildSummary(String response) {
        try {
            JSONObject o = new JSONObject(response);
            if(o.length() > 0) {
                String name = o.optString("docname");
                String date = o.optString("date");
                String diag = o.optString("diagnosis");
                String med = o.optString("medical_advice");
                String lab = o.optString("lab_test_detail");
                String oth = o.optString("other_lab_test");
                ArrayList<PatientFile> list = getList(o.optJSONArray("doctoruplodfiles"));
                updateViews(name, date, diag, med, lab, oth, list);
            }else {
                AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<PatientFile> getList(JSONArray array) {
        ArrayList<PatientFile> list = new ArrayList<>();
        try {
            if(array != null) {
                int size = array.length();
                if(size > 0) {
                    JSONObject o;
                    for(int i=0; i<size; i++) {
                        o = array.optJSONObject(i);
                        if(o != null && o.length() > 0) {
                            String fileName = o.optString("filename") + "." + o.optString("extension");
                            list.add(new PatientFile(o.optString("idlog"), fileName, ""));
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }

    private void updateViews(String name, String dt, String diag, String med, String lab, String oth,
                             ArrayList<PatientFile> list) {
        doctorName.setText(name);
        date.setText(dt);
        diagnosis.setText(diag);
        medicalAdvice.setText(med);
        labTests.setText(lab);
        other.setText(oth);
        mAdapter.refresh(list);
    }

}
