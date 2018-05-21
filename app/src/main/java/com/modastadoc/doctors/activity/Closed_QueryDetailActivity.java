package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by vivek.c on 06/10/16.
 */
public class Closed_QueryDetailActivity extends AppCompatActivity
{
    public String TAG = Closed_QueryDetailActivity.class.getSimpleName();

    TextView tv_Order_Id, tv_Question, tv_Date, tv_Diagnosis, tv_Medication, tv_MedicalTest, tv_LabTestDeatil,
            tv_followupDiagnosis, tv_followupMedication, tv_followupMediclTest,
            tv_followupLabTestDetail, tv_FollowupQuestion, tv_FollowupDate;

    private static final String DETAIL_OBJECT = "queryDetail";
    private static final String QUERY_OBJECT  = "questionObj";
    private static final String QUERY         = "question";
    private static final String QUERY_DATE    = "date";

    private static final String FOLLOWUPOBJECT  = "followupObj";
    private static final String DIAGNOSIS       = "diagnosis";
    private static final String PREV_MEDICATION = "med_desc";
    private static final String ANSWER          = "answer";
    private static final String LAB_TEST_DETAIL = "lab_test_detail";
    private static final String TESTS           = "tests";
    private static final String OTHERLABTESTS   = "other_lab_test";

    String questionId;
    AlertDialog alertDialog;
    RelativeLayout followupLayout;
    TableLayout followUpAnswerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closed_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Closed Query Details");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initialiseWidgets();
    }

    private void initialiseWidgets() {
        tv_Order_Id      = (TextView) findViewById(R.id.tv_orderId);
        tv_Question      = (TextView) findViewById(R.id.question);
        tv_Date          = (TextView) findViewById(R.id.date);
        tv_Diagnosis     = (TextView) findViewById(R.id.diagnosisinfo);
        tv_Medication    = (TextView) findViewById(R.id.medical_advise_info);
        tv_MedicalTest   = (TextView) findViewById(R.id.medical_test_info);
        tv_LabTestDeatil = (TextView) findViewById(R.id.labtest_detail_info);

        tv_FollowupQuestion      = (TextView) findViewById(R.id.fquestion);
        tv_FollowupDate          = (TextView) findViewById(R.id.followupdate);
        tv_followupDiagnosis     = (TextView) findViewById(R.id.fdiagnosisinfo);
        tv_followupMedication    = (TextView) findViewById(R.id.fmedical_advise_info);
        tv_followupMediclTest    = (TextView) findViewById(R.id.fmedical_test_info);
        tv_followupLabTestDetail = (TextView) findViewById(R.id.flabtest_detail_info);

        followupLayout       = (RelativeLayout) findViewById(R.id.followupLayout);
        followUpAnswerLayout = (TableLayout) findViewById(R.id.tab3);

        followupLayout.setVisibility(View.GONE);
        followUpAnswerLayout.setVisibility(View.GONE);
        generateQueryDetail();

    }

    private void showDialog(String message) {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(Closed_QueryDetailActivity.this);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void dismissDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void generateQueryDetail()
    {
        Intent intent = getIntent();
        questionId = intent.getStringExtra("question_Id");
        tv_Order_Id.setText("Order Id:#" + String.valueOf(questionId));

        showDialog("Loading...");

        String URL = ServerConstants.DOMAIN + ServerConstants.QUERY_DETAIL + questionId;

        HashMap<String, String> params = new HashMap<String, String>();

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e( TAG , "ClosedQueryResponse Response : " + response);
                buildQueryDetails(response);
                dismissDialog();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
                Log.e( TAG, "generateQueryDetail Error Response : " + error.toString());
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue( getApplicationContext() );
        volleyQueue.add(postApi);
    }

    public void buildQueryDetails(String response) {
        try {
            JSONObject jsonRootObject = new JSONObject(response);

            String detailObj = jsonRootObject.getString(DETAIL_OBJECT);

            JSONObject jsonRootObjectReort = new JSONObject(detailObj);
            String queryObj = jsonRootObjectReort.getString(QUERY_OBJECT);

            JSONObject queryJson = new JSONObject(queryObj);

            String question = queryJson.getString(QUERY);
            tv_Question.setText(getStringWithoutSpecialChar(question));

            String date = queryJson.getString(QUERY_DATE);
            tv_Date.setText(date);

            String diagnosis = queryJson.getString(DIAGNOSIS);
            if (!(diagnosis.equalsIgnoreCase("null")))
            {
                tv_Diagnosis.setText(getStringWithoutSpecialChar(diagnosis));
            }

            String answer = queryJson.getString(ANSWER);
            tv_Medication.setText(getStringWithoutSpecialChar(answer));

            String labTestDetail = queryJson.getString(LAB_TEST_DETAIL);
            if ( !( labTestDetail.equalsIgnoreCase( "null" ) ) )
            {
                tv_LabTestDeatil.setText(labTestDetail);
            }

            String tests         = queryJson.getString(TESTS);
            String otherLabTests = queryJson.getString(OTHERLABTESTS);
            if ((tests.trim().length() > 0))
            {
                if (!(tests.equalsIgnoreCase("null")))
                {
                    tv_MedicalTest.setText(tests.replaceAll("#!#",",") + "," + getStringWithoutSpecialChar(otherLabTests));
                }
            }
            else
            {
                tv_MedicalTest.setText(otherLabTests);
            }

            JSONArray followupDetailObj = jsonRootObjectReort.getJSONArray(FOLLOWUPOBJECT);
            Log.e( "followupDetailObj" , "" + followupDetailObj );

            for (int i = 0; i < followupDetailObj.length(); i++)
            {
                JSONObject patientJson = followupDetailObj.getJSONObject(i);
                if (patientJson != null)
                {
                    followupLayout.setVisibility(View.VISIBLE);
                    followUpAnswerLayout.setVisibility(View.VISIBLE);
                    String folllowupQuestion = patientJson.getString(QUERY);
                    tv_FollowupQuestion.setText(getStringWithoutSpecialChar(folllowupQuestion));
                    String followupAnswer = patientJson.getString(DIAGNOSIS);
                    tv_followupDiagnosis.setText(getStringWithoutSpecialChar(followupAnswer));

                    String folllwupMedicalAdvice = patientJson.getString(ANSWER);
                    tv_followupMedication.setText(getStringWithoutSpecialChar(folllwupMedicalAdvice));

                    String followupDate = patientJson.getString(QUERY_DATE);
                    tv_FollowupDate.setText(followupDate);

                    String followupMedicalTest = patientJson.getString(TESTS);

                    if (!(followupMedicalTest.equalsIgnoreCase("null")))
                    {
                        tv_followupMediclTest.setText(followupMedicalTest.replaceAll("#!#",","));
                    }

                    String followupTestDetail = patientJson.getString(LAB_TEST_DETAIL);
                    if (!(followupTestDetail.equalsIgnoreCase("null")))
                    {
                        tv_followupLabTestDetail.setText(followupTestDetail);
                    }
                } else
                {
                    followupLayout.setVisibility(View.GONE);
                    followUpAnswerLayout.setVisibility(View.GONE);
                }
            }
        }
        catch (JSONException e)
        {
            Log.e( TAG , "Exc. :" + e.toString());
        }
    }


    private String getStringWithoutSpecialChar(String str){
        char[] array = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (array[i] != '\\'){
                stringBuffer.append(array[i]);
            }
        }
        return stringBuffer.toString();
    }

}
