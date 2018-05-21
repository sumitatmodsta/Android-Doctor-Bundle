package com.modastadoc.doctors.network;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class VolleyRequestHandler {

    private String TAG = VolleyRequestHandler.class.getSimpleName();

    private String ACCESS_URL       =   null;
    private int REQUEST_COUNT       =   0;
    private int REQUEST_METHOD      =   0;
    private int REQUEST_TYPE        =   2;
    private Map<String, String>  REQUEST_PARAMS;
    private JSONObject REQUEST_OBJECT;
    private boolean isProgressEnabled    =   false;
    private String progressIndicatorText =   "Loading";
    private Context mContext;
    private AlertDialog progressDialog;
    private boolean NETWORK_CONNECTED   =   true;
    public VolleyResponse delegate      =   null;
    public Date start_date,end_date;

    CustomProgressDialog customDialog;

    public VolleyRequestHandler(String url,int requestCount,int requestMethod,VolleyConstants typeOfRequest){
        ACCESS_URL      =   url;
        REQUEST_COUNT   =   requestCount;
        REQUEST_METHOD  =   requestMethod;
        REQUEST_OBJECT  =   null;
        if(typeOfRequest == VolleyConstants.STRING_REQUEST){
            REQUEST_TYPE    =   1;
        }else{
            REQUEST_TYPE    =   2;
        }
    }
    public VolleyRequestHandler(String url,int requestCount,int requestMethod,Map<String, String> requestParams){
        ACCESS_URL      =   url;
        REQUEST_COUNT   =   requestCount;
        REQUEST_METHOD  =   requestMethod;
        REQUEST_PARAMS  =   requestParams;
        REQUEST_TYPE    =   1;
    }

    public VolleyRequestHandler(String url,int requestCount,int requestMethod,JSONObject requestParams){
        ACCESS_URL      =   url;
        REQUEST_COUNT   =   requestCount;
        REQUEST_METHOD  =   requestMethod;
        REQUEST_OBJECT  =   requestParams;
        REQUEST_TYPE    =   2;
    }



    public void setProgressEnabled(String progressIndicatorText,Context mContext) {
        this.isProgressEnabled = true;
        this.progressIndicatorText=progressIndicatorText;
        this.mContext=mContext;
    }

    public void performVolleyRequest(){

        initprogressbar();
        if(mContext != null)
        {
            if(!NetworkChecker.getInstance(mContext).haveNetworkConnection())
            {
                NETWORK_CONNECTED   =   false;
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("No Network");
                builder.setMessage("You are not connected to network. Please connect and try again");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        sendResponse("false_nw_err",ACCESS_URL);
                    }
                });

                builder.show();
            }
        }

        if(NETWORK_CONNECTED)
        {
            Log.e("*PERFORMANCE_LOG_E", "" + ACCESS_URL);
            start_date  =   new Date();

            switch (REQUEST_TYPE)
            {
                case 1:
                    if (REQUEST_METHOD == Request.Method.GET)
                    {
                        doStringGetRequest();
                    }
                    else
                    {
                        doStringPostRequest();
                    }
                    break;

                case 2:
                    doJSONRequest();
                    break;

                default:
                    hideprogressbar();
                    break;

            }
        }
        else
        {
            hideprogressbar();
        }
    }


    private void doStringGetRequest()
    {
        StringRequest stringRequest = new StringRequest(REQUEST_METHOD, ACCESS_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.e( TAG , "response : " + response);
                hideprogressbar();
                sendResponse(response,ACCESS_URL);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e( TAG , "onErrorResponse : " + error.toString());

                error.printStackTrace();
                hideprogressbar();

                sendResponse("false",ACCESS_URL);
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                4000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    private void doStringPostRequest() {

        StringRequest stringRequest = new StringRequest(REQUEST_METHOD, ACCESS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e("VOLLEY_RESP", "" + response);
                        hideprogressbar();

                        sendResponse(response,ACCESS_URL);
                    }

                },
                new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                System.out.println("Something went wrong!");
                error.printStackTrace();
                NetworkResponse mResp   =   error.networkResponse;
                Iterator mit=mResp.headers.keySet().iterator();
                while (mit.hasNext()){
                    Log.e("VOLLEY_ERROR_MAP",""+mit.next().toString());
                    Log.e("VOLLEY_ERROR_DATA", ""+mResp.headers.get(mit.next().toString()));
                }
                hideprogressbar();
                sendResponse("false",ACCESS_URL);
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String,String> params = new HashMap<String, String>();
                params  =   REQUEST_PARAMS;
                Log.e("TEST_PARAMS",params.get("nonce").toString());
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                return headers;
            };
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                4000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    private void doJSONRequest()
    {
        Log.e("VOLLEY_BEFORE", "" + isProgressEnabled + " " + progressIndicatorText);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(REQUEST_METHOD,ACCESS_URL,REQUEST_OBJECT,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        System.out.println(response);
                        Log.e("VOLLEY_RESP", "" + response);
                        hideprogressbar();

                        sendResponse(response.toString(),ACCESS_URL);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                        hideprogressbar();

                        sendResponse("false",ACCESS_URL);
                    }
                });

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                4000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance().getRequestQueue().add(jsObjRequest);
    }

    private void initprogressbar()
    {
        if(isProgressEnabled)
        {
            showCustomDialog();
        }
    }

    private void hideprogressbar()
    {
        dismissCustomDialog();
    }

    private void showCustomDialog()
    {
        customDialog = new CustomProgressDialog( mContext );
        customDialog.setCancelable(false);
        customDialog.show();
    }

    private void dismissCustomDialog()
    {
        if( customDialog != null )
        {
            customDialog.stopLoading();
            customDialog.dismiss();
        }
    }

    private void sendResponse(String response,String URL)
    {
        try
        {
            end_date    =   new Date();
            long millse = end_date.getTime() - start_date.getTime();
            long mills = Math.abs(millse);
            int Hours = (int) (mills/(1000 * 60 * 60));
            int Mins = (int) (mills/(1000*60)) % 60;
            long Secs = (int) (mills / 1000) % 60;
            String time_diff    =   ""+Hours+" Hours :  "+Mins+" Minutes : "+Secs+" Seconds";
        }
        catch( Exception e)
        {
            Log.e( TAG , "Gotcha! " + e.toString() );
        }

        delegate.onResponseReceived(response.toString(), REQUEST_COUNT);
    }


}
