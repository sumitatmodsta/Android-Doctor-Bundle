package com.modastadoc.doctors.activity;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.App;
import com.modastadoc.doctors.BuildConfig;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.adapter.TabAdapter;
import com.modastadoc.doctors.common.constants.PreferenceConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.constants.UserConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.fragment.ProfileFragment;
import com.modastadoc.doctors.gcm.GCMContracts;
import com.modastadoc.doctors.gcm.GCMInfoHandler;
import com.modastadoc.doctors.network.AsyncConstants;
import com.modastadoc.doctors.network.AsyncParams;
import com.modastadoc.doctors.network.AsyncResponse;
import com.modastadoc.doctors.network.AsyncWorker;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.notify.AppUpdate;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class HomeActivity extends AppCompatActivity implements AsyncResponse {
    String TAG = HomeActivity.class.getSimpleName();
    final int REQUEST_CODE = 5;
    private TabAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private static final String USER_NAME = "user_nicename";
    private static final String USER_ROLE = "user_role";
    private static final String USER_EMAIL = "user_email";
    private static final String USER_TOKEN = "token";
    private static final String DOC_ID = "id";
    String passwordToSctore;
    android.app.AlertDialog alertDialog;
    String slug;

    private TabLayout tabLayout;
    private Toolbar mToolbar;

    private int tabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("DoctorApp");

        mSectionsPagerAdapter = new TabAdapter(getSupportFragmentManager());

        LocalDataManager.getInstance().set(PreferenceConstants.VERSION_CODE, "" + BuildConfig.VERSION_NAME);


        String thisisit = LocalDataManager.getInstance().get(PreferenceConstants.LoginPrefrences);
        String keyGeneratedDate = LocalDataManager.getInstance().get(PreferenceConstants.KEY_GENERATE_DATE);
        String user_name = LocalDataManager.getInstance().get(PreferenceConstants.Name);
        String password = LocalDataManager.getInstance().get(PreferenceConstants.PASSWORD);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
        String todaysDate = myFormat.format(c.getTime());

        int days = days(keyGeneratedDate, todaysDate);

        Log.e( TAG , "Days since last login : " + days );
        if ( days > 360 )
        {

            new android.app.AlertDialog.Builder( HomeActivity.this )
                    .setTitle("Session Expired")
                    .setMessage("User session has expired. Please Login again")
                    .setCancelable(false)
                    .setPositiveButton("Login", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent loginIntent = new Intent( HomeActivity.this, LoginActivity.class );
                            startActivity(loginIntent);
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.new_home_normal);
        tabLayout.getTabAt(1).setIcon(R.drawable.new_my_acnt_normal);
        //tabLayout.getTabAt(2).setIcon(R.drawable.new_doc_connect_normal);
        tabLayout.getTabAt(2).setIcon(R.drawable.new_more_normal);


        String doc_use = LocalDataManager.getInstance().get(PreferenceConstants.FIRST_TIME_LOGIN_DOC_CONNECT);
        String user_type = LocalDataManager.getInstance().get(PreferenceConstants.USER_TYPE);
        Log.e(TAG, "User type : " + user_type);
        Log.e(TAG, "Doc  user : " + doc_use);

        tabLayout.getTabAt(0).select();
        tabLayout.getTabAt(0).setIcon(R.drawable.new_home_pressed);

        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        tabLayout.setSelectedTabIndicatorHeight(0);


        /***********************************
         * checking bundle has gcm things
         ***********************************/
        slug = GCMContracts.NEW_QUERY_NOTIFY; // default value
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey(GCMContracts.GCM_SLUG)) {
                slug = bundle.getString(GCMContracts.GCM_SLUG);
            }
        }
        /***********************************/

        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        mToolbar.setTitle("DoctorApp");
                        if (tab.getText().equals("Home")) {
                            tab.setIcon(R.drawable.new_home_pressed);
                            tabIndex = 0;
                        } else if (tab.getText().equals("Account")) {
                            tab.setIcon(R.drawable.new_my_acnt_pressed);
                            tabIndex = 1;
                            mToolbar.setTitle("Account");
                            getProfileDetail();
                        } else if (tab.getText().equals("Doc-Connect")) {
                            tab.setIcon(R.drawable.new_doc_connect_pressed);
                            tabIndex = 2;
                        } else if (tab.getText().equals("More")) {
                            tab.setIcon(R.drawable.new_more_pressed);
                            tabIndex = 3;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        if (tab.getText().equals("Home")) {
                            tab.setIcon(R.drawable.new_home_normal);
                        } else if (tab.getText().equals("Account")) {
                            tab.setIcon(R.drawable.new_my_acnt_normal);
                        } else if (tab.getText().equals("Doc-Connect")) {
                            tab.setIcon(R.drawable.new_doc_connect_normal);
                        } else if (tab.getText().equals("More")) {
                            tab.setIcon(R.drawable.new_more_normal);
                        }
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                        Log.i(TAG, "onTabReselected -- "+tab.getText().toString());
                    }
                }
        );

        AppUpdate.getInstance(this).checkAppUpdate();

        new GCMInfoHandler(this).registerGCM();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            sendGCMDetailsToServer();
        }else {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendGCMDetailsToServer();
        }
    }

    private void getProfileDetail() {
        final String URL = ServerConstants.GET_USER_PROFILE;
        HashMap<String, String> params = new HashMap<>();
        PostApi postApi = new PostApi(new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                AppCoreUtil.log(TAG, "getProfileDetail", response);
                try {
                    JSONObject mBaseObject = new JSONObject(response);
                    JSONObject userObject = new JSONObject(mBaseObject.optString("user"));
                    Log.i("profile", userObject.optString("image"));
                    LocalDataManager.getInstance().set(PreferenceConstants.PIC, userObject.optString("image"));
                    Intent intent = new Intent("profile_refresh");
                    LocalBroadcastManager.getInstance(HomeActivity.this).sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AppCoreUtil.log(TAG, "getProfileDetail", error.toString());
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(HomeActivity.this);
        volleyQueue.add(postApi);
    }

    public String getSlug() {
        return slug;
    }

    public static int days(String createdDate, String todayDate) {
        SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
        int days = 0;
        try {
            Date date1 = myFormat.parse(createdDate);
            Date date2 = myFormat.parse(todayDate);
            long diff = date2.getTime() - date1.getTime();
            System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
            days = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    private void sendGCMDetailsToServer() {
        Log.e(TAG, "sendGCMKeyWithServer");

        String gcm_key = LocalDataManager.getInstance().get(UserConstants.GCMTOKEN);

        String m_deviceId = "";

        TelephonyManager TelephonyMgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        m_deviceId = TelephonyMgr.getDeviceId();


        String user_id = LocalDataManager.getInstance().get(UserConstants.DOCID);
        String version_name = BuildConfig.VERSION_NAME;
        int version_code = BuildConfig.VERSION_CODE;
        int api_version = Build.VERSION.SDK_INT;
        String flavor = BuildConfig.FLAVOR;
        String phone_name = Build.MODEL;

        Log.e(TAG, "********************");
        Log.e(TAG, "gcm_key id : " + gcm_key);
        Log.e(TAG, "user_id    : " + user_id);
        Log.e(TAG, "********************");

        String URL = ServerConstants.GCM_REGISTER;
        List nameValuePairs = new ArrayList();
        nameValuePairs.add(new BasicNameValuePair(UserConstants.GCM_TOKEN, gcm_key));
        nameValuePairs.add(new BasicNameValuePair(UserConstants.IMEI, m_deviceId));
        nameValuePairs.add(new BasicNameValuePair(UserConstants.USERID, "" + user_id));
        nameValuePairs.add(new BasicNameValuePair(UserConstants.STATUS, "1"));
        nameValuePairs.add(new BasicNameValuePair(UserConstants.DEVICE, "android"));

        nameValuePairs.add(new BasicNameValuePair(UserConstants.VERSION_NAME, "" + version_name));
        nameValuePairs.add(new BasicNameValuePair(UserConstants.VERSION_CODE, "" + version_code));
        nameValuePairs.add(new BasicNameValuePair(UserConstants.OS_VERSION, "" + api_version));
        nameValuePairs.add(new BasicNameValuePair(UserConstants.GMAIL_EMAIL, "" + "no email"));

        Log.e(TAG, "calling api ...");
        AsyncParams params = new AsyncParams(URL, 1, AsyncConstants.POST_REQUEST, nameValuePairs.toString(), false, false, nameValuePairs);
        AsyncWorker mWorker = new AsyncWorker(this);
        mWorker.delegate = this;
        mWorker.execute(params);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponseFromAPI(String output, int REQUEST_NUMBER) {
        Log.e(TAG, "onResponseFromAPI GCM : " + output);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if(tabIndex == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            HomeActivity.super.onBackPressed();
                        }

                    }).create().show();
        }else {
            tabLayout.getTabAt(0).select();
            tabLayout.getTabAt(0).setIcon(R.drawable.new_home_pressed);
            tabIndex = 0;
        }
    }
}
