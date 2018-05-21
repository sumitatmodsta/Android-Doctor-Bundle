package com.modastadoc.doctors.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.UtilityMethods;
import com.modastadoc.doctors.network.PostApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class NewQueryActivity extends AppCompatActivity
{

    TextView tv_OrderId, tv_Question, tv_Date, tv_Pname, tv_Weight, tv_Height, tv_Diagnosis, tv_Medication, tv_Allergy, tv_reportsAttached;
    TextView tv_labreportsAttached;
    Button btn_Accept;
    Button bt_listen_audio;
    TableRow tableRow_Reports;
    TableRow tr_labReportRow;
    private static final String QUERY_OBJECT = "questionObj";
    private static final String QUERY_ID = "question_id";
    private static final String QUERY = "question";
    private static final String QUERY_DATE = "date";
    private static final String REPORTS_ATTACHED = "file_ids";
    private static final String PATIENT_OBJECT = "patientdetailObj";
    private static final String PATIENT_NAME = "fname";
    private static final String WEIGHT = "weight";
    private static final String HEIGHT = "height";
    private static final String DIAGNOSIS = "diagnosis";
    private static final String PREV_MEDICATION = "med_desc";
    private static final String ALLERGY = "allergy_desc";
    private static final String AUDIO_FILE = "patient_audio_file";

    String question_Id, reportsAttached, labReportsAttached;
    AlertDialog alertDialog;
    String audio_link;

    String uploadedAudioFileId = "";

    String TAG = NewQueryActivity.class.getSimpleName();

    String labReportIdList   = "";
    String labReportNameList = "";

    String reportsIdList = "";
    String reportsNameList = "";

    /************************/
    private MediaPlayer mediaPlayerParent;
    private double startTime = 0;
    private double finalTime = 0;
    private int forwardTime = 5000;
    private int backwardTime = 5000;
    Dialog recorddialog;
    /************************/

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_query);
        initialiseWidgets();
        Intent intent = getIntent();
        question_Id = intent.getStringExtra("question_Id");
        Log.e(TAG, "Question ID : " + question_Id);

        generateQueryDetail(question_Id);

        btn_Accept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                acceptQueryAPIcall();
            }
        });

        tv_reportsAttached.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = View_Reports.createIntent( NewQueryActivity.this , reportsIdList , reportsNameList , "upload");
                startActivity(intent);
            }
        });

        tv_labreportsAttached.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = View_Reports.createIntent( NewQueryActivity.this , labReportIdList , labReportNameList , "lab" );
                startActivity( intent );
            }
        });
    }


    private void queryIntent()
    {
        Intent i = new Intent(getApplicationContext(), QueryAnswerActivity.class);
        i.putExtra("question_Id", question_Id);
        i.putExtra("uploadAudioFileId", uploadedAudioFileId);
        startActivity(i);

        finish();
    }

    private void acceptQueryAPIcall()
    {
        String URL = ServerConstants.DOMAIN + ServerConstants.ACCEPT_QUERY + question_Id;

        HashMap<String, String> params = new HashMap<String, String>();
        showDialog("Loading..");
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.v("Query Accepted", "Response : " + response);
                response = UtilityMethods.removeEscapedCharacters( response );
                dismissDialog();
                queryIntent();

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                dismissDialog();
                backToHome();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    private void backToHome()
    {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
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
        btn_Accept = (Button) findViewById(R.id.btn_Accept);
        tableRow_Reports = (TableRow) findViewById(R.id.reportRow);
        tv_reportsAttached = (TextView) findViewById(R.id.reportsAttached);
        bt_listen_audio = (Button) findViewById(R.id.bt_listen_audio);
        tr_labReportRow = (TableRow) findViewById(R.id.tr_labReportRow);
        tv_labreportsAttached = (TextView) findViewById(R.id.tv_labreportsAttached);
    }

    private void showDialog(String message)
    {

        if (alertDialog != null)
        {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(NewQueryActivity.this);
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

    private void generateQueryDetail(String question_Id)
    {
        String URL = ServerConstants.DOMAIN + ServerConstants.UNANSWERED_QUERY_DETAIL + question_Id;
        Log.e(TAG, "generateQueryDetail : " + URL);

        HashMap<String, String> params = new HashMap<String, String>();
        showDialog("Loading..");

        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e(TAG, "generateQueryDetail Response : " + response);
                response = UtilityMethods.removeEscapedCharacters(response);
                buildQueryDetails(response);
                dismissDialog();
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "generateQueryDetail Error Response : " + error.toString());
                dismissDialog();
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();

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

            String queryObj = jsonRootObject.getString(QUERY_OBJECT);
            Log.v("queryObj", queryObj);
            JSONObject queryJson = new JSONObject(queryObj);

            String question_id = queryJson.getString(QUERY_ID);
            tv_OrderId.setText("Order Id:#" + question_id);

            String question = queryJson.getString(QUERY);

            tv_Question.setText(getStringWithoutSpecialChar(question));
            String date = queryJson.getString(QUERY_DATE);
            tv_Date.setText("Posted On " + date);
            String diagnosis = queryJson.getString(DIAGNOSIS);
            if (!(diagnosis.equalsIgnoreCase("null")))
            {
                tv_Diagnosis.setText(getStringWithoutSpecialChar(diagnosis));
            }

//            reportsAttached = queryJson.getString(REPORTS_ATTACHED);
//            if (!(reportsAttached.equalsIgnoreCase("null")))
//            {
//                if (reportsAttached.trim().length() > 0)
//                {
//                    tableRow_Reports.setVisibility(View.VISIBLE);
//                    tv_reportsAttached.setText("View Reports");
//                }
//            }

            try
            {

                JSONArray uploadedReportsArray = jsonRootObject.getJSONArray("fileIdObjArr");
                for( int i = 0 ; i < uploadedReportsArray.length(); i++ )
                {
                    JSONObject uploadReportObject = uploadedReportsArray.getJSONObject(i);
                    String id = uploadReportObject.getString("idlog");
                    String name = uploadReportObject.getString("reference_name");

                    if( name == null || name.equalsIgnoreCase("null"))
                    {
                        name = "uploaded_report_"+ i;
                    }

                    reportsIdList = reportsIdList + "," + id;
                    reportsNameList = reportsNameList + "," + name;
                }
            }
            catch (Exception e )
            {
                Log.e( TAG , "Ex : " + e.toString() );
            }

            if ( reportsIdList.length() > 0 )
            {
                if (reportsIdList.trim().length() > 0)
                {
                    tableRow_Reports.setVisibility(View.VISIBLE);
                    tv_reportsAttached.setText("View Reports");
                }
            }


            try
            {
                JSONArray labReportsArray = jsonRootObject.getJSONArray("labfileIdObjArr");
                for( int i = 0 ; i < labReportsArray.length() ; i++ )
                {
                    JSONObject labReportObject = labReportsArray.getJSONObject(i);
                    String id = labReportObject.getString("uploadid");
                    String name = labReportObject.getString("filename");
                    labReportIdList = labReportIdList + "," + id;
                    labReportNameList = labReportNameList + "," + name;
                }
            }
            catch( Exception e )
            {
                Log.e( TAG , "Ex : " + e.toString() );
            }


            if ( labReportIdList.length() > 0 )
            {
                if (labReportIdList.trim().length() > 0)
                {
                    tr_labReportRow.setVisibility(View.VISIBLE);
                    tv_labreportsAttached.setText("View Lab Reports");
                }
            }

            String patientDetailObj = jsonRootObject.getString(PATIENT_OBJECT);
            JSONObject patientJson  = new JSONObject(patientDetailObj);
            String name = patientJson.getString(PATIENT_NAME);
            tv_Pname.setText(getStringWithoutSpecialChar(name));
            String weight = patientJson.getString(WEIGHT);
            if (!(weight.equalsIgnoreCase("null")))
            {
                tv_Weight.setText(weight + " kgs");
            }
            String height = patientJson.getString(HEIGHT);
            if (!(height.equalsIgnoreCase("null")))
            {
                tv_Height.setText(height);
                tv_Height.setText(getAppropriateHeight(height));
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

            JSONObject audioJson = jsonRootObject.getJSONObject(AUDIO_FILE);

            audio_link = audioJson.getString("patient_audio_file_link");
            if( audio_link.length() > 0 )
            {
                bt_listen_audio.setVisibility(View.VISIBLE);
                bt_listen_audio.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //startActivityForResult(AudioQueryActivity.createIntent(NewQueryActivity.this, audio_link), 12 );
                        showPopupWindowListenAudio( audio_link);
                    }
                });
            }
        }
        catch (JSONException e)
        {
            Log.e( TAG , "JSON : " + e.toString() );
        }
    }

    private String getAppropriateHeight(String param ){

        ArrayList<String> heightList = UtilityMethods.fillHeightList();

        param = param + " cms";
        int index = -1;
        for( int i = 0 ; i < heightList.size() ; i++ )
        {
            if (heightList.get(i).contains(param))
            {
                return heightList.get(i);
            }
        }

        return "";
    }

    private void showPopupWindowListenAudio( final String audioLink )
    {
        mediaPlayerParent = new MediaPlayer();

        recorddialog = new Dialog(NewQueryActivity.this);
        recorddialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        recorddialog.setContentView(R.layout.listen_record_layout);
        recorddialog.setCancelable(false);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(recorddialog.getWindow().getAttributes());
        lp.width   = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height  = WindowManager.LayoutParams.WRAP_CONTENT;
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

        final FloatingActionButton bt_play    = (FloatingActionButton) recorddialog.findViewById(R.id.bt_play);
        final FloatingActionButton bt_pause   = (FloatingActionButton) recorddialog.findViewById(R.id.bt_pause);
        final FloatingActionButton bt_rewind  = (FloatingActionButton) recorddialog.findViewById(R.id.bt_backword);
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
                try
                {
                    mediaPlayerParent.setDataSource(audioLink);
                    mediaPlayerParent.prepare();
                }
                catch (Exception e)
                {
                    Log.e( TAG , "Here : " + e.toString() );
                }

                mediaPlayerParent.start();
                Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT).show();

                finalTime = mediaPlayerParent.getDuration();
                startTime = mediaPlayerParent.getCurrentPosition();

                bt_pause.setVisibility(View.VISIBLE);
                bt_play.setVisibility(View.GONE);

                mediaPlayerParent.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        bt_play.setVisibility(View.VISIBLE);
                        bt_pause.setVisibility(View.GONE);
                    }
                });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ( requestCode == 12 )
        {
            if (resultCode == Activity.RESULT_OK)
            {
                uploadedAudioFileId = data.getStringExtra("uploadedAudioFileId");
                Log.e( TAG , "uploadedAudioFileId : " + uploadedAudioFileId );
            }

            if (resultCode == Activity.RESULT_CANCELED)
            {
                //Write your code if there's no result
            }
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
