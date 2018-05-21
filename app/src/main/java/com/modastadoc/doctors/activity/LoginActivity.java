package com.modastadoc.doctors.activity;


import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.BuildConfig;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CustomProgressDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 123;
    private Button mSignIn;

    private EditText etEmail, etPassword;

    private EditText mUsername, mPassword;
    AlertDialog alertDialog;
    RelativeLayout rel_ParentLayout;
    TextView tv_FogotPassword;

    CustomProgressDialog customDialog;

    private final String USER_NAME = "user_nicename";
    private final String USER_ROLE = "user_role";
    private final String USER_EMAIL = "user_email";
    private final String USER_TOKEN = "token";
    private final String DOC_ID = "id";

    String TERMS_AND_CONDITIONS_URL = "https://www.modasta.com/terms-of-use/";
    String passwordToSctore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_new);

        etEmail = (EditText) findViewById(R.id.email);
        etPassword = (EditText) findViewById(R.id.password);

        TextView tvTermsConditions = (TextView) findViewById(R.id.terms_conditions);
        String text = "<font color=#ffffff>By Clicking Sign In, you agree to our</font> <font color=#4ba4cb>Terms and Conditions and Privacy Policy</font>";

        tvTermsConditions.setText(Html.fromHtml(text));

        tvTermsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(TERMS_AND_CONDITIONS_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        /*TextView tv_sign_in_modasta = (TextView) findViewById(R.id.tv_sign_in_modasta);
        tv_sign_in_modasta.setText(R.string.login_sign_in_with_modasta);*/

        findViewById(R.id.signin).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signin:
                signIn();
                break;
            case R.id.forgot_password:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;
            default:
                break;
        }
    }

    private void signIn() {
        try {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if(!email.isEmpty() && !pass.isEmpty()) {
                AppCoreUtil.hideKeyboard(this);
                login(email, pass);
            }else {
                if(email.isEmpty())
                    etEmail.setError(getResources().getString(R.string.username_error));
                if(pass.isEmpty())
                    etPassword.setError(getResources().getString(R.string.password_error));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void login(String email, final String password) {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            HashMap<String, String> params = new HashMap<>();
            params.put("email", email);
            params.put("password", password);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.log("Login response", response);
                    passwordToSctore = password;
                    buildLoginResponse(response);
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AppCoreUtil.log("Login Error", error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(LoginActivity.this, "Log in Error ,Check username/password");
                }

            }, params, ServerConstants.LOGIN);
            RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void buildLoginResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);

            ArrayList<String> roles = new ArrayList<>();
            JSONArray role = obj.optJSONArray(USER_ROLE);
            if(role != null) {
                int size = role.length();
                for(int i=0; i<size; i++) {
                    roles.add(role.get(i).toString());
                }
            }


            /***********************************
             *
             * Decide user_permission level.
             *
             ************************************/
            boolean isIMA = false, isPhysician = false;
            int s = roles.size();
            for(int i=0; i<s; i++) {
                if (roles.get(i).equalsIgnoreCase("ima-ka")) {
                    isIMA = true;
                }
                if (roles.get(i).equalsIgnoreCase("physician")) {
                    isPhysician = true;
                }
            }

            if (isIMA && isPhysician) {
                LocalDataManager.getInstance().set(PreferenceConstants.USER_TYPE, PreferenceConstants.BOTH);
            } else if (isPhysician) {
                LocalDataManager.getInstance().set(PreferenceConstants.USER_TYPE, PreferenceConstants.ONLY_PHYSICIAN);
            } else if (isIMA) {
                LocalDataManager.getInstance().set(PreferenceConstants.USER_TYPE, PreferenceConstants.ONLY_IMAKA);
            }
            /************************************/

            String r = role.get(0).toString();
            if (r.equalsIgnoreCase("physician") || r.equalsIgnoreCase("ima-ka")) {
                String user_email = obj.optString(USER_EMAIL);
                String user_token = obj.optString(USER_TOKEN);
                String doc_id = obj.optString(DOC_ID);
                String name = obj.optString("user_display_name");

                Calendar c = Calendar.getInstance();

                SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = myFormat.format(c.getTime());

                AppCoreUtil.log("LoginActivity", "token", "Bearer " + user_token);

                LocalDataManager.getInstance().set(PreferenceConstants.Name, name);
                LocalDataManager.getInstance().set(PreferenceConstants.Email, user_email);
                LocalDataManager.getInstance().set(PreferenceConstants.Token, "Bearer " + user_token);
                LocalDataManager.getInstance().set(PreferenceConstants.DOCID, doc_id);
                LocalDataManager.getInstance().set(PreferenceConstants.KEY_GENERATE_DATE, formattedDate);
                LocalDataManager.getInstance().set(PreferenceConstants.PASSWORD, passwordToSctore);


                /*************************
                 * Get cookie from server
                 *************************/
                getCookie(user_email, passwordToSctore);
                /************************/

            } else {
                AppCoreUtil.showToast(this, "Access Denied");
            }
        }catch (Exception e) {
            e.printStackTrace();
            AppCoreUtil.log("Login Exception", e.toString());
        }
    }

    private void getCookie(String email, String pass) {
        AppDialogUtil.showCustomDialog(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("username", email);
        params.put("password", pass);
        PostApi postApi = new PostApi(new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                AppDialogUtil.dismissCustomDialog();
                AppCoreUtil.log("Login cookie response", response);
                buildCookieResponse(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                AppDialogUtil.dismissCustomDialog();
                AppCoreUtil.log("Login cookie error", error.toString());
                AppCoreUtil.showToast(LoginActivity.this, "Log in Error ,Check username/password");
            }

        }, params, ServerConstants.LOGIN_FOR_COOKIE);

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());
        volleyQueue.add(postApi);
        /*postApi.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
    }

    private void buildCookieResponse(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if(obj.has("cookie")) {
                String cookie = obj.optString("cookie");
                LocalDataManager.getInstance().set(PreferenceConstants.cookie, cookie);

                JSONObject o = obj.optJSONObject("user");
                if(o != null && o.length() > 0) {
                    LocalDataManager.getInstance().set(PreferenceConstants.USER_PREFERRED_LANGUAGE, o.optString("language"));
                    LocalDataManager.getInstance().set(PreferenceConstants.PIC, o.optString("avatar"));
                }

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }else {
                AppCoreUtil.showToast(this, "Access denied. Bad Cookie.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppCoreUtil.showToast(this, "Access denied. Bad Cookie.");
        }
    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onStop()
    {
        super.onStop();
    }


}