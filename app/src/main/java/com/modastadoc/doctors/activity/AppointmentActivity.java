package com.modastadoc.doctors.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
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
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.AppointmentAdapter;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.interfaces.IApmtDialogListener;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.common.utils.DateUtil;
import com.modastadoc.doctors.common.utils.PermissionUtil;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.Booking;
import com.modastadoc.doctors.model.Live;
import com.modastadoc.doctors.model.Patient;
import com.modastadoc.doctors.model.PatientFile;
import com.modastadoc.doctors.model.TimeSlot;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppointmentActivity extends AppCompatActivity implements IApmtDialogListener,
        PermissionUtil.Callbacks {

    private static final String TAG = "AppointmentActivity";

    private static final int REQUEST_CODE_PERMISSION_VIDEO = 1234;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AppointmentAdapter mAdapter;

    private Appointment selectedAppointment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Appointments");
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
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new AppointmentAdapter(this, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    private void refresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        getAppointments();
    }

    @Override
    protected void onResume() {
        super.onResume();
            getAppointments();
    }

    private void getAppointments() {
        if(AppDialogUtil.canShowPopup()) {
            if (AppCoreUtil.isNetworkAvailable()) {
                AppDialogUtil.showCustomDialog(this);
                String URL = ServerConstants.GET_APPOINTMENTS;

                HashMap<String, String> params = new HashMap<String, String>();

                PostApi postApi = new PostApi(new Response.Listener<String>() {
                    @Override

                    public void onResponse(String response) {
                        Log.i(TAG, "getAppointments --> onResponse --> " + response);
                        AppDialogUtil.dismissCustomDialog();
                        buildAppointmentList(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "getAppointments --> onErrorResponse --> " + error.getMessage());
                        AppDialogUtil.dismissCustomDialog();
                        AppCoreUtil.showToast(AppointmentActivity.this, R.string.error_something_wrong_message);
                        error.printStackTrace();
                        mAdapter.refresh(null);
                    }
                }, params, URL);

                RequestQueue volleyQueue = Volley.newRequestQueue(this);
                volleyQueue.add(postApi);
            } else {
                AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
            }
        }
    }

    private void buildAppointmentList(String response) {
        List<Appointment> list = new ArrayList<>();
        try {
            if(response != null && response.trim().length() > 0) {
                JSONObject obj = new JSONObject(response);
                JSONArray arr = obj.optJSONArray("data");
                if(arr != null) {
                    int size = arr.length();
                    if (size > 0) {
                        JSONObject o;
                        for (int i = 0; i < size; i++) {
                            o = arr.getJSONObject(i);
                            list.add(new Appointment(o.optString("order_id"), o.optString("name"), o.optString("date"),
                                    o.optString("time"), o.optString("profileimg"), o.optString("status"), o.optString("delayed_by")));
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter.refresh(list);
    }

    private void getTimeSlotStatus() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_TIME_SLOT_STATUS + selectedAppointment.orderID;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+selectedAppointment.orderID);
            params.put("order_id", selectedAppointment.orderID);
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
                    AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
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
                    getAppointments();
                }
            }else {
                AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
                        R.string.error_something_wrong_message);
            }
        }catch (Exception e){
            e.printStackTrace();
            AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
                    R.string.error_something_wrong_message);
        }
    }

    private void getBookingInfo() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_PATIENT_DETAILS + selectedAppointment.orderID;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+selectedAppointment.orderID);
            params.put("order_id", selectedAppointment.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getPatientInfo --> onResponse --> " + response);
                    buildBookingInfo(response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getPatientInfo --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
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
                        ArrayList<PatientFile> finalList = getFiles(list, o.optJSONArray("labfiles"));
                        if(b != null) {
                            if(b.bookingStatus.equalsIgnoreCase(AppConstants.CONFIRMED) ||
                                    b.bookingStatus.equalsIgnoreCase(AppConstants.DELAYED)) {
                                openLiveScreen(p, b, finalList);
                            }else {
                                AppCoreUtil.showToast(this, R.string.error_something_wrong_message);
                                getAppointments();
                            }
                        }else {
                            AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
                                    R.string.error_something_wrong_message);
                        }
                    }else {
                        AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
                                R.string.error_something_wrong_message);
                    }
                }else {
                    AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }else {
                AppDialogUtil.showAlert(AppointmentActivity.this, R.string.error_title,
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
                b = new Booking(selectedAppointment.orderID, bStatus, dStatus, pStatus, sessionID,
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

    private ArrayList<PatientFile> getFiles(ArrayList<PatientFile> list, JSONArray array) {
        if(array != null) {
            int size = array.length();
            if(size > 0) {
                JSONObject o;
                for(int i=0; i< size; i++) {
                    try {
                        o = array.optJSONObject(i);
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

        return list;
    }

    private void openLiveScreen(Patient p, Booking b, ArrayList<PatientFile> list) {
        Intent in = new Intent(this, Live2Activity.class);
        in.putExtra("patient", p);
        in.putExtra("booking", b);
        in.putExtra("files", list);
        in.putExtra("appointment", selectedAppointment);
        startActivity(in);
    }

    @Override
    public void onCallClicked(Appointment appointment) {
        selectedAppointment = appointment;
        callAppointment();
    }

    @Override
    public void onCancelClicked(Appointment appointment) {
        showCancelPopup(appointment);
    }

    @Override
    public void onDelayClicked(Appointment appointment) {
        showDelayPopup(appointment);
    }

    @Override
    public void onSummaryClicked(Appointment appointment) {
        Intent in = new Intent(this, ViewSummaryActivity.class);
        in.putExtra("order_id", appointment.orderID);
        startActivity(in);
    }

    @Override
    public void onViewDetailsClicked(Appointment appointment) {
        Intent in = new Intent(this, PatientInfoActivity.class);
        in.putExtra("appointment", appointment);
        startActivity(in);
    }

    private void callAppointment() {
        requestPermission();
    }

    private void requestPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(PermissionUtil.hasPermission(perms)) {
            //initializeSession();
            getTimeSlotStatus();
        }else {
            PermissionUtil.requestPermission(this, REQUEST_CODE_PERMISSION_VIDEO,
                    PermissionUtil.getNecessaryPermissions(perms));
        }
    }

    private void showCancelPopup(final Appointment appointment) {
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

    private void showDelayPopup(final Appointment appointment) {
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
                    getAppointments();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "cancelAppointment --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(AppointmentActivity.this, R.string.error_something_wrong_message);
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
            params.put("sdate", DateUtil.get(appointment.date, "dd-MM-yyyy", "yyyy-MM-dd"));
            params.put("delay", delay + "");
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    Log.i(TAG, "delayAppointment --> onResponse --> " + response);
                    AppDialogUtil.dismissCustomDialog();
                    if(response.equalsIgnoreCase("1")) {
                        getAppointments();
                    }else {
                        AppCoreUtil.showToast(AppointmentActivity.this, R.string.error_something_wrong_message);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, "delayAppointment --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(AppointmentActivity.this, R.string.error_something_wrong_message);
                    error.printStackTrace();
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_PERMISSION_VIDEO)
            PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
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
