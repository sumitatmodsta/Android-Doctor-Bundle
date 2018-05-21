package com.modastadoc.doctors.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.FileAdapter;
import com.modastadoc.doctors.adapter.PreviousApmtAdapter;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.common.utils.PermissionUtil;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.Booking;
import com.modastadoc.doctors.model.Patient;
import com.modastadoc.doctors.model.PatientFile;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CircularImageView;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatientInfoActivity extends AppCompatActivity implements View.OnClickListener,
        PermissionUtil.Callbacks {

    private static final String TAG = "PatientInfoActivity";

    private static final int REQUEST_CODE_PERMISSION_VIDEO = 1234;

    private Appointment appointment;

    private TextView pname, orderID, date, time, name, age, gender, height, weight,
            diagnosis, medication, allergy, delayedBy;

    private TextView call, cancel, delay, viewSummary;
    private ImageView status;
    private CircularImageView pic;
    private LinearLayout bottom;

    private RecyclerView recyclerView, prevRecyclerView;
    private FileAdapter mAdapter;
    private PreviousApmtAdapter mPreviousApmtAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CustomProgressDialog mCustomDialog;
    private RelativeLayout mReportView;
    private WebView mReportWebView;
    private Button mCloseReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            boolean notification = b.getBoolean("from_notification", false);
            if(notification) {
                String orderID = b.getString("order_id");
                appointment = new Appointment(orderID, "", "", "", "", "", "");
            }else {
                appointment = b.getParcelable("appointment");
            }
            if(appointment == null) {
                AppCoreUtil.showToast(this, R.string.error_something_wrong_message);
                finish();
            }
        }else {
            finish();
        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Patient Details");
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

        pic = (CircularImageView) findViewById(R.id.pic);
        status = (ImageView) findViewById(R.id.status);
        pname = (TextView) findViewById(R.id.pname);
        delayedBy = (TextView) findViewById(R.id.delayed_by);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        orderID = (TextView) findViewById(R.id.order_id);

        name = (TextView) findViewById(R.id.name);
        age = (TextView) findViewById(R.id.age);
        gender = (TextView) findViewById(R.id.gender);
        height = (TextView) findViewById(R.id.height);
        weight = (TextView) findViewById(R.id.weight);
        diagnosis = (TextView) findViewById(R.id.diagnosis);
        medication = (TextView) findViewById(R.id.medication);
        allergy = (TextView) findViewById(R.id.allergy);

        call = (TextView) findViewById(R.id.call);
        cancel = (TextView) findViewById(R.id.cancel);
        delay = (TextView) findViewById(R.id.delay);
        viewSummary = (TextView) findViewById(R.id.view_summary);

        bottom = (LinearLayout) findViewById(R.id.bottom);

        mReportView = (RelativeLayout) findViewById(R.id.patient_report_view);
        mReportWebView = (WebView) mReportView.findViewById(R.id.patient_report_webview);
        mCloseReport = (Button) mReportView.findViewById(R.id.report_close);
        mCloseReport.setOnClickListener(this);
        mReportView.setVisibility(View.GONE);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        prevRecyclerView = (RecyclerView) findViewById(R.id.pre_cons_recycler_view);

        mAdapter = new FileAdapter(this);
        mPreviousApmtAdapter = new PreviousApmtAdapter(this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getApplicationContext());
        prevRecyclerView.setLayoutManager(mLayoutManager2);
        prevRecyclerView.setItemAnimator(new DefaultItemAnimator());
        prevRecyclerView.setAdapter(mPreviousApmtAdapter);

        /*
        * OnClick Listeners..
        * */

        call.setOnClickListener(this);
        cancel.setOnClickListener(this);
        delay.setOnClickListener(this);
        viewSummary.setOnClickListener(this);

    }

    private void refresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        getPatientInfo();
    }

    private void fillValues() {
        pname.setText(appointment.name);
        date.setText(appointment.date);
        time.setText(appointment.time);
        orderID.setText(appointment.orderID);

        Glide.with(this)
                .load(appointment.picUrl)
                .placeholder(R.drawable.doctor)
                .error(R.drawable.doctor)
                .crossFade()
                .into(pic);

        setStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPatientInfo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mAdapter != null && permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mAdapter.permissionRequestCallback();
        } else if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE){
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call:
                callAppointment();
                break;
            case R.id.cancel:
                showCancelPopup();
                break;
            case R.id.delay:
                showDelayPopup();
                break;
            case R.id.view_summary:
                Intent in = new Intent(this, ViewSummaryActivity.class);
                in.putExtra("order_id", appointment.orderID);
                startActivity(in);
                break;
            case R.id.report_close:
                mReportView.setVisibility(View.GONE);
                break;
        }
    }

    private void buildPatientInfo(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if(obj.length() > 0) {
                JSONObject o = obj.optJSONObject("booking");
                if(o != null && o.length() > 0) {
                    fillPatientDetails(o.optJSONObject("patientdetails"));
                    fillBookingDetails(o.optJSONArray("bookingdetails"));
                    List<PatientFile> list = getFiles(o.optJSONArray("files"));
                    buildUploadedFiles(list, o.optJSONArray("labfiles"));
                    //fillUploadedFiles(o.optJSONArray("files"));
                    buildPreviousConsultation(o.optJSONArray("prevapmt"));
                    fillValues();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillPatientDetails(JSONObject o) {
        if(o != null && o.length() > 0) {
            String sName = o.optString("Name");
            String sAge = o.optString("Age");
            String sGender = o.optString("Gender");
            String sWeight = o.optString("Weight");
            String sHeight = o.optString("Height");
            String sDiagnosis = o.optString("Previously diagnosed conditions");
            String sMedication = o.optString("Medication");
            String sAllergy = o.optString("Allergies");
            String sUrl = o.optString("profileimg");

            appointment.name = sName;
            appointment.picUrl = sUrl;

            name.setText(appointment.name);
            age.setText(sAge);
            gender.setText(sGender);
            weight.setText(sWeight+" Kg");
            height.setText(sHeight +" cm");
            if (sDiagnosis.length() > 0) {
                diagnosis.setText(sDiagnosis);
            } else {
                diagnosis.setText("No");
            }
            if (sMedication.length() > 0) {
                medication.setText(sMedication);
            } else {
                medication.setText("No");
            }
            if (sAllergy.length() > 0) {
                allergy.setText(sAllergy);
            } else {
                allergy.setText("No");
            }
        }
    }

    private void fillBookingDetails(JSONArray array) {
        if(array != null && array.length() > 0) {
            JSONObject o = array.optJSONObject(0);
            if(o != null && o.length() > 0) {
                String status = o.optString("booking_status");
                String date = o.optString("apmt_date");
                String time = o.optString("slot");

                appointment.status = status;
                appointment.delayedBy = o.optString("delayed_by");
                appointment.date = date;
                appointment.time = time;
            }
        }
    }

    private void buildUploadedFiles(List<PatientFile> list, JSONArray a) {
        if(a != null) {
            int size = a.length();
            if(size > 0) {
                JSONObject o;
                for(int i=0; i< size; i++) {
                    try {
                        o = a.optJSONObject(i);
                        if(o != null && o.length() > 0) {
                            list.add(new PatientFile(o.optString("upload_id"), o.optString("test_name"),
                                    AppConstants.FILE_TYPE_LAB));
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        mAdapter.refresh(list);
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
                Toast.makeText(PatientInfoActivity.this, "Image loaded", Toast.LENGTH_SHORT).show();
                dismissCustomDialog();
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

    private void buildPreviousConsultation(JSONArray array) {
        try {
            if(array != null) {
                int size = array.length();
                if(size > 0) {
                    List<Appointment> list = new ArrayList<>();
                    JSONObject o;
                    for(int i=0; i<size; i++) {
                        o = array.optJSONObject(i);
                        if(o != null && o.length() > 0) {
                            list.add(new Appointment(o.optString("id"), o.optString("docname"), o.optString("date"),
                                    o.optString("time"), "", "", ""));
                        }
                    }
                    mPreviousApmtAdapter.refresh(list);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStatus() {
        switch (appointment.status) {
            case AppConstants.CONFIRMED:
                bottom.setVisibility(View.VISIBLE);
                delayedBy.setVisibility(View.GONE);
                call.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                delay.setVisibility(View.VISIBLE);
                viewSummary.setVisibility(View.GONE);
                status.setBackgroundResource(R.drawable.ic_confirmed);
                break;
            case AppConstants.DELAYED:
                bottom.setVisibility(View.VISIBLE);
                delayedBy.setVisibility(View.VISIBLE);
                call.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                delay.setVisibility(View.VISIBLE);
                viewSummary.setVisibility(View.GONE);
                delayedBy.setText("Delayed By "+appointment.delayedBy+" Minutes");
                status.setBackgroundResource(R.drawable.ic_delayed);
                break;
            case AppConstants.COMPLETED:
                bottom.setVisibility(View.VISIBLE);
                call.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                delay.setVisibility(View.GONE);
                viewSummary.setVisibility(View.VISIBLE);
                delayedBy.setVisibility(View.GONE);
                status.setBackgroundResource(R.drawable.ic_completed);
                break;
            default:
                bottom.setVisibility(View.GONE);
                delayedBy.setVisibility(View.GONE);
                status.setBackgroundResource(R.drawable.ic_cancelled);
                break;
        }
    }

    private void getPatientInfo() {
        if(AppDialogUtil.canShowPopup()) {
            if (AppCoreUtil.isNetworkAvailable()) {
                AppDialogUtil.showCustomDialog(this);
                String URL = ServerConstants.GET_PATIENT_DETAILS + appointment.orderID;

                HashMap<String, String> params = new HashMap<>();
                Log.i(TAG, "Order ID -- " + appointment.orderID);
                params.put("orderId", appointment.orderID);
                PostApi postApi = new PostApi(new Response.Listener<String>() {
                    @Override

                    public void onResponse(String response) {
                        AppDialogUtil.dismissCustomDialog();
                        Log.i(TAG, "getPatientInfo --> onResponse --> " + response);
                        buildPatientInfo(response);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.i(TAG, "getPatientInfo --> onErrorResponse --> " + error.getMessage());
                        AppDialogUtil.dismissCustomDialog();
                        AppCoreUtil.showToast(PatientInfoActivity.this, R.string.error_something_wrong_message);
                    }
                }, params, URL);

                RequestQueue volleyQueue = Volley.newRequestQueue(this);
                volleyQueue.add(postApi);
            } else {
                AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
            }
        }
    }

    private void callAppointment() {
        requestPermission();
    }

    private void requestPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(PermissionUtil.hasPermission(perms)) {
            getTimeSlotStatus();
        }else {
            PermissionUtil.requestPermission(this, REQUEST_CODE_PERMISSION_VIDEO,
                    PermissionUtil.getNecessaryPermissions(perms));
        }
    }

    private void showCancelPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cancel);

        TextView orderID = (TextView) dialog.findViewById(R.id.order_id);
        TextView date = (TextView) dialog.findViewById(R.id.date);
        TextView time = (TextView) dialog.findViewById(R.id.time);

        orderID.setText(appointment.orderID);
        date.setText(appointment.date);
        time.setText(appointment.time);

        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                cancelAppointment(appointment);
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {dialog.dismiss();}
        });

        dialog.show();
    }

    private void showDelayPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delay);

        TextView orderID = (TextView) dialog.findViewById(R.id.order_id);
        final TextView date = (TextView) dialog.findViewById(R.id.date);
        TextView time = (TextView) dialog.findViewById(R.id.time);

        final RadioButton radio1 = (RadioButton) dialog.findViewById(R.id.radio1);
        final RadioButton radio2 = (RadioButton) dialog.findViewById(R.id.radio2);
        //final RadioButton radio3 = (RadioButton) dialog.findViewById(R.id.radio3);

        orderID.setText(appointment.orderID);
        date.setText(appointment.date);
        time.setText(appointment.time);

        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                int delay;
                if(radio1.isChecked())
                    delay = 15;
                else if(radio2.isChecked())
                    delay = 30;
                else
                    delay = 45;
                delayAppointment(appointment, delay);
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {dialog.dismiss();}
        });

        dialog.show();
    }

    private void cancelAppointment(Appointment appointment) {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.CANCEL_APPOINTMENT;

            HashMap<String, String> params = new HashMap<>();
            params.put("orderId", appointment.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "cancelAppointment --> onResponse --> " + response);
                    getPatientInfo();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "cancelAppointment --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(PatientInfoActivity.this, R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void delayAppointment(Appointment appointment, int delay) {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.DELAY_APPOINTMENT;

            HashMap<String, String> params = new HashMap<>();
            params.put("orderId", appointment.orderID);
            params.put("sdate", appointment.date);
            //params.put("sdate", DateUtil.get(appointment.date, "dd-MM-yyyy", "yyyy-MM-dd"));
            params.put("delay", delay + "");
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    Log.i(TAG, "delayAppointment --> onResponse --> " + response);
                    AppDialogUtil.dismissCustomDialog();
                    if(response.equalsIgnoreCase("1")) {
                        getPatientInfo();
                    }else {
                        AppCoreUtil.showToast(PatientInfoActivity.this, R.string.error_something_wrong_message);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, "delayAppointment --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(PatientInfoActivity.this, R.string.error_something_wrong_message);
                    error.printStackTrace();
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void getTimeSlotStatus() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_TIME_SLOT_STATUS + appointment.orderID;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+appointment.orderID);
            params.put("order_id", appointment.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getTimeSlotStatus --> onResponse --> " + response);
                    buildTimeSlotStatus(response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getTimeSlotStatus --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppDialogUtil.showAlert(PatientInfoActivity.this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void buildTimeSlotStatus(String response) {
        try {
            JSONObject o = new JSONObject(response);
            if(o.has("slotActive")) {
                long currentTimestamp = o.optLong("currentTimestamp");
                long activeTimestamp = o.optLong("activeTimestamp");
                if(currentTimestamp > 0 && activeTimestamp > 0) {
                    boolean status = o.optBoolean("slotActive");
                    if (status) {
                        getBookingInfo();
                    } else {
                        AppDialogUtil.showAlert(this, "Please Note!", "You can start the consultation only 5 minutes before the scheduled appointment");
                    }
                }else {
                    AppCoreUtil.showToast(this, R.string.error_something_wrong_message);
                    getPatientInfo();
                }
            }else {
                AppDialogUtil.showAlert(PatientInfoActivity.this, R.string.error_title,
                        R.string.error_something_wrong_message);
            }
        }catch (Exception e){
            e.printStackTrace();
            AppDialogUtil.showAlert(PatientInfoActivity.this, R.string.error_title,
                    R.string.error_something_wrong_message);
        }
    }

    private void getBookingInfo() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_PATIENT_DETAILS + appointment.orderID;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+appointment.orderID);
            params.put("order_id", appointment.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getSessionInfo --> onResponse --> " + response);
                    buildBookingInfo(response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getSessionInfo --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppDialogUtil.showAlert(PatientInfoActivity.this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);





            /*AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_SESSION;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+appointment.orderID);
            params.put("order_id", appointment.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getSessionInfo --> onResponse --> " + response);
                    buildSessionInfo(response);
                    //getAppointments();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getSessionInfo --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppDialogUtil.showAlert(PatientInfoActivity.this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
            postApi.setRetryPolicy(new DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void buildBookingInfo(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if(obj.length() > 0) {
                if(obj.has("booking")) {
                    JSONObject o = obj.optJSONObject("booking");
                    if (o != null && o.length() > 0) {
                        //buildBookingDetails(o.optJSONArray("bookingdetails"));

                        Patient p = getPatient(o.optJSONObject("patientdetails"));
                        Booking b = getBooking(o.optJSONArray("bookingdetails"));
                        ArrayList<PatientFile> list = getFiles(o.optJSONArray("files"));
                        if(b != null) {
                            if(b.bookingStatus.equalsIgnoreCase(AppConstants.CONFIRMED) ||
                                    b.bookingStatus.equalsIgnoreCase(AppConstants.DELAYED)) {
                                openLiveScreen(p, b, list);
                            }else {
                                AppCoreUtil.showToast(this, R.string.error_something_wrong_message);
                                getPatientInfo();
                            }
                        }else {
                            AppDialogUtil.showAlert(this, R.string.error_title,
                                    R.string.error_something_wrong_message);
                        }
                    }else {
                        AppDialogUtil.showAlert(this, R.string.error_title,
                                R.string.error_something_wrong_message);
                    }
                }else {
                    AppDialogUtil.showAlert(this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }else {
                AppDialogUtil.showAlert(this, R.string.error_title,
                        R.string.error_something_wrong_message);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Booking getBooking(JSONArray array) {
        Booking b = null;
        if(array != null && array.length() > 0) {
            JSONObject o = array.optJSONObject(0);
            if(o != null && o.length() > 0) {
                String sessionID = o.optString("session");
                String token = o.optString("token");
                String dStatus = o.optString("doc_status");
                String pStatus = o.optString("pat_status");
                String bStatus = o.optString("booking_status");
                String diagnosis = o.optString("diagnosis");
                int duration = o.optInt("duration", 0);
                b = new Booking(appointment.orderID, bStatus, dStatus, pStatus, sessionID,
                        token, duration, diagnosis);
            }
        }

        return b;
    }

    private Patient getPatient(JSONObject object) {
        if(object != null && object.length() > 0) {
            String sName = object.optString("Name");
            String sPic = object.optString("profileimg");
            String sAge = object.optString("Age");
            String sGender = object.optString("Gender");
            String sWeight = object.optString("Weight");
            String sHeight = object.optString("Height");
            String sAddress = object.optString("Address");
            String sCity = object.optString("City");
            String sDiagnosis = object.optString("Previously diagnosed conditions");
            String sMedication = object.optString("Medication");

            return new Patient(sName, sPic, sAge, sGender, sWeight, sHeight, sAddress, sCity, sMedication, sDiagnosis);
        }

        return new Patient("", "", "", "", "", "", "", "", "", "");
    }

    private ArrayList<PatientFile> getFiles(JSONArray array) {
        ArrayList<PatientFile> list = new ArrayList<>();
        if(array != null) {
            int size = array.length();
            if(size > 0) {
                JSONObject o;
                for(int i=0; i< size; i++) {
                    try {
                        o = array.optJSONObject(i);
                        if(o != null && o.length() > 0) {
                            String url = o.optString("filepath");
                            list.add(new PatientFile(o.optString("idlog"), url.substring(url.lastIndexOf("/") + 1), ""));
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return list;
    }

    private void openLiveScreen(Patient p, Booking b, ArrayList<PatientFile> list) {
        Intent in = new Intent(this, Live2Activity.class);
        in.putExtra("patient", p);
        in.putExtra("booking", b);
        in.putExtra("files", list);
        in.putExtra("appointment", appointment);
        startActivity(in);
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        if(requestCode == REQUEST_CODE_PERMISSION_VIDEO) {
            getTimeSlotStatus();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode) {
        if(requestCode == REQUEST_CODE_PERMISSION_VIDEO) {
            AppCoreUtil.showToast(this, "Permissions required for live video.");
        }
    }

    @Override
    public void onPermissionsPermanentlyDenied(int requestCode, List<String> perms) {
        /*if(requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            AppDialogUtil.showDialog(this, R.string.app_name, PermissionUtil.getCameraPermanentlyDeniedMessage(perms),
                    R.string.alert_settings_positive_title, R.string.alert_settings_negative_title, this);
        }else if(requestCode == REQUEST_CODE_PERMISSION_GALLERY) {
            AppDialogUtil.showDialog(this, R.string.app_name, PermissionUtil.getGalleryPermanentlyDeniedMessage(perms),
                    R.string.alert_settings_positive_title, R.string.alert_settings_negative_title, this);
        }*/
    }
}
