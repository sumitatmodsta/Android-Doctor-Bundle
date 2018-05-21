package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.UtilityMethods;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.model.LabTestModel;
import com.modastadoc.doctors.network.MultipartRequest;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class FollowupActivity extends AppCompatActivity
{

    String TAG = FollowupActivity.class.getSimpleName();

    TextView tv_OrderId, tv_Question, tv_Date, tv_Pname, tv_Weight, tv_Height, tv_Diagnosis, tv_Medication, tv_Allergy;
    Button btn_Accept;
    EditText edt_OtherTest, edt_Diagnosis, edt_MedAdvice, edt_Testdetails, edt_Test;
    CheckBox chk_OtherText;
    TextView tv_ParentQuestion, tv_ParentDiag, tv_ParentMedical,
            tv_ParentMedicalTest, tv_ParentLabTest,
            tv_ParentPostDate, tv_reportsAttached, tv_Gender, tv_Age, tv_City;

    TableRow tableRow_Reports;
    TableRow tr_labReport;
    TextView tv_labtestAttached;
    String labReportAttached = "";
    String labReportsNameAttached = "";


    LabTestAdapter labTestAdapter;


    private static final String DETAIL_OBJECT = "queryDetail";
    private static final String QUERY_OBJECT = "questionObj";
    private static final String QUERY_ID = "question_id";
    private static final String QUERY = "question";
    private static final String ANSWER = "answer";
    private static final String TESTS = "tests";
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

    String questionId="", reportsAttached="", submitClickableStatus="", followupQuestionId="", reportsNameAttached="";
    AlertDialog alertDialog;

    EditText btn_selectTest;

    ArrayList<String> labIdList = new ArrayList<>();
    ArrayList<LabTestModel> labTestNameList = new ArrayList<>();
    ArrayList<LabTestModel> selectedLabTest = new ArrayList<>();
    ArrayList<String> selectedTestArray = new ArrayList<>();
    ArrayList<String> selectedLabTestIdArray = new ArrayList<>();
    public ArrayList<String> mTestIdList = new ArrayList<>();

    String patientAudioFileLink = "";
    String doctorAudioFileLink = "";
    String patientFollowupAudioFileLink = "";
    String doctorFollowupAudioFileLink = "";

    Button bt_patient_audio_link;
    Button bt_doctor_audio_link;
    Button bt_patient_followup_audio_link;
    //Button bt_doctor_followup_audio_link;

    String uploadedAudioFileId="";


    /************************/
    private MediaPlayer mediaPlayerParent;
    private double startTime = 0;
    private double finalTime = 0;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    Dialog recorddialog;
    String AudioSavePathInDevice = "";
    /************************/


    /************************/
    MediaRecorder mediaRecorder;
    FloatingActionButton bt_play;
    FloatingActionButton bt_pause;
    FloatingActionButton bt_record;
    FloatingActionButton bt_reload;
    FloatingActionButton bt_stop;
    Button bt_upload_audio_file;
    ImageView iv_close_audio_popup;
    TextView tv_timer_task;
    Uri uriAudioFile;
    int cnt;
    MediaPlayer mediaPlayer;
    CountDownTimer t;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    Random random = new Random();
    /************************/


    /*************************/
    String twoHyphens = "--";
    String lineEnd = "\r\n";
    String boundary = "apiclient-" + System.currentTimeMillis();
    String mimeType = "multipart/form-data;boundary=" + boundary;
    /*************************/

    CustomProgressDialog customDialog;
    TextView tv_audio_attachment_name;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followup);

        Log.e(TAG, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Followup Query");
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

        bt_doctor_audio_link = (Button) findViewById(R.id.bt_doctor_audio_link);
        bt_patient_audio_link = (Button) findViewById(R.id.bt_patient_audio_link);
        bt_patient_followup_audio_link = (Button) findViewById(R.id.bt_patient_followup_audio_link);
        //bt_doctor_followup_audio_link = (Button) findViewById(R.id.bt_doctor_followup_audio_link);
        tv_audio_attachment_name = (TextView) findViewById(R.id.tv_audio_attachment_name);

        tr_labReport = (TableRow) findViewById(R.id.tr_labReport);
        tv_labtestAttached = (TextView) findViewById(R.id.tv_labtestAttached);

        tv_audio_attachment_name.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (uploadedAudioFileId.length() > 0)
                {
                    confirmAudioAttachmentRemoval();
                }
            }
        });

        btn_selectTest = (EditText) findViewById(R.id.select_test);

        btn_selectTest.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    createSelectTestDialog();
                }
                return false;
            }
        });

        chk_OtherText.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (((CheckBox) v).isChecked())
                {
                    edt_OtherTest.setVisibility(View.VISIBLE);
                } else
                {
                    edt_OtherTest.setVisibility(View.GONE);
                }

            }
        });

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
                Intent intent = View_Reports.createIntent(FollowupActivity.this, reportsAttached, reportsNameAttached, "upload");
                startActivity(intent);
            }
        });


        tv_labtestAttached.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = View_Reports.createIntent(FollowupActivity.this, labReportAttached, labReportsNameAttached, "lab");
                startActivity(intent);
            }
        });

        generateQueryDetail();
    }

    private void confirmAudioAttachmentRemoval()
    {
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to remove your audio recording?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                tv_audio_attachment_name.setVisibility(View.GONE);
                //bt_doctor_followup_audio_link.setVisibility(View.VISIBLE);
                uploadedAudioFileId = "";
                Log.e(TAG, "File deleted ? " + new File(AudioSavePathInDevice).getAbsoluteFile().delete());
                AudioSavePathInDevice = "";

            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showDialog(String message)
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(FollowupActivity.this);
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
            btn_Accept.setVisibility(View.GONE);
        } else
        {
            btn_Accept.setVisibility(View.VISIBLE);
        }

        showDialog("Loading...");

        String URL = ServerConstants.DOMAIN + ServerConstants.QUERY_DETAIL + questionId;
        Log.e(TAG, "URL : " + URL);

        HashMap<String, String> params = new HashMap<String, String>();

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "generateQueryDetail response : " + response);
                response = UtilityMethods.removeEscapedCharacters( response );
                buildQueryDetails(response);
                //readLabTextFile();
                buildLabTestDetail(LocalDataManager.getInstance().get(AppConstants.LAB_TESTS, ""));
                dismissDialog();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
                Log.e(TAG, "generateQueryDetail Error Response : " + error.toString());
            }

        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    public void buildQueryDetails(String response)
    {
        Log.e(TAG, "Response : " + response);
        try
        {
            /**************************************************
             *
             * Audio - Audio links
             *
             **************************************************/
            JSONObject jsonRootObject = new JSONObject(response);
            String detailObj = jsonRootObject.getString(DETAIL_OBJECT);
            JSONObject jsonRootObjectReort = new JSONObject(detailObj);

            patientAudioFileLink         = jsonRootObjectReort.getString("patient_audio_file_link");
            patientFollowupAudioFileLink = jsonRootObjectReort.getString("patient_followup_audio_file_link");
            doctorAudioFileLink          = jsonRootObjectReort.getString("doc_audio_file_link");
            doctorFollowupAudioFileLink  = jsonRootObjectReort.getString("followup_doc_audio_file_link");

            if (patientAudioFileLink.equalsIgnoreCase("null"))
            {
                patientAudioFileLink = "";
            }

            if (patientFollowupAudioFileLink.equalsIgnoreCase("null"))
            {
                patientFollowupAudioFileLink = "";
            }

            if (doctorAudioFileLink.equalsIgnoreCase("null"))
            {
                doctorAudioFileLink = "";
            }

            if (doctorFollowupAudioFileLink.equalsIgnoreCase("null"))
            {
                doctorFollowupAudioFileLink = "";
            }

            if (patientAudioFileLink.length() > 0)
            {
                bt_patient_audio_link.setVisibility(View.VISIBLE);
                bt_patient_audio_link.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showPopupWindowListenAudio(patientAudioFileLink);
                    }
                });
            } else
            {
                bt_patient_audio_link.setVisibility(View.GONE);
            }

            if (patientFollowupAudioFileLink.length() > 0)
            {
                bt_patient_followup_audio_link.setVisibility(View.VISIBLE);
                bt_patient_followup_audio_link.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showPopupWindowListenAudio(patientFollowupAudioFileLink);
                    }
                });
            } else
            {
                bt_patient_followup_audio_link.setVisibility(View.GONE);
            }


            if (doctorAudioFileLink.length() > 0)
            {
                bt_doctor_audio_link.setVisibility(View.VISIBLE);
                bt_doctor_audio_link.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showPopupWindowListenAudio(doctorAudioFileLink);
                    }
                });
            } else
            {
                bt_doctor_audio_link.setVisibility(View.GONE);
            }

            /*if (doctorFollowupAudioFileLink.length() > 0)
            {
                bt_doctor_followup_audio_link.setVisibility(View.VISIBLE);
                bt_doctor_followup_audio_link.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showPopupWindowListenAudio(doctorFollowupAudioFileLink);
                    }
                });
            } else
            {
                bt_doctor_followup_audio_link.setVisibility(View.VISIBLE);
                bt_doctor_followup_audio_link.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //uploadFileUsingVolley(AudioSavePathInDevice,uriAudioFile);
                        showRecordAudioFilePopup();
                    }
                });
            }*/
            /***************************************************/


            /*************************
             *
             * follow up lab test object
             *
             *************************/
            String otherLabTestIds = jsonRootObjectReort.getString(LABTESTID);

            if (!(otherLabTestIds.equalsIgnoreCase("null")))
            {
                String[] selectedLabTestId = otherLabTestIds.split(",");
                for (int i = 0; i < selectedLabTestId.length; i++)
                {
                    String id = selectedLabTestId[i];
                    selectedLabTestIdArray.add(id);
                }
                //labTestAdapter.setSelectedId(selectedLabTestIdArray);
            }

            /********************************************************
             * Follow up json response
             **************************/
            JSONArray followupArr = jsonRootObjectReort.getJSONArray("followupObj");
            JSONObject followupObj = followupArr.getJSONObject(0);

            String order_id = followupObj.getString("reply_to");
            tv_OrderId.setText("Order Id:#" + order_id);

            String question_id = followupObj.getString("question_id");
            followupQuestionId = question_id;

            String question_text = followupObj.getString("question");
            tv_Question.setText(getStringWithoutSpecialChar(question_text));

            String query_date = followupObj.getString("date_time");
            tv_Date.setText("Posted On " + query_date);

            String diagnosis = followupObj.getString(DIAGNOSIS);
            if (!(diagnosis.equalsIgnoreCase("null")))
            {
                tv_Diagnosis.setText(getStringWithoutSpecialChar(diagnosis));
                edt_Diagnosis.setText(getStringWithoutSpecialChar(diagnosis));
            }
            String answer = followupObj.getString(ANSWER);
            edt_MedAdvice.setText(getStringWithoutSpecialChar(answer));
            String labTestDetail = followupObj.getString(LAB_TEST_DETAIL);
            if (!(labTestDetail.equalsIgnoreCase("null")))
            {
                edt_Testdetails.setText(labTestDetail.replaceAll("#!#",","));
            }
            String tests = followupObj.getString(TESTS);
            if (!(tests.equalsIgnoreCase("null")))
            {
                btn_selectTest.setText(tests);
            }

            String otherLabTests = followupObj.getString(OTHER_LAB_TESTS);
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

