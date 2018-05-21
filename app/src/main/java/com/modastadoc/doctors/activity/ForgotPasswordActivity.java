package com.modastadoc.doctors.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.android.volley.Request;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.network.Alert;
import com.modastadoc.doctors.network.VolleyConstants;
import com.modastadoc.doctors.network.VolleyRequestHandler;
import com.modastadoc.doctors.network.VolleyResponse;

import org.json.JSONObject;

/**
 * Created by vivek.c on 26/10/16.
 */
public class ForgotPasswordActivity extends AppCompatActivity implements VolleyResponse{
    private EditText mUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_forgot);

        mUserName   =   (EditText) findViewById(R.id.email);
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgot();
            }
        });
    }

    private void forgot() {
        String email = mUserName.getText().toString().trim();
        if(!email.isEmpty()) {
            requestPassword(email);
        }else {
            mUserName.setError(getResources().getString(R.string.username_error));
        }
    }

    private void requestPassword(String username){
        if(AppCoreUtil.isNetworkAvailable()) {
            AppCoreUtil.hideKeyboard(this);
            String URL = ServerConstants.FORGOT_PASSWORD + "user_login=" + username;
            VolleyRequestHandler mHandler = new VolleyRequestHandler(URL, 1, Request.Method.GET, VolleyConstants.STRING_REQUEST);
            mHandler.delegate = this;
            mHandler.setProgressEnabled("Loading..", this);
            mHandler.performVolleyRequest();
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    @Override
    public void onResponseReceived(String response, int requestCount) {
        if(response != null && !response.equals("") && !response.equals("false")){
            try{

                JSONObject mResponseObj =   new JSONObject(response);
                if(mResponseObj.has("status")){
                    String status   =   mResponseObj.getString("status");
                    Alert alert =   new Alert(this);
                    if(status.equals("ok")){
                        String message  =   "Link for password reset has been emailed to you. Please check your email.";
                        if(mResponseObj.has("msg")) {
                            message = mResponseObj.getString("msg");
                        }
                        alert.showSimpleAlert("Success",message);
                    }else{
                        alert.showSimpleAlert("Sorry","We couldn't find your email.");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Alert alert =   new Alert(this);
            alert.showSimpleAlert("Sorry","We couldn't find your email.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}