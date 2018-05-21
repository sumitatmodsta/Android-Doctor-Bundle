package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.network.PostApi;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by vivek.c on 22/09/16.
 */
public class DoctorSummaryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    EditText edt_FromTime, edt_ToTime;
    Button btn_Submit;
    TextView tv_Text, tv_Audio, tv_Video, tv_Walkings;
    private static final String SUMMARY_REPORT = "report";
    private static final String QUERY_TEXT = "query";
    private static final String QUERY_AUDIO = "audio";
    private static final String QUERY_VIDEO = "video";
    private static final String WALKINS = "walking";
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Summary Report");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        initialiseWidgets();
        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateSummaryReport();
            }
        });
        edt_FromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        DoctorSummaryActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "fromDatepicker");

            }
        });

        edt_ToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        DoctorSummaryActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "toDatepicker");

            }
        });
    }

    private void showDialog(String message) {

        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(DoctorSummaryActivity.this);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void dismissDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void initialiseWidgets() {
        edt_FromTime = (EditText) findViewById(R.id.edt_fromtime);
        edt_ToTime = (EditText) findViewById(R.id.totime);
        btn_Submit = (Button) findViewById(R.id.submit);
        tv_Text = (TextView) findViewById(R.id.textQuery);
        tv_Audio = (TextView) findViewById(R.id.audioQuery);
        tv_Video = (TextView) findViewById(R.id.videoQuery);
        tv_Walkings = (TextView) findViewById(R.id.walkingQuery);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        int currentMonth = monthOfYear + 1;
        String Month;
        String selectedDay;
        Log.v("dialog", "" + view.getTag());
        if (currentMonth < 10) {
            Month = "0" + String.valueOf(currentMonth);
        } else {
            Month = String.valueOf(currentMonth);
        }
        if (dayOfMonth < 10) {
            selectedDay = "0" + String.valueOf(dayOfMonth);
        } else {
            selectedDay = String.valueOf(dayOfMonth);
        }
        String date = selectedDay + "-" + Month + "-" + year;
        if (view.getTag().equalsIgnoreCase("fromDatepicker")) {
            edt_FromTime.setText(date);
        } else {
            edt_ToTime.setText(date);
        }

    }

    private void generateSummaryReport() {

        String fromDate = edt_FromTime.getText().toString();
        String toDate = edt_ToTime.getText().toString();

        String URL = ServerConstants.DOMAIN + ServerConstants.SUMMARY_REPORT;
        showDialog("Loading...");
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        params.put("type", "summary");

        PostApi postApi = new PostApi(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.v("SummaryResponse", "Response : " + response);
                buildQuestionsView(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Error SummaryResponse", "Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();

            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    private void buildQuestionsView(String response) {

        try {
            JSONObject jsonRootObject = new JSONObject(response);
            String reportQuery = jsonRootObject.getString(SUMMARY_REPORT);
            JSONObject jsonRootObjectReort = new JSONObject(reportQuery);
            String textQuery = jsonRootObjectReort.getString(QUERY_TEXT);
            tv_Text.setText(textQuery);
            String audioQuery = jsonRootObjectReort.getString(QUERY_AUDIO);
            tv_Audio.setText(audioQuery);
            String videoQuery = jsonRootObjectReort.getString(QUERY_VIDEO);
            tv_Video.setText(videoQuery);
            String walkings = jsonRootObjectReort.getString(WALKINS);
            tv_Walkings.setText(walkings);
            dismissDialog();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