//            String followupReports = followupObj.getString(REPORTS_ATTACHED);
//
//            if (!(followupReports.equalsIgnoreCase("null")))
//            {
//                reportsAttached = followupReports;
//                tableRow_Reports.setVisibility(View.VISIBLE);
//                tv_reportsAttached.setText("View Reports");
//            }

            reportsAttached = "";
            reportsNameAttached = "";
            JSONArray attachedReportDetailArray = jsonRootObjectReort.getJSONArray("fileID");
            for (int i = 0; i < attachedReportDetailArray.length(); i++)
            {
                JSONObject attachedReportObject = attachedReportDetailArray.getJSONObject(i);
                Log.e("current_bug", "-> " + attachedReportObject.toString());
                String reportId = attachedReportObject.getString("idlog");
                String reportPath = attachedReportObject.getString("reference_name");

                reportsAttached = reportsAttached + "," + reportId;
                reportsNameAttached = reportsNameAttached + "," + reportPath;

            }


            if (reportsAttached.length() > 0)
            {
                tableRow_Reports.setVisibility(View.VISIBLE);
                tv_reportsAttached.setText("View Report");
            }


            JSONArray attachedLabReportArray = jsonRootObjectReort.getJSONArray("labtestID");
            for (int i = 0; i < attachedLabReportArray.length(); i++)
            {
                JSONObject attachedReportObject = attachedLabReportArray.getJSONObject(i);
                String reportName = attachedReportObject.getString("filename");
                String reportId = attachedReportObject.getString("uploadid");


                labReportAttached = labReportAttached + "," + reportId;
                labReportsNameAttached = labReportsNameAttached + "," + reportName;
            }

            if (labReportAttached.length() > 0)
            {
                tr_labReport.setVisibility(View.VISIBLE);
                tv_labtestAttached.setText("View Lab Report");
            }

            /*************************************************************
             *
             * Question Object
             *
             * **********************************************************/
            String queryObj = jsonRootObjectReort.getString(QUERY_OBJECT);
            JSONObject parentQuestionObj = new JSONObject(queryObj);
            String testsPrevious = parentQuestionObj.getString(TESTS);

            /*String testId = parentQuestionObj.optString("tests_id");
            String[] testIdArray = testId.split(",");
            for (int i = 0 ; i < testIdArray.length ; i++) {
                mTestIdList.add(testIdArray[i]);
            }*/

            if (!(testsPrevious.equalsIgnoreCase("null")))
            {
                String[] temp_list = testsPrevious.split(",");
                for (int i = 0; i < temp_list.length; i++)
                {
                    String temp = temp_list[i];
                    selectedTestArray.add(temp);
                }
            }
            chk_OtherText.setChecked(true);
            edt_OtherTest.setVisibility(View.VISIBLE);

            String diagnosisPrevious = parentQuestionObj.getString(DIAGNOSIS);
            if (!(diagnosisPrevious.equalsIgnoreCase("null")))
            {
                tv_Diagnosis.setText(getStringWithoutSpecialChar(diagnosisPrevious));
            }

            /*FollowuUp Root question details*/
            String followupPrentID = followupObj.getString(FOLLOW_UP_PARENTID);
            getParentQueryDetails(followupPrentID);

            /*Patient details object*/
            String patientDetailObj = jsonRootObjectReort.getString(PATIENT_OBJECT);
            JSONObject patientJson = new JSONObject(patientDetailObj);

            String labTestId = jsonRootObjectReort.getString(LABTESTID);

            String name = patientJson.getString(PATIENT_NAME);
            tv_Pname.setText(name);
            String weight = patientJson.getString(WEIGHT);
            if (!(weight.equalsIgnoreCase("null")))
            {
                tv_Weight.setText(weight + " kgs");
            }

            String height = patientJson.getString(HEIGHT);
            if (!(height.equalsIgnoreCase("null")))
            {
                tv_Height.setText(getAppropriateHeight(height));
            }

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
        } catch (JSONException e)
        {
            Log.e("FollowupActivity", "Ex1 : " + e.toString());
        }
    }

    private String getAppropriateHeight(String param)
    {

        ArrayList<String> heightList = UtilityMethods.fillHeightList();

        param = param + " cms";
        int index = -1;
        for (int i = 0; i < heightList.size(); i++)
        {
            if (heightList.get(i).contains(param))
            {
                return heightList.get(i);
            }
        }

        return "";
    }

    public void MediaRecorderReady()
    {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void showRecordAudioFilePopup()
    {
        MediaRecorderReady();

        recorddialog = new Dialog(FollowupActivity.this);
        recorddialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        recorddialog.setContentView(R.layout.record_audio_layout);
        recorddialog.setCancelable(false);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(recorddialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        recorddialog.getWindow().setAttributes(lp);

        recorddialog.show();

        bt_play = (FloatingActionButton) recorddialog.findViewById(R.id.bt_play);
        bt_record = (FloatingActionButton) recorddialog.findViewById(R.id.bt_record);
        bt_reload = (FloatingActionButton) recorddialog.findViewById(R.id.bt_reload);
        bt_stop = (FloatingActionButton) recorddialog.findViewById(R.id.bt_stop);
        bt_pause = (FloatingActionButton) recorddialog.findViewById(R.id.bt_pause);

        bt_upload_audio_file = (Button) recorddialog.findViewById(R.id.bt_upload_audio_file);
        iv_close_audio_popup = (ImageView) recorddialog.findViewById(R.id.iv_close_audio_popup);


        tv_timer_task = (TextView) recorddialog.findViewById(R.id.tv_timer_task);
        tv_timer_task.setText("00:00:00");

        /*********************
         *
         * Zero state.
         *
         ********************/
        bt_record.setVisibility(View.VISIBLE);
        bt_play.setVisibility(View.GONE);
        bt_stop.setVisibility(View.GONE);
        bt_reload.setVisibility(View.GONE);
        bt_pause.setVisibility(View.GONE);
        /********************/

        bt_upload_audio_file.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (AudioSavePathInDevice.length() > 0 && uriAudioFile != null)
                {
                    if (mediaRecorder != null)
                    {
                        mediaRecorder.release();
                    }

                    uploadFileUsingVolley(AudioSavePathInDevice, uriAudioFile);

                } else
                {
                    Toast.makeText(FollowupActivity.this, "Failed!", Toast.LENGTH_LONG).show();
                }

            }
        });

        iv_close_audio_popup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recorddialog.dismiss();
                if (mediaRecorder != null)
                {
                    mediaRecorder.release();
                }

                if (mediaPlayer != null)
                {
                    mediaPlayer.release();
                }

                if (t != null)
                {
                    t.cancel();
                }

                cnt = 0;
            }
        });

        bt_record.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recordingStatus();

                startTimer();

                AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CreateRandomAudioFileName(5) + "AudioRecording.3gp";

                MediaRecorderReady();

                try
                {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    //visualizer.startListening();
                    //Log.e(TAG, "visualizer start listening");
                } catch (IllegalStateException e)
                {
                    Log.e(TAG, "IllegalStateException : " + e.toString());
                } catch (IOException e)
                {
                    Log.e(TAG, "IOException : " + e.toString());
                } catch (Exception e)
                {
                    Log.e(TAG, "Exception : " + e.toString());
                }


                Toast.makeText(FollowupActivity.this, "Recording started", Toast.LENGTH_SHORT).show();
            }

        });

        bt_stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stopStatus();

                uriAudioFile = Uri.fromFile(new File(AudioSavePathInDevice));

                cnt = 0;
                stopTimer();

                mediaRecorder.stop();
                Toast.makeText(FollowupActivity.this, "Recording Completed", Toast.LENGTH_SHORT).show();
            }
        });

        bt_play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playingStatus();

                mediaPlayer = new MediaPlayer();

                try
                {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e)
                {
                    Log.e(TAG, "IOException : " + e.toString());
                }

                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        stopStatus();
                    }
                });
                Toast.makeText(FollowupActivity.this, "Recording Playing", Toast.LENGTH_SHORT).show();
            }
        });

        bt_pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pauseStatus();

                try
                {
                    mediaPlayer.pause();
                } catch (Exception e)
                {
                    Log.e(TAG, "Exception : " + e.toString());
                }

            }
        });


        bt_reload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                reloadingStatus();

                Log.e(TAG, "File Deleted ? " + new File(AudioSavePathInDevice).getAbsoluteFile().delete());

                tv_timer_task.setText("00:00:00");

                cnt = 0;

                t.cancel();

                if (mediaRecorder != null)
                {
                    mediaRecorder.reset();
                }

                if (mediaPlayer != null)
                {
                    mediaPlayer.reset();
                }
            }
        });
    }

    public static byte[] getBytes(Context context, Uri uri) throws IOException
    {
        InputStream iStream = context.getContentResolver().openInputStream(uri);
        try
        {
            return getBytes(iStream);
        } finally
        {
            try
            {

                iStream.close();
            } catch (Exception ignored)
            {
                Log.e("uploadFile", "" + ignored.toString());
            }
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException
    {

        byte[] bytesResult = null;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        try
        {
            int len;
            while ((len = inputStream.read(buffer)) != -1)
            {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        } finally
        {
            // close the stream
            try
            {
                byteBuffer.close();
            } catch (IOException ignored)
            { /* do nothing */ }

        }
        return bytesResult;
    }

    public void uploadFileUsingVolley(String file_name, Uri uri)
    {
        Context context = this;
        byte[] multipartBody = null;

        byte[] fileData1 = null;
        try
        {
            fileData1 = getBytes(FollowupActivity.this, uri);
        } catch (Exception e)
        {

            Log.e(TAG, "Ex : " + e.toString());
            return;
        }


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try
        {
            buildPart(dos, fileData1, file_name);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            multipartBody = bos.toByteArray();
        } catch (IOException e)
        {

            Log.e(TAG, "E. " + e.toString());
            return;
        }

        showCustomDialog();
        String upLoadServerUrl = ServerConstants.UPLOAD_AUDIO;

        MultipartRequest multipartRequest = new MultipartRequest(upLoadServerUrl, null, mimeType, multipartBody, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "Upload response : " + response);
                dismissCustomDialog();
                try
                {
                    if (recorddialog != null) recorddialog.dismiss();

                    JSONObject responseObj = new JSONObject(response);
                    String status = responseObj.getString("status");

                    if (status.equalsIgnoreCase("201"))
                    {
                        Toast.makeText(FollowupActivity.this, "Audio File uploaded!", Toast.LENGTH_LONG).show();
                        String upload_id = responseObj.getString("upload_id");
                        uploadedAudioFileId = upload_id;

                        //bt_doctor_followup_audio_link.setVisibility(View.GONE);
                        tv_audio_attachment_name.setVisibility(View.VISIBLE);
                        tv_audio_attachment_name.setText("audio_attachment.3gp");
                    } else
                    {
                        Toast.makeText(FollowupActivity.this, "Audio File failed!", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e)
                {
                    Toast.makeText(FollowupActivity.this, "Audio File failed!", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissCustomDialog();
                Toast.makeText(FollowupActivity.this, "Error : " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue volleyQueue = Volley.newRequestQueue(FollowupActivity.this);
        volleyQueue.add(multipartRequest);
    }

    private void dismissCustomDialog()
    {
        if (customDialog != null)
        {
            customDialog.stopLoading();
            customDialog.dismiss();
        }
    }

    private void showCustomDialog()
    {
        customDialog = new CustomProgressDialog(this);
        customDialog.setCancelable(false);
        customDialog.show();
    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData, String fileName) throws IOException
    {
        String doc_id = LocalDataManager.getInstance().get(PreferenceConstants.DOCID);

        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"audio_qry\"; filename=\"" + fileName + "::" + doc_id + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0)
        {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    private void stopTimer()
    {
        t.cancel();
        //tv_timer_task.setText("00:00");
    }

    public String CreateRandomAudioFileName(int string)
    {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string)
        {
            stringBuilder.append(RandomAudioFileName.charAt(random.nextInt(RandomAudioFileName.length())));
            i++;
        }
        return stringBuilder.toString();
    }

    private void startTimer()
    {
        cnt = 0;
        t = new CountDownTimer(Long.MAX_VALUE, 1000)
        {

            @Override
            public void onTick(long millisUntilFinished)
            {

                cnt++;
                String time = new Integer(cnt).toString();

                long millis = cnt;
                int seconds = (int) (millis / 60);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                tv_timer_task.setText(String.format("%d:%02d:%02d", minutes, seconds, millis));
            }

            @Override
            public void onFinish()
            {
            }
        };

        t.start();
    }

    private void recordingStatus()
    {
        Log.e(TAG, "recordingStatus");
        bt_stop.setVisibility(View.VISIBLE);

        bt_record.setVisibility(View.GONE);
        bt_pause.setVisibility(View.GONE);
        bt_reload.setVisibility(View.GONE);
        bt_play.setVisibility(View.GONE);
    }

    private void stopStatus()
    {
        Log.e(TAG, "stopStatus");
        bt_play.setVisibility(View.VISIBLE);
        bt_reload.setVisibility(View.VISIBLE);

        bt_pause.setVisibility(View.GONE);
        bt_stop.setVisibility(View.GONE);
        bt_record.setVisibility(View.GONE);
    }

    private void pauseStatus()
    {
        Log.e(TAG, "pauseStatus");
        bt_play.setVisibility(View.VISIBLE);
        bt_reload.setVisibility(View.VISIBLE);

        bt_pause.setVisibility(View.GONE);
        bt_stop.setVisibility(View.GONE);
        bt_record.setVisibility(View.GONE);
    }

    private void playingStatus()
    {
        Log.e(TAG, "playingStatus");
        bt_pause.setVisibility(View.VISIBLE);
        bt_reload.setVisibility(View.VISIBLE);

        bt_play.setVisibility(View.GONE);
        bt_stop.setVisibility(View.GONE);
        bt_record.setVisibility(View.GONE);
    }

    private void reloadingStatus()
    {
        Log.e(TAG, "reloadingStatus");
        bt_record.setVisibility(View.VISIBLE);

        bt_play.setVisibility(View.GONE);
        bt_reload.setVisibility(View.GONE);
        bt_stop.setVisibility(View.GONE);
        bt_pause.setVisibility(View.GONE);
    }

    private void showPopupWindowListenAudio(final String audioLink)
    {
        Log.e(TAG, "Playing : " + audioLink);

        mediaPlayerParent = new MediaPlayer();

        recorddialog = new Dialog(FollowupActivity.this);
        recorddialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        recorddialog.setContentView(R.layout.listen_record_layout);
        recorddialog.setCancelable(false);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(recorddialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        recorddialog.getWindow().setAttributes(lp);

        recorddialog.show();

        ImageView iv_close_dialog = (ImageView) recorddialog.findViewById(R.id.iv_close_audio_popup);
        iv_close_dialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recorddialog.dismiss();
                mediaPlayerParent.release();
            }
        });

        final FloatingActionButton bt_play = (FloatingActionButton) recorddialog.findViewById(R.id.bt_play);
        final FloatingActionButton bt_pause = (FloatingActionButton) recorddialog.findViewById(R.id.bt_pause);
        final FloatingActionButton bt_rewind = (FloatingActionButton) recorddialog.findViewById(R.id.bt_backword);
        final FloatingActionButton bt_forward = (FloatingActionButton) recorddialog.findViewById(R.id.bt_forward);

        bt_play.setVisibility(View.VISIBLE);
        bt_pause.setVisibility(View.GONE);

        bt_forward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int temp = (int) startTime;

                if ((temp + forwardTime) <= finalTime)
                {
                    startTime = startTime + forwardTime;
                    mediaPlayerParent.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped forward 5 seconds", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(getApplicationContext(), "Cannot jump forward 5 seconds", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bt_play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mediaPlayerParent.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        mp.release();
                        bt_play.setVisibility(View.VISIBLE);
                        bt_pause.setVisibility(View.GONE);
                    }
                });

                try
                {
                    mediaPlayerParent.setDataSource(audioLink);
                    //mediaPlayerParent.setDataSource( "https://geekanddummy.com/wp-content/uploads/2014/02/starting-engine-Ford-Mondeo-Mk-3-diesel.mp3" );
                    mediaPlayerParent.prepare();
                } catch (Exception e)
                {
                    Log.e(TAG, "Here : " + e.toString());
                }

                mediaPlayerParent.start();
                Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();

                finalTime = mediaPlayerParent.getDuration();
                startTime = mediaPlayerParent.getCurrentPosition();

                bt_pause.setVisibility(View.VISIBLE);
                bt_play.setVisibility(View.GONE);

            }
        });

        bt_pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mediaPlayerParent.pause();
                bt_play.setVisibility(View.VISIBLE);
                bt_pause.setVisibility(View.GONE);
            }
        });

        bt_rewind.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int temp = (int) startTime;

                if ((temp - backwardTime) > 0)
                {
                    startTime = startTime - backwardTime;
                    mediaPlayerParent.seekTo((int) startTime);
                    Toast.makeText(getApplicationContext(), "You have Jumped backward 5 seconds", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(getApplicationContext(), "Cannot jump backward 5 seconds", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                response = UtilityMethods.removeEscapedCharacters(response);
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
        postApi.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    /* Getiing parent query details*/
    private void buildParentQueryDetails(String response)
    {
        Log.e(TAG, "buildParentQueryDetails : " + response);
        try
        {
            JSONObject jsonRootObject = new JSONObject(response);

            String detailObj = jsonRootObject.getString(DETAIL_OBJECT);

            JSONObject jsonRootObjectReort = new JSONObject(detailObj);
            String queryObj = jsonRootObjectReort.getString(QUERY_OBJECT);

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


            reportsAttached = "";
            reportsNameAttached = "";
            JSONArray attachedReportDetailArray = jsonRootObjectReort.getJSONArray("fileID");
            for (int i = 0; i < attachedReportDetailArray.length(); i++)
            {
                JSONObject attachedReportObject = attachedReportDetailArray.getJSONObject(i);

                String reportId = attachedReportObject.getString("idlog");
                String reportPath = attachedReportObject.getString("reference_name");

                reportsAttached = reportsAttached + "," + reportId;
                reportsNameAttached = reportsNameAttached + "," + reportPath;
            }

            if (reportsAttached.length() > 0)
            {
                tableRow_Reports.setVisibility(View.VISIBLE);
                tv_reportsAttached.setText("View Report");
            }
        } catch (JSONException e)
        {
            Log.e("FollowupActivity", "Ex : " + e.toString());
        }
    }

    private void submitQuery()
    {
        for (int i = 0; i < selectedTestArray.size(); i++)
        {
            Log.e(TAG, "Name : " + selectedTestArray.get(i));
        }

        for (int i = 0; i < selectedLabTestIdArray.size(); i++)
        {
            Log.e(TAG, "ID : " + selectedLabTestIdArray.get(i));
        }

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
            params.put("question_id", followupQuestionId);
            params.put("diagnosis", diagnosis);
            params.put("medical_advice", medicalAd);
            params.put("medical_test", TextUtils.join(",", selectedLabTestIdArray));
            params.put("other_test", otherTest);
            params.put("test_detail", testDetails);
            params.put("uploadAudioFileId", uploadedAudioFileId);

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
            Log.e("1_2_3", "-> " + jsonRootObject);

            JSONArray jsonArray = jsonRootObject.optJSONArray(LAB_TEST_DETAIL_OBJECT);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int lab_id = jsonObject.getInt(LAB_ID);
                String lab_TestName = jsonObject.optString(LAB_TEST_NAME).toString();

                labIdList.add(String.valueOf(lab_id));
                if (containsExists("" + lab_id))
                {

                } else
                {
                    labTestNameList.add(new LabTestModel("" + lab_id, lab_TestName, false));
                }

            }

            Log.e(TAG, "Enabling edittext");
            btn_selectTest.setEnabled(true);
            btn_selectTest.setText("Select lab tests");

        } catch (Exception e)
        {
            Log.e(TAG, "Ex2 : " + e.toString());
        }
    }

    private boolean containsExists(String id)
    {
        for (int i = 0; i < selectedLabTestIdArray.size(); i++)
        {
            if (id.equalsIgnoreCase(selectedLabTestIdArray.get(i)))
            {
                return true;
            }
        }

        return false;
    }

    private boolean checkIfExists(String id)
    {
        for (int i = 0; i < selectedLabTest.size(); i++)
        {
            if (id.equalsIgnoreCase(selectedLabTest.get(i).getId()))
            {
                return true;
            }
        }

        return false;
    }

    private void createSelectTestDialog()
    {
        /*******************************
         * Multi-select-searchable list
         ******************************/
        for (int i = 0; i < labTestNameList.size(); i++)
        {
            LabTestModel model = labTestNameList.get(i);
            if (checkIfExists(model.getId()))
            {
                labTestNameList.get(i).setChecked(true);
            } else
            {
                labTestNameList.get(i).setChecked(false);
            }
        }

        Log.e(TAG, "Size          : " + labTestNameList.size());
        Log.e(TAG, "Selected Size : " + selectedLabTest.size());
        Log.e(TAG, "it worked?    : " + labTestNameList.get(0).isChecked());


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
                ArrayList<LabTestModel> list = labTestAdapter.getSelectedLabs();
                selectedTestArray.clear();
                selectedLabTestIdArray.clear();
                for (int i = 0; i < list.size(); i++)
                {
                    String name = list.get(i).getLab();
                    String id = list.get(i).getId();

                    selectedTestArray.add(name);
                    selectedLabTestIdArray.add(id);
                }
                mTestIdList = selectedLabTestIdArray;
                if (selectedTestArray.size() > 0)
                {
                    btn_selectTest.setText(TextUtils.join(",", selectedTestArray));
                }else {
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
                    LabTestModel model_added = labTestNameList.get(positions);
                    selectedLabTest.add(model_added);
                } else
                {
                    labTestAdapter.removeThisItem(positions);
                    LabTestModel model_removed = labTestNameList.get(positions);

                    for (int i = 0; i < selectedLabTest.size(); i++)
                    {
                        if (selectedLabTest.get(i).getId().equalsIgnoreCase(model_removed.getId()))
                        {
                            selectedLabTest.remove(i);
                            break;
                        }
                    }

                    for (int i = 0; i < selectedLabTestIdArray.size(); i++)
                    {
                        if (selectedLabTestIdArray.get(i).equalsIgnoreCase(model_removed.getId()))
                        {
                            selectedLabTestIdArray.remove(i);
                        }
                    }

                    for (int i = 0; i < selectedTestArray.size(); i++)
                    {
                        if (selectedTestArray.get(i).equalsIgnoreCase(model_removed.getLab()))
                        {
                            selectedTestArray.remove(i);
                        }
                    }
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
