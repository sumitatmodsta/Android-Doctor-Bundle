package com.modastadoc.doctors.activity;

import android.Manifest;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.common.utils.PermissionUtil;
import com.modastadoc.doctors.model.Appointment;
import com.modastadoc.doctors.model.Chat;
import com.modastadoc.doctors.network.PostApi;
import com.ogaclejapan.arclayout.ArcLayout;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LiveActivity extends AppCompatActivity implements  Session.SessionListener,
        PublisherKit.PublisherListener, Session.ConnectionListener, Session.SignalListener,
        View.OnClickListener, PermissionUtil.Callbacks {

    private static final String TAG = "LiveActivity";

    private static final String API_KEY = "45595052";
    //private String SESSION_ID = "";
    //private String TOKEN = "";
    private static final String LOG_TAG = LiveActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private static final int REQUEST_CODE_PERMISSION_CAMERA = 234;

    public static Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    public static Connection mConnection;

    public static List<Chat> mChatList = new ArrayList<>();

    private int index = 0;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    private TextView timer;
    private ImageView options;

    private ArcLayout arcLayout;

    private boolean isConnected;
    public static boolean isChatVisible;
    private boolean isVideoDisabled;

    private Appointment apmt;

    private ImageView video;

    String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
        arcLayout = (ArcLayout) findViewById(R.id.arc_layout);
        timer = (TextView) findViewById(R.id.timer);
        options = (ImageView) findViewById(R.id.options);
        video = (ImageView) findViewById(R.id.video);

        apmt = getIntent().getParcelableExtra("appointment");

        Log.i("kunasi", apmt.date+" - "+apmt.status+" - "+apmt.orderID);

        /*
        *   OnClick Listeners
        * */
        video.setOnClickListener(this);
        findViewById(R.id.summary).setOnClickListener(this);
        //findViewById(R.id.both).setOnClickListener(this);
        findViewById(R.id.chat).setOnClickListener(this);
        options.setOnClickListener(this);
        findViewById(R.id.call).setOnClickListener(this);
        if(!PermissionUtil.hasPermission(perms)) {
            PermissionUtil.requestPermission(this, 1234, PermissionUtil.getNecessaryPermissions(perms));
        }

        //getPatientInfo();
    }

    private void getPatientInfo() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_PATIENT_DETAILS +apmt.orderID;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+apmt.orderID);
            params.put("orderId", apmt.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getPatientInfo --> onResponse --> " + response);
                    //buildSessionInfo(response);
                    //getAppointments();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getPatientInfo --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(LiveActivity.this, R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
            postApi.setRetryPolicy(new DefaultRetryPolicy(
                    4000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }else {
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video:
                if(isVideoDisabled) {
                    isVideoDisabled = false;
                    mPublisher.setPublishVideo(true);
                }else {
                    isVideoDisabled = true;
                    mPublisher.setPublishVideo(false);
                }
                //mPublisher.setPublishVideo(true);
                //mPublisher.setPublishAudio(false);
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.summary:
                Intent in = new Intent(this, PatientDetailActivity.class);
                in.putExtra("appointment", apmt);
                in.putExtra("open_summary", true);
                startActivity(in);
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.chat:
                //mSession.sendSignal("chat", "Hello "+(++index), mConnection);
                Intent in2 = new Intent(this, PatientDetailActivity.class);
                in2.putExtra("appointment", apmt);
                startActivity(in2);
                arcLayout.setVisibility(View.GONE);
                break;
            case R.id.options:
                arcLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.call:
                if(!isConnected)
                    requestPermission();
                break;
            default:
                break;
        }
    }

    private void getSessionInfo() {
        if(AppCoreUtil.isNetworkAvailable()) {
            AppDialogUtil.showCustomDialog(this);
            String URL = ServerConstants.GET_SESSION;

            HashMap<String, String> params = new HashMap<>();
            Log.i(TAG, "Order ID -- "+apmt.orderID);
            params.put("order_id", apmt.orderID);
            PostApi postApi = new PostApi(new Response.Listener<String>() {
                @Override

                public void onResponse(String response) {
                    AppDialogUtil.dismissCustomDialog();
                    Log.i(TAG, "getSessionInfo --> onResponse --> " + response);
                    buildSessionInfo(response);
                    //getAppointments();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.i(TAG, "getSessionInfo --> onErrorResponse --> " + error.getMessage());
                    AppDialogUtil.dismissCustomDialog();
                    AppCoreUtil.showToast(LiveActivity.this, R.string.error_something_wrong_message);
                }
            }, params, URL);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);
            volleyQueue.add(postApi);
            postApi.setRetryPolicy(new DefaultRetryPolicy(
                    4000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
                    initializeSession(sessionID, token);
                }else {
                    AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
                }
            }else {
                AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
            }
        }catch (Exception e) {
            e.printStackTrace();
            AppCoreUtil.showToast(this, getString(R.string.error_no_network_message));
        }
    }

    private void initializeSession(String sessionID, String token) {
        // initialize and connect to the session
        mSession = new Session.Builder(this, API_KEY, sessionID).build();
        mSession.setSessionListener(this);
        mSession.setSignalListener(this);
        mSession.setConnectionListener(this);
        mSession.connect(token);
        isConnected = true;
    }

    private void requestPermission() {
        if(PermissionUtil.hasPermission(perms)) {
            //initializeSession();
            getSessionInfo();
        }else {
            PermissionUtil.requestPermission(this, REQUEST_CODE_PERMISSION_CAMERA,
                    PermissionUtil.getNecessaryPermissions(perms));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_PERMISSION_CAMERA)
            PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
    }

    private void openBottomSheet() {
        final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);

        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet, null);
        final RadioButton radio1 = (RadioButton) sheetView.findViewById(R.id.radio1);
        final RadioButton radio2 = (RadioButton) sheetView.findViewById(R.id.radio2);
        final RadioButton radio3 = (RadioButton) sheetView.findViewById(R.id.radio3);

        TextView cancel = (TextView) sheetView.findViewById(R.id.cancel);
        TextView ok = (TextView) sheetView.findViewById(R.id.ok);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
                if(radio1.isChecked()) {
                    startTimer(15 * 60000, 1000);
                }else if(radio2.isChecked()) {
                    startTimer(30 * 60000, 1000);
                }else {
                    startTimer(45*60000, 1000);
                }
            }
        });

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.setCancelable(false);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mBottomSheetDialog.show();
    }

    @Override
    public void onConnectionCreated(Session session, Connection connection) {
        Log.i(LOG_TAG, "Connection Created");
        mConnection = connection;
    }

    @Override
    public void onConnectionDestroyed(Session session, Connection connection) {
        Log.i(LOG_TAG, "Connection Destroyed");
        mConnection = null;
    }

    // SessionListener methods

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);
        /*mPublisher.setCapturer(new CameraVideoCapturer(this));
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);*/
        //mPublisher.set
        /*View view = mPublisher.getView();
        view.setBackgroundResource(R.drawable.rounded_shape);

        Canvas canvas = new Canvas();*/

        mPublisherViewContainer.addView(mPublisher.getView());
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (mSubscriber == null) {
            mSubscriberViewContainer.setVisibility(View.VISIBLE);
            mPublisherViewContainer.setVisibility(View.VISIBLE);
            timer.setVisibility(View.VISIBLE);
            options.setVisibility(View.VISIBLE);
            startTimer(60000, 1000);
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
            //startTimer(60000, 1000);
        }
    }

    private void startTimer(final long max, long tick) {
        /*CountDownTimer t;
        t = */new CountDownTimer(max, tick) {

            public void onTick(long millisUntilFinished) {
                long remainedSecs = millisUntilFinished / 1000;
                Log.i("kunasi -- ", "tot " + remainedSecs+" -- ");
                Log.i("kunasi -- ", "min " + (remainedSecs/60)+" -- ");
                Log.i("kunasi -- ", "sec " + (remainedSecs%60)+" -- ");
                timer.setText("" + (remainedSecs / 60) + ":" + (remainedSecs % 60));// manage it accordign to you
            }

            public void onFinish() {
                timer.setText("00:00");
                cancel();
                openBottomSheet();
            }
        }.start();
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }

    // PublisherListener methods

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
    }

    @Override
    public void onSignalReceived(Session session, String s, String s1, Connection connection) {
        //Toast.makeText(this, s1, Toast.LENGTH_LONG).show();
        mChatList.add(new Chat(1, s1));
        if(isChatVisible) {
            Intent data = new Intent("chat");
            sendBroadcast(data);
        }else {
            Intent in = new Intent(this, PatientDetailActivity.class);
            in.putExtra("appointment", apmt);
            startActivity(in);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        if(requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            getSessionInfo();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode) {
        if(requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            AppCoreUtil.showToast(this, "Permission required for accessing camera.");
        }
    }

    @Override
    public void onPermissionsPermanentlyDenied(int requestCode, List<String> perms) {
        /*if(requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            AppDialogUtil.showDialog(this, R.string.app_name, PermissionUtil.getCameraPermanentlyDeniedMessage(perms),
                    R.string.alert_settings_positive_title, R.string.alert_settings_negative_title, this);
        }else if(requestCode == REQUEST_CODE_PERMISSION_GALLERY) {
            AppDialogUtil.showDialog(this, R.string.app_name, PermissionUtil.getGalleryPermanentlyDeniedMessage(perms),
                    R.string.alert_settings_positive_title, R.string.alert_settings_negative_title, this);
        }*/
    }
}
