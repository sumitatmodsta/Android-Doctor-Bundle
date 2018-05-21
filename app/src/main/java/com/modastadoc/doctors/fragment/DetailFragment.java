package com.modastadoc.doctors.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.activity.PatientInfoActivity;
import com.modastadoc.doctors.adapter.FileAdapter;
import com.modastadoc.doctors.adapter.PreviousApmtAdapter;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.PatientFile;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";

    private TextView name, age, gender, height, weight, diagnosis, medication, allergy;

    private RecyclerView recyclerView, prevRecyclerView;
    private PreviousApmtAdapter mPreviousApmtAdapter;
    private FileAdapter mAdapter;

    private static Appointment appointment;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
        }
    };

    private void refresh() {
        getPatientInfo();
    }

    public static DetailFragment getInstance(Appointment apmt) {
        appointment = apmt;
        return new DetailFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        name = (TextView) view.findViewById(R.id.name);
        age = (TextView) view.findViewById(R.id.age);
        gender = (TextView) view.findViewById(R.id.gender);
        height = (TextView) view.findViewById(R.id.height);
        weight = (TextView) view.findViewById(R.id.weight);
        diagnosis = (TextView) view.findViewById(R.id.diagnosis);
        medication = (TextView) view.findViewById(R.id.medication);
        allergy = (TextView) view.findViewById(R.id.allergy);

        getActivity().registerReceiver(mReceiver, new IntentFilter("refresh_patient_details"));

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        prevRecyclerView = (RecyclerView) view.findViewById(R.id.pre_cons_recycler_view);
        mAdapter = new FileAdapter(this.getActivity());
        mPreviousApmtAdapter = new PreviousApmtAdapter(this.getActivity());

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getActivity().getApplicationContext());
        prevRecyclerView.setLayoutManager(mLayoutManager2);
        prevRecyclerView.setItemAnimator(new DefaultItemAnimator());
        prevRecyclerView.setAdapter(mPreviousApmtAdapter);

        getPatientInfo();

        return view;
    }

    private void getPatientInfo() {
        try {
            if (AppCoreUtil.isNetworkAvailable()) {
                if(appointment != null) {
                    //AppDialogUtil.showCustomDialog(this);
                    String URL = ServerConstants.GET_PATIENT_DETAILS + appointment.orderID;

                    HashMap<String, String> params = new HashMap<>();
                    Log.i(TAG, "Order ID -- " + appointment.orderID);
                    params.put("orderId", appointment.orderID);
                    PostApi postApi = new PostApi(new Response.Listener<String>() {
                        @Override

                        public void onResponse(String response) {
                            //AppDialogUtil.dismissCustomDialog();
                            Log.i(TAG, "getPatientInfo --> onResponse --> " + response);
                            buildPatientInfo(response);
                            //getAppointments();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.i(TAG, "getPatientInfo --> onErrorResponse --> " + error.getMessage());
                            //AppDialogUtil.dismissCustomDialog();
                            AppCoreUtil.showToast(DetailFragment.this.getActivity(), R.string.error_something_wrong_message);
                        }
                    }, params, URL);

                    RequestQueue volleyQueue = Volley.newRequestQueue(DetailFragment.this.getActivity());
                    volleyQueue.add(postApi);
                }
            } else {
                AppCoreUtil.showToast(DetailFragment.this.getActivity(), getString(R.string.error_no_network_message));
            }
        }catch (Exception e){
            e.printStackTrace();
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

    private void fillPatientDetails(JSONObject o) {
        if(o != null && o.length() > 0) {
            String sAge = o.optString("Age");
            String sGender = o.optString("Gender");
            String sWeight = o.optString("Weight");
            String sHeight = o.optString("Height");
            String sDiagnosis = o.optString("Previously diagnosed conditions");
            String sMedication = o.optString("Medication");
            String sAllergy = o.optString("Allergies");

            name.setText(appointment.name);
            age.setText(sAge);
            gender.setText(sGender);
            weight.setText(sWeight + " Kg");
            height.setText(sHeight + " cm");
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

    private void buildPatientInfo(String response) {
        try {
            try {
                JSONObject obj = new JSONObject(response);
                if(obj.length() > 0) {
                    JSONObject o = obj.optJSONObject("booking");
                    if(o != null && o.length() > 0) {
                        fillPatientDetails(o.optJSONObject("patientdetails"));
                        List<PatientFile> list = getFiles(o.optJSONArray("files"));
                        buildUploadedFiles(list, o.optJSONArray("labfiles"));
                        buildPreviousConsultation(o.optJSONArray("prevapmt"));
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }
}
