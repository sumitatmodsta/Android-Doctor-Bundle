package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by vivek.c on 19/10/16.
 */
public class ConsultationFeeActivity extends AppCompatActivity {
    private final String TAG = "ConsultationFeeActivity";
    AlertDialog alertDialog;
    TextView tv_Head, tv_GeneralfeeIndian, tv_SpeciafeeIndian, tv_GeneralfeeInternational, tv_SpecialfeeInternational;

    /*private TextView title, videoIndian, videoInternational, queryPoolIndian, queryPollInternationl,
            queryDirectIndian, queryDirectInternational;*/
    private TextView title, videoIndian, queryPoolIndian, queryDirectIndian;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consut_fee);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Consultation Fee");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initialiseWidgets();
        generateFees();
    }

    private void initialiseWidgets() {
        title = (TextView) findViewById(R.id.title);
        videoIndian = (TextView) findViewById(R.id.video_indian);
        //videoInternational = (TextView) findViewById(R.id.video_international);
        queryPoolIndian = (TextView) findViewById(R.id.query_pool_indian);
        //queryPollInternationl = (TextView) findViewById(R.id.query_pool_international);
        queryDirectIndian = (TextView) findViewById(R.id.query_direct_indian);
        //queryDirectInternational = (TextView) findViewById(R.id.query_direct_international);



        /*tv_Head = (TextView)findViewById(R.id.txtHead);
        tv_GeneralfeeIndian = (TextView)findViewById(R.id.generalfeeindian);
        tv_GeneralfeeInternational =(TextView)findViewById(R.id.generalFeeinternational);
        tv_SpeciafeeIndian = (TextView)findViewById(R.id.specialfeeindian);
        tv_SpecialfeeInternational = (TextView)findViewById(R.id.specialFeeinternational);*/

    }

    private void showDialog(String message) {

        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = new ProgressDialog(ConsultationFeeActivity.this);
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void dismissDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void generateFees() {
        showDialog("Loading...");

        HashMap<String, String> params = new HashMap<>();

        PostApi postApi = new PostApi(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "generateFees --> onResponse --> " + response);
                buildFees(response);
                dismissDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dismissDialog();
                Log.i(TAG, "generateFees --> onErrorResponse --> " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Something went wrong ,Please check your Internet connection", Toast.LENGTH_LONG).show();
                Log.v("FollowupResponseError", "Error Response : " + error.toString());
            }
        }, params, ServerConstants.CONSULTATION_FEE);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
    }

    private void buildFees(String response) {
        try {
            JSONObject object = new JSONObject(response);
            String t = object.optString("head-text");
            title.setText(t);
            if(object.has("fees")) {
                JSONObject o = object.optJSONObject("fees");
                if(o.has("query")) {
                    JSONObject query = o.optJSONObject("query");
                    if(query.has("pool")) {
                        JSONObject pool = query.optJSONObject("pool");
                        String ind = pool.optString("indian");
                        //String intl = pool.optString("international");
                        if(!ind.equalsIgnoreCase("null")) {
                            queryPoolIndian.setText(ind);
                        }
                        /*if(!intl.equalsIgnoreCase("null")) {
                            queryPollInternationl.setText(intl);
                        }*/
                    }

                    if(query.has("direct")) {
                        JSONObject direct = query.optJSONObject("direct");
                        String ind = direct.optString("indian");
                        //String intl = direct.optString("international");
                        if(!ind.equalsIgnoreCase("null")) {
                            queryDirectIndian.setText(ind);
                        }
                        /*if(!intl.equalsIgnoreCase("null")) {
                            queryDirectInternational.setText(intl);
                        }*/
                    }
                }

                if(o.has("video")) {
                    JSONObject video = o.optJSONObject("video");
                    String ind = video.optString("indian");
                    //String intl = video.optString("international");
                    if(!ind.equalsIgnoreCase("null")) {
                        videoIndian.setText(ind);
                    }
                    /*if(!intl.equalsIgnoreCase("null")) {
                        videoInternational.setText(intl);
                    }*/
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void buildFees2(String response) {
        try {
            JSONObject jsonRootObject = new JSONObject(response);
            String headText = jsonRootObject.getString("head-text");
            tv_Head.setText(headText);
            String fees = jsonRootObject.getString("fees");
            JSONObject feesObject = new JSONObject(fees);

            String feesObjectGp = feesObject.getString("gp");
            JSONObject gpObject = new JSONObject(feesObjectGp);

            String indiaGp = gpObject.getString("indian");
            tv_GeneralfeeIndian.setText(indiaGp+"/-");

            String interGp = gpObject.getString("inter");
            tv_GeneralfeeInternational.setText(interGp+"/-");

            String feesObjectSpecial = feesObject.getString("splt");
            JSONObject spObject = new JSONObject(feesObjectSpecial);
            String indiaSp = spObject.getString("indian");
            tv_SpeciafeeIndian.setText(indiaSp+"/-");
            String interSp = spObject.getString("inter");
            tv_SpecialfeeInternational.setText(interSp+"/-");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
