package com.modastadoc.doctors.network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.modastadoc.doctors.R;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import java.util.Date;
import java.util.List;

/**
 * Created by vijay.hiremath on 13/10/16.
 */
public class AsyncWorker extends AsyncTask<AsyncParams, String, String>
{
    String TAG = AsyncWorker.class.getSimpleName();
    private ProgressDialog progress;
    private String response;
    private int REQUEST_NUMBER;
    public Context currentContext;
    public AsyncResponse delegate = null;
    public Boolean NETWORK_CONNECTED = false;
    // public LoadToast loadToast;
    private boolean isProgressEnabled = false;
    private String loadingMessage = "Loading..";
    public AlertDialog alertDialog;
    public String ACCESS_URL;
    public Date start_date, end_date;

    CustomProgressDialog customDialog;

    public AsyncWorker(Context context)
    {
        currentContext = context;
    }

    public void setProgress(String message)
    {
        isProgressEnabled = true;
        loadingMessage = message;
    }


    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();


        if (currentContext != null)
        {
            if (!NetworkChecker.getInstance(currentContext).haveNetworkConnection())
            {
                NETWORK_CONNECTED = false;
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(currentContext, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("No Network");
                builder.setMessage("You are not connected to network. Please connect and try again");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        sendResponse("false_nw_err", "");
                    }
                });
                builder.show();

                isProgressEnabled = false;

            } else
            {
                if (isProgressEnabled)
                {
                    showCustomDialog();
                }
            }
        }


    }

    private void showCustomDialog()
    {
        customDialog = new CustomProgressDialog(currentContext);
        customDialog.setCancelable(false);
        customDialog.show();
    }

    private void dismissCustomDialog()
    {
        if (customDialog != null)
        {
            customDialog.stopLoading();
            customDialog.dismiss();
        }
    }


    @Override
    protected String doInBackground(AsyncParams... params)
    {
        try
        {
            ACCESS_URL = params[0].URL;
            String content = params[0].REQUEST_BODY;
            AsyncConstants requestType = params[0].REQUEST_TYPE;
            boolean isHeaderRequired = params[0].isHeaderRequired;
            this.REQUEST_NUMBER = params[0].REQUEST_NUMBER;
            boolean isJson = params[0].isJSON;
            List namevalue = params[0].formEntity;

            start_date = new Date();

            if (NetworkChecker.getInstance(currentContext).haveNetworkConnection())
            {
                if (requestType == AsyncConstants.POST_REQUEST)
                {
                    HttpRequestWorker mWorker = new HttpRequestWorker();
                    if (!isJson)
                    {
                        mWorker = new HttpRequestWorker(namevalue);
                    }

                    response = mWorker.PostRequest(ACCESS_URL, content);

                } else if (requestType == AsyncConstants.GET_REQUEST)
                {
                    HttpRequestWorker mWorker = new HttpRequestWorker();
                    response = mWorker.GetRequest(ACCESS_URL);
                } else
                {
                    Log.e("ASYNC_ERROR", "NOT FOUND");
                }
            } else
            {
                Alert alert = new Alert(currentContext);
                alert.showSimpleAlert("No Network", "You are not connected to network. Please connect and try again");
                return "false";
            }

            return response;

        } catch (Exception ex)
        {
            return "Exception";
        }
    }

    @Override
    protected void onPostExecute(String result)
    {
        Log.e( TAG , "" + result );
        if (isProgressEnabled)
        {
            dismissCustomDialog();
        }
        try
        {
            sendResponse(result, ACCESS_URL);
        } catch (Exception e)
        {
            e.printStackTrace();
            result = "" + result;
            sendResponse(result, ACCESS_URL);
        }
    }

    private void sendResponse(String result, String URL)
    {
        end_date = new Date();

        long millse = end_date.getTime() - start_date.getTime();
        long mills = Math.abs(millse);
        int Hours = (int) (mills / (1000 * 60 * 60));
        int Mins = (int) (mills / (1000 * 60)) % 60;
        long Secs = (int) (mills / 1000) % 60;


        delegate.onResponseFromAPI(result, REQUEST_NUMBER);
    }
}
