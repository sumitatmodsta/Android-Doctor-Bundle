package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.LabTestAdapter;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.model.LabTestModel;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vivek.c on 04/10/16.
 * Updated by vivek.c on 16/10/2016.
 */
public class SuggestionDetailActivity extends AppCompatActivity
{

    TextView tv_OrderId, tv_Question, tv_Date, tv_Pname,
            tv_Weight, tv_Height, tv_Diagnosis,
            tv_Medication, tv_Allergy, tv_PostQueryHead, tv_Gender, tv_Age, tv_City;
    Button btn_Accept;
    EditText edt_OtherTest, edt_Diagnosis, edt_MedAdvice, edt_Testdetails, edt_Test, edt_ReviewComment;
    CheckBox chk_OtherText;
    TextView tv_ParentQuestion, tv_ParentDiag, tv_ParentMedical, tv_ParentMedicalTest, tv_ParentLabTest, tv_ParentPostDate, tv_reportsAttached;
    TableRow tableRow_Reports;
    private static final String DETAIL_OBJECT = "queryDetail";
    private static final String QUERY_OBJECT = "questionObj";
    private static final String QUERY_ID = "question_id";
    private static final String QUERY = "question";
    private static final String ANSWER = "answer";
    private static final String TESTS = "tests";
    private static final String REVIEWER_COMMENT = "reviewer_comment";
    private static final String REPORTS_ATTACHED = "file_ids";
    private static final String FOLLOW_UP_PARENTID = "reply_to";
    private static final String OTHER_LAB_TESTS = "other_lab_test";
    private static final String LAB_TEST_DETAIL = "lab_test_detail";
    private static final String QUERY_DATE = "date";
    private static final String PATIENT_OBJECT = "patientdetailObj";
    private static final String PATIENT_NAME = "fname";
    private static final String PATIENT_AGE = "age";
    private static final String PATIENT_GENDER = "gender";
    private static final String PATIENT_CITY = "city";
    private static final String WEIGHT = "weight";
    private static final String HEIGHT = "height";
    private static final String DIAGNOSIS = "diagnosis";
    private static final String PREV_MEDICATION = "med_desc";
    private static final String ALLERGY = "allergy_desc";
    private static final String PARENT_ID = "reply_to";
    private static final String LABTESTID = "labtestID";
    String questionId, reportsAttached, submitClickableStatus;
    AlertDialog alertDialog;
    EditText btn_selectTest;
    ArrayList<String> labIdList = new ArrayList<>();
    ArrayList<LabTestModel> labTestNameList = new ArrayList<>();
    ArrayList<String> selectedTestArray = new ArrayList<>();
    ArrayList<String> selectedLabTestIdArray = new ArrayList<>();
    TableLayout tab_PostQueryDeatils;
    LabTestAdapter labTestAdapter;
    public  ArrayList<String> mTestIdList = new ArrayList<>();
    public ArrayList<String> mTextNameList = new ArrayList<>();
    private boolean isListUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Suggestions Detail");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initialiseWidgets();
    }


    private void initialiseWidgets()
    {
        tv_OrderId = (TextView) findViewById(R.id.tv_orderId);
        tv_Question = (TextView) findViewById(R.id.question);
        tv_Date = (TextView) findViewById(R.id.date);
        tv_Pname = (TextView) findViewById(R.id.name);
        tv_Weight = (TextView) findViewById(R.id.weight);
        tv_Height = (TextView) findViewById(R.id.height);
        tv_Diagnosis = (TextView) findViewById(R.id.diagnosis);
        tv_Medication = (TextView) findViewById(R.id.medication);
        tv_Allergy = (TextView) findViewById(R.id.allergy);
        btn_Accept = (Button) findViewById(R.id.btn_Submit);

        tv_Gender = (TextView) findViewById(R.id.gender);
        tv_Age = (TextView) findViewById(R.id.age);
        tv_City = (TextView) findViewById(R.id.city);

        edt_OtherTest = (EditText) findViewById(R.id.edt_otherTests);
        edt_OtherTest.setVisibility(View.GONE);
        edt_Diagnosis = (EditText) findViewById(R.id.edt_diagnosis);
        edt_MedAdvice = (EditText) findViewById(R.id.edt_medicaladvice);
        edt_Testdetails = (EditText) findViewById(R.id.edt_testdetails);
        edt_Test = (EditText) findViewById(R.id.edt_test);
        edt_Test.setVisibility(View.GONE);
        chk_OtherText = (CheckBox) findViewById(R.id.checkBox);

        tv_ParentQuestion = (TextView) findViewById(R.id.followupquestion);
        tv_ParentDiag = (TextView) findViewById(R.id.diagnosisinfo);
        tv_ParentMedical = (TextView) findViewById(R.id.medical_advise_info);
        tv_ParentMedicalTest = (TextView) findViewById(R.id.medical_test_info);
        tv_ParentLabTest = (TextView) findViewById(R.id.labtest_detail_info);
        tv_ParentPostDate = (TextView) findViewById(R.id.posted_on_info);
        tableRow_Reports = (TableRow) findViewById(R.id.reportRow);
        tv_reportsAttached = (TextView) findViewById(R.id.reportsAttached);

        edt_ReviewComment = (EditText) findViewById(R.id.edt_reviewComment);
        tv_PostQueryHead = (TextView) findViewById(R.id.tv_postQueryTitle);
        tab_PostQueryDeatils = (TableLayout) findViewById(R.id.tabPostQueryDetails);

        btn_selectTest = (EditText) findViewById(R.id.select_test);

        btn_selectTest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createSelectTestDialog();
            }
        });

        chk_OtherText.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //is chkIos checked?
                if (((CheckBox) v).isChecked())
                {
                    //Case 1
                    edt_OtherTest.setVisibility(View.VISIBLE);
                } else
                {
                    //case 2
                    edt_OtherTest.setVisibility(View.GONE);
                }

            }
        });
        generateQueryDetail();
        btn_Accept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitQuery();
            }
        });

        tv_reportsAttached.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = View_Reports.createIntent( SuggestionDetailActivity.this , reportsAttached , "" , "upload");
                startActivity(intent);
            }
        });
    }

    private void showDialog(String message)
    {

        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(SuggestionDetailActivity.this);
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

    private void generateQueryDetail()
    {

        Intent intent = getIntent();
        questionId = intent.getStringExtra("question_Id");
        submitClickableStatus = intent.getStringExtra("clickstatus");
        if (submitClickableStatus.equalsIgnoreCase("false"))
        {
            btn_Accept.setEnabled(false);
        } else
        {
            btn_Accept.setEnabled(true);
        }

        showDialog("Loading...");
        String URL = ServerConstants.DOMAIN + ServerConstants.QUERY_DETAIL + questionId;

        HashMap<String, String> params = new HashMap<String, String>();

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.v("QueryFollowupResponse", "Response : " + response);
                buildQueryDetails(response);
                //readLabTextFile();
                buildLabTestDetail(LocalDataManager.getInstance().get(AppConstants.LAB_TESTS));
                dismissDialog();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
                Log.v("FollowupResponseError", "Error Response : " + error.toString());
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    public void buildQueryDetails(String response)
    {
        try
        {
            JSONObject jsonRootObject = new JSONObject(response);
            String detailObj = jsonRootObject.getString(DETAIL_OBJECT);
            Log.v("jsonRootObject", detailObj);
            JSONObject jsonRootObjectReort = new JSONObject(detailObj);
            String queryObj = jsonRootObjectReort.getString(QUERY_OBJECT);
            Log.v("queryObj", queryObj);
            JSONObject queryJson = new JSONObject(queryObj);

            String question_id = queryJson.getString(QUERY_ID);

            String parentQuestionId = queryJson.getString(PARENT_ID);
            tv_OrderId.setText("Order Id:#" + question_id);

            String question = queryJson.getString(QUERY);
            tv_Question.setText(getStringWithoutSpecialChar(question));
            String date = queryJson.getString(QUERY_DATE);
            tv_Date.setText("Posted On " + date);
            String diagnosis = queryJson.getString(DIAGNOSIS);
            if (!(diagnosis.equalsIgnoreCase("null")))
            {
                tv_Diagnosis.setText(getStringWithoutSpecialChar(diagnosis));
                edt_Diagnosis.setText(getStringWithoutSpecialChar(diagnosis));
            }
            String answer = queryJson.getString(ANSWER);
            edt_MedAdvice.setText(getStringWithoutSpecialChar(answer));
            String labTestDetail = queryJson.getString(LAB_TEST_DETAIL);
            if (!(labTestDetail.equalsIgnoreCase("null")))
            {
                edt_Testdetails.setText(labTestDetail);
            }
            String tests = queryJson.getString(TESTS);
            String[] testNameArray = tests.split(",");
            for (int i = 0 ; i < testNameArray.length ; i++) {
                mTextNameList.add(testNameArray[i]);
            }

            String testId = queryJson.optString("tests_id");
            String[] testIdArray = testId.split(",");
            for (int i = 0 ; i < testIdArray.length ; i++) {
                mTestIdList.add(testIdArray[i]);
            }

            if (!(tests.equalsIgnoreCase("null")))
            {
                btn_selectTest.setText(tests.replaceAll("#!#",","));
            }
            String otherLabTestIds = jsonRootObjectReort.getString(LABTESTID);

            if (!(otherLabTestIds.equalsIgnoreCase("null")))
            {
                String[] selectedLabTestId = otherLabTestIds.split(",");
                for (int i = 0; i < selectedLabTestId.length; i++)
                {
                    String id = selectedLabTestId[i];
                    selectedLabTestIdArray.add(id);
                }
            }
            String reviwer_Comment = queryJson.getString(REVIEWER_COMMENT);
            if (!(reviwer_Comment.equalsIgnoreCase("null")))
            {
                edt_ReviewComment.setText(getStringWithoutSpecialChar(reviwer_Comment));
            }
            String otherLabTests = queryJson.getString(OTHER_LAB_TESTS);
            if (!(otherLabTests.equalsIgnoreCase("null")))
            {
                chk_OtherText.setChecked(true);
                edt_OtherTest.setVisibility(View.VISIBLE);
                edt_OtherTest.setText(getStringWithoutSpecialChar(otherLabTests));
            } else
            {
                chk_OtherText.setChecked(false);
                edt_OtherTest.setVisibility(View.GONE);
            }

            //Followup parent_id informations
            String followupPrentID = queryJson.getString(FOLLOW_UP_PARENTID);
            getParentQueryDetails(followupPrentID);

            String patientDetailObj = jsonRootObjectReort.getString(PATIENT_OBJECT);
            JSONObject patientJson = new JSONObject(patientDetailObj);
            String name = patientJson.getString(PATIENT_NAME);
            tv_Pname.setText(name);
            String gender = patientJson.getString(PATIENT_GENDER);
            if (!(gender.equalsIgnoreCase("null")))
            {
                tv_Gender.setText(gender);
            }
            String age = patientJson.getString(PATIENT_AGE);
            if (!(age.equalsIgnoreCase("null")))
            {
                tv_Age.setText(age);
            }
            String city = patientJson.getString(PATIENT_CITY);
            if (!(city.equalsIgnoreCase("null")))
            {
                tv_City.setText(getStringWithoutSpecialChar(city));
            }
            String weight = patientJson.getString(WEIGHT);
            if (!(weight.equalsIgnoreCase("null")))
            {
                tv_Weight.setText(weight);
            }
            String height = patientJson.getString(HEIGHT);
            if (!(height.equalsIgnoreCase("null")))
            {
                tv_Height.setText(height);
            }
            String pMedication = patientJson.getString(PREV_MEDICATION);
            if (!(pMedication.equalsIgnoreCase("null")))
            {
                tv_Medication.setText(getStringWithoutSpecialChar(pMedication));
            }
            String allergy = patientJson.getString(ALLERGY);
            if (!(allergy.equalsIgnoreCase("null")))
            {
                tv_Allergy.setText(getStringWithoutSpecialChar(allergy));
            }

            String Reports = queryJson.getString(REPORTS_ATTACHED);
            Log.v("reports", "" + Reports);
            if (Reports.trim().length() > 0)
            {
                tableRow_Reports.setVisibility(View.VISIBLE);
                tv_reportsAttached.setText("View Report");
                reportsAttached = Reports;
            }

        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }
    }

    private void getParentQueryDetails(String followupPrentID)
    {

        String URL = ServerConstants.DOMAIN + ServerConstants.QUERY_DETAIL + followupPrentID;

        HashMap<String, String> params = new HashMap<String, String>();

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.v("QueryDetailResponse", "Response : " + response);
                buildParentQueryDetails(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.v("DetailResponseError", "Error Response : " + error.toString());
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    private void buildParentQueryDetails(String response)
    {
        try
        {
            JSONObject jsonRootObject = new JSONObject(response);

            String detailObj = jsonRootObject.getString(DETAIL_OBJECT);

            JSONObject jsonRootObjectReort = new JSONObject(detailObj);
            String queryObj = jsonRootObjectReort.getString(QUERY_OBJECT);
            if (!queryObj.equalsIgnoreCase("null"))
            {

                tv_PostQueryHead.setVisibility(View.VISIBLE);
                tab_PostQueryDeatils.setVisibility(View.VISIBLE);

                JSONObject queryJson = new JSONObject(queryObj);
                String question = queryJson.getString(QUERY);
                tv_ParentQuestion.setText(getStringWithoutSpecialChar(question));

                String date = queryJson.getString(QUERY_DATE);
                tv_ParentPostDate.setText("Posted On " + date);
                String diagnosis = queryJson.getString(DIAGNOSIS);
                if (!(diagnosis.equalsIgnoreCase("null")))
                {
                    tv_ParentDiag.setText(getStringWithoutSpecialChar(diagnosis));
                }

                String labTestDetail = queryJson.getString(LAB_TEST_DETAIL);
                if (!(labTestDetail.equalsIgnoreCase("null")))
                {
                    tv_ParentLabTest.setText(labTestDetail);
                }

                String medicalTests = queryJson.getString(TESTS);
                if (!(medicalTests.equalsIgnoreCase("null")))
                {
                    tv_ParentMedicalTest.setText(getStringWithoutSpecialChar(medicalTests));
                }

                String pMedication = queryJson.getString(ANSWER);
                if (!(pMedication.equalsIgnoreCase("null")))
                {
                    tv_ParentMedical.setText(getStringWithoutSpecialChar(pMedication));
                }
            } else
            {
                tv_PostQueryHead.setVisibility(View.GONE);
                tab_PostQueryDeatils.setVisibility(View.GONE);
            }
        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }
    }

    private void submitQuery()
    {
        String URL = ServerConstants.DOMAIN + ServerConstants.SUBMIT_QUERY;

        String diagnosis = edt_Diagnosis.getText().toString();
        String medicalAd = edt_MedAdvice.getText().toString();
        if ((diagnosis.trim().length() <= 0) || (medicalAd.trim().length() <= 0))
        {

            if (diagnosis.trim().length() <= 0)
            {
                edt_Diagnosis.setError("Please fill out this field.");
            }
            if (medicalAd.trim().length() <= 0)
            {
                edt_MedAdvice.setError("Please fill out this field.");
            }
        } else
        {
            String testDetails = edt_Testdetails.getText().toString();
            String otherTest = edt_OtherTest.getText().toString();
            String testName = btn_selectTest.getText().toString();
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("question_id", questionId);
            params.put("diagnosis", diagnosis);
            params.put("medical_advice", medicalAd);
            if (isListUpdated) {
                params.put("medical_test", TextUtils.join(",", selectedLabTestIdArray));
            }else {
                params.put("medical_test", TextUtils.join(",", mTestIdList));
            }
            params.put("other_test", otherTest);
            params.put("test_detail", testDetails);

            showDialog("Loading..");
            PostApi postApi = new PostApi(new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.v("Query Accepted", "Response : " + response);
                    dismissDialog();
                    navigateIntent();

                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.v("Query not accepted", "Error Response : " + error.toString());
                    dismissDialog();
                    Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
            volleyQueue.add(postApi);
        }
    }

    private void navigateIntent()
    {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    private void buildLabTestDetail(String result)
    {

        String LAB_TEST_DETAIL_OBJECT = "lablist";
        String LAB_ID = "id";
        String LAB_TEST_NAME = "lab_test_type";
        try
        {

            JSONObject jsonRootObject = new JSONObject(result);
            //Get the instance of JSONArray that contains JSONObjects

            JSONArray jsonArray = jsonRootObject.optJSONArray(LAB_TEST_DETAIL_OBJECT);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int lab_id = jsonObject.getInt(LAB_ID);
                labIdList.add(String.valueOf(lab_id));
                String lab_TestName = jsonObject.optString(LAB_TEST_NAME).toString();
                labTestNameList.add(new LabTestModel("" + lab_id, lab_TestName, false));

            }

        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }
    }

    private void createSelectTestDialog()
    {

        /*******************************
         * Multi-select-searchable list
         ******************************/
        RecyclerView rl_labtest;

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        final Dialog dialogTest = new Dialog(this);
        dialogTest.setContentView(R.layout.custom_tests_spinner);

        rl_labtest = (RecyclerView) dialogTest.findViewById(R.id.rl_labtest);
        rl_labtest.setLayoutManager(mLayoutManager);
        labTestAdapter = new LabTestAdapter(labTestNameList, this);
        rl_labtest.setAdapter(labTestAdapter);
        labTestAdapter.setSelectedId(mTestIdList);

        Button but_cancel = (Button) dialogTest.findViewById(R.id.cancelButton);
        Button submit = (Button) dialogTest.findViewById(R.id.testbutton);
        submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isListUpdated = true;
                ArrayList<LabTestModel> list = labTestAdapter.getSelectedLabs();
                Log.e("selected : ", "" + list);
                selectedLabTestIdArray.clear();
                selectedTestArray.clear();
                for (int i = 0; i < list.size(); i++)
                {
                    String name = list.get(i).getLab();
                    selectedTestArray.add(name);
                    String id = list.get(i).getId();
                    selectedLabTestIdArray.add(id);
                }

                mTestIdList = selectedLabTestIdArray;
                if (selectedTestArray.size() > 0)
                {
                    btn_selectTest.setText(TextUtils.join(",", selectedTestArray).replaceAll("#!#",","));
                } else {
                    btn_selectTest.setText("");
                }
                dialogTest.dismiss();
            }
        });
        but_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialogTest.dismiss();
            }
        });

        dialogTest.show();
        labTestAdapter.setOnItemClickListener(new LabTestAdapter.OptionClickListener()
        {
            @Override
            public void onItemClick(int positions, boolean isChecked)
            {
                if (isChecked)
                {
                    labTestAdapter.addThisItem(positions);
                } else
                {
                    labTestAdapter.removeThisItem(positions);
                }

            }
        });

        EditText editText = (EditText) dialogTest.findViewById(R.id.inputSearch);
        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (TextUtils.isEmpty(s))
                {
                    labTestAdapter.getFilter().filter("");
                } else
                {
                    labTestAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
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

