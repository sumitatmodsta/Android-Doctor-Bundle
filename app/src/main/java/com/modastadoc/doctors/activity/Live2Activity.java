package com.modastadoc.doctors.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.AppConstants;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.interfaces.IDialogListener;
import com.modastadoc.doctors.common.interfaces.IFragmentlistener;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.common.utils.DateUtil;
import com.modastadoc.doctors.custom.CustomAudioDevice;
import com.modastadoc.doctors.database.LocalDataManager;
import com.modastadoc.doctors.fragment.ChatFragment;
import com.modastadoc.doctors.fragment.DetailFragment;
import com.modastadoc.doctors.fragment.SummaryFragment;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.Booking;
import com.modastadoc.doctors.model.Chat;
import com.modastadoc.doctors.model.LabTest;
import com.modastadoc.doctors.model.Patient;
import com.modastadoc.doctors.model.PatientFile;
import com.modastadoc.doctors.network.PostApi;
import com.modastadoc.doctors.widget.CircularImageView;
import com.modastadoc.doctors.widget.CustomProgressDialog;
import com.ogaclejapan.arclayout.ArcLayout;
import com.opentok.android.AudioDeviceManager;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Live2Activity extends AppCompatActivity implements  Session.SessionListener,
        PublisherKit.PublisherListener, Session.ConnectionListener, Session.SignalListener,
        View.OnClickListener, Publisher.CameraListener, Session.ReconnectionListener,
        View.OnTouchListener, IFragmentlistener, IDialogListener {

    private static final String TAG = "Live2Activity";

    private static final String API_KEY = "45595052";
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private   Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private Connection mConnection;

    private List<Chat> mChatList = new ArrayList<>();
    private List<LabTest> mLabTest = new ArrayList<>();
    private List<LabTest> mSelectedLabTest = new ArrayList<>();
    private boolean isTimeOut;
    private boolean isDocumentUploaded;
    private String documentPath;
    private String mDiagnosis = "", mAdvice = "", mOther = "";

    private int index = 0;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    private TextView timer, preCall, reconnect;
    private ImageView options, video, audio;
    private Button ready;

    private ArcLayout arcLayout;

    private boolean isConnected, isAlreadyExtended;
    public static boolean isChatVisible;
    private boolean isVideoDisabled, isAudioMuted;

    private Appointment mAppointment;
    private Patient mPatient;
    private Booking mBooking;
    private ArrayList<PatientFile> mList;
    //private Live mLive;

    private RelativeLayout mDetailsContainer, mSettingsContainer/*, mBottomContainer*/;
    private FrameLayout mVideoContainer;
    private LinearLayout mWaitingContainer;
    private static final String[] CONTENT = new String[] { "Chat", "Patient Details", "Summary"};
    private ViewPager mViewPager;
    private TextView tabChat, tabDetails, tabSummary;
    private TextView waiting1, waiting2;

    private CustomProgressDialog mCustomDialog;
    private RelativeLayout mReportView;
    private WebView mReportWebView;
    private Button mCloseReport;

    private boolean isSummaryVisible, isSessionStarted;

    private float dX;
    private float dY;
    private int lastAction;
    private int screenHight, screenWidth;
    private boolean isMinimized;
    private int eventCount = 0;

    private CountDownTimer mTimer;
    private long remainingMillis;
    private static final long MILLISFORMINUTE = 60000;

    private Dialog mDialog;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live2);

        Bundle b = getIntent().getExtras();
        if(b != null) {
            mAppointment = b.getParcelable("appointment");
            mPatient = b.getParcelable("patient");
            mBooking = b.getParcelable("booking");
            mList = b.getParcelableArrayList("files");
            if(mAppointment == null || mPatient == null || mBooking == null) {
                AppCoreUtil.showToast(this, R.string.error_something_wrong_message);
                finish();
            }
        }else {
            finish();
        }

        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
        arcLayout = (ArcLayout) findViewById(R.id.arc_layout);
        timer = (TextView) findViewById(R.id.timer);
        options = (ImageView) findViewById(R.id.options);
        video = (ImageView) findViewById(R.id.video);
        audio = (ImageView) findViewById(R.id.audio);

        ready = (Button) findViewById(R.id.ready);
        preCall = (TextView) findViewById(R.id.pre_call_test);
        reconnect = (TextView) findViewById(R.id.reconnect);
        waiting1 = (TextView) findViewById(R.id.waiting1);
        waiting2 = (TextView) findViewById(R.id.waiting2);

        mDetailsContainer  = (RelativeLayout) findViewById(R.id.details_container);
        mVideoContainer    = (FrameLayout) findViewById(R.id.video_container);
        mSettingsContainer = (RelativeLayout) findViewById(R.id.settings_container);
        //mBottomContainer = (RelativeLayout) findViewById(R.id.bottom_container);
        mWaitingContainer  = (LinearLayout) findViewById(R.id.waiting_container);

        tabChat = (TextView) findViewById(R.id.tab_chat);
        tabDetails = (TextView) findViewById(R.id.tab_details);
        tabSummary = (TextView) findViewById(R.id.tab_summary);

        // Report View
        mReportView = (RelativeLayout) findViewById(R.id.patient_report_view);
        mReportWebView = (WebView) mReportView.findViewById(R.id.patient_report_webview);
        mCloseReport = (Button) mReportView.findViewById(R.id.report_close);
        mCloseReport.setOnClickListener(this);
        mReportView.setVisibility(View.GONE);

        /*
        *   OnClick Listeners
        * */
        video.setOnClickListener(this);
        audio.setOnClickListener(this);
        findViewById(R.id.summary).setOnClickListener(this);
        findViewById(R.id.camera_toggle).setOnClickListener(this);
        //findViewById(R.id.both).setOnClickListener(this);
        findViewById(R.id.chat).setOnClickListener(this);
        findViewById(R.id.details).setOnClickListener(this);
        options.setOnClickListener(this);
        findViewById(R.id.call).setOnClickListener(this);
        tabChat.setOnClickListener(this);
        tabDetails.setOnClickListener(this);
        tabSummary.setOnClickListener(this);
        ready.setOnClickListener(this);
        preCall.setOnClickListener(this);
        reconnect.setOnClickListener(this);

        FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                tabSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}

        });

        setView();
        openScreen(0);
        getLabTests();
        initializeView();
    }

    public void ShowPatientDetails(String url, HashMap<String, String> params) {
        showCustomDialog();
        mReportView.setVisibility(View.VISIBLE);
        mReportWebView.getSettings().setJavaScriptEnabled(true);
        mReportWebView.getSettings().setLoadWithOverviewMode(true);
        mReportWebView.getSettings().setUseWideViewPort(true);
        mReportWebView.loadUrl(url, params);

        mReportWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                Toast.makeText(Live2Activity.this, "Image loaded", Toast.LENGTH_SHORT).show();
                dismissCustomDialog();
            }
        });
    }

    private void showCustomDialog()
    {
        mCustomDialog = new CustomProgressDialog(this);
        mCustomDialog.setCancelable(true);
        mCustomDialog.show();
    }

    private void dismissCustomDialog()
    {
        if (mCustomDialog != null)
        {
            mCustomDialog.dismiss();
        }
    }

    private Runnable timedTask = new Runnable(){

        @Override
        public void run() {
            getVideoStatus();
        }};

    private void getVideoStatus() {
        String URL = ServerConstants.GET_VIDEO_STATUS+mAppointment.orderID;

        HashMap<String, String> params = new HashMap<>();
        Log.i(TAG, "Order ID -- "+mAppointment.orderID);
        //params.put("orderId", apmt.orderID);
        PostApi postApi = new PostApi(new Response.Listener<String>() {
            @Override

            public void onResponse(String response) {
                //AppDialogUtil.dismissCustomDialog();
                Log.i(TAG, "getVideoStatus --> onResponse --> " + response);
                buildVideoStatus(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.i(TAG, "getVideoStatus --> onErrorResponse --> " + error.getMessage());
                mHandler.postDelayed(timedTask, 10000);
            }
        }, params, URL);

        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        volleyQueue.add(postApi);
    }

    private void buildVideoStatus(String response) {
        try {
            JSONObject o = new JSONObject(response);
            if(o.optString("pat_status").equalsIgnoreCase(AppConstants.STATUS_WAITING)) {
                startConsultation();
            }else {
                mHandler.postDelayed(timedTask, 10000);
            }
        }catch (Exception e) {
            e.printStackTrace();
            mHandler.postDelayed(timedTask, 10000);
        }
    }

    private void initializeView() {
        if(mBooking.docStatus != null && mBooking.docStatus.equalsIgnoreCase(AppConstants.STATUS_WAITING)) {
            if(mBooking.patStatus != null && mBooking.patStatus.equalsIgnoreCase(AppConstants.STATUS_WAITING)) {
                startConsultation();
            }else {
                ready.setVisibility(View.GONE);
                preCall.setVisibility(View.GONE);
                waiting1.setText(getResources().getString(R.string.title_waiting_message3));
                waiting2.setText(getResources().getString(R.string.title_waiting_message4));
                mHandler.post(timedTask);
            }
        }else {
            if(mBooking.patStatus != null && mBooking.patStatus.equalsIgnoreCase(AppConstants.STATUS_WAITING)) {
                waiting1.setText(getResources().getString(R.string.title_waiting_message5));
                waiting2.setText(getResources().getString(R.string.title_waiting_message6));
            }
        }
    }

    private void startConsultation() {
        initializeSession();
        mWaitingContainer.setVisibility(View.GONE);
        preCall.setVisibility(View.GONE);
        mVideoContainer.setVisibility(View.VISIBLE);
    }

    private void setView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView name = (TextView) findViewById(R.id.name);
        CircularImageView pic = (CircularImageView) findViewById(R.id.pic);

        toolbar.setTitle(mAppointment.name);
        name.setText(mAppointment.name);
        Glide.with(this)
                .load(mAppointment.picUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.doctor)
                .crossFade()
                .into(pic);
    }

    private void fillDefault() {
        tabChat.setTextColor(getResources().getColor(R.color.white));
        tabDetails.setTextColor(getResources().getColor(R.color.white));
        tabSummary.setTextColor(getResources().getColor(R.color.white));
    }

    private void tabSelected(int position) {
        fillDefault();
        switch (position) {
            case 1:
                tabDetails.setTextColor(getResources().getColor(R.color.red));
                break;
            case 2 :
                tabSummary.setTextColor(getResources().getColor(R.color.red));
                break;
            default:
                tabChat.setTextColor(getResources().getColor(R.color.red));
                break;
        }
    }

    private void openScreen(int index) {
        switch (index) {
            case 1:
                mViewPager.setCurrentItem(1);
                break;
            case 2:
                mViewPager.setCurrentItem(2);
                break;
            default:
                mViewPager.setCurrentItem(0);
                tabSelected(0);
                break;
        }

    }

    class GoogleMusicAdapter extends FragmentPagerAdapter {
        public GoogleMusicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return ChatFragment.getInstance(mAppointment);
            else if(position == 1)
                return DetailFragment.getInstance(mAppointment);
            /*else if(position == 2)
                return UploadedFileFragment.getInstance(apmt);*/
            else
                return SummaryFragment.getInstance(mAppointment, Live2Activity.this);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

    private void getLabTests() {
        HashMap<String, String> params = new HashMap<>();
        PostApi postApi = new PostApi(new Response.Listener<String>() {
            @Override

            public void onResponse(String response) {
                AppCoreUtil.log(TAG, "getLabTests", response);
                buildLabTests(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                buildLabTests(LocalDataManager.getInstance().get("labtestlist"));
            }
        }, params, ServerConstants.GET_NEW_LAB_LIST);

        RequestQueue volleyQueue = Volley.newRequestQueue(this);
        volleyQueue.add(postApi);
    }

    private void buildLabTests(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if(obj.has("labtestlist")) {
                JSONArray a = obj.optJSONArray("labtestlist");
                if(a != null) {
                    int size = a.length();
                    JSONObject o;
                    for(int i=0; i<size; i++) {
                        o = a.optJSONObject(i);
                        if(o != null && o.length() > 0) {
                            mLabTest.add(new LabTest(o.optString("id"), o.optString("name")));
                        }
                    }
                }
                LocalDataManager.getInstance().set("labtestlist", response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_toggle:
                mPublisher.cycleCamera();
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.video:
                if(isVideoDisabled) {
                    isVideoDisabled = false;
                    mPublisher.setPublishVideo(true);
                    setImageDrawable(video, R.drawable.ic_video_on);
                }else {
                    isVideoDisabled = true;
                    mPublisher.setPublishVideo(false);
                    setImageDrawable(video, R.drawable.ic_video_off);
                }
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.audio:
                if(isAudioMuted) {
                    isAudioMuted = false;
                    mPublisher.setPublishAudio(true);
                    setImageDrawable(audio, R.drawable.ic_mic_on);
                }else {
                    isAudioMuted = true;
                    mPublisher.setPublishAudio(false);
                    setImageDrawable(audio, R.drawable.ic_mic_off);
                }
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.summary:
                if(isSummaryVisible) {
                    Intent in = new Intent("can_show_summary");
                    in.putExtra("title", getResources().getString(R.string.text_show_to_patient));
                    sendBroadcast(in);
                }
                summaryClicked();
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.details:
                detailsClicked();
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.chat:
                chatClicked();
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.options:
                if(arcLayout.getVisibility() == View.VISIBLE) {
                    arcLayout.setVisibility(View.GONE);
                }else {
                    arcLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.call:
                arcLayout.setVisibility(View.GONE);
                AppDialogUtil.showAlert(Live2Activity.this, "", "Please submit the summary to end the call");
                close();
                break;
            case R.id.tab_chat:
                tabClicked(0);
                break;
            case R.id.tab_details:
                tabClicked(1);
                break;
            case R.id.tab_summary:
                tabClicked(2);
                break;
            case R.id.ready:
                readyClicked();
                break;
            case R.id.pre_call_test:
                Intent in = new Intent(this, TestActivity.class);
                in.putExtra("order_id", mAppointment.orderID);
                startActivity(in);
                break;
            case R.id.reconnect:
                reconnect();
                break;
            case R.id.report_close:
                mReportView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float newX, newY;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;
                eventCount = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                eventCount += 1;
                lastAction = MotionEvent.ACTION_MOVE;
                newX = event.getRawX() + dX;
                newY = event.getRawY() + dY;
                // check if the view out of screen
                if ((newX <= 0 || newX >= screenWidth-view.getWidth()) ||
                        (newY <= 0 || newY >= screenHight-view.getHeight())) {
                    break;
                }

                view.setY(newY);
                view.setX(newX);
                break;

            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP");
                Log.i(TAG, "last Action -- "+lastAction);
                //if (lastAction == MotionEvent.ACTION_DOWN)
                //Toast.makeText(this, "Clicked!", Toast.LENGTH_SHORT).show();
                if(eventCount < 4)
                    maximize();
                break;

            default:
                return false;
        }
        return true;
    }

    private void setImageDrawable(ImageView view, int resourceID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setImageDrawable(getResources().getDrawable(resourceID, getApplicationContext().getTheme()));
        } else {
            view.setImageDrawable(getResources().getDrawable(resourceID));
        }
    }

    private void readyClicked() {
        getSessionInfo();
    }

    private void getSessionInfo() {
        if(AppCoreUtil.isNetworkAvailable()) {


            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_SESSION;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+mAppointment.orderID);
            params.put("order_id", mAppointment.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getSessionInfo --> onResponse --> " + response);
                    buildSessionInfo(response);

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getSessionInfo --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppDialogUtil.showAlert(Live2Activity.this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void buildSessionInfo(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if(obj.length() > 0) {
                JSONObject o = obj.getJSONObject("booking");
                if(o != null && o.length() > 0) {
                    String sessionID = o.optString("callsession");
                    String token = o.optString("token");
                    String status = o.optString("doc_status");
                    int duration = o.optInt("duration", 0);
                    Log.i(TAG, "sessio id -- "+sessionID);
                    Log.i(TAG, "token -- "+token);
                    Log.i(TAG, "duration -- "+duration);
                    mBooking.sessionID = sessionID;
                    mBooking.token = token;
                    mBooking.docStatus = status;
                    mBooking.duration = duration;

                    if(mBooking.patStatus != null && mBooking.patStatus.equalsIgnoreCase(AppConstants.STATUS_WAITING)) {
                        startConsultation();
                    }else {
                        mHandler.post(timedTask);
                        ready.setVisibility(View.GONE);
                        preCall.setVisibility(View.GONE);
                        waiting1.setText(getResources().getString(R.string.title_waiting_message3));
                        waiting2.setText(getResources().getString(R.string.title_waiting_message4));
                    }
                }else {
                    AppDialogUtil.showAlert(Live2Activity.this, R.string.error_title,
                            R.string.error_something_wrong_message);
                }
            }else {
                AppDialogUtil.showAlert(Live2Activity.this, R.string.error_title,
                        R.string.error_something_wrong_message);
            }
        }catch (Exception e) {
            e.printStackTrace();
            AppDialogUtil.showAlert(Live2Activity.this, R.string.error_title,
                    R.string.error_something_wrong_message);
        }
    }

    private void tabClicked(int index) {
        mViewPager.setCurrentItem(index);
    }

    private void reconnect() {
        try {
            mSession.disconnect();
            removePublisher();
            removeSubscriber();
            mSession.connect(mBooking.token);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void summaryClicked() {
        minimizeScreen();
        tabClicked(2);
    }

    private void detailsClicked() {
        minimizeScreen();
        tabClicked(1);
    }

    private void chatClicked() {
        minimizeScreen();
        tabClicked(0);
    }

    private void minimize() {
        Intent data = new Intent("chat");
        sendBroadcast(data);
        if(!isMinimized) {
            tabClicked(0);
            minimizeScreen();
        }
    }

    private void maximize() {
        Log.i(TAG, "maximize");
        isMinimized = false;
        LocalDataManager.getInstance().set("can_show_summary", false);
        AppCoreUtil.hideKeyboard(this);
        FrameLayout.LayoutParams vParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vParams.gravity = Gravity.TOP;
        mVideoContainer.setLayoutParams(vParams);

        mVideoContainer.setTop(0);
        mVideoContainer.setLeft(0);
        mVideoContainer.setX(0);
        mVideoContainer.setY(0);

        mDetailsContainer.setVisibility(View.GONE);
        mSettingsContainer.setVisibility(View.VISIBLE);
        reconnect.setVisibility(View.VISIBLE);
        //mBottomContainer.setVisibility(View.VISIBLE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dpToPixels(90), dpToPixels(120));
        params.setMargins(dpToPixels(16), dpToPixels(16), 0, 0);
        mPublisherViewContainer.setLayoutParams(params);
        mSubscriberViewContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mVideoContainer.setOnTouchListener(null);
    }

    private void minimizeScreen() {
        mDetailsContainer.setVisibility(View.VISIBLE);
        mVideoContainer.setOnTouchListener(this);
        FrameLayout.LayoutParams vParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vParams.gravity = Gravity.CENTER;
        mVideoContainer.setLayoutParams(vParams);
        mSettingsContainer.setVisibility(View.GONE);
        reconnect.setVisibility(View.GONE);
        //mBottomContainer.setVisibility(View.GONE);
        FrameLayout.LayoutParams pParams = new FrameLayout.LayoutParams(dpToPixels(45), dpToPixels(60));
        pParams.setMargins(dpToPixels(90), 0, 0, 0);
        mPublisherViewContainer.setLayoutParams(pParams);
        FrameLayout.LayoutParams sParams = new FrameLayout.LayoutParams(dpToPixels(100), dpToPixels(100));
        sParams.setMargins(0, dpToPixels(40), 0, 0);
        mSubscriberViewContainer.setLayoutParams(sParams);
        isMinimized = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHight = getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSession();
        LocalDataManager.getInstance().set("can_show_summary", false);
        if(mTimer != null) {
            mTimer.cancel();
        }
        try {
            mHandler.removeCallbacks(timedTask);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initializeSession() {
        CustomAudioDevice customAudioDevice = new CustomAudioDevice(this);
        AudioDeviceManager.setAudioDevice(customAudioDevice);

        // initialize and connect to the session
        mSession = new Session.Builder(this, API_KEY, mBooking.sessionID).build();
        mSession.setSessionListener(this);
        mSession.setSignalListener(this);
        mSession.setConnectionListener(this);
        mSession.setReconnectionListener(this);
        mSession.connect(mBooking.token);
        isConnected = true;
    }

    private void close() {
        minimizeScreen();
        LocalDataManager.getInstance().set("can_show_summary", true);
        if(isSummaryVisible) {
            Intent in = new Intent("can_show_summary");
            in.putExtra("title", getResources().getString(R.string.text_submit));
            sendBroadcast(in);
        }

        tabClicked(2);
    }

    private void disConnect() {
        closeSession();
        finish();
    }

    private void closeSession() {
        if(mSession != null) {
            mSession.disconnect();
        }
        removeSubscriber();
        removePublisher();
        mVideoContainer.setVisibility(View.GONE);
        mSettingsContainer.setVisibility(View.GONE);
        reconnect.setVisibility(View.GONE);
        mDetailsContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionCreated(Session session, Connection connection) {
        Log.i(TAG, "Connection Created");
        mConnection = connection;
    }

    @Override
    public void onConnectionDestroyed(Session session, Connection connection) {
        Log.i(TAG, "Connection Destroyed");
        mConnection = null;
    }

    // SessionListener methods

    @Override
    public void onConnected(Session session) {
        Log.i(TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        /*mPublisher = new Publisher.Builder(this)
                .capturer(new CustomVideoCapturer(this)).build();*/
        mPublisher.setPublisherListener(this);
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

        mPublisherViewContainer.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TAG, "Stream Received");
        if (mSubscriber == null) {
            isSessionStarted = true;
            if(!isMinimized) {
                mSettingsContainer.setVisibility(View.VISIBLE);
                reconnect.setVisibility(View.VISIBLE);
            }
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriberViewContainer.addView(mSubscriber.getView());
            if(remainingMillis < 10) {
                remainingMillis = (mBooking.duration > 0 ? mBooking.duration : 15)*MILLISFORMINUTE;
            }else {
                if(mPublisher != null) {
                    mPublisher.setPublishAudio(!isAudioMuted);
                    mPublisher.setPublishVideo(!isVideoDisabled);
                }
            }
            startTimer(remainingMillis, 1000);
        }
    }

    private void startTimer(final long max, long tick) {
        if(mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new CountDownTimer(max, tick) {

            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                long remainedSecs = millisUntilFinished / 1000;
                if(!isAlreadyExtended) {
                    if (remainedSecs == 180) {
                        showDialog();
                    }
                }
                long min = remainedSecs / 60;
                long sec = remainedSecs % 60;
                timer.setText((min>9?min:"0"+min) + ":" + (sec>9?sec:"0"+sec));// manage it according to you
            }

            public void onFinish() {
                timer.setText("00:00");
                cancel();
                timeOut();
            }
        }.start();
    }

    private void showDialog() {
        Log.i(TAG, "showDialog");
        if(mDialog == null || !mDialog.isShowing()) {
            Log.i(TAG, "showDialog -- show");
            mDialog = new Dialog(this);
            // Include dialog.xml file
            mDialog.setContentView(R.layout.layout_bottom_sheet);
            mDialog.setCancelable(false);
            mDialog.show();
            mDialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isAlreadyExtended = true;
                    onPositiveButtonClicked();
                    dismissDialog();
                }
            });
            mDialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNegativeButtonClicked();
                    dismissDialog();
                }
            });
        }else {
            Log.i(TAG, "showDialog not show");
        }
    }

    private void dismissDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void timeOut() {
        isTimeOut = true;
        dismissDialog();
        mPublisher.setPublishVideo(false);
        mVideoContainer.setVisibility(View.GONE);
        mDetailsContainer.setVisibility(View.VISIBLE);
        mSettingsContainer.setVisibility(View.GONE);
        reconnect.setVisibility(View.GONE);
        removeSubscriber();
        removePublisher();
        LocalDataManager.getInstance().set("can_show_summary", true);
        if(isSummaryVisible) {
            Intent in = new Intent("can_show_summary");
            in.putExtra("title", getResources().getString(R.string.text_submit));
            sendBroadcast(in);
        }
        tabClicked(2);
        AppCoreUtil.showToast(this, "Your consultation has completed. Please submit the summary");
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TAG, "Stream Dropped");
        removeSubscriber();
    }

    private void removeSubscriber() {
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    private void removePublisher() {
        if(mPublisher != null) {
            mPublisher = null;
            mPublisherViewContainer.removeAllViews();
        }
    }

    @Override
    public void onReconnected(Session session) {

    }

    @Override
    public void onReconnecting(Session session) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(TAG, "Session error: " + opentokError.getMessage());
    }

    // PublisherListener methods

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(TAG, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(TAG, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(TAG, "Publisher error: " + opentokError.getMessage());
    }

    @Override
    public void onSignalReceived(Session session, String s, String s1, Connection connection) {
        if(s.equalsIgnoreCase("msg")) {
            mChatList.add(new Chat(1, s1));
            minimize();
        }else if(s.equalsIgnoreCase("filesent")) {
            Intent data = new Intent("refresh_patient_details");
            sendBroadcast(data);
            AppCoreUtil.showToast(this, "Patient uploaded new file");
        }
    }

    @Override
    public void onCameraChanged(Publisher publisher, int i) {
        Log.i(TAG, "onCameraChanged: ");
    }

    @Override
    public void onCameraError(Publisher publisher, OpentokError opentokError) {
        Log.i(TAG, "onCameraError error: " + opentokError.getMessage());
    }

    public void unPublish() {
        try {
            mSession.unpublish(mPublisher);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publish() {
        try {
            mSession.publish(mPublisher);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        return mSession;
    }

    public Connection getConnection() {
        return mConnection;
    }

    public List<Chat> getChatList() {
        return mChatList;
    }

    public void addChat(Chat c) {
        mChatList.add(c);
    }

    public List<LabTest> getLabTest() {
        return mLabTest;
    }

    public List<LabTest> getSelectedLabTest() {
        return mSelectedLabTest;
    }

    public void addLabTest(LabTest test) {
        if(!mSelectedLabTest.contains(test))
            mSelectedLabTest.add(test);
    }

    public void removeLabTest(LabTest test) {
        mSelectedLabTest.remove(test);
    }

    public void setSummaryInfo(String diagnosis, String advice, String other) {
        this.mDiagnosis = diagnosis;
        this.mAdvice = advice;
        this.mOther = other;
    }

    public String getDiagnosis() {
        return mDiagnosis;
    }

    public String getAdvice() {
        return mAdvice;
    }

    public String getOther() {
        return mOther;
    }

    public void setIsSummaryVisible(boolean isSummaryVisible) {
        this.isSummaryVisible = isSummaryVisible;
    }

    public void setDocumentPath(String path) {
        this.documentPath = path;
    }

    public String getDocumentPath() {
        return this.documentPath;
    }

    public void setIsDocumentUploaded(boolean isUploaded) {
        this.isDocumentUploaded = isUploaded;
    }

    public boolean getIsDocumentUploaded() {
        return this.isDocumentUploaded;
    }

    public boolean getIsTimeOut() {
        return isTimeOut;
    }

    @Override
    public void onSummaryCompleted() {
        if(mSession != null && mConnection != null)
            mSession.sendSignal("refresh", "refresh", mConnection);
        disConnect();
    }

    private int dpToPixels(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if(!isSessionStarted)
            super.onBackPressed();
    }

    private void updateTimer() {
        if(mTimer != null) {
            mTimer.cancel();
        }
        long extTime = (5*MILLISFORMINUTE) + remainingMillis;
        startTimer(extTime, 1000);
    }

    @Override
    public void onPositiveButtonClicked() {
        updateTimer();
        //startTimer(5*MILLISFORMINUTE, 1000);
        extendTime();
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    private void extendTime() {
        if(AppCoreUtil.isNetworkAvailable()) {
            String URL = ServerConstants.EXTEND_APPOINTMENT;

            HashMap<String, String> params = new HashMap<>();
            params.put("orderId", mAppointment.orderID);
            params.put("sdate", DateUtil.get(mAppointment.date, "dd-MM-yyyy", "yyyy-MM-dd"));
            params.put("delay", "5");
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    Log.i(TAG, "extendTime --> onResponse --> " + response);
                    mSession.sendSignal("timerupdate", "timerupdate", mConnection);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(TAG, "extendTime --> onErrorResponse --> " + error.getMessage());
                    error.printStackTrace();
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
        }
    }
}
