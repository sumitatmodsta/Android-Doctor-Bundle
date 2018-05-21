package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.common.utils.AppCoreUtil;
import com.modastadoc.doctors.common.utils.AppDialogUtil;
import com.modastadoc.doctors.custom.CustomAudioDevice;
import com.modastadoc.doctors.network.PostApi;
import com.opentok.android.AudioDeviceManager;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONObject;

import java.util.HashMap;

public class TestActivity extends AppCompatActivity implements Session.SessionListener,
        PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {

    private static final String LOGTAG = "TestActivity";

    private static final String APIKEY = "45595052";

    private static final int TEST_DURATION = 20; //test quality duration in seconds
    private static final int TIME_WINDOW = 3; //3 seconds
    private static final int TIME_VIDEO_TEST = 15; //time interval to check the video quality in seconds

    private ImageView videoPoor, videoGood, videoBest, audioPoor, audioGood, audioBest;
    private View videoView1, videoView2, audioView1, audioView2;

    private static final int QUALITY_STATUS_BEST = 2;
    private static final int QUALITY_STATUS_GOOD = 1;


    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private double mVideoPLRatio = 0.0;
    private long mVideoBw = 0;

    private double mAudioPLRatio = 0.0;
    private long mAudioBw = 0;

    private long mPrevVideoPacketsLost = 0;
    private long mPrevVideoPacketsRcvd = 0;
    private double mPrevVideoTimestamp = 0;
    private long mPrevVideoBytes = 0;

    private long mPrevAudioPacketsLost = 0;
    private long mPrevAudioPacketsRcvd = 0;
    private double mPrevAudioTimestamp = 0;
    private long mPrevAudioBytes = 0;

    private long mStartTestTime = 0;

    private boolean audioOnly = false;

    private Handler mHandler = new Handler();

    private ProgressDialog mProgressDialog;
    private AlertDialog dialog;

    private String mOrderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Bundle b = getIntent().getExtras();
        mOrderID = b.getString("order_id");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Test Your Video & Audio");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        videoPoor = (ImageView) findViewById(R.id.video_poor);
        videoGood = (ImageView) findViewById(R.id.video_good);
        videoBest = (ImageView) findViewById(R.id.video_best);
        audioPoor = (ImageView) findViewById(R.id.audio_poor);
        audioGood = (ImageView) findViewById(R.id.audio_good);
        audioBest = (ImageView) findViewById(R.id.audio_best);

        videoView1 = findViewById(R.id.video_view1);
        videoView2 = findViewById(R.id.video_view2);
        audioView1 = findViewById(R.id.audio_view1);
        audioView2 = findViewById(R.id.audio_view2);

        getPretestToken();
    }

    private void getPretestToken() {
        if(AppDialogUtil.canShowPopup()) {
            if (AppCoreUtil.isNetworkAvailable()) {
                AppDialogUtil.showCustomDialog(this);
                HashMap<String, String> params = new HashMap<>();
                params.put("order_id", mOrderID);

                PostApi postApi = new PostApi(new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        AppDialogUtil.dismissCustomDialog();
                        Log.i(LOGTAG, "getPretestToken response -- " + response);
                        buildPretestToken(response);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        AppDialogUtil.dismissCustomDialog();
                        Log.i(LOGTAG, "getPretestToken error -- " + error.getMessage());
                        AppDialogUtil.showError(TestActivity.this);
                    }
                }, params, ServerConstants.PRECALL_TEST);

                RequestQueue volleyQueue = Volley.newRequestQueue(this);
                volleyQueue.add(postApi);
            } else {
                AppDialogUtil.showInternetError(this);
            }
        }
    }

    private void buildPretestToken(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if(obj.has("booking")) {
                JSONObject o = obj.optJSONObject("booking");
                if(o != null && o.length() > 0) {
                    String session = o.optString("callsession");
                    String token = o.optString("token");
                    sessionConnect(session, token);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mSession != null) {
            mSession.disconnect();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(dialog!= null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void sessionConnect(String session, String token) {
        Log.i(LOGTAG, "Connecting session");
        if (mSession == null) {
            try {
                // Add a custom audio device before session initialization
                CustomAudioDevice customAudioDevice = new CustomAudioDevice(this);
                //customAudioDevice.setRendererMute(true);
                AudioDeviceManager.setAudioDevice(customAudioDevice);
            }catch (Exception e) {
                e.printStackTrace();
            }

            mSession = new Session.Builder(this, APIKEY, session).build();
            mSession.setSessionListener(this);

            mProgressDialog = ProgressDialog.show(this, "Checking your available bandwidth", "Please wait");
            mSession.connect(token);
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOGTAG, "Session is connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);
        mPublisher.setAudioFallbackEnabled(false);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOGTAG, "Session is disconnected");

        mPublisher = null;
        mSubscriber = null;
        mSession = null;

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOGTAG, "Session error: " + opentokError.getMessage());
        showAlert("Error", "Session error: " + opentokError.getMessage());
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOGTAG, "Session onStreamDropped");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOGTAG, "Session onStreamReceived");
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOGTAG, "Publisher onStreamCreated");
        if (mSubscriber == null) {
            subscribeToStream(stream);
        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOGTAG, "Publisher onStreamDestroyed");
        if (mSubscriber == null) {
            unsubscribeFromStream(stream);
        }
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.i(LOGTAG, "Publisher error: " + opentokError.getMessage());
        showAlert("Error", "Publisher error: " + opentokError.getMessage());
    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.i(LOGTAG, "Subscriber onConnected");
        //mHandler.postDelayed(statsRunnable, TEST_DURATION * 1000);
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        Log.i(LOGTAG, "Subscriber onDisconnected");
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        Log.i(LOGTAG, "Subscriber error: " + opentokError.getMessage());
        showAlert("Error", "Subscriber error: " + opentokError.getMessage());
    }

    private void subscribeToStream(Stream stream) {
        mSubscriber = new Subscriber.Builder(this, stream).build();

        mSubscriber.setSubscriberListener(this);
        mSession.subscribe(mSubscriber);
        mSubscriber.setVideoStatsListener(new SubscriberKit.VideoStatsListener() {

            @Override
            public void onVideoStats(SubscriberKit subscriber,
                                     SubscriberKit.SubscriberVideoStats stats) {

                if (mStartTestTime == 0) {
                    mStartTestTime = System.currentTimeMillis() / 1000;
                }
                checkVideoStats(stats);

                //check quality of the video call after TIME_VIDEO_TEST seconds
                if (((System.currentTimeMillis() / 1000 - mStartTestTime) > TIME_VIDEO_TEST) && !audioOnly) {
                    //checkVideoQuality();
                    checkQuality();
                }
            }

        });

        mSubscriber.setAudioStatsListener(new SubscriberKit.AudioStatsListener() {
            @Override
            public void onAudioStats(SubscriberKit subscriber, SubscriberKit.SubscriberAudioStats stats) {

                checkAudioStats(stats);

            }
        });
    }

    private void unsubscribeFromStream(Stream stream) {
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriber = null;
        }
    }

    private void checkVideoStats(SubscriberKit.SubscriberVideoStats stats) {
        double videoTimestamp = stats.timeStamp / 1000;

        //initialize values
        if (mPrevVideoTimestamp == 0) {
            mPrevVideoTimestamp = videoTimestamp;
            mPrevVideoBytes = stats.videoBytesReceived;
        }

        if (videoTimestamp - mPrevVideoTimestamp >= TIME_WINDOW) {
            //calculate video packets lost ratio
            if (mPrevVideoPacketsRcvd != 0) {
                long pl = stats.videoPacketsLost - mPrevVideoPacketsLost;
                long pr = stats.videoPacketsReceived - mPrevVideoPacketsRcvd;
                long pt = pl + pr;

                if (pt > 0) {
                    mVideoPLRatio = (double) pl / (double) pt;
                }
            }

            mPrevVideoPacketsLost = stats.videoPacketsLost;
            mPrevVideoPacketsRcvd = stats.videoPacketsReceived;

            //calculate video bandwidth
            mVideoBw = (long) ((8 * (stats.videoBytesReceived - mPrevVideoBytes)) / (videoTimestamp - mPrevVideoTimestamp));

            mPrevVideoTimestamp = videoTimestamp;
            mPrevVideoBytes = stats.videoBytesReceived;

            Log.i(LOGTAG, "Video bandwidth (bps): " + mVideoBw + " Video Bytes received: " + stats.videoBytesReceived + " Video packet lost: " + stats.videoPacketsLost + " Video packet loss ratio: " + mVideoPLRatio);

        }
    }

    private void checkAudioStats(SubscriberKit.SubscriberAudioStats stats) {
        double audioTimestamp = stats.timeStamp / 1000;

        //initialize values
        if (mPrevAudioTimestamp == 0) {
            mPrevAudioTimestamp = audioTimestamp;
            mPrevAudioBytes = stats.audioBytesReceived;
        }

        if (audioTimestamp - mPrevAudioTimestamp >= TIME_WINDOW) {
            //calculate audio packets lost ratio
            if (mPrevAudioPacketsRcvd != 0) {
                long pl = stats.audioPacketsLost - mPrevAudioPacketsLost;
                long pr = stats.audioPacketsReceived - mPrevAudioPacketsRcvd;
                long pt = pl + pr;

                if (pt > 0) {
                    mAudioPLRatio = (double) pl / (double) pt;
                }
            }
            mPrevAudioPacketsLost = stats.audioPacketsLost;
            mPrevAudioPacketsRcvd = stats.audioPacketsReceived;

            //calculate audio bandwidth
            mAudioBw = (long) ((8 * (stats.audioBytesReceived - mPrevAudioBytes)) / (audioTimestamp - mPrevAudioTimestamp));

            mPrevAudioTimestamp = audioTimestamp;
            mPrevAudioBytes = stats.audioBytesReceived;

            Log.i(LOGTAG, "Audio bandwidth (bps): " + mAudioBw + " Audio Bytes received: " + stats.audioBytesReceived + " Audio packet lost: " + stats.audioPacketsLost + " Audio packet loss ratio: " + mAudioPLRatio);

        }

    }

    private void checkQuality() {
        if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (mSession != null) {
            mSession.disconnect();
            int videoStatus = 0, audioStatus = 0;
            Log.i(LOGTAG, "Check video quality stats data");
            if (mVideoBw > 300000 && mVideoPLRatio < 0.005) {
                videoStatus = QUALITY_STATUS_BEST;
                Log.i(LOGTAG, "Video Best");
            }else if(mVideoBw > 150000 && mVideoPLRatio < 0.03) {
                videoStatus = QUALITY_STATUS_GOOD;
                Log.i(LOGTAG, "Video Good");
            }else {
                Log.i(LOGTAG, "Video Poor");
            }

            Log.i(LOGTAG, "Check audio quality stats data");
            if (mAudioBw > 30000 && mAudioPLRatio < 0.005) {
                audioStatus = QUALITY_STATUS_BEST;
                Log.i(LOGTAG, "Audio Best");
            }else if(mAudioBw > 25000 && mAudioPLRatio < 0.05) {
                audioStatus = QUALITY_STATUS_GOOD;
                Log.i(LOGTAG, "Audio Good");
            }else {
                Log.i(LOGTAG, "Audio Poor");
            }

            update(videoStatus, audioStatus);
        }
    }

    private void update(int videoStatus, int audioStatus) {

        switch (videoStatus) {
            case QUALITY_STATUS_BEST:
                videoBest.setImageResource(R.drawable.ic_done);
                videoView1.setBackground(ContextCompat.getDrawable(TestActivity.this, R.drawable.red_to_orange_gradient_drawable));
                videoView2.setBackground(ContextCompat.getDrawable(TestActivity.this, R.drawable.orange_to_green_gradient_drawable));
                GradientDrawable drawable = (GradientDrawable) videoBest.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.thickGreen));
                videoBest.setBackground(drawable);
                drawable = (GradientDrawable) videoGood.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.orange));
                videoGood.setBackground(drawable);
                drawable = (GradientDrawable) videoPoor.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.red));
                videoPoor.setBackground(drawable);
                break;
            case QUALITY_STATUS_GOOD:
                videoView1.setBackground(ContextCompat.getDrawable(TestActivity.this, R.drawable.red_to_orange_gradient_drawable));
                drawable = (GradientDrawable) videoGood.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.orange));
                videoGood.setBackground(drawable);
                drawable = (GradientDrawable) videoPoor.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.red));
                videoPoor.setBackground(drawable);
                videoGood.setImageResource(R.drawable.ic_done);
                break;
            case 0:
                drawable = (GradientDrawable) videoPoor.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.red));
                videoPoor.setBackground(drawable);
                videoPoor.setImageResource(R.drawable.ic_videocam_off);
                break;
        }

        switch (audioStatus) {
            case QUALITY_STATUS_BEST:
                audioBest.setImageResource(R.drawable.ic_done);
                audioView1.setBackground(ContextCompat.getDrawable(TestActivity.this, R.drawable.red_to_orange_gradient_drawable));
                audioView2.setBackground(ContextCompat.getDrawable(TestActivity.this, R.drawable.orange_to_green_gradient_drawable));
                GradientDrawable drawable = (GradientDrawable) audioBest.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.thickGreen));
                audioBest.setBackground(drawable);
                drawable = (GradientDrawable) audioGood.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.orange));
                audioGood.setBackground(drawable);
                drawable = (GradientDrawable) audioPoor.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.red));
                audioPoor.setBackground(drawable);
                break;
            case QUALITY_STATUS_GOOD:
                audioView1.setBackground(ContextCompat.getDrawable(TestActivity.this, R.drawable.red_to_orange_gradient_drawable));
                drawable = (GradientDrawable) audioGood.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.orange));
                audioGood.setBackground(drawable);
                drawable = (GradientDrawable) audioPoor.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.red));
                audioPoor.setBackground(drawable);
                audioGood.setImageResource(R.drawable.ic_done);
                break;
            case 0:
                drawable = (GradientDrawable) audioPoor.getBackground();
                drawable.setColor(ContextCompat.getColor(TestActivity.this, R.color.red));
                audioPoor.setBackground(drawable);
                audioPoor.setImageResource(R.drawable.ic_volume_off);
                break;
        }
    }

    private void checkVideoQuality() {
        if (mSession != null) {
            Log.i(LOGTAG, "Check video quality stats data");
            if (mVideoBw < 150000 || mVideoPLRatio > 0.03) {
                Log.i(LOGTAG, "Your bandwidth is too low for video");
                //go to audio call to check the quality with video disabled
                showAlert("Voice-only", "Your bandwidth is too low for video");
                mProgressDialog = ProgressDialog.show(this, "Checking your available bandwidth for voice only", "Please wait");
                mPublisher.setPublishVideo(false);
                mSubscriber.setSubscribeToVideo(false);
                mSubscriber.setVideoStatsListener(null);
                audioOnly = true;
            } else {
                //quality is good for video call
                mSession.disconnect();
                Log.i(LOGTAG, "All good");
                showAlert("All good", "You're all set!");
            }
        }
    }

    private void checkAudioQuality() {
        if (mSession != null) {
            Log.i(LOGTAG, "Check audio quality stats data");
            if (mAudioBw < 25000 || mAudioPLRatio > 0.05) {
                Log.i(LOGTAG, "You can't connect successfully");
                showAlert("Not good", "You can't connect successfully");
            } else {
                Log.i(LOGTAG, "Audio Only");
                showAlert("Voice-only", "Your bandwidth is too low for video");
            }
        }
    }

    private void showAlert(String title, String Message) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(Message)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    finish();
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private Runnable statsRunnable = new Runnable() {

        @Override
        public void run() {
            if (mSession != null) {
                checkAudioQuality();
                mSession.disconnect();
            }
        }
    };
}
